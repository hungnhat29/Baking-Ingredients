package com.swd392.baking.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductDTO {
    private Integer productId;
    private String productName;
    private String description;
    private Integer categoryId;
    private String size;
    private Integer stockQuantity;
    private String mainImageUrl;
    private List<String> imageUrls;
    private Boolean isFeatured;
    private Integer viewCount;
    private Integer soldCount;

    // Price information from ProductSize
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private List<ProductSizeDTO> sizes;
}
