package com.example.lets_play.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import com.example.lets_play.config.AppConstants;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * DTO used to create or update a product.
 *
 * <p>
 * Validation constraints are applied to ensure product data meets business
 * rules. Use this class as the request body for endpoints that accept
 * product information.
 * </p>
 *
 * @since 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductRequest {
    /** Product name visible to clients. */
    @NotBlank(message = "Product name is required")
    @Size(
            min = AppConstants.PRODUCT_NAME_MIN,
            max = AppConstants.PRODUCT_NAME_MAX,
            message = "Product name must be between 2 and 100 characters"
    )
    private String name;

    /** Optional long-form description of the product. */
    @Size(
        max = AppConstants.PRODUCT_DESCRIPTION_MAX,
        message = "Description cannot exceed 500 characters"
    )
    private String description;

    /** Price of the product in the configured currency (must be positive). */
    @NotNull(message = "Price is required")
    @Positive(message = "Price must be positive")
    private Double price;
}
