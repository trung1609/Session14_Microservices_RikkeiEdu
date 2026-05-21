package com.trung.productservice.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {


    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public List<String> getMockProducts() {
        return Arrays.asList("MacBook Pro M3", "iPhone 15 Pro Max", "iPad Air 6");
    }
}
