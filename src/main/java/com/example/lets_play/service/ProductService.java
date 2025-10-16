package com.example.lets_play.service;

import com.example.lets_play.dto.ProductRequest;
import com.example.lets_play.exception.ForbiddenException;
import com.example.lets_play.exception.ResourceNotFoundException;
import com.example.lets_play.model.Product;
import com.example.lets_play.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service class for managing product-related business logic and
 * operations.
 *
 * <p>
 * This service provides product management features including
 * creation, retrieval, updating, and deletion. It uses owner-based
 * access control: users manage their own products while
 * administrators retain full access.
 * </p>
 *
 * <p>Key features:</p>
 * <ul>
 *   <li>
 *     Owner-based product management with administrative
 *     override
 *   </li>
 *   <li>
 *     Comprehensive CRUD operations for products
 *   </li>
 *   <li>
 *     User-specific product retrieval capabilities
 *   </li>
 *   <li>
 *     Selective updates with null-safety handling
 *   </li>
 * </ul>
 *
 * <p>
 * <strong>API Note:</strong>
 * Product read operations are public. Write operations require
 * ownership verification.
 * </p>
 * <p>
 * <strong>Implementation Note:</strong> Uses manual authorization checks
 * based on product ownership and user roles.
 * </p>
 * <p>
 * <strong>Security:</strong> Owner-based access control with administrative
 * privileges for write and delete operations.
 * </p>
 *
 * @author Zone01 Developer
 * @version 1.0
 * @since 2024
 */
@Service
public class ProductService {

    /**
     * Repository for product data access operations.
     *
     * @see com.example.lets_play.repository.ProductRepository
     */
    @Autowired
    private ProductRepository productRepository;

    /**
    * Creates a new product associated with the specified user.
    *
    * <p>
    * Creates a product from the supplied request and associates it
    * with the authenticated user (who becomes the owner).
    * </p>
    *
    * @param request the product creation data
    * @param userId the unique identifier of the creating user
    * @return the created Product with assigned ID and owner information
    * @throws jakarta.validation.ConstraintViolationException
    *         if request data is invalid
     */
    public Product createProduct(
        final ProductRequest request,
        final String userId
    ) {
        Product product = new Product();
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setUserId(userId);

        return productRepository.save(product);
    }

    /**
    * Retrieves all products in the system.
    *
    * <p>
    * Returns a complete list of all products in the system, including
    * products from all users. No filtering or access control is
    * applied at this level.
    * </p>
    *
    * @return the list of all products in the system
     */
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    /**
    * Retrieves a specific product by its unique identifier.
    *
    * <p>
    * Finds and returns a product with the specified ID. If no product
    * exists with the given ID, a ResourceNotFoundException is thrown.
    * </p>
    *
    * @param id the unique identifier of the product to retrieve
    * @return the product with the specified ID
    * @throws ResourceNotFoundException if no product exists with the ID
     */
    public Product getProductById(final String id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product",
                        "id",
                        id
                ));
    }

    /**
    * Retrieves all products owned by a specific user.
    *
    * @param userId the unique identifier of the user whose products
    *               are returned
    * @return the list of products owned by the specified user; empty if none
     */
    public List<Product> getProductsByUserId(final String userId) {
        return productRepository.findByUserId(userId);
    }

    /**
    * Updates an existing product with proper ownership verification.
    *
    * <p>
    * Updates product information after verifying that the current user
    * has permission to modify the product. Users may update their
    * own products; administrators may update any product. Only non-null
    * fields from the request are applied.
    * </p>
    *
    * @param id the unique identifier of the product to update
    * @param request the update request containing new product information
    * @param currentUserId the ID of the currently authenticated user
    * @param currentUserRole the role of the currently authenticated user
    * @return the updated product with new information
    * @throws ResourceNotFoundException if no product exists with the ID
     */
    public Product updateProduct(final String id,
                                 final ProductRequest request,
                                 final String currentUserId,
                                 final String currentUserRole) {
    Product product = productRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException(
            "Product",
            "id",
            id
        ));

    // Check authorization: user can update their own products or an
    // admin can update any product
    final boolean isOwner =
            product.getUserId().equals(currentUserId);
    final boolean isAdmin =
            "ADMIN".equals(currentUserRole);

    if (!isOwner && !isAdmin) {
        final String msg = "You are not allowed to update this product";
        throw new ForbiddenException(msg);
    }

        if (request.getName() != null) {
            product.setName(request.getName());
        }
        if (request.getDescription() != null) {
            product.setDescription(request.getDescription());
        }
        if (request.getPrice() != null) {
            product.setPrice(request.getPrice());
        }

        return productRepository.save(product);
    }

    /**
     * Deletes a product with proper ownership verification.
     *
     * @param id the unique identifier of the product to delete
     * @param currentUserId the ID of the currently authenticated user
     * @param currentUserRole the role of the currently authenticated user
     * @throws ResourceNotFoundException if no product exists with the ID
     */
    public void deleteProduct(
            final String id,
            final String currentUserId,
            final String currentUserRole
    ) {
    final Product product = productRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException(
            "Product",
            "id",
            id
        ));
    // Check authorization: user can delete their own products or an
    // admin can delete any product
    final boolean isOwner =
            product.getUserId().equals(currentUserId);
    final boolean isAdmin =
            "ADMIN".equals(currentUserRole);

    if (!isOwner && !isAdmin) {
        final String msg = "You are not allowed to delete this product";
        throw new ForbiddenException(msg);
    }

        productRepository.delete(product);
    }
}
