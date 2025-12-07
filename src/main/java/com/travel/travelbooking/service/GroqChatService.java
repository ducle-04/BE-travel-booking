package com.travel.travelbooking.service;

import com.travel.travelbooking.dto.*;
import com.travel.travelbooking.entity.*;
import com.travel.travelbooking.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
        String reply = callGroq(userMessage, buildSmartContext(userMessage.toLowerCase()));

        if (user != null) {
            ChatHistory h = new ChatHistory();
            h.setUser(user);
            h.setUserMessage(userMessage);
            h.setBotReply(reply);
            historyRepository.save(h);
        }
        return reply;
    }

    private String buildSmartContext(String msg) {
        StringBuilder ctx = new StringBuilder("=== DỮ LIỆU DU LỊCH MỚI NHẤT ===\n\n");

        // Top điểm đến
        destinationRepository.findTop5PopularDestinations().forEach(d ->
                ctx.append(String.format("Hot %s (%s) - %d tour - %d lượt đặt\n",
                        d.getDestinationName(), formatRegion(d.getRegion()), d.getTourCount(), d.getBookingCount()))
        );
        ctx.append("\n");

        // Top tour nổi bật
        tourRepository.findTop10PopularTours().stream().limit(8).forEach(t -> {
            Tour tour = tourRepository.findById(t.getTourId()).orElse(null);
            if (tour == null) return;

            int slotsLeft = tour.getMaxParticipants() - tour.getTotalParticipants();
            List<String> dates = startDateRepository.findStartDatesByTourId(t.getTourId())
                    .stream().limit(4).map(d -> d.format(df)).toList();

            ctx.append(String.format("Star %s → %s | %.0fđ | %.1f★ | Còn %d chỗ | Khởi hành: %s\n",
                    t.getTourName(), t.getDestinationName(), tour.getPrice(), t.getAverageRating(),
                    slotsLeft, dates.isEmpty() ? "Liên hệ" : String.join(", ", dates)));
        });
        ctx.append("\n");

        // Danh mục
        ctx.append("Danh mục tour: ")
                .append(String.join(", ", categoryRepository.findByStatusOrderByDisplayOrderAsc(CategoryStatus.ACTIVE)
                        .stream().map(TourCategory::getName).toList()))
                .append("\n\n");

        // Tìm kiếm từ khóa
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
                        Điểm đến: %s | Loại: %s
                        Giá: %.0fđ | Thời gian: %s
                        Còn %d/%d chỗ
                        Khởi hành: %s
                        Đánh giá: %.1f★ | Link: /tour/%d
                        """,
                                t.getName(), t.getDestinationName(),
                                Optional.ofNullable(t.getCategoryName()).orElse("Khác"),
                                t.getPrice(), t.getDuration(),
                                left, t.getMaxParticipants(),
                                dates.isEmpty() ? "Liên hệ" : String.join(", ", dates),
                                t.getAverageRating(), t.getId()));
                    });
            ctx.append("\n");
        }

        // Giá rẻ
        Double maxPrice = extractPrice(msg);
        if (maxPrice != null) {
            ctx.append(String.format("Tour giá dưới %.0fđ:\n", maxPrice));
            tourRepository.findFilteredTours(null, TourStatus.ACTIVE, null, maxPrice, null,
                            org.springframework.data.domain.PageRequest.of(0, 10))
                    .forEach(t -> ctx.append(String.format("• %s - %.0fđ - %s\n",
                            t.getName(), t.getPrice(), t.getDestinationName())));
            ctx.append("\n");
        }

        // Theo miền
        Region region = detectRegion(msg);
        if (region != null) {
            ctx.append(String.format("Điểm đến miền %s:\n", formatRegion(region)));
            destinationRepository.findByRegionWithTourCount(region)
                    .forEach(d -> ctx.append(String.format("• %s (%d tour)\n",
                            d.getName(), d.getToursCount() != null ? d.getToursCount() : 0)));
        }

        String result = ctx.toString();
        return result.length() > 7500 ? result.substring(0, 7500) + "\n...(còn nhiều tour khác)" : result;
    }

    private String formatRegion(Region r) {
        return switch (r) {
            case BAC -> "Bắc";
            case TRUNG -> "Trung";
            case NAM -> "Nam";
        };
    }

    private String extractMainKeyword(String msg) {
        return msg.replaceAll("(?i)\\b(tìm|tour|đi|đến|muốn|cho|ở|không|à|ạ|nhé|du lịch|được|gì|có|muốn)\\b", " ")
                .replaceAll("\\s+", " ").trim();
    }

    private Double extractPrice(String msg) {
        Matcher m = Pattern.compile("(\\d+[.,]?\\d*)\\s*(tr|triệu|ngàn|k|đồng|đ)", Pattern.CASE_INSENSITIVE)
                .matcher(msg.replaceAll("\\s", ""));
        if (m.find()) {
            double val = Double.parseDouble(m.group(1).replace(",", "."));
            String unit = m.group(0).toLowerCase();
            if (unit.contains("tr")) val *= 1_000_000;
            else if (unit.contains("ngàn") || unit.contains("k")) val *= 1_000;
            return val;
        }
        return null;
    }

    private Region detectRegion(String msg) {
        if (msg.matches(".*\\b(bắc|miền bắc|hà nội|sapa|ha long|hạ long)\\b.*")) return Region.BAC;
        if (msg.matches(".*\\b(trung|miền trung|đà nẵng|huế|hội an|phong nha)\\b.*")) return Region.TRUNG;
        if (msg.matches(".*\\b(nam|miền nam|phú quốc|sài gòn|hồ chí minh|vũng tàu|cần thơ)\\b.*")) return Region.NAM;
        return null;
    }

    // Thay thế toàn bộ method callGroq() bằng cái này:
    @SuppressWarnings("unchecked")
    private String callGroq(String userMessage, String context) {
        String systemPrompt = """
            Bạn là trợ lý du lịch siêu thân thiện, nói tiếng Việt tự nhiên như người thật.
            Chỉ dùng dữ liệu thực tế bên dưới, không bịa thông tin.
            Gợi ý tour kèm tên, giá, ngày khởi hành, chỗ trống, và link /tour/{id}
            Nếu không biết → "Mình chưa tìm thấy tour phù hợp, bạn cho thêm thông tin nhé!"
            
            DỮ LIỆU MỚI NHẤT:
            """ + context;

        var messages = List.of(
                Map.of("role", "system", "content", systemPrompt),
                Map.of("role", "user", "content", userMessage)
        );

        var body = Map.of("model", model, "messages", messages, "temperature", 0.65, "max_tokens", 1200);
        var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(groqApiKey);

        try {
            var response = restTemplate.exchange(apiUrl, HttpMethod.POST, new HttpEntity<>(body, headers),
                    new ParameterizedTypeReference<Map<String, Object>>() {});

            var res = response.getBody();
            if (res == null || !res.containsKey("choices")) {
                return "Mình đang hơi chậm, bạn thử lại nha! 😅";
            }

            // Safe cast với @SuppressWarnings
            var choices = (List<Map<String, Object>>) res.get("choices");
            var choice = choices.get(0);
            var message = (Map<String, String>) choice.get("message");

            return message.get("content").trim();

        } catch (Exception e) {
            return "Mình đang gặp chút lỗi mạng. Bạn thử lại sau 30s nhé! 🙏";
        }
    }

    public List<ChatHistoryDTO> getHistory(String username) {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            return Collections.emptyList();
        }

        return historyRepository.findByUserIdOrderByTimestampAsc(user.getId())
                .stream()
                .map(history -> new ChatHistoryDTO(
                        history.getId(),
                        history.getUserMessage(),
                        history.getBotReply(),
                        history.getTimestamp(),
                        user.getUsername() // hoặc history.getUser().getUsername() nếu muốn lấy từ entity
                ))
                .toList();
    }
}