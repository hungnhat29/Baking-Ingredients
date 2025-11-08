package com.swd392.baking.controller;

import com.swd392.baking.model.AddToCartRequest;
import com.swd392.baking.model.CartDTO;
import com.swd392.baking.service.CartService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    /**
     * Hiển thị trang giỏ hàng (Guest only)
     */
    @GetMapping("/cart")
    public String viewCart(Model model, HttpSession session) {
        String sessionId = getOrCreateSessionId(session);
        CartDTO cart = cartService.getCart(null, sessionId);

        model.addAttribute("cart", cart);
        model.addAttribute("cartItems", cart.getItems());
        model.addAttribute("totalAmount", cart.getTotalAmount());
        model.addAttribute("totalItems", cart.getTotalItems());

        return "cart";
    }

    /**
     * API: Thêm sản phẩm vào giỏ hàng (Guest only)
     */
    @PostMapping("/api/cart")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> addToCart(
            @Valid @RequestBody AddToCartRequest request,
            HttpSession session) {

        Map<String, Object> response = new HashMap<>();

        try {
            String sessionId = getOrCreateSessionId(session);

            // Thêm vào giỏ hàng với sessionId
            CartDTO cart = cartService.addToCart(request, null, sessionId);

            response.put("success", true);
            response.put("message", "Sản phẩm đã được thêm vào giỏ hàng!");
            response.put("cart", cart);
            response.put("redirectUrl", "/cart");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Lỗi: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    /**
     * API: Lấy thông tin giỏ hàng (Guest only)
     */
    @GetMapping("/api/cart")
    @ResponseBody
    public ResponseEntity<CartDTO> getCart(HttpSession session) {
        String sessionId = getOrCreateSessionId(session);
        CartDTO cart = cartService.getCart(null, sessionId);
        return ResponseEntity.ok(cart);
    }

    /**
     * API: Cập nhật số lượng sản phẩm trong giỏ hàng (Guest only)
     */
    @PutMapping("/api/cart/items/{cartItemId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateCartItem(
            @PathVariable Integer cartItemId,
            @RequestParam Integer quantity,
            HttpSession session) {

        Map<String, Object> response = new HashMap<>();

        try {
            String sessionId = getOrCreateSessionId(session);

            CartDTO cart = cartService.updateCartItemQuantity(cartItemId, quantity, null, sessionId);

            response.put("success", true);
            response.put("message", "Giỏ hàng đã được cập nhật");
            response.put("cart", cart);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Lỗi cập nhật: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    /**
     * API: Xóa sản phẩm khỏi giỏ hàng (Guest only)
     */
    @DeleteMapping("/api/cart/items/{cartItemId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> removeCartItem(
            @PathVariable Integer cartItemId,
            HttpSession session) {

        Map<String, Object> response = new HashMap<>();

        try {
            String sessionId = getOrCreateSessionId(session);

            CartDTO cart = cartService.removeCartItem(cartItemId, null, sessionId);

            response.put("success", true);
            response.put("message", "Sản phẩm đã được xóa khỏi giỏ hàng");
            response.put("cart", cart);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Lỗi xóa sản phẩm: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    /**
     * API: Xóa toàn bộ giỏ hàng (Guest only)
     */
    @DeleteMapping("/api/cart")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> clearCart(HttpSession session) {

        Map<String, Object> response = new HashMap<>();

        try {
            String sessionId = getOrCreateSessionId(session);

            cartService.clearCart(null, sessionId);

            response.put("success", true);
            response.put("message", "Giỏ hàng đã được xóa");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Lỗi xóa giỏ hàng: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    /**
     * API: Lấy preview giỏ hàng (cho header/notification) - Guest only
     */
    @GetMapping("/api/cart/preview")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getCartPreview(HttpSession session) {

        Map<String, Object> response = new HashMap<>();

        try {
            String sessionId = getOrCreateSessionId(session);
            CartDTO cart = cartService.getCart(null, sessionId);

            response.put("success", true);
            response.put("totalItems", cart.getTotalItems());
            response.put("totalAmount", cart.getTotalAmount());
            response.put("items", cart.getItems());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Lỗi tải giỏ hàng: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    // ==================== Helper Methods ====================

    /**
     * Lấy hoặc tạo mới sessionId cho guest user
     */
    private String getOrCreateSessionId(HttpSession session) {
        String sessionId = (String) session.getAttribute("sessionId");
        if (sessionId == null || sessionId.isEmpty()) {
            sessionId = UUID.randomUUID().toString();
            session.setAttribute("sessionId", sessionId);
        }
        return sessionId;
    }
}