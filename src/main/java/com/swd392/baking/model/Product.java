package com.swd392.baking.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "PRODUCTS")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Integer productId;

    @NotBlank(message = "Product name is required")
    @Size(max = 500, message = "Product name must not exceed 500 characters")
    @Column(name = "product_name", length = 500, nullable = false)
    private String productName;

    @Lob
    @Column(name = "description", columnDefinition = "NVARCHAR(MAX)")
    private String description;

    @NotNull(message = "Category is required")
    @Column(name = "category_id", nullable = false)
    private Integer categoryId;

    @Column(name = "size", length = 100)
    private String size;

    @Column(name = "stock_quantity", nullable = false)
    private Integer stockQuantity = 0;

    @Column(name = "main_image_url", length = 500)
    private String mainImageUrl;

    @Lob
    @Column(name = "image_urls", columnDefinition = "NVARCHAR(MAX)")
    private String imageUrls; // JSON array: ["url1", "url2", "url3"]

    @Column(name = "is_featured", nullable = false)
    private Boolean isFeatured = false;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "view_count", nullable = false)
    private Integer viewCount = 0;

    @Column(name = "sold_count", nullable = false)
    private Integer soldCount = 0;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Relationship: One Product has Many ProductSizes
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ProductSize> productSizes = new ArrayList<>();

    // Helper methods for bidirectional relationship
    public void addProductSize(ProductSize productSize) {
        productSizes.add(productSize);
        productSize.setProduct(this);
    }

    public void removeProductSize(ProductSize productSize) {
        productSizes.remove(productSize);
        productSize.setProduct(null);
    }

    // Transient method to parse image URLs from JSON
    @Transient
    public List<String> getImageUrlsList() {
        if (imageUrls == null || imageUrls.isEmpty()) {
            return new ArrayList<>();
        }
        try {
            // Simple JSON parsing - you can use Jackson for complex cases
            return List.of(imageUrls.replace("[", "")
                    .replace("]", "")
                    .replace("\"", "")
                    .split(","));
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    @Transient
    public void setImageUrlsList(List<String> urls) {
        if (urls == null || urls.isEmpty()) {
            this.imageUrls = null;
            return;
        }
        // Convert to JSON array format
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < urls.size(); i++) {
            json.append("\"").append(urls.get(i)).append("\"");
            if (i < urls.size() - 1) {
                json.append(",");
            }
        }
        json.append("]");
        this.imageUrls = json.toString();
    }
}