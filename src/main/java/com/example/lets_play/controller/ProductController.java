package com.example.lets_play.controller;

import com.example.lets_play.dto.ProductRequest;
import com.example.lets_play.model.Product;
import com.example.lets_play.security.UserPrincipal;
import com.example.lets_play.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    @PostMapping
    public ResponseEntity<Product> createProduct(
            @Valid @RequestBody ProductRequest request,
            Authentication authentication) {
        
        if (authentication == null || authentication.getPrincipal() == null) {
            return ResponseEntity.status(401).build();
        }
        
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Product product = productService.createProduct(request, userPrincipal.getId());
        return ResponseEntity.ok(product);
    }

    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts() {
        List<Product> products = productService.getAllProducts();
        return ResponseEntity.ok(products);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable String id) {
        Product product = productService.getProductById(id);
        return ResponseEntity.ok(product);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Product>> getProductsByUserId(@PathVariable String userId) {
        List<Product> products = productService.getProductsByUserId(userId);
        return ResponseEntity.ok(products);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Product> updateProduct(
            @PathVariable String id,
            @Valid @RequestBody ProductRequest request,
            Authentication authentication) {
        
        if (authentication == null || authentication.getPrincipal() == null) {
            return ResponseEntity.status(401).build();
        }
        
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        String currentUserId = userPrincipal.getId();
        String currentUserRole = userPrincipal.getAuthorities().iterator().next().getAuthority().replace("ROLE_", "");
        
        Product product = productService.updateProduct(id, request, currentUserId, currentUserRole);
        return ResponseEntity.ok(product);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProduct(
            @PathVariable String id,
            Authentication authentication) {
        
        if (authentication == null || authentication.getPrincipal() == null) {
            return ResponseEntity.status(401).build();
        }
        
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        String currentUserId = userPrincipal.getId();
        String currentUserRole = userPrincipal.getAuthorities().iterator().next().getAuthority().replace("ROLE_", "");
        
        productService.deleteProduct(id, currentUserId, currentUserRole);
        return ResponseEntity.ok().build();
    }
}