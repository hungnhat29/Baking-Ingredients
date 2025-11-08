package com.swd392.baking.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartItemDTO {

    private Integer cartItemId;
    private Integer productId;
    private String productName;
    private String mainImageUrl;
    private Integer quantity;
    private BigDecimal price;
    private BigDecimal subTotal;

    // Thông tin size
    private String sizeSelected;
    private Integer priceId;

    // Thông tin sản phẩm bổ sung
    private String description;
    private Integer stockQuantity;
}