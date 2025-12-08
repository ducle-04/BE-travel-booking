package com.travel.travelbooking.service;

import com.travel.travelbooking.dto.ChatHistoryDTO;
import com.travel.travelbooking.entity.*;
import com.travel.travelbooking.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GroqChatService {

    private final TourRepository tourRepository;
    private final DestinationRepository destinationRepository;
    private final TourCategoryRepository categoryRepository;
    private final TourStartDateRepository startDateRepository;
    private final ChatHistoryRepository historyRepository;
    private final UserRepository userRepository;

    @Value("${groq.api.key}")
    private String groqApiKey;

    @Value("${groq.api.url}")
    private String apiUrl;

    @Value("${groq.model}")
    private String model;

    private final RestTemplate restTemplate = new RestTemplate();
    private final DateTimeFormatter df = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public String chat(String userMessage, String username) {
        User user = username != null ? userRepository.findByUsername(username) : null;
        String context = buildSmartContext(userMessage.toLowerCase());

        String reply = callGroq(userMessage, context, user);

        if (user != null) {
            ChatHistory h = new ChatHistory();
            h.setUser(user);
            h.setUserMessage(userMessage);
            h.setBotReply(reply);
            historyRepository.save(h);
        }
        return reply;
    }

    // ==================== LỊCH SỬ CHAT ====================
    private List<Map<String, String>> buildRecentHistoryMessages(User user) {
        if (user == null) return Collections.emptyList();

        return historyRepository.findTop5ByUserOrderByTimestampDesc(user)
                .stream()
                .sorted(Comparator.comparing(ChatHistory::getTimestamp))
                .map(h -> List.of(
                        Map.of("role", "user", "content", h.getUserMessage()),
                        Map.of("role", "assistant", "content", h.getBotReply())
                ))
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }

    @Cacheable(value = "chatContext", key = "#msg.hashCode()", unless = "#result.length() > 2500")
    private String buildSmartContext(String msg) {

        StringBuilder ctx = new StringBuilder("=== TOUR HOT & DỮ LIỆU MỚI NHẤT ===\n");

        // TOP ĐIỂM ĐẾN HOT
        ctx.append("ĐIỂM ĐẾN ĐANG HOT NHẤT HIỆN TẠI:\n");
        destinationRepository.findTop5PopularDestinations()
                .forEach(d -> {
                    String regionText = switch (d.getRegion()) {
                        case BAC -> "miền Bắc";
                        case TRUNG -> "miền Trung";
                        case NAM -> "miền Nam";
                        default -> "Việt Nam";
                    };

                    String hotness = d.getBookingCount() > 500 ? "siêu hot, đang cháy hàng"
                            : d.getBookingCount() > 200 ? "rất được ưa chuộng"
                            : "đang lên ngôi";

                    ctx.append(String.format("• %s (%s) – %s với %d tour và %d lượt đặt\n",
                            d.getDestinationName(), regionText, hotness, d.getTourCount(), d.getBookingCount()));
                });
        ctx.append("\n");

        // PHÂN TÍCH GIÁ (khoảng giá min–max)
        Double[] range = extractPriceRange(msg);
        boolean hasPriceFilter = range != null;

        // Chỉ hiển thị TOP TOUR HOT nếu user không yêu cầu lọc giá
        if (!hasPriceFilter) {
            ctx.append("TOUR ĐANG HOT NHẤT:\n");
            tourRepository.findTop10PopularTours().stream().limit(6).forEach(t -> {
                Tour tour = tourRepository.findById(t.getTourId()).orElse(null);
                if (tour == null) return;

                int left = tour.getMaxParticipants() - tour.getTotalParticipants();
                String dates = startDateRepository.findStartDatesByTourId(t.getTourId())
                        .stream().limit(3)
                        .map(d -> d.format(df))
                        .collect(Collectors.joining(", "));

                String seatInfo = left <= 0 ? "hết chỗ rồi ạ"
                        : left <= 5 ? "chỉ còn " + left + " chỗ cuối cùng"
                        : "còn " + left + " chỗ";

                ctx.append(String.format("• %s đi %s – %.0fđ – %s – ngày %s. /tour/%d\n",
                        t.getTourName(), t.getDestinationName(), tour.getPrice(),
                        seatInfo, dates, t.getTourId()));
            });
            ctx.append("\n");
        }

        // Nếu user lọc giá → chỉ đưa tour phù hợp giá vào context
        if (hasPriceFilter) {
            Double min = range[0];
            Double max = range[1];

            ctx.append("TOUR THEO KHOẢNG GIÁ:\n");

            tourRepository.findFilteredTours(
                    null,
                    TourStatus.ACTIVE,
                    min,
                    max,
                    null,
                    org.springframework.data.domain.PageRequest.of(0, 12)
            ).forEach(t -> ctx.append(
                    String.format("• %s – %.0fđ – %s – Link: /tour/%d\n",
                            t.getName(), t.getPrice(), t.getDestinationName(), t.getId())
            ));

            ctx.append("\n");
        }

        // Lọc theo từ khóa
        String keyword = extractMainKeyword(msg);
        if (keyword.length() >= 2) {
            tourRepository.findByNameContainingIgnoreCaseWithCounts(keyword)
                    .stream().limit(6)
                    .forEach(t -> {
                        List<String> dates = startDateRepository.findStartDatesByTourId(t.getId())
                                .stream().limit(4).map(d -> d.format(df)).toList();
                        int left = t.getMaxParticipants() - t.getTotalParticipants();

                        ctx.append(String.format("""
                        ──────────────────
                        Tour %s
                        Điểm đến: %s
                        Giá: %.0fđ
                        Còn %d/%d chỗ
                        Khởi hành: %s
                        Link: /tour/%d
                        """,
                                t.getName(),
                                t.getDestinationName(),
                                t.getPrice(),
                                left, t.getMaxParticipants(),
                                dates.isEmpty() ? "Liên hệ" : String.join(", ", dates),
                                t.getId()));
                    });
            ctx.append("\n");
        }

        // Lọc theo vùng
        Region region = detectRegion(msg);
        if (region != null) {
            String reg = switch (region) {
                case BAC -> "miền Bắc";
                case TRUNG -> "miền Trung";
                case NAM -> "miền Nam";
            };
            ctx.append("Tour khu vực ").append(reg).append(" nổi bật:\n");
            destinationRepository.findByRegionWithTourCount(region)
                    .stream().limit(6)
                    .forEach(d -> ctx.append(String.format("• %s (%d tour)\n",
                            d.getName(), d.getToursCount())));
        }

        String result = ctx.toString();
        return result.length() > 2200
                ? result.substring(0, 2200) + "\n...Còn nhiều tour khác nữa!"
                : result;
    }

    // ==================== CALL GROQ ====================
    private String callGroq(String userMessage, String context, User user) {

        String systemPrompt = """
        Bạn là tư vấn viên du lịch siêu nhiệt tình, dễ thương và nói chuyện cực kỳ tự nhiên như người thật.
        Trả lời ngắn gọn 3-5 câu thôi, dùng nhiều emoji vui vẻ, ngôn ngữ gần gũi, hay dùng từ "ạ", "nha", "luôn ạ".
        Chỉ dùng dữ liệu thực tế bên dưới, không bịa thông tin.
        Gợi ý tour kèm tên + giá + chỗ trống + ngày đi + link /tour/{id}
        
        QUAN TRỌNG:
        - Nhớ chính xác những gì khách đã nói ở các lượt trước (xem lịch sử chat bên dưới).
        - Nếu có tour phù hợp → gợi ý luôn thật tự nhiên, không hỏi thừa.
        - Chỉ khi không có tour nào mới hỏi thêm thông tin.
        
        LƯU Ý BẮT BUỘC:
        - Chỉ gợi ý tour có trong danh sách context bên dưới.
        - Không được tự tạo tour, tự tạo ngày, giá, ID.
        - Nếu tour không còn tồn tại hoặc đã bị xóa → KHÔNG ĐƯỢC NHẮC ĐẾN.
        - Nếu không tìm thấy tour phù hợp -> "Mình chưa tìm thấy tour phù hợp, bạn cho thêm thông tin nhé!"
            
        Luôn kết thúc bằng 1 câu hỏi mở để kéo khách tiếp tục chat.
        
        DỮ LIỆU TOUR HOT (cập nhật realtime):
        """ + context;
        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content", systemPrompt));

        messages.addAll(buildRecentHistoryMessages(user));
        messages.add(Map.of("role", "user", "content", userMessage));

        // Luôn giữ system prompt
        if (messages.size() > 15) {
            List<Map<String, String>> trimmed = new ArrayList<>();
            trimmed.add(messages.get(0));
            int from = messages.size() - 14;
            trimmed.addAll(messages.subList(Math.max(from, 1), messages.size()));
            messages = trimmed;
        }

        var body = Map.of(
                "model", model,
                "messages", messages,
                "temperature", 0.75,
                "max_tokens", 420
        );

        var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(groqApiKey);

        try {
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    apiUrl, HttpMethod.POST, new HttpEntity<>(body, headers),
                    new ParameterizedTypeReference<Map<String, Object>>() {}
            );

            Map<String, Object> res = response.getBody();
            if (res == null || !res.containsKey("choices")) {
                return "Em hơi lag xíu, anh/chị nhắn lại giúp em nha!";
            }

            Map<String, Object> message = (Map<String, Object>)
                    ((List<?>) res.get("choices")).get(0);

            String content = (String) ((Map<?, ?>) message.get("message")).get("content");
            return content != null ? content.trim() : "Em bị lỗi xíu, mình nhắn lại nha!";
        } catch (Exception e) {
            e.printStackTrace();
            return "Hệ thống lỗi nhẹ ạ, anh/chị thử lại sau 30 giây nha!";
        }
    }

    // ==================== TIỆN ÍCH ====================
    private String extractMainKeyword(String msg) {
        return msg.replaceAll("(?i)\\b(tìm|tour|đi|đến|muốn|cho|ở|không|à|ạ|nhé|du lịch|được|gì|có|em|anh|chị|giá rẻ|khuyến mãi|dưới|trên|khoảng|)\\b", " ")
                .replaceAll("\\s+", " ").trim();
    }

    private Double[] extractPriceRange(String msg) {
        // Regex đúng trong JAVA (chỉ dùng \\ khi cần)
        Pattern pattern = Pattern.compile("(\\d+(?:[.,]\\d+)?)(?:\\s*(tr|triệu|t|ngàn|k|đ|đồng))?", Pattern.CASE_INSENSITIVE);
        Matcher m = pattern.matcher(msg);

        List<Double> prices = new ArrayList<>();

        while (m.find()) {
            double val = Double.parseDouble(m.group(1).replace(",", "."));

            String unit = m.group(2); // đơn vị có thể null
            if (unit != null) {
                unit = unit.toLowerCase();
                if (unit.contains("tr") || unit.contains("triệu") || unit.equals("t"))
                    val *= 1_000_000;
                else if (unit.contains("ngàn") || unit.contains("k"))
                    val *= 1_000;
            }

            prices.add(val);
        }

        if (prices.size() == 1) return new Double[]{null, prices.get(0)};
        if (prices.size() >= 2) return new Double[]{prices.get(0), prices.get(1)};
        return null;
    }


    private Region detectRegion(String msg) {
        if (msg.matches(".*\\b(bắc|miền bắc|hà nội|sapa|ha long|hạ long|mai châu)\\b.*")) return Region.BAC;
        if (msg.matches(".*\\b(trung|miền trung|đà nẵng|huế|hội an|đà lạt|quảng bình|quy nhơn)\\b.*")) return Region.TRUNG;
        if (msg.matches(".*\\b(nam|miền nam|phú quốc|sài gòn|hồ chí minh|vũng tàu|cần thơ|mũi né)\\b.*")) return Region.NAM;
        return null;
    }

    public List<ChatHistoryDTO> getHistory(String username) {
        User user = userRepository.findByUsername(username);
        if (user == null) return Collections.emptyList();

        return historyRepository.findByUserIdOrderByTimestampAsc(user.getId())
                .stream()
                .map(h -> new ChatHistoryDTO(
                        h.getId(),
                        h.getUserMessage(),
                        h.getBotReply(),
                        h.getTimestamp(),
                        username
                ))
                .toList();
    }
}
