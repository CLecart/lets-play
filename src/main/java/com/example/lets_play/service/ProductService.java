package com.example.lets_play.service;

import com.example.lets_play.dto.ProductRequest;
import com.example.lets_play.exception.BadRequestException;
import com.example.lets_play.exception.ResourceNotFoundException;
import com.example.lets_play.model.Product;
import com.example.lets_play.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    public Product createProduct(ProductRequest request, String userId) {
        Product product = new Product();
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setUserId(userId);

        return productRepository.save(product);
    }

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Product getProductById(String id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
    }

    public List<Product> getProductsByUserId(String userId) {
        return productRepository.findByUserId(userId);
    }

    public Product updateProduct(String id, ProductRequest request, String currentUserId, String currentUserRole) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));

        // Check authorization: user can update their own products or admin can update any
        if (!product.getUserId().equals(currentUserId) && !"ADMIN".equals(currentUserRole)) {
            throw new BadRequestException("You can only update your own products");
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

    public void deleteProduct(String id, String currentUserId, String currentUserRole) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));

        // Check authorization: user can delete their own products or admin can delete any
        if (!product.getUserId().equals(currentUserId) && !"ADMIN".equals(currentUserRole)) {
            throw new BadRequestException("You can only delete your own products");
        }

        productRepository.delete(product);
    }
}