package com.travel.travelbooking.service;

import com.travel.travelbooking.dto.*;
import com.travel.travelbooking.repository.BookingRepository;
import com.travel.travelbooking.repository.DashboardRepository;
import com.travel.travelbooking.repository.DestinationRepository;
import com.travel.travelbooking.repository.TourRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final DashboardRepository dashboardRepository;
    private final TourRepository tourRepository;
    private final DestinationRepository destinationRepository;
    private  final BookingRepository bookingRepository;

    @Override
    public DashboardStatsDTO getUserStats() {
        LocalDateTime startOfDay = LocalDateTime.now()
                .withHour(0).withMinute(0).withSecond(0).withNano(0);

        DashboardStatsDTO stats = new DashboardStatsDTO();

        // === USER STATS ===
        stats.setTotalUsers(dashboardRepository.countActiveUsers());
        stats.setTotalCustomers(dashboardRepository.countUsersByRole("USER"));
        stats.setTotalStaff(dashboardRepository.countUsersByRole("STAFF"));
        stats.setTotalAdmins(dashboardRepository.countUsersByRole("ADMIN"));
        stats.setTotalInactive(dashboardRepository.countInactiveOrBanned());
        stats.setNewUsersToday(dashboardRepository.countNewUsersToday(startOfDay));
        stats.setTotalDeleted(dashboardRepository.countDeletedUsers());

        // === TOUR STATS ===
        var tourStatsData = tourRepository.getTourStats();
        stats.setTotalTours(tourStatsData.getTotalTours());
        stats.setActiveTours(tourStatsData.getActiveTours());
        stats.setTotalConfirmedBookings(tourStatsData.getTotalBookings());

        // Tổng lượt xem + tổng số reviews
        var allTours = tourRepository.findAll();

        stats.setTotalReviews(dashboardRepository.countTotalReviews());

        // Top 10 tour phổ biến
        List<PopularTourDTO> popularTours = tourRepository.findTop10PopularTours();
        stats.setTopPopularTours(popularTours);

        stats.setTop5BookedTours(tourRepository.findTop5BookedTours());

        stats.setDestinationStatsByRegion(destinationRepository.countDestinationsByRegion());
        stats.setTop5PopularDestinations(destinationRepository.findTop5PopularDestinations());

        stats.setLatestBookings(bookingRepository.findTop5LatestBookings());

        stats.setActualRevenue(bookingRepository.getActualRevenue());
        stats.setExpectedRevenue(bookingRepository.getExpectedRevenue());
        stats.setLatestTours(tourRepository.findTop10LatestTours());
        return stats;
    }

    @Override
    public DashboardStatsDTO getFullDashboardStats() {
        // Hiện tại nếu chưa cần thêm gì khác, cứ dùng lại getUserStats()
        return getUserStats();
    }

    @Override
    public List<Object[]> getUserRegistrationLast7Days() {
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        return dashboardRepository.countNewUsersLast7Days(sevenDaysAgo);
    }

    @Override
    public List<PopularDestinationDTO> getTop5PopularDestinationsPublic() {
        return destinationRepository.findTop5PopularDestinations();
    }

    @Override
    public List<PopularTourDTO> getTop10PopularToursPublic() {
        return tourRepository.findTop10PopularTours();
    }

    @Override
    public List<TopBookedTourDTO> getTop10MostBookedToursPublic() {
        return tourRepository.findTopBookedTours(PageRequest.of(0, 10)).getContent();
    }

    @Override
    public List<LatestTourDTO> getLatestToursPublic(int limit) {
        return tourRepository.findLatestTours(PageRequest.of(0, limit)).getContent();
    }

    // Xuất file báo cáo Excel
    @Override
    public byte[] exportDashboardToExcel() throws IOException {
        DashboardStatsDTO stats = getFullDashboardStats();

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Báo Cáo Dashboard");

        int rowNum = 0;

        // === TIÊU ĐỀ + NGÀY ===
        Row titleRow = sheet.createRow(rowNum++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("BÁO CÁO DASHBOARD - TRAVEL BOOKING");
        CellStyle titleStyle = createBoldStyle(workbook, 18, HorizontalAlignment.CENTER);
        titleCell.setCellStyle(titleStyle);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 8));

        Row dateRow = sheet.createRow(rowNum++);
        dateRow.createCell(0).setCellValue("Ngày xuất báo cáo: " +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));

        rowNum += 2; // Dãn cách

        // === 1. DOANH THU ===
        addSectionTitle(sheet, rowNum++, "DOANH THU");
        addKeyValueRow(sheet, rowNum++, "Doanh thu thực tế (Hoàn thành)",
                formatCurrency(stats.getActualRevenue()) + " ₫");
        addKeyValueRow(sheet, rowNum++, "Doanh thu dự kiến (Đã xác nhận)",
                formatCurrency(stats.getExpectedRevenue()) + " ₫");

        rowNum += 1;

        // === 2. THỐNG KÊ CHUNG ===
        addSectionTitle(sheet, rowNum++, "THỐNG KÊ CHUNG");
        addKeyValueRow(sheet, rowNum++, "Tổng tour", stats.getTotalTours() + " tour");
        addKeyValueRow(sheet, rowNum++, "Tour đang hoạt động", stats.getActiveTours() + " tour");
        addKeyValueRow(sheet, rowNum++, "Tổng lượt đặt tour", stats.getTotalConfirmedBookings() + " lượt");
        addKeyValueRow(sheet, rowNum++, "Tổng người dùng", stats.getTotalUsers() + " người");
        addKeyValueRow(sheet, rowNum++, "Người dùng mới hôm nay", "+" + stats.getNewUsersToday());

        rowNum += 2;

        // === 3. TOP 10 TOUR PHỔ BIẾN NHẤT ===
        addSectionTitle(sheet, rowNum++, "TOP 10 TOUR PHỔ BIẾN NHẤT");
        String[] tourHeaders = {"STT", "Tên tour", "Điểm đến", "Lượt xem", "Lượt đặt", "Đánh giá"};
        addTableHeader(sheet, rowNum++, tourHeaders);
        int idx = 1;
        for (PopularTourDTO tour : stats.getTopPopularTours()) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(idx++);
            row.createCell(1).setCellValue(tour.getTourName());
            row.createCell(2).setCellValue(tour.getDestinationName());
            row.createCell(3).setCellValue(tour.getViews());
            row.createCell(4).setCellValue(tour.getBookingsCount());
            row.createCell(5).setCellValue(tour.getAverageRating() + " ★");
        }

        rowNum += 2;

        // === 4. TOP 5 TOUR ĐẶT NHIỀU NHẤT ===
        addSectionTitle(sheet, rowNum++, "TOP 5 TOUR ĐẶT NHIỀU NHẤT");
        String[] bookedHeaders = {"STT", "Tên tour", "Điểm đến", "Lượt đặt"};
        addTableHeader(sheet, rowNum++, bookedHeaders);
        idx = 1;
        for (TopBookedTourDTO tour : stats.getTop5BookedTours()) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(idx++);
            row.createCell(1).setCellValue(tour.getTourName());
            row.createCell(2).setCellValue(tour.getDestinationName());
            row.createCell(3).setCellValue(tour.getBookingCount());
        }

        rowNum += 2;

        // === 5. TOP 5 ĐIỂM ĐẾN NỔI BẬT ===
        addSectionTitle(sheet, rowNum++, "TOP 5 ĐIỂM ĐẾN NỔI BẬT");
        String[] destHeaders = {"STT", "Điểm đến", "Khu vực", "Số tour", "Lượt đặt"};
        addTableHeader(sheet, rowNum++, destHeaders);
        idx = 1;
        for (PopularDestinationDTO dest : stats.getTop5PopularDestinations()) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(idx++);
            row.createCell(1).setCellValue(dest.getDestinationName());
            row.createCell(2).setCellValue(switch (dest.getRegion()) {
                case BAC -> "Miền Bắc";
                case TRUNG -> "Miền Trung";
                case NAM -> "Miền Nam";
            });
            row.createCell(3).setCellValue(dest.getTourCount());
            row.createCell(4).setCellValue(dest.getBookingCount());
        }

        rowNum += 2;

        // === 6. 5 ĐƠN ĐẶT TOUR GẦN NHẤT ===
        addSectionTitle(sheet, rowNum++, "5 ĐƠN ĐẶT TOUR GẦN NHẤT");

        String[] bookingHeaders = {
                "Mã đơn",
                "Khách hàng",
                "SĐT",
                "Tour",
                "Ngày đặt",
                "Giá trị",
                "Trạng thái"
        };

        addTableHeader(sheet, rowNum++, bookingHeaders);

        for (LatestBookingDTO b : stats.getLatestBookings()) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue("#" + b.getBookingId());
            row.createCell(1).setCellValue(b.getCustomerName());
            row.createCell(2).setCellValue(b.getCustomerPhone());
            row.createCell(3).setCellValue(b.getTourName());
            row.createCell(4).setCellValue(b.getBookingDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
            row.createCell(5).setCellValue(formatCurrency(b.getTotalPrice()) + " ₫");
            row.createCell(6).setCellValue(
                    switch (b.getStatus()) {
                        case CONFIRMED -> "Đã xác nhận";
                        case PENDING -> "Chờ thanh toán";
                        case CANCEL_REQUEST -> "Yêu cầu hủy";
                        case CANCELLED -> "Đã hủy";
                        case COMPLETED -> "Hoàn thành";
                        case REJECTED -> "Bị từ chối";
                        case DELETED -> "Đã xóa";
                    }
            );
        }


        // Auto size tất cả cột
        for (int i = 0; i < 9; i++) {
            sheet.autoSizeColumn(i);
        }

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        workbook.write(bos);
        workbook.close();
        return bos.toByteArray();
    }

    // === HÀM HỖ TRỢ ĐỂ CODE SẠCH HƠN ===
    private CellStyle createBoldStyle(Workbook wb, int fontSize, HorizontalAlignment align) {
        CellStyle style = wb.createCellStyle();
        Font font = wb.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) fontSize);
        style.setFont(font);
        style.setAlignment(align);
        return style;
    }

    private void addSectionTitle(Sheet sheet, int rowNum, String title) {
        Row row = sheet.createRow(rowNum);
        Cell cell = row.createCell(0);
        cell.setCellValue(title);
        CellStyle style = createBoldStyle(sheet.getWorkbook(), 14, HorizontalAlignment.LEFT);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        cell.setCellStyle(style);
        sheet.addMergedRegion(new CellRangeAddress(rowNum, rowNum, 0, 8));
    }

    private void addKeyValueRow(Sheet sheet, int rowNum, String label, String value) {
        Row row = sheet.createRow(rowNum);
        row.createCell(0).setCellValue(label);
        row.createCell(1).setCellValue(value);
        CellStyle valueStyle = sheet.getWorkbook().createCellStyle();
        Font font = sheet.getWorkbook().createFont();
        font.setBold(true);
        valueStyle.setFont(font);
        row.getCell(1).setCellStyle(valueStyle);
    }

    private void addTableHeader(Sheet sheet, int rowNum, String[] headers) {
        Row row = sheet.createRow(rowNum);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = row.createCell(i);
            cell.setCellValue(headers[i]);
            CellStyle style = sheet.getWorkbook().createCellStyle();
            Font font = sheet.getWorkbook().createFont();
            font.setBold(true);
            style.setFont(font);
            style.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
            style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            cell.setCellStyle(style);
        }
    }

    private String formatCurrency(Double amount) {
        if (amount == null) return "0";
        return String.format("%,.0f", amount);
    }
}
