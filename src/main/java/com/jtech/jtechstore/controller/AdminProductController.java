package com.jtech.jtechstore.controller;

import com.jtech.jtechstore.model.AppUser;
import com.jtech.jtechstore.model.Product;
import com.jtech.jtechstore.repository.CategoryRepository;
import com.jtech.jtechstore.repository.ProductRepository;
import com.jtech.jtechstore.repository.PromotionRepository;
import com.jtech.jtechstore.service.ProductExcelImportService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.List;

@Controller
@RequestMapping("/admin/products")
public class AdminProductController {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final PromotionRepository promotionRepository;
    private final ProductExcelImportService productExcelImportService;

    public AdminProductController(ProductRepository productRepository,
                                  CategoryRepository categoryRepository,
                                  PromotionRepository promotionRepository,
                                  ProductExcelImportService productExcelImportService) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.promotionRepository = promotionRepository;
        this.productExcelImportService = productExcelImportService;
    }

    @GetMapping
    public String listProducts(HttpSession session, Model model) {
        if (!isAdmin(session)) {
            return "redirect:/login";
        }

        List<Product> products = productRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));

        long lowStockCount = products.stream()
                .filter(p -> p.getQuantity() != null && p.getQuantity() > 0 && p.getQuantity() < 5)
                .count();

        long outOfStockCount = products.stream()
                .filter(p -> p.getQuantity() == null || p.getQuantity() <= 0)
                .count();

        model.addAttribute("products", products);
        model.addAttribute("lowStockCount", lowStockCount);
        model.addAttribute("outOfStockCount", outOfStockCount);

        return "admin/products/list";
    }

    @GetMapping("/add")
    public String addForm(HttpSession session, Model model) {
        if (!isAdmin(session)) {
            return "redirect:/login";
        }

        model.addAttribute("product", new Product());
        addFormData(model);
        model.addAttribute("pageTitle", "Thêm sản phẩm");

        return "admin/products/form";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id,
                           HttpSession session,
                           Model model) {
        if (!isAdmin(session)) {
            return "redirect:/login";
        }

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));

        model.addAttribute("product", product);
        addFormData(model);
        model.addAttribute("pageTitle", "Cập nhật sản phẩm");

        return "admin/products/form";
    }

    @PostMapping("/save")
    public String saveProduct(@Valid @ModelAttribute("product") Product product,
                              BindingResult result,
                              @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                              HttpSession session,
                              Model model) {
        if (!isAdmin(session)) {
            return "redirect:/login";
        }

        if (product.getQuantity() == null) {
            product.setQuantity(0);
        }

        if (result.hasErrors()) {
            addFormData(model);
            model.addAttribute("pageTitle", product.getId() == null ? "Thêm sản phẩm" : "Cập nhật sản phẩm");
            return "admin/products/form";
        }

        if (product.getPrice() != null && product.getPrice() < 0) {
            model.addAttribute("error", "Giá sản phẩm không được âm");
            addFormData(model);
            model.addAttribute("pageTitle", product.getId() == null ? "Thêm sản phẩm" : "Cập nhật sản phẩm");
            return "admin/products/form";
        }

        if (product.getQuantity() < 0) {
            model.addAttribute("error", "Số lượng tồn kho không được âm");
            addFormData(model);
            model.addAttribute("pageTitle", product.getId() == null ? "Thêm sản phẩm" : "Cập nhật sản phẩm");
            return "admin/products/form";
        }

        if (product.getId() != null) {
            Product oldProduct = productRepository.findById(product.getId()).orElse(null);

            if (oldProduct != null && (product.getImageUrl() == null || product.getImageUrl().isBlank())) {
                product.setImageUrl(oldProduct.getImageUrl());
            }
        }

        try {
            if (imageFile != null && !imageFile.isEmpty()) {
                String fileName = saveImage(imageFile);
                product.setImageUrl("/uploads/products/" + fileName);
            }
        } catch (IOException e) {
            model.addAttribute("error", "Lưu ảnh thất bại: " + e.getMessage());
            addFormData(model);
            model.addAttribute("pageTitle", product.getId() == null ? "Thêm sản phẩm" : "Cập nhật sản phẩm");
            return "admin/products/form";
        }

        productRepository.save(product);

        return "redirect:/admin/products";
    }

    @PostMapping("/import")
    public String importProducts(@RequestParam("excelFile") MultipartFile excelFile,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {
        if (!isAdmin(session)) {
            return "redirect:/login";
        }

        try {
            int count = productExcelImportService.importProducts(excelFile);
            redirectAttributes.addFlashAttribute("success", "Import Excel thành công " + count + " sản phẩm.");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/admin/products";
    }

    @GetMapping("/import-template")
    public void downloadImportTemplate(HttpSession session,
                                       HttpServletResponse response) throws IOException {
        if (!isAdmin(session)) {
            response.sendRedirect("/login");
            return;
        }

        String fileName = "mau_import_san_pham_jtech.xlsx";
        String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8).replace("+", "%20");

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + encodedFileName);

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("SanPham");

            String[] headers = {
                    "Tên sản phẩm",
                    "Danh mục",
                    "Thương hiệu",
                    "Giá gốc",
                    "Số lượng",
                    "Ảnh URL",
                    "CPU",
                    "RAM",
                    "Ổ cứng",
                    "Màn hình",
                    "Bảo hành",
                    "Mô tả"
            };

            XSSFCellStyle headerStyle = (XSSFCellStyle) workbook.createCellStyle();
            headerStyle.setFillForegroundColor(new XSSFColor(new java.awt.Color(71, 231, 252), null));
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);

            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.BLACK.getIndex());
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

            Row header = sheet.createRow(0);
            header.setHeightInPoints(24);

            for (int i = 0; i < headers.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            Row sample = sheet.createRow(1);
            sample.setHeightInPoints(22);

            sample.createCell(0).setCellValue("Laptop Dell Inspiron 15");
            sample.createCell(1).setCellValue("Laptop");
            sample.createCell(2).setCellValue("Dell");
            sample.createCell(3).setCellValue(15000000);
            sample.createCell(4).setCellValue(10);
            sample.createCell(5).setCellValue("https://example.com/dell.jpg");
            sample.createCell(6).setCellValue("Intel Core i5");
            sample.createCell(7).setCellValue("8GB");
            sample.createCell(8).setCellValue("512GB SSD");
            sample.createCell(9).setCellValue("15.6 inch");
            sample.createCell(10).setCellValue("12 tháng");
            sample.createCell(11).setCellValue("Laptop văn phòng, phù hợp học tập và làm việc.");

            for (int i = 0; i <= 11; i++) {
                Cell cell = sample.getCell(i);

                if (cell != null) {
                    if (i == 3) {
                        cell.setCellStyle(moneyStyle);
                    } else {
                        cell.setCellStyle(normalStyle);
                    }
                }
            }

            sheet.setColumnWidth(0, 28 * 256);
            sheet.setColumnWidth(1, 18 * 256);
            sheet.setColumnWidth(2, 18 * 256);
            sheet.setColumnWidth(3, 15 * 256);
            sheet.setColumnWidth(4, 12 * 256);
            sheet.setColumnWidth(5, 38 * 256);
            sheet.setColumnWidth(6, 20 * 256);
            sheet.setColumnWidth(7, 12 * 256);
            sheet.setColumnWidth(8, 18 * 256);
            sheet.setColumnWidth(9, 16 * 256);
            sheet.setColumnWidth(10, 14 * 256);
            sheet.setColumnWidth(11, 50 * 256);

            sheet.createFreezePane(0, 1);

            workbook.write(response.getOutputStream());
        }
    }

    @GetMapping("/delete/{id}")
    public String deleteProduct(@PathVariable Long id,
                                HttpSession session) {
        if (!isAdmin(session)) {
            return "redirect:/login";
        }

        productRepository.deleteById(id);

        return "redirect:/admin/products";
    }

    @GetMapping("/inventory")
    public String inventory(HttpSession session, Model model) {
        if (!isAdmin(session)) {
            return "redirect:/login";
        }

        List<Product> products = productRepository.findAll(Sort.by(Sort.Direction.ASC, "quantity"));

        List<Product> lowStockProducts = products.stream()
                .filter(p -> p.getQuantity() != null && p.getQuantity() > 0 && p.getQuantity() < 5)
                .toList();

        List<Product> outOfStockProducts = products.stream()
                .filter(p -> p.getQuantity() == null || p.getQuantity() <= 0)
                .toList();

        model.addAttribute("products", products);
        model.addAttribute("lowStockProducts", lowStockProducts);
        model.addAttribute("outOfStockProducts", outOfStockProducts);

        return "admin/products/inventory";
    }

    private void addFormData(Model model) {
        model.addAttribute("categories", categoryRepository.findAll(Sort.by(Sort.Direction.ASC, "name")));
        model.addAttribute("promotions", promotionRepository.findAll(Sort.by(Sort.Direction.DESC, "id")));
    }

    private String saveImage(MultipartFile imageFile) throws IOException {
        String originalName = imageFile.getOriginalFilename();

        if (originalName == null || originalName.isBlank()) {
            originalName = "product.jpg";
        }

        String cleanName = originalName.replaceAll("[^a-zA-Z0-9\\.\\-_]", "_");
        String fileName = System.currentTimeMillis() + "_" + cleanName;

        Path uploadDir = Paths.get("uploads/products");

        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }

        Path filePath = uploadDir.resolve(fileName);
        Files.copy(imageFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        return fileName;
    }

    private boolean isAdmin(HttpSession session) {
        AppUser currentUser = (AppUser) session.getAttribute("currentUser");
        return currentUser != null && "ADMIN".equals(currentUser.getRole());
    }
}