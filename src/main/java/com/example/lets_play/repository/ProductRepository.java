package com.example.lets_play.repository;

import com.example.lets_play.model.Product;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends MongoRepository<Product, String> {
    /**
     * Finds products created by the given user ID (owner of the product).
     *
     * @param userId the owner's user ID
     * @return list of products owned by the specified user
     * @since 1.0
     */
    List<Product> findByUserId(String userId);
}