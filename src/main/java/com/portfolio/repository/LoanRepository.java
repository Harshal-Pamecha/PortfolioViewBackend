package com.portfolio.repository;

import com.portfolio.entity.Loan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Loan entity database operations.
 */
public interface LoanRepository extends JpaRepository<Loan, Integer> {
    /**
     * Retrieves all loans owned by the specified user.
     *
     * @param userId the ID of the user
     * @return a list of loans
     */
    List<Loan> findByUserId(Integer userId);

    /**
     * Retrieves a loan by its ID, ensuring it belongs to the specified user.
     *
     * @param id the ID of the loan
     * @param userId the ID of the user
     * @return an Optional containing the loan if found and owned by the user
     */
    Optional<Loan> findByIdAndUserId(Integer id, Integer userId);
}
