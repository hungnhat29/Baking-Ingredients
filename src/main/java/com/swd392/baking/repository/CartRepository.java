package com.swd392.baking.repository;

import com.swd392.baking.model.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Integer> {

    Optional<Cart> findByUserId(Integer userId);

    Optional<Cart> findBySessionId(String sessionId);

    @Query("SELECT c FROM Cart c LEFT JOIN FETCH c.cartItems ci LEFT JOIN FETCH ci.product WHERE c.userId = :userId")
    Optional<Cart> findByUserIdWithItems(Integer userId);

    @Query("SELECT c FROM Cart c LEFT JOIN FETCH c.cartItems ci LEFT JOIN FETCH ci.product WHERE c.sessionId = :sessionId")
    Optional<Cart> findBySessionIdWithItems(String sessionId);

    void deleteByUserId(Integer userId);

    void deleteBySessionId(String sessionId);
}