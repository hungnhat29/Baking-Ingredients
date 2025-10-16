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
public class ProductSizeDTO {
    private Integer priceId;
    private String size;
    private String sku;
    private BigDecimal regularPrice;
    private BigDecimal promotionPrice;
    private BigDecimal effectivePrice;
    private Integer discountPercentage;
    private Boolean isPromotionActive;
}
