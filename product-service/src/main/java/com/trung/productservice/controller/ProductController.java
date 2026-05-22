package com.trung.productservice.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {
    private List<String> products = new ArrayList<>(Arrays.asList(
            "Laptop Dell XPS",
            "MacBook Pro M3",
            "Bàn phím cơ Keychron"
    ));
    @GetMapping
    public ResponseEntity<List<String>> getAllProducts() {
        return ResponseEntity.ok(products);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteProduct(@PathVariable int id) {
        return ResponseEntity.ok("Deleted product with ID: " + id + " successfully!");
    }
}
