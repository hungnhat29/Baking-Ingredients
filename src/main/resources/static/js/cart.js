/**
 * Cart Management Module
 * Xử lý tất cả các thao tác liên quan đến giỏ hàng
 */

const CartModule = (function() {
    'use strict';

    // Cấu hình
    const CONFIG = {
        apiEndpoint: '/api/cart',
        selectors: {
            cartTotal: '.cart-total',
            cartBadge: '.badge.bg-primary.rounded-pill',
            cartItemsList: '#cart-items-list',
            offcanvasCart: '#offcanvasCart',
            addToCartBtn: '[data-add-to-cart]',
            cartItemQuantity: '[data-cart-item-quantity]',
            removeCartItem: '[data-remove-cart-item]',
            clearCart: '[data-clear-cart]'
        }
    };

    /**
     * Hiển thị thông báo toast
     */
    function showToast(message, type = 'success') {
        // Sử dụng Bootstrap Toast hoặc custom notification
        const toastHTML = `
            <div class="toast align-items-center text-white bg-${type === 'success' ? 'success' : 'danger'} border-0 position-fixed top-0 end-0 m-3" 
                 role="alert" aria-live="assertive" aria-atomic="true" style="z-index: 9999;">
                <div class="d-flex">
                    <div class="toast-body">${message}</div>
                    <button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast"></button>
                </div>
            </div>
        `;

        const toastElement = $(toastHTML).appendTo('body');
        const toast = new bootstrap.Toast(toastElement[0], { delay: 3000 });
        toast.show();

        // Xóa element sau khi ẩn
        toastElement.on('hidden.bs.toast', function() {
            $(this).remove();
        });
    }

    /**
     * Format số tiền
     */
    function formatCurrency(amount) {
        if (!amount) return '0₫';
        return new Intl.NumberFormat('vi-VN', {
            style: 'currency',
            currency: 'VND'
        }).format(amount);
    }

    /**
     * Cập nhật UI header (tổng tiền và số lượng items)
     */
    function updateHeaderCart(cart) {
        // Cập nhật tổng tiền
        $(CONFIG.selectors.cartTotal).text(formatCurrency(cart.totalAmount || 0));

        // Cập nhật số lượng items trong badge
        const badge = $(CONFIG.selectors.cartBadge);
        if (cart.totalItems > 0) {
            badge.text(cart.totalItems).show();
        } else {
            badge.text('0').hide();
        }
    }

    /**
     * Render cart items trong offcanvas
     */
    function renderCartItems(cart) {
        const container = $(CONFIG.selectors.cartItemsList);

        if (!cart.items || cart.items.length === 0) {
            container.html(`
                <div class="text-center py-5">
                    <svg width="80" height="80" viewBox="0 0 24 24" class="text-muted mb-3">
                        <use xlink:href="#cart"></use>
                    </svg>
                    <p class="text-muted">Giỏ hàng của bạn đang trống</p>
                    <a href="/" class="btn btn-primary">Tiếp tục mua sắm</a>
                </div>
            `);
            return;
        }

        let itemsHTML = cart.items.map(item => `
            <li class="list-group-item d-flex justify-content-between lh-sm py-3" data-cart-item-id="${item.cartItemId}">
                <div class="d-flex gap-3 flex-grow-1">
                    <img src="${item.mainImageUrl || '/images/placeholder.jpg'}" 
                         alt="${item.productName}" 
                         class="img-thumbnail" 
                         style="width: 60px; height: 60px; object-fit: cover;">
                    
                    <div class="flex-grow-1">
                        <h6 class="my-0">${item.productName}</h6>
                        ${item.sizeSelected ? `<small class="text-muted">Size: ${item.sizeSelected}</small><br>` : ''}
                        <small class="text-muted">${formatCurrency(item.price)} x ${item.quantity}</small>
                        
                        <div class="d-flex gap-2 mt-2 align-items-center">
                            <div class="input-group input-group-sm" style="max-width: 120px;">
                                <button class="btn btn-outline-secondary btn-decrease-qty" 
                                        type="button" 
                                        data-cart-item-id="${item.cartItemId}">-</button>
                                <input type="number" 
                                       class="form-control text-center" 
                                       value="${item.quantity}" 
                                       min="1" 
                                       max="${item.stockQuantity || 999}"
                                       data-cart-item-quantity="${item.cartItemId}"
                                       readonly>
                                <button class="btn btn-outline-secondary btn-increase-qty" 
                                        type="button" 
                                        data-cart-item-id="${item.cartItemId}">+</button>
                            </div>
                            <button class="btn btn-sm btn-link text-danger p-0" 
                                    data-remove-cart-item="${item.cartItemId}">
                                Xóa
                            </button>
                        </div>
                    </div>
                </div>
                <div class="text-end">
                    <strong>${formatCurrency(item.subTotal)}</strong>
                </div>
            </li>
        `).join('');

        container.html(`
            <ul class="list-group mb-3">
                ${itemsHTML}
            </ul>
            <div class="card">
                <div class="card-body">
                    <div class="d-flex justify-content-between mb-2">
                        <span>Tổng cộng:</span>
                        <strong class="text-primary fs-5">${formatCurrency(cart.totalAmount)}</strong>
                    </div>
                    <div class="d-grid gap-2">
                        <a href="/checkout" class="btn btn-primary btn-lg">Thanh toán</a>
                        <a href="/cart" class="btn btn-outline-primary">Xem giỏ hàng</a>
                        <button class="btn btn-sm btn-link text-danger" data-clear-cart>Xóa tất cả</button>
                    </div>
                </div>
            </div>
        `);

        // Bind events cho các nút vừa tạo
        bindCartItemEvents();
    }

    /**
     * Lấy thông tin cart từ API
     */
    async function fetchCart() {
        try {
            const response = await fetch(CONFIG.apiEndpoint);
            if (!response.ok) throw new Error('Không thể tải giỏ hàng');

            const cart = await response.json();
            updateHeaderCart(cart);
            return cart;
        } catch (error) {
            console.error('Error fetching cart:', error);
            showToast('Không thể tải giỏ hàng', 'error');
            return null;
        }
    }

    /**
     * Thêm sản phẩm vào giỏ hàng
     */
    async function addToCart(productData) {
        try {
            const response = await fetch(CONFIG.apiEndpoint, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(productData)
            });

            const result = await response.json();

            if (!response.ok || !result.success) {
                throw new Error(result.message || 'Không thể thêm vào giỏ hàng');
            }

            showToast(result.message);
            updateHeaderCart(result.cart);

            // Mở offcanvas để hiển thị cart
            const offcanvas = new bootstrap.Offcanvas($(CONFIG.selectors.offcanvasCart)[0]);
            offcanvas.show();

            return result.cart;
        } catch (error) {
            console.error('Error adding to cart:', error);
            showToast(error.message, 'error');
            return null;
        }
    }

    /**
     * Cập nhật số lượng sản phẩm
     */
    async function updateQuantity(cartItemId, quantity) {
        try {
            const response = await fetch(`${CONFIG.apiEndpoint}/items/${cartItemId}?quantity=${quantity}`, {
                method: 'PUT'
            });

            const result = await response.json();

            if (!response.ok || !result.success) {
                throw new Error(result.message || 'Không thể cập nhật');
            }

            showToast(result.message);
            updateHeaderCart(result.cart);
            renderCartItems(result.cart);

            return result.cart;
        } catch (error) {
            console.error('Error updating quantity:', error);
            showToast(error.message, 'error');
            return null;
        }
    }

    /**
     * Xóa sản phẩm khỏi giỏ hàng
     */
    async function removeItem(cartItemId) {
        try {
            const response = await fetch(`${CONFIG.apiEndpoint}/items/${cartItemId}`, {
                method: 'DELETE'
            });

            const result = await response.json();

            if (!response.ok || !result.success) {
                throw new Error(result.message || 'Không thể xóa sản phẩm');
            }

            showToast(result.message);
            updateHeaderCart(result.cart);
            renderCartItems(result.cart);

            return result.cart;
        } catch (error) {
            console.error('Error removing item:', error);
            showToast(error.message, 'error');
            return null;
        }
    }

    /**
     * Xóa toàn bộ giỏ hàng
     */
    async function clearCart() {
        if (!confirm('Bạn có chắc muốn xóa tất cả sản phẩm?')) return;

        try {
            const response = await fetch(CONFIG.apiEndpoint, {
                method: 'DELETE'
            });

            const result = await response.json();

            if (!response.ok || !result.success) {
                throw new Error(result.message || 'Không thể xóa giỏ hàng');
            }

            showToast(result.message);
            updateHeaderCart({ totalAmount: 0, totalItems: 0 });
            renderCartItems({ items: [] });
        } catch (error) {
            console.error('Error clearing cart:', error);
            showToast(error.message, 'error');
        }
    }

    /**
     * Bind events cho cart items (tăng/giảm số lượng, xóa)
     */
    function bindCartItemEvents() {
        // Tăng số lượng
        $('.btn-increase-qty').off('click').on('click', function() {
            const cartItemId = $(this).data('cart-item-id');
            const input = $(`input[data-cart-item-quantity="${cartItemId}"]`);
            const newQty = parseInt(input.val()) + 1;
            const maxQty = parseInt(input.attr('max'));

            if (newQty <= maxQty) {
                updateQuantity(cartItemId, newQty);
            } else {
                showToast('Đã đạt số lượng tối đa', 'error');
            }
        });

        // Giảm số lượng
        $('.btn-decrease-qty').off('click').on('click', function() {
            const cartItemId = $(this).data('cart-item-id');
            const input = $(`input[data-cart-item-quantity="${cartItemId}"]`);
            const newQty = parseInt(input.val()) - 1;

            if (newQty >= 1) {
                updateQuantity(cartItemId, newQty);
            }
        });

        // Xóa item
        $('[data-remove-cart-item]').off('click').on('click', function() {
            const cartItemId = $(this).data('remove-cart-item');
            removeItem(cartItemId);
        });

        // Xóa tất cả
        $('[data-clear-cart]').off('click').on('click', function() {
            clearCart();
        });
    }

    /**
     * Khởi tạo module
     */
    function init() {
        // Load cart KHI MỞ offcanvas (không tự động load khi vào trang)
        $(CONFIG.selectors.offcanvasCart).on('show.bs.offcanvas', async function() {
            const cart = await fetchCart();
            if (cart) {
                renderCartItems(cart);
            }
        });

        // Chỉ cập nhật header (không load full cart)
        fetchCartSummary();

        // Bind event cho nút Add to Cart (global - cho các trang khác)
        $(document).on('click', '[data-add-to-cart]', function(e) {
            e.preventDefault();

            const $btn = $(this);
            const productData = {
                productId: parseInt($btn.data('product-id')),
                quantity: parseInt($btn.data('quantity') || 1),
                sizeSelected: $btn.data('size-selected') || null,
                priceId: parseInt($btn.data('price-id')) || null
            };

            addToCart(productData);
        });
    }

    /**
     * Chỉ lấy summary để update header (nhẹ hơn)
     */
    async function fetchCartSummary() {
        try {
            const response = await fetch(CONFIG.apiEndpoint);
            if (!response.ok) return;

            const cart = await response.json();
            updateHeaderCart(cart);
        } catch (error) {
            console.error('Error fetching cart summary:', error);
            // Không hiển thị lỗi để không làm phiền user
        }
    }

    // Public API
    return {
        init: init,
        addToCart: addToCart,
        updateQuantity: updateQuantity,
        removeItem: removeItem,
        clearCart: clearCart,
        fetchCart: fetchCart,
        formatCurrency: formatCurrency
    };
})();

// Khởi tạo khi DOM ready
$(document).ready(function() {
    CartModule.init();
});