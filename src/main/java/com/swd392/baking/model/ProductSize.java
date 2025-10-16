package com.swd392.baking.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "PRODUCTS_SIZE")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductSize {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "price_id")
    private Integer priceId;

    // Relationship: Many ProductSizes belong to One Product
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    @NotNull(message = "Product is required")
    private Product product;

    @NotBlank(message = "Size is required")
    @Size(max = 100, message = "Size must not exceed 100 characters")
    @Column(name = "size", length = 100, nullable = false)
    private String size;

    @NotBlank(message = "SKU is required")
    @Size(max = 100, message = "SKU must not exceed 100 characters")
    @Column(name = "sku", length = 100, nullable = false, unique = true)
    private String sku;

    @NotNull(message = "Regular price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Regular price must be greater than 0")
    @Column(name = "regular_price", precision = 18, scale = 2, nullable = false)
    private BigDecimal regularPrice;

    @DecimalMin(value = "0.0", inclusive = false, message = "Promotion price must be greater than 0")
    @Column(name = "promotion_price", precision = 18, scale = 2)
    private BigDecimal promotionPrice;

    @Column(name = "promotion_start")
    private LocalDateTime promotionStart;

    @Column(name = "promotion_end")
    private LocalDateTime promotionEnd;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Helper method to check if promotion is active
    @Transient
    public boolean isPromotionActive() {
        if (promotionPrice == null || promotionStart == null || promotionEnd == null) {
            return false;
        }
        LocalDateTime now = LocalDateTime.now();
        return now.isAfter(promotionStart) && now.isBefore(promotionEnd);
    }

    // Helper method to get current effective price
    @Transient
    public BigDecimal getEffectivePrice() {
        return isPromotionActive() ? promotionPrice : regularPrice;
    }

    // Helper method to calculate discount percentage
    @Transient
    public Integer getDiscountPercentage() {
        if (!isPromotionActive() || promotionPrice == null) {
            return 0;
        }
        BigDecimal discount = regularPrice.subtract(promotionPrice);
        return discount.multiply(BigDecimal.valueOf(100))
                .divide(regularPrice, 0, BigDecimal.ROUND_HALF_UP)
                .intValue();
    }
}