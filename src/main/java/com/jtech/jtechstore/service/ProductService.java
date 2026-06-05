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
            return productRepository.findByNameContainingIgnoreCase(keyword);
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
                    .filter(p -> p.getBrand() != null && p.getBrand().equalsIgnoreCase(brand))
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
                        .sorted(Comparator.comparing(Product::getName, String.CASE_INSENSITIVE_ORDER))
                        .toList();
            } else if (sort.equals("name_desc")) {
                products = products.stream()
                        .sorted(Comparator.comparing(Product::getName, String.CASE_INSENSITIVE_ORDER).reversed())
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
        if (product.getQuantity() == null) {
            product.setQuantity(0);
        }

        productRepository.save(product);
    }

    public void delete(Long id) {
        productRepository.deleteById(id);
    }
}