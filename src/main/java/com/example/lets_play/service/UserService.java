package com.example.lets_play.service;

import com.example.lets_play.dto.UserCreateRequest;
import com.example.lets_play.dto.UserResponse;
import com.example.lets_play.dto.UserUpdateRequest;
import com.example.lets_play.exception.BadRequestException;
import com.example.lets_play.exception.ResourceNotFoundException;
import com.example.lets_play.model.User;
import com.example.lets_play.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service class for managing user-related business logic and operations.
 * 
 * <p>This service provides comprehensive user management functionality including user creation,
 * retrieval, updating, and deletion. It handles password encryption, email uniqueness validation,
 * and role-based authorization for user operations.</p>
 * 
 * <p>Key features:
 * <ul>
 *   <li>Secure password handling with BCrypt encryption</li>
 *   <li>Email uniqueness validation</li>
 *   <li>Role-based access control with administrative privileges</li>
 *   <li>User profile self-management capabilities</li>
 * </ul></p>
 * 
 * @apiNote This service integrates with Spring Security for authentication and authorization
 * @implNote Uses method-level security annotations for access control
 * @security Passwords are automatically encrypted before storage using BCrypt
 * 
 * @author Zone01 Developer
 * @version 1.0
 * @since 2024
 */
@Service
public class UserService {

    /**
     * Repository for user data access operations.
     * 
     * @see com.example.lets_play.repository.UserRepository
     */
    @Autowired
    private UserRepository userRepository;

    /**
     * Password encoder for secure password hashing using BCrypt algorithm.
     * 
     * @see org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
     */
    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Creates a new user account in the system.
     * 
     * <p>This method validates email uniqueness, encrypts the password using BCrypt,
     * and stores the user with the specified role. Default role is "USER" if not specified.</p>
     * 
     * @param request the user creation data including name, email, password, and optional role
     * @return UserResponse containing the created user information without password
     * 
     * @throws BadRequestException if the email is already registered in the system
     * @throws jakarta.validation.ConstraintViolationException if request data is invalid
     * 
     * @apiNote Password is automatically encrypted before storage
     * @implNote Email uniqueness is checked before user creation
     * @security Password is hashed using BCrypt with default strength (10 rounds)
     */
    public UserResponse createUser(UserCreateRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email is already taken!");
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole());

        User savedUser = userRepository.save(user);
        return convertToResponse(savedUser);
    }

    /**
     * Retrieves all users in the system (Admin only).
     * 
     * <p>This method returns a list of all registered users in the system, excluding
     * sensitive information such as passwords. Access is restricted to administrators only.</p>
     * 
     * @return List of UserResponse objects containing user information without passwords
     * 
     * @throws org.springframework.security.access.AccessDeniedException
     *         if the current user lacks ADMIN role
     * 
     * @apiNote Only administrators can access the complete user list
     * @security Requires ADMIN role authorization via method-level security
     */
    @PreAuthorize("hasRole('ADMIN')")
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves a specific user by their unique identifier.
     * 
     * <p>This method finds and returns user information for the specified ID.
     * Sensitive information such as passwords is excluded from the response.</p>
     * 
     * @param id the unique identifier of the user to retrieve
     * @return UserResponse containing user information without password
     * 
     * @throws ResourceNotFoundException if no user exists with the specified ID
     * 
     * @apiNote Access control is handled at the controller level
     * @implNote Uses Optional-based lookup with custom exception handling
     */
    public UserResponse getUserById(String id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        return convertToResponse(user);
    }

    /**
     * Updates user information with proper authorization checks.
     * 
     * <p>This method allows users to update their own profile information or administrators
     * to update any user's information. Role changes are restricted to administrators only.
     * All updates are applied selectively - only non-null fields are updated.</p>
     * 
     * @param id the unique identifier of the user to update
     * @param request the update request containing new user information
     * @param currentUserId the ID of the currently authenticated user
     * @param currentUserRole the role of the currently authenticated user
     * @return UserResponse containing the updated user information
     * 
     * @throws ResourceNotFoundException if no user exists with the specified ID
     * @throws BadRequestException if user tries to update another user's profile without admin rights
     * 
     * @apiNote Role updates are restricted to administrators only
     * @implNote Performs manual authorization checks based on user ID and role
     * @security Passwords are re-encrypted when updated; role changes require admin privileges
     */
    public UserResponse updateUser(String id, UserUpdateRequest request, String currentUserId, String currentUserRole) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        // Check authorization: user can update their own profile or admin can update any
        if (!id.equals(currentUserId) && !"ADMIN".equals(currentUserRole)) {
            throw new BadRequestException("You can only update your own profile");
        }

        if (request.getName() != null) {
            user.setName(request.getName());
        }
        if (request.getPassword() != null) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        if (request.getRole() != null && "ADMIN".equals(currentUserRole)) {
            user.setRole(request.getRole());
        }

        User updatedUser = userRepository.save(user);
        return convertToResponse(updatedUser);
    }

    /**
     * Deletes a user account from the system (Admin or own account).
     * 
     * <p>This method permanently removes a user account from the system. The operation
     * is irreversible and all associated data will be removed. Access is restricted to
     * administrators or the account owner.</p>
     * 
     * @param id the unique identifier of the user to delete
     * 
     * @throws ResourceNotFoundException if no user exists with the specified ID
     * @throws org.springframework.security.access.AccessDeniedException
     *         if the user is not an admin and trying to delete another user's account
     * 
     * @apiNote Account deletion is permanent and cannot be undone
     * @security Users can only delete their own account unless they have ADMIN role
     */
    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.id")
    public void deleteUser(String id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        userRepository.delete(user);
    }

    /**
     * Converts a User entity to a UserResponse DTO for safe external exposure.
     * 
     * <p>This utility method transforms internal User entities into response DTOs,
     * excluding sensitive information such as passwords. This ensures that sensitive
     * data is never exposed in API responses.</p>
     * 
     * @param user the User entity to convert
     * @return UserResponse DTO containing safe user information
     * 
     * @implNote This method excludes password and other sensitive fields
     * @security Ensures sensitive data is never exposed in API responses
     */
    private UserResponse convertToResponse(User user) {
        return new UserResponse(user.getId(), user.getName(), user.getEmail(), user.getRole());
    }
}