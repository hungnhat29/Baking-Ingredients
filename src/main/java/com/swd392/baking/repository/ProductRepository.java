package com.swd392.baking.repository;

import com.swd392.baking.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {

    // Lấy 10 sản phẩm có view_count cao nhất
    List<Product> findTop10ByIsActiveTrueOrderByViewCountDesc();

    // Lấy sản phẩm theo ID (chỉ active)
    Optional<Product> findByProductIdAndIsActiveTrue(Integer productId);

    // Lấy sản phẩm featured
    List<Product> findByIsFeaturedTrueAndIsActiveTrue();

    // Tìm theo category
    List<Product> findByCategoryIdAndIsActiveTrue(Integer categoryId);
}