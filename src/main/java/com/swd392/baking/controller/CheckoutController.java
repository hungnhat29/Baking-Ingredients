package com.swd392.baking.controller;

import com.swd392.baking.model.CartDTO;
import com.swd392.baking.service.CartService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class CheckoutController {

    private final CartService cartService;

    @GetMapping("/checkout")
    public String showCheckoutPage(Model model, HttpSession session) {

        // Get cart to verify it's not empty
        CartDTO cart = cartService.getCart(null, null);

        // Add cart info to model (optional, for server-side rendering)
        model.addAttribute("cart", cart);

        return "checkout";
    }

    @GetMapping("/order-success")
    public String orderSuccess(Model model, HttpSession session) {

        CartDTO cart = cartService.getCart(null, null);

        model.addAttribute("cart", cart);

        return "order-success";
    }
}
