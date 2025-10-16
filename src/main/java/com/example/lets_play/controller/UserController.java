package com.example.lets_play.controller;

import com.example.lets_play.dto.UserCreateRequest;
import com.example.lets_play.dto.UserResponse;
import com.example.lets_play.dto.UserUpdateRequest;
import com.example.lets_play.security.AppUserPrincipal;
import com.example.lets_play.security.UserPrincipal;
import com.example.lets_play.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
// AppConstants intentionally unused at controller level; CORS configured centrally

import java.util.List;

/**
 * REST controller for user-related operations.
 *
 * <p>Provides CRUD endpoints for user accounts. Operations are protected by
 * role-based authorization and ownership checks.</p>
 *
 * <p><strong>Access control:</strong> ADMIN role is required for creation and
 * listing. Retrieval, update and deletion require ADMIN role or ownership.</p>
 *
 * <p><strong>Implementation Note:</strong> Uses @PreAuthorize for method-level
 * security.</p>
 *
 * @author Zone01 Developer
 * @version 1.0
 * @since 2024
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    /**
     * Service layer for user-related business logic and database operations.
     *
     * @see com.example.lets_play.service.UserService
     */
    @Autowired
    private UserService userService;

    /**
     * Creates a new user account (Admin only).
     *
     * <p>This endpoint allows administrators to create new user accounts with
     * specified roles and permissions. The request is validated and the
     * password is automatically encrypted before storage.</p>
     *
     * @param request the user creation data including name, email, password, and optional role
     * @return ResponseEntity containing the created user information (without password)
     *
     * @throws org.springframework.security.access.AccessDeniedException
     *         if the current user lacks ADMIN role
     * @throws com.example.lets_play.exception.BadRequestException
     *         if the email is already registered
     *
     * <p><strong>API Note:</strong> Only users with ADMIN role can create new accounts</p>
     * <p><strong>Security:</strong> Requires ADMIN role authorization</p>
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> createUser(
            @Valid @RequestBody final UserCreateRequest request) {
        final UserResponse user = userService.createUser(request);
        return ResponseEntity.ok(user);
    }

    /**
     * Retrieves all users in the system (Admin only).
     *
     * <p>This endpoint returns a list of all registered users in the system.
     * Sensitive information such as passwords is excluded from the response.</p>
     *
     * @return ResponseEntity containing list of all users without sensitive data
     *
     * @throws org.springframework.security.access.AccessDeniedException
     *         if the current user lacks ADMIN role
     *
     * <p><strong>API Note:</strong> Only administrators can view all users</p>
     * <p><strong>Security:</strong> Requires ADMIN role authorization</p>
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        final List<UserResponse> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    /**
     * Retrieves a specific user by ID (Admin or own account).
     *
     * <p>This endpoint allows administrators to view any user's details, or regular users
     * to view their own account information. Sensitive data is excluded from the response.</p>
     *
     * @param id the unique identifier of the user to retrieve
     * @return ResponseEntity containing the user information without sensitive data
     *
     * @throws org.springframework.security.access.AccessDeniedException
     *         if the user is not an admin and trying to access another user's data
     * @throws com.example.lets_play.exception.ResourceNotFoundException
     *         if the user with the specified ID is not found
     *
     * <p><strong>API Note:</strong> Users can only view their own profile unless they have ADMIN role</p>
     * <p><strong>Security:</strong> Access controlled by role or ownership verification</p>
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.id")
    public ResponseEntity<UserResponse> getUserById(@PathVariable final String id) {
        final UserResponse user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    /**
     * Updates a user's information (Admin or own account).
     *
     * <p>This endpoint allows administrators to update any user's information, or regular
     * users to update their own account details. Role changes are restricted to administrators.</p>
     *
     * @param id the unique identifier of the user to update
     * @param request the updated user information
     * @param authentication the current user's authentication context
     * @return ResponseEntity containing the updated user information
     *
     * @throws org.springframework.security.access.AccessDeniedException
     *         if the user is not an admin and trying to update another user's data
     * @throws com.example.lets_play.exception.ResourceNotFoundException
     *         if the user with the specified ID is not found
     * @throws com.example.lets_play.exception.BadRequestException
     *         if trying to change email to an already existing one
     *
     * <p><strong>API Note:</strong> Non-admin users cannot change their role even when updating their own profile</p>
     * <p><strong>Security:</strong> Role changes restricted to administrators only</p>
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.id")
    public ResponseEntity<UserResponse> updateUser(
        @PathVariable final String id,
        @Valid @RequestBody final UserUpdateRequest request,
        final Authentication authentication) {

        final Object principal = authentication.getPrincipal();
        final AppUserPrincipal appPrincipal;
        if (principal instanceof AppUserPrincipal) {
            appPrincipal = (AppUserPrincipal) principal;
        } else if (principal instanceof UserPrincipal) {
            // backwards compatibility with concrete implementation
            appPrincipal = (UserPrincipal) principal;
        } else {
            // fallback: treat as anonymous (no id, no role)
            throw new IllegalStateException("Unexpected principal type: " + principal.getClass());
        }

        final String currentUserId = appPrincipal.getId();
        final String currentUserRole;
        {
        currentUserRole = resolveRole(authentication);
        }

        final UserResponse user = userService.updateUser(
                id,
                request,
                currentUserId,
                currentUserRole
        );
        return ResponseEntity.ok(user);
    }

    /**
     * Deletes a user account (Admin or own account).
     *
     * <p>This endpoint allows administrators to delete any user account, or regular users
     * to delete their own account. The operation is irreversible and all associated data
     * will be permanently removed.</p>
     *
     * @param id the unique identifier of the user to delete
     * @return ResponseEntity with no content indicating successful deletion
     *
     * @throws org.springframework.security.access.AccessDeniedException
     *         if the user is not an admin and trying to delete another user's account
     * @throws com.example.lets_play.exception.ResourceNotFoundException
     *         if the user with the specified ID is not found
     *
     * <p><strong>API Note:</strong> Account deletion is permanent and cannot be undone</p>
     * <p><strong>Security:</strong> Users can only delete their own account unless they have ADMIN role</p>
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.id")
    public ResponseEntity<?> deleteUser(@PathVariable final String id) {
        userService.deleteUser(id);
        return ResponseEntity.ok().build();
    }

    /**
     * Resolve the primary role name from the authentication authorities.
     */
    private String resolveRole(final Authentication authentication) {
        if (authentication == null) {
            return "";
        }

        return authentication.getAuthorities()
                .stream()
                .findFirst()
                .map(a -> a.getAuthority().replace("ROLE_", ""))
                .orElse("");
    }
}
