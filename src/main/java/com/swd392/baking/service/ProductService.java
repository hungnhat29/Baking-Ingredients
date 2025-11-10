package com.swd392.baking.service;

import com.swd392.baking.model.ProductDTO;
import com.swd392.baking.model.ProductSizeDTO;
import com.swd392.baking.model.Product;
import com.swd392.baking.model.ProductSize;
import com.swd392.baking.repository.ProductRepository;
import com.swd392.baking.repository.ProductSizeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductSizeRepository productSizeRepository;

    /**
     * Lấy sản phẩm theo ID
     */
    public ProductDTO getProductById(Integer productId) {
        Product product = productRepository.findByProductIdAndIsActiveTrue(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + productId));

        // Tăng view count
        incrementViewCount(productId);

        return convertToDTO(product);
    }

    /**
     * Lấy 10 sản phẩm có view count cao nhất
     */
    public List<ProductDTO> getTop10MostViewed() {
        List<Product> products = productRepository.findTop10ByIsActiveTrueOrderByViewCountDesc();
        return products.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Lấy sản phẩm featured cho trang home
     */
    public List<ProductDTO> getFeaturedProducts() {
        List<Product> products = productRepository.findByIsFeaturedTrueAndIsActiveTrue();
        return products.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Tăng view count
     */
    @Transactional
    public void incrementViewCount(Integer productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        product.setViewCount(product.getViewCount() + 1);
        productRepository.save(product);
    }

    /**
     * Lấy sản pham active by category
     */
    public List<ProductDTO> getActiveProductsByCategory(Integer categoryId) {
        List<Product> products = productRepository.findProductsByCategory(categoryId);
        return products.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Lấy sản pham active by category
     */
    public List<ProductDTO> listAllProductIsActive() {
        List<Product> products = productRepository.listAllProducts();
        return products.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Convert Product entity to DTO
     */
    private ProductDTO convertToDTO(Product product) {
        List<ProductSize> sizes = productSizeRepository.findByProduct_ProductId(product.getProductId());

        // Convert sizes to DTO
        List<ProductSizeDTO> sizeDTOs = sizes.stream()
                .map(this::convertSizeToDTO)
                .collect(Collectors.toList());

        // Calculate min and max price
        BigDecimal minPrice = sizes.stream()
                .map(ProductSize::getEffectivePrice)
                .min(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);

        BigDecimal maxPrice = sizes.stream()
                .map(ProductSize::getEffectivePrice)
                .max(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);

        return ProductDTO.builder()
                .productId(product.getProductId())
                .productName(product.getProductName())
                .description(product.getDescription())
                .categoryId(product.getCategoryId())
                .size(product.getSize())
                .stockQuantity(product.getStockQuantity())
                .mainImageUrl(product.getMainImageUrl())
                .imageUrls(product.getImageUrlsList())
                .isFeatured(product.getIsFeatured())
                .viewCount(product.getViewCount())
                .soldCount(product.getSoldCount())
                .minPrice(minPrice)
                .maxPrice(maxPrice)
                .sizes(sizeDTOs)
                .build();
    }

    /**
     * Convert ProductSize entity to DTO
     */
    private ProductSizeDTO convertSizeToDTO(ProductSize size) {
        return ProductSizeDTO.builder()
                .priceId(size.getPriceId())
                .size(size.getSize())
                .sku(size.getSku())
                .regularPrice(size.getRegularPrice())
                .promotionPrice(size.getPromotionPrice())
                .effectivePrice(size.getEffectivePrice())
                .discountPercentage(size.getDiscountPercentage())
                .isPromotionActive(size.isPromotionActive())
                .build();
    }

    public List<ProductDTO> getRelatedProductsByCategory(Integer productId, Integer limit) {
        // Lấy thông tin sản phẩm hiện tại
        Product currentProduct = productRepository.findByProductIdAndIsActiveTrue(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + productId));

        // Lấy tất cả sản phẩm cùng category
        List<Product> relatedProducts = productRepository.findByCategoryIdAndIsActiveTrue(currentProduct.getCategoryId());

        // Loại bỏ sản phẩm hiện tại và giới hạn số lượng
        return relatedProducts.stream()
                .filter(p -> !p.getProductId().equals(productId))
                .limit(limit != null ? limit : 8)
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Overload method với limit mặc định
     */
    public List<ProductDTO> getRelatedProductsByCategory(Integer productId) {
        return getRelatedProductsByCategory(productId, 8);
    }
}
