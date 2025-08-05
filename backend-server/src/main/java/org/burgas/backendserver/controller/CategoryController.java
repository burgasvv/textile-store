package org.burgas.backendserver.controller;

import lombok.RequiredArgsConstructor;
import org.burgas.backendserver.dto.category.CategoryRequest;
import org.burgas.backendserver.dto.category.CategoryWithProducts;
import org.burgas.backendserver.dto.category.CategoryWithoutProducts;
import org.burgas.backendserver.service.CategoryService;
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
@RequestMapping(value = "/categories")
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public ResponseEntity<List<CategoryWithoutProducts>> getAllCategories() {
        return ResponseEntity
                .status(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(this.categoryService.findAll());
    }

    @GetMapping(value = "/by-id")
    public ResponseEntity<CategoryWithProducts> getCategoryById(@RequestParam UUID categoryId) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(this.categoryService.findById(categoryId));
    }

    @PostMapping(value = "/create")
    public ResponseEntity<CategoryWithProducts> createCategory(@RequestBody CategoryRequest categoryRequest) {
        CategoryWithProducts categoryWithProducts = this.categoryService.create(categoryRequest);
        return ResponseEntity
                .status(HttpStatus.FOUND)
                .contentType(MediaType.APPLICATION_JSON)
                .location(URI.create("/categories/by-id?categoryId=" + categoryWithProducts.getId()))
                .body(categoryWithProducts);
    }

    @PostMapping(value = "/create-multiple")
    public ResponseEntity<String> createCategories(@RequestBody List<CategoryRequest> categoryRequests) {
        String categoryServiceMultiple = this.categoryService.createMultiple(categoryRequests);
        return ResponseEntity
                .status(HttpStatus.FOUND)
                .contentType(new MediaType(MediaType.TEXT_PLAIN, StandardCharsets.UTF_8))
                .location(URI.create("/categories"))
                .body(categoryServiceMultiple);
    }

    @PostMapping(value = "/update")
    public ResponseEntity<CategoryWithProducts> updateCategory(@RequestBody CategoryRequest categoryRequest, @RequestParam UUID categoryId) {
        CategoryWithProducts categoryWithProducts = this.categoryService.updateOnMaster(categoryId, categoryRequest);
        return ResponseEntity
                .status(HttpStatus.FOUND)
                .contentType(MediaType.APPLICATION_JSON)
                .location(URI.create("/categories/by-id?categoryId=" + categoryWithProducts.getId()))
                .body(categoryWithProducts);
    }

    @DeleteMapping(value = "/delete")
    public ResponseEntity<String> deleteCategory(@RequestParam UUID categoryId) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(this.categoryService.deleteFromMaster(categoryId));
    }
}
