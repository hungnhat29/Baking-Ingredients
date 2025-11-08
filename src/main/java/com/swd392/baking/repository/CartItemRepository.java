package com.swd392.baking.repository;

import com.swd392.baking.model.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Integer> {

    List<CartItem> findByCartCartId(Integer cartId);

    Optional<CartItem> findByCartCartIdAndProductProductIdAndSizeSelectedAndPriceId(
            Integer cartId, Integer productId, String sizeSelected, Integer priceId);

    void deleteByCartCartId(Integer cartId);
}