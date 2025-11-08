package com.swd392.baking.service;

import com.swd392.baking.model.Cart;
import com.swd392.baking.model.CartItem;
import com.swd392.baking.model.Product;
import com.swd392.baking.model.ProductSize;
import com.swd392.baking.model.AddToCartRequest;
import com.swd392.baking.model.CartDTO;
import com.swd392.baking.model.CartItemDTO;
import com.swd392.baking.repository.CartRepository;
import com.swd392.baking.repository.CartItemRepository;
import com.swd392.baking.repository.ProductRepository;
import com.swd392.baking.repository.ProductSizeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final ProductSizeRepository productSizeRepository;

    /**
     * Thêm sản phẩm vào giỏ hàng
     */
    @Transactional
    public CartDTO addToCart(AddToCartRequest request, Integer userId, String sessionId) {
        // Validate input
        validateAddToCartRequest(request, userId, sessionId);

        // Tìm hoặc tạo mới giỏ hàng
        Cart cart = findOrCreateCart(userId, sessionId);

        // Lấy thông tin sản phẩm
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + request.getProductId()));

        // Xác định giá sản phẩm dựa trên size
        BigDecimal price = determinePrice(product, request.getPriceId());

        // Kiểm tra xem sản phẩm đã có trong giỏ hàng chưa (cùng product và size)
        Optional<CartItem> existingItem = findExistingCartItem(
                cart.getCartId(),
                request.getProductId(),
                request.getSizeSelected(),
                request.getPriceId()
        );

        if (existingItem.isPresent()) {
            // Nếu đã có, tăng số lượng
            CartItem item = existingItem.get();
            item.setQuantity(item.getQuantity() + request.getQuantity());
            cartItemRepository.save(item);
        } else {
            // Nếu chưa có, tạo mới cart item
            CartItem newItem = CartItem.builder()
                    .cart(cart)
                    .product(product)
                    .quantity(request.getQuantity())
                    .price(price)
                    .sizeSelected(request.getSizeSelected())
                    .priceId(request.getPriceId())
                    .build();
            cartItemRepository.save(newItem);
        }

        // Cập nhật thời gian của cart
        cartRepository.save(cart);

        // Trả về cart DTO
        return getCart(userId, sessionId);
    }

    /**
     * Lấy thông tin giỏ hàng
     */
    @Transactional(readOnly = true)
    public CartDTO getCart(Integer userId, String sessionId) {
        Optional<Cart> cartOpt = findCart(userId, sessionId);

        if (cartOpt.isEmpty()) {
            return CartDTO.builder()
                    .items(List.of())
                    .totalAmount(BigDecimal.ZERO)
                    .totalItems(0)
                    .build();
        }

        Cart cart = cartOpt.get();
        List<CartItemDTO> items = cart.getCartItems().stream()
                .map(this::convertToCartItemDTO)
                .collect(Collectors.toList());

        BigDecimal totalAmount = items.stream()
                .map(CartItemDTO::getSubTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Integer totalItems = items.stream()
                .mapToInt(CartItemDTO::getQuantity)
                .sum();

        return CartDTO.builder()
                .cartId(cart.getCartId())
                .userId(cart.getUserId())
                .sessionId(cart.getSessionId())
                .items(items)
                .totalAmount(totalAmount)
                .totalItems(totalItems)
                .build();
    }

    /**
     * Cập nhật số lượng sản phẩm trong giỏ hàng
     */
    @Transactional
    public CartDTO updateCartItemQuantity(Integer cartItemId, Integer quantity, Integer userId, String sessionId) {
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("Cart item not found"));

        // Verify ownership
        verifyCartOwnership(cartItem.getCart(), userId, sessionId);

        if (quantity <= 0) {
            cartItemRepository.delete(cartItem);
        } else {
            cartItem.setQuantity(quantity);
            cartItemRepository.save(cartItem);
        }

        return getCart(userId, sessionId);
    }

    /**
     * Xóa sản phẩm khỏi giỏ hàng
     */
    @Transactional
    public CartDTO removeCartItem(Integer cartItemId, Integer userId, String sessionId) {
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("Cart item not found"));

        // Verify ownership
        verifyCartOwnership(cartItem.getCart(), userId, sessionId);

        cartItemRepository.delete(cartItem);

        return getCart(userId, sessionId);
    }

    /**
     * Xóa toàn bộ giỏ hàng
     */
    @Transactional
    public void clearCart(Integer userId, String sessionId) {
        Optional<Cart> cartOpt = findCart(userId, sessionId);
        cartOpt.ifPresent(cart -> {
            cartItemRepository.deleteByCartCartId(cart.getCartId());
            cartRepository.delete(cart);
        });
    }

    /**
     * Merge giỏ hàng của guest vào giỏ hàng của user khi đăng nhập
     */
    @Transactional
    public void mergeGuestCartToUser(String sessionId, Integer userId) {
        Optional<Cart> guestCartOpt = cartRepository.findBySessionIdWithItems(sessionId);

        if (guestCartOpt.isEmpty()) {
            return; // Không có giỏ hàng guest
        }

        Cart guestCart = guestCartOpt.get();
        Cart userCart = findOrCreateCart(userId, null);

        // Merge các cart items
        for (CartItem guestItem : guestCart.getCartItems()) {
            Optional<CartItem> existingItem = findExistingCartItem(
                    userCart.getCartId(),
                    guestItem.getProduct().getProductId(),
                    guestItem.getSizeSelected(),
                    guestItem.getPriceId()
            );

            if (existingItem.isPresent()) {
                // Cộng dồn số lượng
                CartItem item = existingItem.get();
                item.setQuantity(item.getQuantity() + guestItem.getQuantity());
                cartItemRepository.save(item);
            } else {
                // Chuyển item sang cart của user
                guestItem.setCart(userCart);
                cartItemRepository.save(guestItem);
            }
        }

        // Xóa giỏ hàng guest
        cartRepository.delete(guestCart);
    }

    // ==================== Private Helper Methods ====================

    private void validateAddToCartRequest(AddToCartRequest request, Integer userId, String sessionId) {
        if ((userId == null && sessionId == null) || (userId != null && sessionId != null)) {
            throw new IllegalArgumentException("Either userId OR sessionId must be present, not both");
        }

        if (request.getQuantity() == null || request.getQuantity() < 1) {
            throw new IllegalArgumentException("Quantity must be at least 1");
        }
    }

    private Cart findOrCreateCart(Integer userId, String sessionId) {
        Optional<Cart> cartOpt = findCart(userId, sessionId);

        if (cartOpt.isPresent()) {
            return cartOpt.get();
        }

        // Tạo mới cart
        Cart newCart = Cart.builder()
                .userId(userId)
                .sessionId(sessionId)
                .build();

        return cartRepository.save(newCart);
    }

    private Optional<Cart> findCart(Integer userId, String sessionId) {
        if (userId != null) {
            return cartRepository.findByUserIdWithItems(userId);
        } else if (sessionId != null) {
            return cartRepository.findBySessionIdWithItems(sessionId);
        }
        return Optional.empty();
    }

    private BigDecimal determinePrice(Product product, Integer priceId) {
        if (priceId != null) {
            // Nếu có priceId, lấy giá từ ProductSize
            ProductSize productSize = productSizeRepository.findById(priceId)
                    .orElseThrow(() -> new RuntimeException("Product size not found with id: " + priceId));

            // Sử dụng method getEffectivePrice() có sẵn trong ProductSize
            // Method này đã tự động kiểm tra promotion và trả về giá phù hợp
            return productSize.getEffectivePrice();
        }

        // Nếu không có priceId, sử dụng giá mặc định của product
        // (giả sử Product entity có trường basePrice hoặc price)
        // Bạn cần điều chỉnh tùy theo cấu trúc Product entity của bạn
        throw new RuntimeException("Price information not provided");
    }

    private Optional<CartItem> findExistingCartItem(Integer cartId, Integer productId,
                                                    String sizeSelected, Integer priceId) {
        return cartItemRepository.findByCartCartIdAndProductProductIdAndSizeSelectedAndPriceId(
                cartId, productId, sizeSelected, priceId);
    }

    private void verifyCartOwnership(Cart cart, Integer userId, String sessionId) {
        boolean isOwner = (userId != null && userId.equals(cart.getUserId())) ||
                (sessionId != null && sessionId.equals(cart.getSessionId()));

        if (!isOwner) {
            throw new RuntimeException("Unauthorized access to cart");
        }
    }

    private CartItemDTO convertToCartItemDTO(CartItem item) {
        return CartItemDTO.builder()
                .cartItemId(item.getCartItemId())
                .productId(item.getProduct().getProductId())
                .productName(item.getProduct().getProductName())
                .mainImageUrl(item.getProduct().getMainImageUrl())
                .quantity(item.getQuantity())
                .price(item.getPrice())
                .subTotal(item.getSubTotal())
                .sizeSelected(item.getSizeSelected())
                .priceId(item.getPriceId())
                .description(item.getProduct().getDescription())
                .stockQuantity(item.getProduct().getStockQuantity())
                .build();
    }
}