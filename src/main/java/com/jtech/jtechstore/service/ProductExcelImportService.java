package com.jtech.jtechstore.service;

import com.jtech.jtechstore.model.Category;
import com.jtech.jtechstore.model.Product;
import com.jtech.jtechstore.repository.CategoryRepository;
import com.jtech.jtechstore.repository.ProductRepository;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

@Service
public class ProductExcelImportService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public ProductExcelImportService(ProductRepository productRepository,
                                     CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    public int importProducts(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("Vui lòng chọn file Excel để import.");
        }

        String fileName = file.getOriginalFilename();

        if (fileName == null ||
                (!fileName.endsWith(".xlsx") && !fileName.endsWith(".xls"))) {
            throw new RuntimeException("File import phải có định dạng .xlsx hoặc .xls.");
        }

        int importedCount = 0;

        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = WorkbookFactory.create(inputStream)) {

            Sheet sheet = workbook.getSheetAt(0);

            if (sheet == null) {
                throw new RuntimeException("File Excel không có sheet dữ liệu.");
            }

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);

                if (row == null || isRowEmpty(row)) {
                    continue;
                }

                String name = getString(row.getCell(0));
                String categoryName = getString(row.getCell(1));
                String brand = getString(row.getCell(2));
                Double price = getDouble(row.getCell(3));
                Integer quantity = getInteger(row.getCell(4));
                String imageUrl = getString(row.getCell(5));
                String cpu = getString(row.getCell(6));
                String ram = getString(row.getCell(7));
                String storage = getString(row.getCell(8));
                String screen = getString(row.getCell(9));
                String warranty = getString(row.getCell(10));
                String description = getString(row.getCell(11));

                if (name == null || name.isBlank()) {
                    throw new RuntimeException("Dòng " + (i + 1) + ": Tên sản phẩm không được để trống.");
                }

                if (price == null || price < 0) {
                    throw new RuntimeException("Dòng " + (i + 1) + ": Giá sản phẩm không hợp lệ.");
                }

                if (quantity == null || quantity < 0) {
                    throw new RuntimeException("Dòng " + (i + 1) + ": Số lượng tồn kho không hợp lệ.");
                }

                Category category = null;

                if (categoryName != null && !categoryName.isBlank()) {
                    String finalCategoryName = categoryName.trim();

                    category = categoryRepository.findByNameIgnoreCase(finalCategoryName)
                            .orElseGet(() -> {
                                Category newCategory = new Category();
                                newCategory.setName(finalCategoryName);
                                newCategory.setDescription("Danh mục được tạo tự động khi import Excel");
                                return categoryRepository.save(newCategory);
                            });
                }

                Product product = new Product();
                product.setName(name.trim());
                product.setCategory(category);
                product.setBrand(brand);
                product.setPrice(price);
                product.setQuantity(quantity);
                product.setImageUrl(imageUrl);
                product.setCpu(cpu);
                product.setRam(ram);
                product.setStorage(storage);
                product.setScreen(screen);
                product.setWarranty(warranty);
                product.setDescription(description);

                productRepository.save(product);
                importedCount++;
            }

            if (importedCount == 0) {
                throw new RuntimeException("File Excel không có dòng sản phẩm hợp lệ để import.");
            }

            return importedCount;

        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Import Excel thất bại: " + e.getMessage());
        }
    }

    private boolean isRowEmpty(Row row) {
        for (int i = 0; i <= 11; i++) {
            Cell cell = row.getCell(i);

            if (cell != null && !getString(cell).isBlank()) {
                return false;
            }
        }

        return true;
    }

    private String getString(Cell cell) {
        if (cell == null) {
            return "";
        }

        DataFormatter formatter = new DataFormatter();
        return formatter.formatCellValue(cell).trim();
    }

    private Double getDouble(Cell cell) {
        String value = getString(cell);

        if (value == null || value.isBlank()) {
            return null;
        }

        try {
            value = value.replace(",", "").replace(" ", "");
            return Double.parseDouble(value);
        } catch (Exception e) {
            return null;
        }
    }

    private Integer getInteger(Cell cell) {
        String value = getString(cell);

        if (value == null || value.isBlank()) {
            return null;
        }

        try {
            value = value.replace(",", "").replace(" ", "");
            return (int) Double.parseDouble(value);
        } catch (Exception e) {
            return null;
        }
    }
}