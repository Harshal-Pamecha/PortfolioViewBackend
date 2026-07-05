package com.portfolio.service;

import com.portfolio.constants.ServiceConstants;
import com.portfolio.entity.User;
import com.portfolio.repository.UserRepository;
import com.portfolio.security.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Service class for managing user operations.
 * Provides CRUD operations and user lookup functionality.
 */
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository repository;
    private final com.portfolio.repository.FamilyMemberRepository familyMemberRepository;



    /**
     * Retrieves a user by ID.
     *
     * @param id the user ID
     * @return the user
     * @throws java.util.NoSuchElementException if user not found
     */
    public User getById(final Integer id) {
        return repository.findById(id).orElseThrow();
    }

    /**
     * Finds a user by email address.
     *
     * @param email the user's email
     * @return the user
     * @throws RuntimeException if user not found
     */
    public User findByEmail(final String email) {
        return repository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException(ServiceConstants.ERROR_USER_NOT_FOUND));
    }

    /**
     * Creates a new user.
     *
     * @param user the user to create
     * @return the created user
     */
    public User create(final User user) {
        User savedUser = repository.save(user);

        // Create default 'SELF' family member
        com.portfolio.entity.FamilyMember self = new com.portfolio.entity.FamilyMember();
        self.setUser(savedUser);
        self.setName("SELF");
        self.setBalance(java.math.BigDecimal.ZERO);
        self.setCurrency(com.portfolio.entity.Currency.USD);
        familyMemberRepository.save(self);

        return savedUser;
    }

    /**
     * Updates an existing user. Only the currently authenticated user may update their own profile.
     *
     * @param id the user ID
     * @param user the user data to update
     * @return the updated user
     * @throws SecurityException if the caller is not the owner of the account
     */
    public User update(final Integer id, final User user) {
        Integer currentUserId = UserContext.getCurrentUserId();
        if (currentUserId == null || !currentUserId.equals(id)) {
            throw new SecurityException("Unauthorized: cannot modify another user's profile");
        }
        User existing = repository.findById(id).orElseThrow();
        if (user.getBaseCurrency() != null) {
            existing.setBaseCurrency(user.getBaseCurrency());
        }
        if (user.getSubscriptionPlan() != null) {
            existing.setSubscriptionPlan(user.getSubscriptionPlan());
        }
        if (user.getLongTermThresholds() != null) {
            existing.setLongTermThresholds(user.getLongTermThresholds());
        }
        return repository.save(existing);
    }

    /**
     * Deletes a user by ID. Only the currently authenticated user may delete their own account.
     *
     * @param id the user ID
     * @throws SecurityException if the caller is not the owner of the account
     */
    public void delete(final Integer id) {
        Integer currentUserId = UserContext.getCurrentUserId();
        if (currentUserId == null || !currentUserId.equals(id)) {
            throw new SecurityException("Unauthorized: cannot delete another user's account");
        }
        repository.deleteById(id);
    }
}
