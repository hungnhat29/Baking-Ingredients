package com.swd392.baking.controller;

import com.swd392.baking.model.ProductDTO;
import com.swd392.baking.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    /**
     * GET /api/products/{id}
     * Lấy thông tin chi tiết sản phẩm theo ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProductDTO> getProductById(@PathVariable Integer id) {
        ProductDTO product = productService.getProductById(id);
        return ResponseEntity.ok(product);
    }

    /**
     * GET /api/products/top-viewed
     * Lấy 10 sản phẩm có view count cao nhất
     */
    @GetMapping("/top-viewed")
    public ResponseEntity<List<ProductDTO>> getTop10MostViewed() {
        List<ProductDTO> products = productService.getTop10MostViewed();
        return ResponseEntity.ok(products);
    }

    /**
     * GET /api/products/featured
     * Lấy sản phẩm featured
     */
    @GetMapping("/featured")
    public ResponseEntity<List<ProductDTO>> getFeaturedProducts() {
        List<ProductDTO> products = productService.getFeaturedProducts();
        return ResponseEntity.ok(products);
    }

    @GetMapping("/{id}/related")
    public ResponseEntity<List<ProductDTO>> getRelatedProducts(
            @PathVariable Integer id,
            @RequestParam(required = false, defaultValue = "8") Integer limit) {
        List<ProductDTO> products = productService.getRelatedProductsByCategory(id, limit);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/list-products")
    public ResponseEntity<List<ProductDTO>> getAllProductIsActive() {
        List<ProductDTO> products = productService.listAllProductIsActive();
        return ResponseEntity.ok(products);
    }

    @GetMapping("/by-category/{id}")
    public ResponseEntity<List<ProductDTO>> getProductsByCategory(@PathVariable Integer id) {
        List<ProductDTO> products = productService.getActiveProductsByCategory(id);
        return ResponseEntity.ok(products);
    }
}
