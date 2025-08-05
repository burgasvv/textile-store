package org.burgas.backendserver.controller;

import lombok.RequiredArgsConstructor;
import org.burgas.backendserver.dto.product.ProductRequest;
import org.burgas.backendserver.dto.product.ProductWithCategory;
import org.burgas.backendserver.dto.product.ProductWithoutCategory;
import org.burgas.backendserver.service.ProductService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/products")
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public ResponseEntity<List<ProductWithoutCategory>> getAllProducts() {
        return ResponseEntity
                .status(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(this.productService.findAll());
    }

    @GetMapping(value = "/by-id")
    public ResponseEntity<ProductWithCategory> getProductById(@RequestParam UUID productId) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(this.productService.findById(productId));
    }

    @PostMapping(value = "/create")
    public ResponseEntity<ProductWithCategory> createProduct(@RequestBody ProductRequest productRequest) {
        ProductWithCategory productWithCategory = this.productService.createOnMaster(productRequest);
        return ResponseEntity
                .status(HttpStatus.FOUND)
                .contentType(MediaType.APPLICATION_JSON)
                .location(URI.create("/products/by-id?productId=" + productWithCategory.getId()))
                .body(productWithCategory);
    }

    @PostMapping(value = "/update")
    public ResponseEntity<ProductWithCategory> updateProduct(@RequestBody ProductRequest productRequest, @RequestParam UUID productId) {
        ProductWithCategory productWithCategory = this.productService.updateOnMaster(productId, productRequest);
        return ResponseEntity
                .status(HttpStatus.FOUND)
                .contentType(MediaType.APPLICATION_JSON)
                .location(URI.create("/products/by-id?productId=" + productWithCategory.getId()))
                .body(productWithCategory);
    }

    @DeleteMapping(value = "/delete")
    public ResponseEntity<String> deleteProduct(@RequestParam UUID productId) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .contentType(new MediaType(MediaType.TEXT_PLAIN, StandardCharsets.UTF_8))
                .body(this.productService.deleteFromMaster(productId));
    }
}
