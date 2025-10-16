package com.example.lets_play.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import com.example.lets_play.config.AppConstants;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * Persistent entity representing a product managed by the application.
 *
 * <p>This MongoDB document stores product metadata such as name, description,
 * price and the owner's user ID. It is used by service and repository layers
 * for CRUD operations and should not expose sensitive information.</p>
 *
 * @since 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "products")
public class Product {

    @Id
    private String id;

    @NotBlank(message = "Product name is required")
    @Size(min = AppConstants.PRODUCT_NAME_MIN, max = AppConstants.PRODUCT_NAME_MAX,
          message = "Product name must be between 2 and 100 characters")
    private String name;

    @Size(
        max = AppConstants.PRODUCT_DESCRIPTION_MAX,
        message = "Description cannot exceed 500 characters"
    )
    private String description;

    @NotNull(message = "Price is required")
    @Positive(message = "Price must be positive")
    private Double price;

    @NotBlank(message = "User ID is required")
    private String userId;

    public Product(final String nameParam,
                   final String descriptionParam,
                   final Double priceParam,
                   final String userIdParam) {
        this.name = nameParam;
        this.description = descriptionParam;
        this.price = priceParam;
        this.userId = userIdParam;
    }
}
