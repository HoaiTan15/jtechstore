package com.jtech.jtechstore.controller;

import com.jtech.jtechstore.model.AppUser;
import com.jtech.jtechstore.model.Order;
import com.jtech.jtechstore.model.Product;
import com.jtech.jtechstore.repository.AppUserRepository;
import com.jtech.jtechstore.repository.CategoryRepository;
import com.jtech.jtechstore.repository.OrderRepository;
import com.jtech.jtechstore.repository.ProductRepository;
import com.jtech.jtechstore.repository.PromotionRepository;
import com.jtech.jtechstore.repository.ReviewRepository;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.WorkbookUtil;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
public class DashboardController {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final OrderRepository orderRepository;
    private final PromotionRepository promotionRepository;
    private final AppUserRepository appUserRepository;
    private final ReviewRepository reviewRepository;

    public DashboardController(ProductRepository productRepository,
                               CategoryRepository categoryRepository,
                               OrderRepository orderRepository,
                               PromotionRepository promotionRepository,
                               AppUserRepository appUserRepository,
                               ReviewRepository reviewRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.orderRepository = orderRepository;
        this.promotionRepository = promotionRepository;
        this.appUserRepository = appUserRepository;
        this.reviewRepository = reviewRepository;
    }

    @GetMapping("/admin/dashboard")
    public String dashboard(@RequestParam(value = "month", required = false) Integer month,
                            @RequestParam(value = "year", required = false) Integer year,
                            Model model,
                            HttpSession session) {
        if (!isAdmin(session)) {
            return "redirect:/login";
        }

        LocalDate now = LocalDate.now();

        if (month == null || month < 1 || month > 12) {
            month = now.getMonthValue();
        }

        if (year == null || year < 2000) {
            year = now.getYear();
        }

        YearMonth selectedYearMonth = YearMonth.of(year, month);
        LocalDateTime startDate = selectedYearMonth.atDay(1).atStartOfDay();
        LocalDateTime endDate = selectedYearMonth.plusMonths(1).atDay(1).atStartOfDay();

        List<Product> products = productRepository.findAll();
        List<Order> orders = orderRepository.findAll();
        List<AppUser> customers = appUserRepository.findByRoleOrderByIdDesc("USER");

        long totalProducts = products.size();
        long totalCategories = categoryRepository.count();
        long totalOrders = orders.size();
        long totalPromotions = promotionRepository.count();
        long totalCustomers = customers.size();
        long totalReviews = reviewRepository.count();

        long pendingOrders = orders.stream()
                .filter(o -> Order.STATUS_PENDING.equals(o.getStatus()))
                .count();

        long confirmedOrders = orders.stream()
                .filter(o -> Order.STATUS_CONFIRMED.equals(o.getStatus()))
                .count();

        long shippingOrders = orders.stream()
                .filter(o -> Order.STATUS_SHIPPING.equals(o.getStatus()))
                .count();

        long completedOrders = orders.stream()
                .filter(o -> Order.STATUS_COMPLETED.equals(o.getStatus()))
                .count();

        long cancelledOrders = orders.stream()
                .filter(o -> Order.STATUS_CANCELLED.equals(o.getStatus()))
                .count();

        double totalRevenue = orders.stream()
                .filter(o -> Order.STATUS_COMPLETED.equals(o.getStatus()))
                .filter(o -> o.getTotalAmount() != null)
                .mapToDouble(Order::getTotalAmount)
                .sum();

        Double monthlyRevenueValue = orderRepository.getRevenueByStatusAndDateRange(
                Order.STATUS_COMPLETED,
                startDate,
                endDate
        );

        Long monthlyCompletedOrdersValue = orderRepository.countOrdersByStatusAndDateRange(
                Order.STATUS_COMPLETED,
                startDate,
                endDate
        );

        double monthlyRevenue = monthlyRevenueValue != null ? monthlyRevenueValue : 0;
        long monthlyCompletedOrders = monthlyCompletedOrdersValue != null ? monthlyCompletedOrdersValue : 0;

        int totalStock = products.stream()
                .mapToInt(p -> p.getQuantity() != null ? p.getQuantity() : 0)
                .sum();

        long lowStockCount = products.stream()
                .filter(p -> p.getQuantity() != null && p.getQuantity() > 0 && p.getQuantity() < 5)
                .count();

        long outOfStockCount = products.stream()
                .filter(p -> p.getQuantity() == null || p.getQuantity() <= 0)
                .count();

        Map<String, Long> categoryStats = new LinkedHashMap<>();
        for (Product product : products) {
            String categoryName = product.getCategory() != null
                    ? product.getCategory().getName()
                    : "Chưa phân loại";

            categoryStats.put(categoryName, categoryStats.getOrDefault(categoryName, 0L) + 1);
        }

        Map<String, Long> brandStats = new LinkedHashMap<>();
        for (Product product : products) {
            String brand = product.getBrand() != null && !product.getBrand().isBlank()
                    ? product.getBrand()
                    : "Chưa có thương hiệu";

            brandStats.put(brand, brandStats.getOrDefault(brand, 0L) + 1);
        }

        List<Order> recentOrders = orders.stream()
                .sorted((o1, o2) -> {
                    if (o1.getOrderDate() == null && o2.getOrderDate() == null) {
                        return 0;
                    }

                    if (o1.getOrderDate() == null) {
                        return 1;
                    }

                    if (o2.getOrderDate() == null) {
                        return -1;
                    }

                    return o2.getOrderDate().compareTo(o1.getOrderDate());
                })
                .limit(5)
                .toList();

        List<Product> lowStockProducts = products.stream()
                .filter(p -> p.getQuantity() != null && p.getQuantity() > 0 && p.getQuantity() < 5)
                .limit(5)
                .toList();

        model.addAttribute("totalProducts", totalProducts);
        model.addAttribute("totalCategories", totalCategories);
        model.addAttribute("totalOrders", totalOrders);
        model.addAttribute("totalPromotions", totalPromotions);
        model.addAttribute("totalCustomers", totalCustomers);
        model.addAttribute("totalReviews", totalReviews);

        model.addAttribute("pendingOrders", pendingOrders);
        model.addAttribute("confirmedOrders", confirmedOrders);
        model.addAttribute("shippingOrders", shippingOrders);
        model.addAttribute("completedOrders", completedOrders);
        model.addAttribute("cancelledOrders", cancelledOrders);

        model.addAttribute("totalRevenue", totalRevenue);
        model.addAttribute("monthlyRevenue", monthlyRevenue);
        model.addAttribute("monthlyCompletedOrders", monthlyCompletedOrders);
        model.addAttribute("selectedMonth", month);
        model.addAttribute("selectedYear", year);

        model.addAttribute("totalStock", totalStock);
        model.addAttribute("lowStockCount", lowStockCount);
        model.addAttribute("outOfStockCount", outOfStockCount);

        model.addAttribute("categoryStats", categoryStats);
        model.addAttribute("brandStats", brandStats);

        model.addAttribute("recentOrders", recentOrders);
        model.addAttribute("lowStockProducts", lowStockProducts);

        return "admin/dashboard";
    }

    @GetMapping("/admin/dashboard/export-total-revenue")
    public void exportTotalRevenue(HttpSession session,
                                   HttpServletResponse response) throws IOException {
        if (!isAdmin(session)) {
            response.sendRedirect("/login");
            return;
        }

        List<Order> completedOrders = orderRepository.findAll()
                .stream()
                .filter(o -> Order.STATUS_COMPLETED.equals(o.getStatus()))
                .toList();

        exportRevenueExcel(
                response,
                "doanh_thu_hoan_thanh.xlsx",
                "Doanh thu hoàn thành",
                completedOrders
        );
    }

    @GetMapping("/admin/dashboard/export-monthly-revenue")
    public void exportMonthlyRevenue(@RequestParam(value = "month", required = false) Integer month,
                                     @RequestParam(value = "year", required = false) Integer year,
                                     HttpSession session,
                                     HttpServletResponse response) throws IOException {
        if (!isAdmin(session)) {
            response.sendRedirect("/login");
            return;
        }

        LocalDate now = LocalDate.now();

        if (month == null || month < 1 || month > 12) {
            month = now.getMonthValue();
        }

        if (year == null || year < 2000) {
            year = now.getYear();
        }

        YearMonth selectedYearMonth = YearMonth.of(year, month);
        LocalDateTime startDate = selectedYearMonth.atDay(1).atStartOfDay();
        LocalDateTime endDate = selectedYearMonth.plusMonths(1).atDay(1).atStartOfDay();

        List<Order> monthlyCompletedOrders = orderRepository.findAll()
                .stream()
                .filter(o -> Order.STATUS_COMPLETED.equals(o.getStatus()))
                .filter(o -> o.getOrderDate() != null)
                .filter(o -> !o.getOrderDate().isBefore(startDate) && o.getOrderDate().isBefore(endDate))
                .toList();

        exportRevenueExcel(
                response,
                "doanh_thu_thang_" + month + "_" + year + ".xlsx",
                "Doanh thu tháng " + month + "-" + year,
                monthlyCompletedOrders
        );
    }

    private void exportRevenueExcel(HttpServletResponse response,
                                    String fileName,
                                    String sheetName,
                                    List<Order> orders) throws IOException {
        String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8).replace("+", "%20");

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + encodedFileName);

        try (Workbook workbook = new XSSFWorkbook()) {
            String safeSheetName = WorkbookUtil.createSafeSheetName(sheetName);
            Sheet sheet = workbook.createSheet(safeSheetName);

            CellStyle titleStyle = workbook.createCellStyle();
            titleStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
            titleStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            titleStyle.setAlignment(HorizontalAlignment.CENTER);
            titleStyle.setVerticalAlignment(VerticalAlignment.CENTER);

            Font titleFont = workbook.createFont();
            titleFont.setBold(true);
            titleFont.setColor(IndexedColors.WHITE.getIndex());
            titleFont.setFontHeightInPoints((short) 14);
            titleStyle.setFont(titleFont);

            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFillForegroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);

            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            CellStyle normalStyle = workbook.createCellStyle();
            normalStyle.setBorderTop(BorderStyle.THIN);
            normalStyle.setBorderBottom(BorderStyle.THIN);
            normalStyle.setBorderLeft(BorderStyle.THIN);
            normalStyle.setBorderRight(BorderStyle.THIN);
            normalStyle.setVerticalAlignment(VerticalAlignment.CENTER);

            CellStyle moneyStyle = workbook.createCellStyle();
            moneyStyle.cloneStyleFrom(normalStyle);

            DataFormat dataFormat = workbook.createDataFormat();
            moneyStyle.setDataFormat(dataFormat.getFormat("#,##0"));

            Row titleRow = sheet.createRow(0);
            titleRow.setHeightInPoints(28);

            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue(sheetName.toUpperCase());
            titleCell.setCellStyle(titleStyle);

            sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(0, 0, 0, 6));

            Row header = sheet.createRow(2);
            header.setHeightInPoints(22);

            String[] headers = {
                    "STT",
                    "Mã đơn",
                    "Khách hàng",
                    "Số điện thoại",
                    "Ngày đặt",
                    "Trạng thái",
                    "Tổng tiền"
            };

            for (int i = 0; i < headers.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowIndex = 3;
            int stt = 1;
            double totalRevenue = 0;

            for (Order order : orders) {
                Row row = sheet.createRow(rowIndex++);

                Cell cell0 = row.createCell(0);
                cell0.setCellValue(stt++);
                cell0.setCellStyle(normalStyle);

                Cell cell1 = row.createCell(1);
                cell1.setCellValue(order.getId() != null ? order.getId() : 0);
                cell1.setCellStyle(normalStyle);

                Cell cell2 = row.createCell(2);
                cell2.setCellValue(order.getCustomerName() != null ? order.getCustomerName() : "");
                cell2.setCellStyle(normalStyle);

                Cell cell3 = row.createCell(3);
                cell3.setCellValue(order.getPhone() != null ? order.getPhone() : "");
                cell3.setCellStyle(normalStyle);

                Cell cell4 = row.createCell(4);
                cell4.setCellValue(order.getOrderDate() != null ? order.getOrderDate().toString() : "");
                cell4.setCellStyle(normalStyle);

                Cell cell5 = row.createCell(5);
                cell5.setCellValue(order.getStatus() != null ? order.getStatus() : "");
                cell5.setCellStyle(normalStyle);

                double total = order.getTotalAmount() != null ? order.getTotalAmount() : 0;
                totalRevenue += total;

                Cell cell6 = row.createCell(6);
                cell6.setCellValue(total);
                cell6.setCellStyle(moneyStyle);
            }

            Row totalRow = sheet.createRow(rowIndex + 1);

            Cell labelCell = totalRow.createCell(5);
            labelCell.setCellValue("Tổng doanh thu");
            labelCell.setCellStyle(headerStyle);

            Cell valueCell = totalRow.createCell(6);
            valueCell.setCellValue(totalRevenue);
            valueCell.setCellStyle(moneyStyle);

            sheet.setColumnWidth(0, 8 * 256);
            sheet.setColumnWidth(1, 12 * 256);
            sheet.setColumnWidth(2, 28 * 256);
            sheet.setColumnWidth(3, 18 * 256);
            sheet.setColumnWidth(4, 26 * 256);
            sheet.setColumnWidth(5, 18 * 256);
            sheet.setColumnWidth(6, 18 * 256);

            sheet.createFreezePane(0, 3);

            workbook.write(response.getOutputStream());
        }
    }

    private boolean isAdmin(HttpSession session) {
        AppUser user = (AppUser) session.getAttribute("currentUser");
        return user != null && "ADMIN".equals(user.getRole());
    }
}