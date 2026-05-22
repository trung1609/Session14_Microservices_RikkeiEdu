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
    @PreAuthorize("hasAuthority('PRODUCT_READ')")
    public List<String> getProducts() {
        return products;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('PRODUCT_CREATE')")
    public String createProduct() {
        return "Đã tạo sản phẩm (200 OK)";
    }
}
