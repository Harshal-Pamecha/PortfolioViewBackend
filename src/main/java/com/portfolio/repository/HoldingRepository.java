package com.portfolio.repository;

import com.portfolio.entity.Holding;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Holding entity database operations.
 */
public interface HoldingRepository extends JpaRepository<Holding, Integer> {
    /**
     * Retrieves all holdings owned by the specified user.
     *
     * @param userId the ID of the user
     * @return a list of holdings
     */
    List<Holding> findByUserId(Integer userId);

    @Query("SELECT h FROM Holding h JOIN FETCH h.account a JOIN FETCH a.familyMember f WHERE h.user.id = :userId")
    List<Holding> findByUserIdWithRelations(@Param("userId") Integer userId);

    /**
     * Retrieves a holding by its ID, ensuring it belongs to the specified user.
     *
     * @param id the ID of the holding
     * @param userId the ID of the user
     * @return an Optional containing the holding if found and owned by the user
     */
    Optional<Holding> findByIdAndUserId(Integer id, Integer userId);

    @Query("SELECT h FROM Holding h JOIN FETCH h.account a JOIN FETCH a.familyMember f WHERE h.id = :id AND h.user.id = :userId")
    Optional<Holding> findByIdAndUserIdWithRelations(@Param("id") Integer id, @Param("userId") Integer userId);

    /**
     * Retrieves all holdings matching the ticker symbol for a specific user.
     *
     * @param userId the ID of the user
     * @param tickerSymbol the ticker symbol to search for
     * @return a list of holdings
     */
    List<Holding> findByUserIdAndTickerSymbol(Integer userId, String tickerSymbol);

    @Query("SELECT h FROM Holding h JOIN FETCH h.account a JOIN FETCH a.familyMember f WHERE h.user.id = :userId AND h.tickerSymbol = :tickerSymbol")
    List<Holding> findByUserIdAndTickerSymbolWithRelations(@Param("userId") Integer userId, @Param("tickerSymbol") String tickerSymbol);

    /**
     * Retrieves all holdings matching the account ID and ticker symbol, ordered by purchase date.
     */
    List<Holding> findByAccountIdAndTickerSymbolOrderByPurchaseDateAsc(Integer accountId, String tickerSymbol);

    /**
     * Retrieves all holdings matching the account ID and name, ordered by purchase date.
     */
    List<Holding> findByAccountIdAndNameOrderByPurchaseDateAsc(Integer accountId, String name);

    /**
     * Deletes all holdings matching the ticker symbol for a specific user.
     *
     * @param userId the ID of the user
     * @param tickerSymbol the ticker symbol to delete
     */
    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.transaction.annotation.Transactional
    @org.springframework.data.jpa.repository.Query("DELETE FROM Holding h WHERE h.user.id = :userId AND h.tickerSymbol = :tickerSymbol")
    void deleteByUserIdAndTickerSymbol(@org.springframework.data.repository.query.Param("userId") Integer userId, @org.springframework.data.repository.query.Param("tickerSymbol") String tickerSymbol);

    /**
     * Deletes all holdings for a specific account.
     *
     * @param accountId the ID of the account
     */
    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.transaction.annotation.Transactional
    @org.springframework.data.jpa.repository.Query("DELETE FROM Holding h WHERE h.account.id = :accountId")
    void deleteByAccountId(@org.springframework.data.repository.query.Param("accountId") Integer accountId);
}
