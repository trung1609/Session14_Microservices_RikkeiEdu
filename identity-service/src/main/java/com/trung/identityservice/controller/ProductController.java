package com.trung.identityservice.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    @GetMapping
    public List<String> getMockProducts() {
        return Arrays.asList("MacBook Pro M3", "iPhone 15 Pro Max", "iPad Air 6");
    }
}
