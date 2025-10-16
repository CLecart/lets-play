package com.example.lets_play.controller;

import com.example.lets_play.dto.ProductRequest;
import com.example.lets_play.model.Product;
import com.example.lets_play.security.AppUserPrincipal;
import com.example.lets_play.security.UserPrincipal;
import com.example.lets_play.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller for managing product operations.
 *
 * <p>
 * This controller provides CRUD operations for products and implements owner-
 * based and role-based access control to enable secure product management.
 * </p>
 *
 * <p>
 * Access control rules:
 * </p>
 * <ul>
 *   <li>Product creation: requires authentication</li>
 *   <li>Product listing and retrieval: public access</li>
 *   <li>Product update/deletion: owner or ADMIN role required</li>
 * </ul>
 *
 * <p>
 * API note: read operations are publicly accessible; write operations require
 * authentication.
 * </p>
 *
 * <p>
 * Implementation note: uses manual authentication checks with null-safe
 * handling.
 * </p>
 *
 * <p>
 * Security: owner-based access control with administrative override.
 * </p>
 *
 * @author Zone01 Developer
 * @version 1.0
 * @since 2024
 */
@RestController
@RequestMapping("/api/products")
public class ProductController {

    /** HTTP status code returned when the request is not authenticated. */
    private static final int UNAUTHORIZED = 401;

    /**
     * Resolve the authenticated principal into an AppUserPrincipal abstraction.
     *
     * <p>Returns null when authentication or principal is missing or not
     * recognized.</p>
     *
     * @param authentication the current authentication context; may be null
     * @return the resolved AppUserPrincipal, or null when the principal is
     *         missing or not recognized
     */
    private AppUserPrincipal resolvePrincipal(
        final Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            return null;
        }

        final Object principal = authentication.getPrincipal();
        if (principal instanceof AppUserPrincipal) {
            return (AppUserPrincipal) principal;
        }
        if (principal instanceof UserPrincipal) {
            return (UserPrincipal) principal;
        }

        return null;
    }

    /**
     * Service layer for product-related business logic and database operations.
     *
     * @see com.example.lets_play.service.ProductService
     */
    @Autowired
    private ProductService productService;

    /**
     * Creates a new product (authenticated users only).
     *
     * <p>
     * Allows an authenticated user to create a new product. The created product
     * is associated with the authenticated user as the owner. Product data is
     * validated before persistence.
     * </p>
     *
    * @param request the product creation data (name, description, price)
    * @param authentication the current user's authentication context; must be
    *        non-null for creation
    * @return a response entity containing the created product with assigned id
    *         and owner
     * @throws jakarta.validation.ConstraintViolationException if the product
     *         data fails validation constraints
     *
     * <p>
     * API note: the authenticated user becomes the owner of the created
     * product.
     * </p>
     */
    @PostMapping
    public ResponseEntity<Product> createProduct(
        @Valid @RequestBody final ProductRequest request,
        final Authentication authentication) {

        final AppUserPrincipal appPrincipal = resolvePrincipal(authentication);
        if (appPrincipal == null) {
            return ResponseEntity.status(UNAUTHORIZED).build();
        }

        final String ownerId = appPrincipal.getId();
        final Product product = productService.createProduct(
            request,
            ownerId);

        return ResponseEntity.ok(product);
    }

    /**
     * Retrieves all products in the system (public access).
     *
     * <p>
     * Returns a list of all products in the system, including owner
     * information. No authentication is required which makes this suitable
     * for public product catalogs.
     * </p>
     *
     * @return ResponseEntity containing a list of all products
     */
    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts() {
        final List<Product> products = productService.getAllProducts();
        return ResponseEntity.ok(products);
    }

    /**
     * Retrieves a specific product by ID (public access).
     *
     * <p>
     * Returns detailed information about a product identified by its unique id.
     * No authentication is required for viewing product details.
     * </p>
     *
     * @param id the unique identifier of the product to retrieve
     * @param authentication the current user's authentication context; may be
     *        null when the caller is anonymous
     * @return a response entity containing the product information
     * @throws com.example.lets_play.exception.ResourceNotFoundException if the
     *         product with the specified id is not found
     */
    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(
            @PathVariable final String id,
            final Authentication authentication) {
        // Authentication is optional for public read. When present, caller
        // information is preserved for auditing/authorization checks.
        final Product product = productService.getProductById(id);
        return ResponseEntity.ok(product);
    }

    /**
     * Retrieves all products owned by a specific user (public access).
     *
     * <p>
     * Returns a list of products created by the given user. Useful for user
     * profile pages and author product listings.
     * </p>
     *
     * @param userId the user's unique identifier
     * @return ResponseEntity containing a list of products owned by the user
     * @throws com.example.lets_play.exception.ResourceNotFoundException if the
     *         user with the specified id is not found
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Product>> getProductsByUserId(
            @PathVariable final String userId) {
        final List<Product> products = productService.getProductsByUserId(
            userId);
        return ResponseEntity.ok(products);
    }

    /**
     * Updates an existing product (owner or admin only).
     *
     * <p>
     * Allows the product owner or administrators to modify product fields. The
     * system validates permissions prior to applying changes.
     * </p>
     *
    * @param id the product's unique identifier
    * @param request the updated product data
    * @param authentication the current user's authentication context; must be
    *        non-null for write operations
    * @return a response entity containing the updated product
     * @throws com.example.lets_play.exception.ResourceNotFoundException if the
     *         product with the specified id is not found
     * @throws org.springframework.security.access.AccessDeniedException if the
     *         user is not the owner and lacks ADMIN role
     */
    @PutMapping("/{id}")
    public ResponseEntity<Product> updateProduct(
            @PathVariable final String id,
            @Valid @RequestBody final ProductRequest request,
            final Authentication authentication) {

        final AppUserPrincipal appPrincipal = resolvePrincipal(authentication);
        if (appPrincipal == null) {
            return ResponseEntity.status(UNAUTHORIZED).build();
        }

        final String currentUserId = appPrincipal.getId();
        final String currentUserRole = resolveRole(authentication);

        final Product product = productService.updateProduct(
                id,
                request,
                currentUserId,
                currentUserRole);

        return ResponseEntity.ok(product);
    }

    /**
     * Deletes a product (owner or admin only).
     *
     * <p>
     * Permanently deletes a product. This operation is irreversible and will
     * remove the product from the system.
     * </p>
     *
    * @param id the product's unique identifier
    * @param authentication the current user's authentication context; must be
    *        non-null for delete operations
    * @return a response entity with no content on successful deletion
     * @throws com.example.lets_play.exception.ResourceNotFoundException if the
     *         product with the specified id is not found
     * @throws org.springframework.security.access.AccessDeniedException if the
     *         user is not the owner and lacks ADMIN role
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProduct(
            @PathVariable final String id,
            final Authentication authentication) {

        final AppUserPrincipal appPrincipal = resolvePrincipal(authentication);
        if (appPrincipal == null) {
            return ResponseEntity.status(UNAUTHORIZED).build();
        }

        final String currentUserId = appPrincipal.getId();
        final String currentUserRole = resolveRole(authentication);

        productService.deleteProduct(
                id,
                currentUserId,
                currentUserRole);

        return ResponseEntity.ok().build();
    }

    /**
     * Resolve the primary role name from the authentication authorities.
     *
     * <p>
     * Returns empty string when no authority is present. Strips the
     * "ROLE_" prefix when present to normalize role names used by
     * services.
     * </p>
    *
    * @param authentication the authentication context; may be null
    * @return primary role name without the "ROLE_" prefix, or an empty
    *         string when no authority is present
     */
    private String resolveRole(final Authentication authentication) {
        if (authentication == null) {
            return "";
        }
        final var authorityOpt = authentication.getAuthorities().stream()
            .findFirst();

        if (authorityOpt.isEmpty()) {
            return "";
        }

        final var first = authorityOpt.get();
        final String authority = first.getAuthority();
        final String normalized = authority.replace("ROLE_", "");
        return normalized;
    }
}
