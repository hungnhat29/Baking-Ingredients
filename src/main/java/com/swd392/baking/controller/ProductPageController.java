package com.swd392.baking.controller;

import com.swd392.baking.model.ProductDTO;
import com.swd392.baking.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductPageController {

    private final ProductService productService;

    /**
     * Display product detail page
     */
    @GetMapping("/{id}")
    public String getProductDetailPage(@PathVariable Integer id, Model model) {
        try {
            // Get product details
            ProductDTO product = productService.getProductById(id);
            model.addAttribute("product", product);

            // Get related products (featured products as example)
            List<ProductDTO> relatedProducts = productService.getFeaturedProducts();
            model.addAttribute("relatedProducts", relatedProducts);

            return "product-page";
        } catch (RuntimeException e) {
            model.addAttribute("error", "Product not found");
            return "error";
        }
    }
}