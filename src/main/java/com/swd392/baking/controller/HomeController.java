package com.swd392.baking.controller;

import com.swd392.baking.model.ProductDTO;
import com.swd392.baking.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final ProductService productService;

    /**
     * Trang chủ - index.html
     */
    @GetMapping("/")
    public String home(Model model) {
        // Lấy sản phẩm featured
        List<ProductDTO> featuredProducts = productService.getFeaturedProducts();
        model.addAttribute("featuredProducts", featuredProducts);

        // Lấy top 10 sản phẩm xem nhiều nhất
        List<ProductDTO> topViewedProducts = productService.getTop10MostViewed();
        model.addAttribute("topViewedProducts", topViewedProducts);

        return "index"; // Trả về templates/index.html
    }

    /**
     * Trang chủ alternative - /home
     */
    @GetMapping("/home")
    public String homeAlternative(Model model) {
        return home(model);
    }

    @GetMapping("/category")
    public String category(Model model) {
        return "category";
    }
}
