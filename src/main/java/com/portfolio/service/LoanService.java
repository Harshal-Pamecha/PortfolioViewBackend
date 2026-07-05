package com.portfolio.service;

import com.portfolio.entity.Loan;
import com.portfolio.entity.User;
import com.portfolio.repository.LoanRepository;
import com.portfolio.repository.UserRepository;
import com.portfolio.security.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service layer for Loan entity business logic.
 */
@Service
@RequiredArgsConstructor
public class LoanService {

    private final LoanRepository repository;
    private final UserRepository userRepository;

    /**
     * Retrieves all loans for the currently authenticated user.
     *
     * @return list of loans
     * @throws SecurityException if the user is not authenticated
     */
    public List<Loan> getAll() {
        Integer userId = UserContext.getCurrentUserId();
        if (userId == null) {
            throw new SecurityException("User is not authenticated");
        }
        return repository.findByUserId(userId);
    }

    /**
     * Retrieves a loan by its ID for the currently authenticated user.
     *
     * @param id the ID of the loan
     * @return the loan
     * @throws SecurityException if the user is not authenticated
     * @throws java.util.NoSuchElementException if the loan is not found or not owned by the user
     */
    public Loan getById(final Integer id) {
        Integer userId = UserContext.getCurrentUserId();
        if (userId == null) {
            throw new SecurityException("User is not authenticated");
        }
        return repository.findByIdAndUserId(id, userId).orElseThrow();
    }

    /**
     * Creates a new loan for the currently authenticated user.
     *
     * @param loan the loan to create
     * @return the created loan
     * @throws SecurityException if the user is not authenticated
     */
    public Loan create(final Loan loan) {
        Integer userId = UserContext.getCurrentUserId();
        if (userId == null) {
            throw new SecurityException("User is not authenticated");
        }
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new SecurityException("User not found"));
        loan.setUser(user);
        return repository.save(loan);
    }

    /**
     * Updates an existing loan for the currently authenticated user.
     *
     * @param id the ID of the loan to update
     * @param loan the loan details to update
     * @return the updated loan
     * @throws SecurityException if the user is not authenticated
     * @throws java.util.NoSuchElementException if the loan is not found or not owned by the user
     */
    public Loan update(final Integer id, Loan loan) {
        Integer userId = UserContext.getCurrentUserId();
        if (userId == null) {
            throw new SecurityException("User is not authenticated");
        }
        Loan existing = repository.findByIdAndUserId(id, userId).orElseThrow();
        loan.setId(id);
        loan.setUser(existing.getUser()); // Set the required user association before saving
        return repository.save(loan);
    }

    /**
     * Deletes a loan by its ID for the currently authenticated user.
     *
     * @param id the ID of the loan to delete
     * @throws SecurityException if the user is not authenticated
     * @throws java.util.NoSuchElementException if the loan is not found or not owned by the user
     */
    public void delete(final Integer id) {
        Integer userId = UserContext.getCurrentUserId();
        if (userId == null) {
            throw new SecurityException("User is not authenticated");
        }
        repository.findByIdAndUserId(id, userId).orElseThrow();
        repository.deleteById(id);
    }
}
