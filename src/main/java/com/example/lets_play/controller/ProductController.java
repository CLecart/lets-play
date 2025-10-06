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

/**
 * REST controller for managing product operations in the system.
 * 
 * <p>This controller provides comprehensive CRUD operations for product management, including
 * creation, retrieval, updating, and deletion of products. It implements user-based ownership
 * and role-based access control for secure product management.</p>
 * 
 * <p>Access control rules:
 * <ul>
 *   <li>Product creation: Requires authentication</li>
 *   <li>Product listing and retrieval: Public access</li>
 *   <li>Product update/deletion: Owner or ADMIN role required</li>
 * </ul>
 * 
 * <p><strong>API Note:</strong> Product read operations are publicly accessible, while write operations require authentication</p>
 * <p><strong>Implementation Note:</strong> Uses manual authentication checks with null-safe handling</p>
 * <p><strong>Security:</strong> Owner-based access control with administrative override</p>
 * 
 * @author Zone01 Developer
 * @version 1.0
 * @since 2024
 */
@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/products")
public class ProductController {

    /**
     * Service layer for product-related business logic and database operations.
     * 
     * @see com.example.lets_play.service.ProductService
     */
    @Autowired
    private ProductService productService;

    /**
     * Creates a new product (Authenticated users only).
     * 
     * <p>This endpoint allows authenticated users to create new products. The product
     * is automatically associated with the authenticated user as the owner. All product
     * data is validated before creation.</p>
     * 
     * @param request the product creation data including name, description, and price
     * @param authentication the current user's authentication context
     * @return ResponseEntity containing the created product with assigned ID and owner
     * 
     * @throws jakarta.validation.ConstraintViolationException
     *         if the product data fails validation constraints
     *         with status 401 if authentication is null or invalid
     * 
     * <p><strong>API Note:</strong> The authenticated user becomes the owner of the created product</p>
     * <p><strong>Implementation Note:</strong> Performs null-safe authentication checks before processing</p>
     * <p><strong>Security:</strong> Requires valid JWT authentication</p>
     */
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

    /**
     * Retrieves all products in the system (Public access).
     * 
     * <p>This endpoint returns a list of all products in the system including owner information.
     * No authentication is required, making it suitable for public product catalogs.</p>
     * 
     * @return ResponseEntity containing list of all products with complete information
     * 
     * <p><strong>API Note:</strong> This endpoint is publicly accessible without authentication</p>
     * <p><strong>Implementation Note:</strong> Returns products from all users in the system</p>
     */
    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts() {
        List<Product> products = productService.getAllProducts();
        return ResponseEntity.ok(products);
    }

    /**
     * Retrieves a specific product by ID (Public access).
     * 
     * <p>This endpoint returns detailed information about a specific product identified
     * by its unique ID. No authentication is required for viewing product details.</p>
     * 
     * @param id the unique identifier of the product to retrieve
     * @return ResponseEntity containing the product information
     * 
     * @throws com.example.lets_play.exception.ResourceNotFoundException
     *         if the product with the specified ID is not found
     * 
     * <p><strong>API Note:</strong> This endpoint is publicly accessible without authentication</p>
     */
    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable String id) {
        Product product = productService.getProductById(id);
        return ResponseEntity.ok(product);
    }

    /**
     * Retrieves all products owned by a specific user (Public access).
     * 
     * <p>This endpoint returns a list of all products created by a specific user.
     * Useful for displaying user profiles or product listings by author.</p>
     * 
     * @param userId the unique identifier of the user whose products to retrieve
     * @return ResponseEntity containing list of products owned by the specified user
     * 
     * @throws com.example.lets_play.exception.ResourceNotFoundException
     *         if the user with the specified ID is not found
     * 
     * <p><strong>API Note:</strong> This endpoint is publicly accessible without authentication</p>
     * <p><strong>Implementation Note:</strong> Returns empty list if user exists but has no products</p>
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Product>> getProductsByUserId(@PathVariable String userId) {
        List<Product> products = productService.getProductsByUserId(userId);
        return ResponseEntity.ok(products);
    }

    /**
     * Updates an existing product (Owner or Admin only).
     * 
     * <p>This endpoint allows the product owner or administrators to update product information.
     * The system validates that the user has permission to modify the product before applying changes.</p>
     * 
     * @param id the unique identifier of the product to update
     * @param request the updated product information
     * @param authentication the current user's authentication context
     * @return ResponseEntity containing the updated product information
     * 
     *         with status 401 if authentication is null or invalid
     * @throws com.example.lets_play.exception.ResourceNotFoundException
     *         if the product with the specified ID is not found
     * @throws org.springframework.security.access.AccessDeniedException
     *         if the user is not the owner and lacks ADMIN role
     * 
     * <p><strong>API Note:</strong> Only the product owner or administrators can update products</p>
     * <p><strong>Implementation Note:</strong> Performs null-safe authentication checks and role validation</p>
     * <p><strong>Security:</strong> Owner-based access control with administrative override</p>
     */
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

    /**
     * Deletes a product (Owner or Admin only).
     * 
     * <p>This endpoint allows the product owner or administrators to permanently delete
     * a product from the system. The operation is irreversible.</p>
     * 
     * @param id the unique identifier of the product to delete
     * @param authentication the current user's authentication context
     * @return ResponseEntity with no content indicating successful deletion
     * 
     *         with status 401 if authentication is null or invalid
     * @throws com.example.lets_play.exception.ResourceNotFoundException
     *         if the product with the specified ID is not found
     * @throws org.springframework.security.access.AccessDeniedException
     *         if the user is not the owner and lacks ADMIN role
     * 
     * <p><strong>API Note:</strong> Product deletion is permanent and cannot be undone</p>
     * <p><strong>Implementation Note:</strong> Performs null-safe authentication checks and role validation</p>
     * <p><strong>Security:</strong> Owner-based access control with administrative override</p>
     */
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