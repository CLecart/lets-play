package com.example.lets_play.controller;

import com.example.lets_play.dto.UserCreateRequest;
import com.example.lets_play.dto.UserResponse;
import com.example.lets_play.dto.UserUpdateRequest;
import com.example.lets_play.security.UserPrincipal;
import com.example.lets_play.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for managing user-related operations in the system.
 * 
 * <p>This controller provides comprehensive CRUD operations for user management, including
 * creation, retrieval, updating, and deletion of user accounts. All operations are secured
 * with role-based access control and proper authorization checks.</p>
 * 
 * <p>Access control rules:
 * <ul>
 *   <li>User creation and listing: ADMIN role required</li>
 *   <li>User retrieval, update, deletion: ADMIN role OR own user account</li>
 * </ul></p>
 * 
 * @apiNote All endpoints require valid JWT authentication except where explicitly noted
 * @implNote Uses method-level security with @PreAuthorize annotations
 * @security Role-based access control enforced at method level
 * 
 * @author Zone01 Developer
 * @version 1.0
 * @since 2024
 */
@CrossOrigin(origins = "*", maxAge = 3600)
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
     * <p>This endpoint allows administrators to create new user accounts with specified roles
     * and permissions. The request is validated and the password is automatically encrypted
     * before storage.</p>
     * 
     * @param request the user creation data including name, email, password, and optional role
     * @return ResponseEntity containing the created user information (without password)
     * 
     * @throws org.springframework.security.access.AccessDeniedException
     *         if the current user lacks ADMIN role
     * @throws com.example.lets_play.exception.BadRequestException
     *         if the email is already registered
     * 
     * @apiNote Only users with ADMIN role can create new accounts
     * @security Requires ADMIN role authorization
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody UserCreateRequest request) {
        UserResponse user = userService.createUser(request);
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
     * @apiNote Only administrators can view all users
     * @security Requires ADMIN role authorization
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        List<UserResponse> users = userService.getAllUsers();
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
     * @apiNote Users can only view their own profile unless they have ADMIN role
     * @security Access controlled by role or ownership verification
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.id")
    public ResponseEntity<UserResponse> getUserById(@PathVariable String id) {
        UserResponse user = userService.getUserById(id);
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
     * @apiNote Non-admin users cannot change their role even when updating their own profile
     * @security Role changes restricted to administrators only
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.id")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable String id,
            @Valid @RequestBody UserUpdateRequest request,
            Authentication authentication) {
        
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        String currentUserId = userPrincipal.getId();
        String currentUserRole = userPrincipal.getAuthorities().iterator().next().getAuthority().replace("ROLE_", "");
        
        UserResponse user = userService.updateUser(id, request, currentUserId, currentUserRole);
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
     * @apiNote Account deletion is permanent and cannot be undone
     * @security Users can only delete their own account unless they have ADMIN role
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.id")
    public ResponseEntity<?> deleteUser(@PathVariable String id) {
        userService.deleteUser(id);
        return ResponseEntity.ok().build();
    }
}