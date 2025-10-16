package com.swd392.baking.repository;

import com.swd392.baking.model.ProductSize;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductSizeRepository extends JpaRepository<ProductSize, Integer> {

    // Lấy tất cả sizes của một sản phẩm
    List<ProductSize> findByProduct_ProductId(Integer productId);

    // Tìm theo SKU
    Optional<ProductSize> findBySku(String sku);
}