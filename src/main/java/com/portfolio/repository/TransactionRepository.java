package com.portfolio.repository;

import com.portfolio.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Transaction entity database operations.
 */
public interface TransactionRepository extends JpaRepository<Transaction, Integer> {

    @Query(value = "SELECT t FROM Transaction t " +
            "LEFT JOIN FETCH t.sourceAccount sa " +
            "LEFT JOIN FETCH sa.familyMember " +
            "LEFT JOIN FETCH t.destAccount da " +
            "LEFT JOIN FETCH da.familyMember " +
            "LEFT JOIN FETCH t.holding " +
            "WHERE t.user.id = :userId",
           countQuery = "SELECT count(t) FROM Transaction t WHERE t.user.id = :userId")
    org.springframework.data.domain.Page<Transaction> findByUserIdWithRelations(@Param("userId") Integer userId, org.springframework.data.domain.Pageable pageable);

    @Query("SELECT t FROM Transaction t " +
            "LEFT JOIN FETCH t.sourceAccount sa " +
            "LEFT JOIN FETCH sa.familyMember " +
            "LEFT JOIN FETCH t.destAccount da " +
            "LEFT JOIN FETCH da.familyMember " +
            "LEFT JOIN FETCH t.holding " +
            "WHERE t.id = :id AND t.user.id = :userId")
    Optional<Transaction> findByIdAndUserIdWithRelations(@Param("id") Integer id, @Param("userId") Integer userId);



    /**
     * Finds all transactions where the account is either the source or destination.
     *
     * @param sourceId the ID of the source account
     * @param destId the ID of the destination account
     * @return a list of transactions
     */
    java.util.List<Transaction> findBySourceAccountIdOrDestAccountId(Integer sourceId, Integer destId);

    /**
     * Finds transactions by their associated holding ID.
     *
     * @param holdingId the ID of the holding
     * @return a list containing the transactions
     */
    java.util.List<Transaction> findByHoldingId(Integer holdingId);
}
