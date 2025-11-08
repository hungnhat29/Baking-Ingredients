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
public class CartDTO {

    private Integer cartId;
    private Integer userId;
    private String sessionId;
    private List<CartItemDTO> items;
    private BigDecimal totalAmount;
    private Integer totalItems;
}