package com.portfolio.repository;

import com.portfolio.entity.Account;
import com.portfolio.entity.AccountType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Account entity database operations.
 */
public interface AccountRepository extends JpaRepository<Account, Integer> {


    @Query("SELECT a FROM Account a JOIN FETCH a.familyMember WHERE a.user.id = :userId")
    List<Account> findByUserIdWithRelations(@Param("userId") Integer userId);

    /**
     * Retrieves all accounts owned by a specific family member and user.
     *
     * @param familyMemberId the ID of the family member
     * @param userId the ID of the user
     * @return a list of accounts belonging to the family member
     */
    List<Account> findByFamilyMemberIdAndUserId(Integer familyMemberId, Integer userId);

    @Query("SELECT a FROM Account a JOIN FETCH a.familyMember WHERE a.familyMember.id = :familyMemberId AND a.user.id = :userId")
    List<Account> findByFamilyMemberIdAndUserIdWithRelations(@Param("familyMemberId") Integer familyMemberId, @Param("userId") Integer userId);

    /**
     * Retrieves an account by its ID, ensuring it belongs to the specified user.
     *
     * @param id the ID of the account
     * @param userId the ID of the user
     * @return an Optional containing the account if found and owned by the user
     */
    Optional<Account> findByIdAndUserId(Integer id, Integer userId);

    @Query("SELECT a FROM Account a JOIN FETCH a.familyMember WHERE a.id = :id AND a.user.id = :userId")
    Optional<Account> findByIdAndUserIdWithRelations(@Param("id") Integer id, @Param("userId") Integer userId);

    /**
     * Checks if an account with the given name already exists for a specific family member and user.
     *
     * @param name the name of the account
     * @param familyMemberId the ID of the family member
     * @param userId the ID of the user
     * @return true if an account with the specified name exists for the family member and user
     */
    @Query("SELECT COUNT(a) > 0 FROM Account a WHERE LOWER(TRIM(a.name)) = LOWER(TRIM(:name)) AND a.familyMember.id = :familyMemberId AND a.user.id = :userId")
    boolean existsByNameAndFamilyMemberIdAndUserId(@Param("name") String name, @Param("familyMemberId") Integer familyMemberId, @Param("userId") Integer userId);

    /**
     * Checks if an account with the given name already exists for a specific family member and user, excluding a specific account ID.
     *
     * @param name the name of the account
     * @param familyMemberId the ID of the family member
     * @param userId the ID of the user
     * @param id the ID of the account to exclude from the check
     * @return true if another account with the specified name exists for the family member and user
     */
    @Query("SELECT COUNT(a) > 0 FROM Account a WHERE LOWER(TRIM(a.name)) = LOWER(TRIM(:name)) AND a.familyMember.id = :familyMemberId AND a.user.id = :userId AND a.id <> :id")
    boolean existsByNameAndFamilyMemberIdAndUserIdAndIdNot(@Param("name") String name, @Param("familyMemberId") Integer familyMemberId, @Param("userId") Integer userId, @Param("id") Integer id);

    /**
     * Sums the balances of all cash accounts (BANK or WALLET) for a family member.
     *
     * @param familyMemberId the ID of the family member
     * @param userId the ID of the user
     * @param types list of cash account types (BANK, WALLET)
     * @return sum of balances
     */
    @Query("SELECT SUM(a.balance) FROM Account a WHERE a.familyMember.id = :familyMemberId AND a.user.id = :userId AND a.type IN :types")
    BigDecimal sumBalanceByFamilyMemberIdAndUserIdAndTypes(
        @Param("familyMemberId") Integer familyMemberId,
        @Param("userId") Integer userId,
        @Param("types") List<AccountType> types
    );

    /**
     * Checks if any cash accounts exist for a family member.
     *
     * @param familyMemberId the ID of the family member
     * @param userId the ID of the user
     * @param types list of cash account types (BANK, WALLET)
     * @return true if cash accounts exist, false otherwise
     */
    boolean existsByFamilyMemberIdAndUserIdAndTypeIn(Integer familyMemberId, Integer userId, List<AccountType> types);

    @Query("SELECT a FROM Account a WHERE a.familyMember.id = :familyMemberId AND a.user.id = :userId AND a.type IN :types")
    List<Account> findByFamilyMemberIdAndUserIdAndTypeIn(
        @Param("familyMemberId") Integer familyMemberId,
        @Param("userId") Integer userId,
        @Param("types") List<AccountType> types
    );
}
