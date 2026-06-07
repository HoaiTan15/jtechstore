package com.jtech.jtechstore.service;

import com.jtech.jtechstore.model.Product;
import com.jtech.jtechstore.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
public class ProductService {
    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<Product> getAll(String keyword) {
        if (keyword != null && !keyword.trim().isEmpty()) {
            return productRepository.findByNameContainingIgnoreCase(keyword.trim());
        }

        return productRepository.findAll();
    }

    public List<Product> filterProducts(String keyword,
                                        Long categoryId,
                                        String brand,
                                        Double minPrice,
                                        Double maxPrice,
                                        String sort) {
        List<Product> products = getAll(keyword);

        if (categoryId != null) {
            products = products.stream()
                    .filter(p -> p.getCategory() != null && p.getCategory().getId().equals(categoryId))
                    .toList();
        }

        if (brand != null && !brand.trim().isEmpty()) {
            products = products.stream()
                    .filter(p -> p.getBrand() != null && p.getBrand().equalsIgnoreCase(brand.trim()))
                    .toList();
        }

        if (minPrice != null) {
            products = products.stream()
                    .filter(p -> p.getSalePrice() >= minPrice)
                    .toList();
        }

        if (maxPrice != null) {
            products = products.stream()
                    .filter(p -> p.getSalePrice() <= maxPrice)
                    .toList();
        }

        if (sort != null) {
            if (sort.equals("price_asc")) {
                products = products.stream()
                        .sorted(Comparator.comparingDouble(Product::getSalePrice))
                        .toList();
            } else if (sort.equals("price_desc")) {
                products = products.stream()
                        .sorted(Comparator.comparingDouble(Product::getSalePrice).reversed())
                        .toList();
            } else if (sort.equals("name_asc")) {
                products = products.stream()
                        .sorted(Comparator.comparing(
                                p -> p.getName() != null ? p.getName() : "",
                                String.CASE_INSENSITIVE_ORDER
                        ))
                        .toList();
            } else if (sort.equals("name_desc")) {
                products = products.stream()
                        .sorted(Comparator.comparing(
                                (Product p) -> p.getName() != null ? p.getName() : "",
                                String.CASE_INSENSITIVE_ORDER
                        ).reversed())
                        .toList();
            }
        }

        return products;
    }

    public List<String> getBrands() {
        return productRepository.findDistinctBrands();
    }

    public List<Product> getRelatedProducts(Long categoryId, Long currentProductId) {
        return productRepository.findAll()
                .stream()
                .filter(p -> p.getCategory() != null)
                .filter(p -> p.getCategory().getId().equals(categoryId))
                .filter(p -> !p.getId().equals(currentProductId))
                .limit(4)
                .toList();
    }

    public Product getById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));
    }

    public void save(Product product) {
        if (product == null) {
            throw new RuntimeException("Dữ liệu sản phẩm không hợp lệ");
        }

        if (product.getName() == null || product.getName().trim().isEmpty()) {
            throw new RuntimeException("Tên sản phẩm không được để trống");
        }

        if (product.getPrice() == null) {
            throw new RuntimeException("Giá sản phẩm không được để trống");
        }

        if (product.getPrice() < 0) {
            throw new RuntimeException("Giá sản phẩm không được nhỏ hơn 0");
        }

        if (product.getQuantity() == null) {
            product.setQuantity(0);
        }

        if (product.getQuantity() < 0) {
            throw new RuntimeException("Số lượng tồn kho không được nhỏ hơn 0");
        }

        product.setName(product.getName().trim());

        if (product.getBrand() != null) {
            product.setBrand(product.getBrand().trim());
        }

        if (product.getCpu() != null) {
            product.setCpu(product.getCpu().trim());
        }

        if (product.getRam() != null) {
            product.setRam(product.getRam().trim());
        }

        if (product.getStorage() != null) {
            product.setStorage(product.getStorage().trim());
        }

        if (product.getScreen() != null) {
            product.setScreen(product.getScreen().trim());
        }

        if (product.getWarranty() != null) {
            product.setWarranty(product.getWarranty().trim());
        }

        productRepository.save(product);
    }

    public void delete(Long id) {
        productRepository.deleteById(id);
    }
}