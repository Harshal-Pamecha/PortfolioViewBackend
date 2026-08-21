package com.portfolio.service;

import com.portfolio.entity.Account;
import com.portfolio.entity.AccountType;
import com.portfolio.entity.Transaction;
import com.portfolio.entity.TransactionType;
import com.portfolio.entity.User;
import com.portfolio.entity.FamilyMember;
import com.portfolio.repository.AccountRepository;
import com.portfolio.repository.FamilyMemberRepository;
import com.portfolio.repository.HoldingRepository;
import com.portfolio.repository.TransactionRepository;
import com.portfolio.repository.UserRepository;
import com.portfolio.security.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * Service layer for Account entity business logic.
 */
@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository repository;
    private final UserRepository userRepository;
    private final HoldingRepository holdingRepository;
    private final TransactionRepository transactionRepository;
    private final FamilyMemberRepository familyMemberRepository;
    private final TransactionService transactionService;

    /**
     * Retrieves all accounts for the currently authenticated user, optionally
     * filtered by family member.
     *
     * @param familyMemberId the ID of the family member to filter by (optional)
     * @return list of accounts
     * @throws SecurityException if the user is not authenticated
     */
    public List<Account> getAll(Integer familyMemberId) {
        Integer userId = UserContext.getCurrentUserId();
        if (userId == null) {
            throw new SecurityException("User is not authenticated");
        }
        if (familyMemberId != null) {
            return repository.findByFamilyMemberIdAndUserIdWithRelations(familyMemberId, userId);
        }
        return repository.findByUserIdWithRelations(userId);
    }

    /**
     * Retrieves an account by its ID for the currently authenticated user.
     *
     * @param id the ID of the account
     * @return the account
     * @throws SecurityException                if the user is not authenticated
     * @throws java.util.NoSuchElementException if the account is not found or not
     *                                          owned by the user
     */
    public Account getById(final Integer id) {
        Integer userId = UserContext.getCurrentUserId();
        if (userId == null) {
            throw new SecurityException("User is not authenticated");
        }
        return repository.findByIdAndUserIdWithRelations(id, userId).orElseThrow();
    }

    /**
     * Creates a new account for the currently authenticated user.
     *
     * @param account the account to create
     * @return the created account
     * @throws SecurityException if the user is not authenticated
     */
    @Transactional
    public Account create(final Account account) {
        Integer userId = UserContext.getCurrentUserId();
        if (userId == null) {
            throw new SecurityException("User is not authenticated");
        }

        if (account.getName() != null) {
            account.setName(account.getName().trim());
        }

        if (account.getFamilyMember() != null && account.getFamilyMember().getId() != null) {
            List<Account> existingAccounts = repository.findByFamilyMemberIdAndUserId(account.getFamilyMember().getId(), userId);
            boolean exists = existingAccounts.stream()
                .anyMatch(a -> a.getName().equalsIgnoreCase(account.getName()));
            if (exists) {
                throw new IllegalArgumentException(
                        "An account with the name '" + account.getName() + "' already exists for this family member.");
            }
            FamilyMember fm = familyMemberRepository.findByIdAndUserId(account.getFamilyMember().getId(), userId)
                    .orElseThrow(() -> new IllegalArgumentException("Family member not found"));
            account.setFamilyMember(fm);
        } else {
            throw new IllegalArgumentException("Family member is required.");
        }

        User user = userRepository.findById(userId).orElseThrow(() -> new SecurityException("User not found"));
        account.setUser(user);

        BigDecimal initialBalance = account.getBalance() != null ? account.getBalance() : BigDecimal.ZERO;
        AccountType type = account.getType();
        boolean isCashAccount = (type == AccountType.BANK || type == AccountType.WALLET);

        if (isCashAccount && initialBalance.compareTo(BigDecimal.ZERO) != 0) {
            // Set balance to 0 first, save the account
            account.setBalance(BigDecimal.ZERO);
            Account saved = repository.save(account);

            Transaction tx = new Transaction();
            tx.setUser(saved.getUser());
            tx.setDate(java.time.LocalDateTime.now());
            
            if (initialBalance.compareTo(BigDecimal.ZERO) > 0) {
                tx.setType(TransactionType.INFLOW);
                tx.setAmount(initialBalance);
                tx.setDestAccount(saved);
                tx.setNotes("Initial Deposit: " + saved.getName());
            } else {
                tx.setType(TransactionType.OUTFLOW);
                tx.setAmount(initialBalance.negate());
                tx.setSourceAccount(saved);
                tx.setNotes("Initial Balance Adjustment (Negative): " + saved.getName());
            }
            
            transactionService.create(tx);
            return repository.findById(saved.getId()).orElse(saved);
        } else {
            return repository.save(account);
        }
    }

    /**
     * Updates an existing account for the currently authenticated user.
     *
     * @param id      the ID of the account to update
     * @param account the account details to update
     * @return the updated account
     * @throws SecurityException                if the user is not authenticated
     * @throws java.util.NoSuchElementException if the account is not found or not
     *                                          owned by the user
     */
    @Transactional
    public Account update(final Integer id, final Account account) {
        Integer userId = UserContext.getCurrentUserId();
        if (userId == null) {
            throw new SecurityException("User is not authenticated");
        }

        if (account.getName() != null) {
            account.setName(account.getName().trim());
        }

        if (account.getFamilyMember() != null && account.getFamilyMember().getId() != null) {
            List<Account> existingAccounts = repository.findByFamilyMemberIdAndUserId(account.getFamilyMember().getId(), userId);
            boolean exists = existingAccounts.stream()
                .anyMatch(a -> !a.getId().equals(id) && a.getName().equalsIgnoreCase(account.getName()));
            if (exists) {
                throw new IllegalArgumentException(
                        "An account with the name '" + account.getName() + "' already exists for this family member.");
            }
        }

        Account existing = repository.findByIdAndUserId(id, userId).orElseThrow();

        if (account.getFamilyMember() != null && account.getFamilyMember().getId() != null) {
            if (existing.getFamilyMember() != null && !existing.getFamilyMember().getId().equals(account.getFamilyMember().getId())) {
                throw new IllegalArgumentException("Cannot change the family member of an existing account.");
            }
        }
        
        BigDecimal difference = calculateDifference(existing, account);

        existing.setName(account.getName());
        existing.setType(account.getType());
        existing.setCurrency(account.getCurrency());

        AccountType type = existing.getType();
        boolean isCashAccount = (type == AccountType.BANK || type == AccountType.WALLET);

        if (isCashAccount) {
            // For BANK/WALLET, we save all fields EXCEPT balance directly.
            // We then create a transaction, which will update the balance via TransactionService.
            existing = repository.save(existing);
            
            if (difference.compareTo(BigDecimal.ZERO) != 0) {
                Transaction tx = new Transaction();
                tx.setUser(existing.getUser());
                tx.setDate(java.time.LocalDateTime.now());
                
                if (difference.compareTo(BigDecimal.ZERO) > 0) {
                    tx.setType(TransactionType.INFLOW);
                    tx.setAmount(difference);
                    tx.setDestAccount(existing);
                    tx.setNotes("Balance Adjustment (Inflow): " + existing.getName());
                } else {
                    tx.setType(TransactionType.OUTFLOW);
                    tx.setAmount(difference.negate());
                    tx.setSourceAccount(existing);
                    tx.setNotes("Balance Adjustment (Outflow): " + existing.getName());
                }
                
                transactionService.create(tx);
            }
            
            // Refresh to return the account with the updated balance
            return repository.findById(id).orElse(existing);
        } else {
            // For other accounts, we update balance directly
            existing.setBalance(account.getBalance());
            return repository.save(existing);
        }
    }

    /**
     * Deletes an account by its ID for the currently authenticated user.
     *
     * @param id the ID of the account to delete
     * @throws SecurityException                if the user is not authenticated
     * @throws java.util.NoSuchElementException if the account is not found or not
     *                                          owned by the user
     */
    @Transactional
    public void delete(final Integer id) {
        Integer userId = UserContext.getCurrentUserId();
        if (userId == null) {
            throw new SecurityException("User is not authenticated");
        }
        repository.findByIdAndUserId(id, userId).orElseThrow();

        // Safely reverse transaction impacts before deleting
        List<Transaction> transactions = transactionRepository.findBySourceAccountIdOrDestAccountId(id, id);
        for (Transaction t : transactions) {
            transactionService.delete(t);
        }

        holdingRepository.deleteByAccountId(id);
        repository.deleteById(id);
    }

    private BigDecimal calculateDifference(Account existing, Account account) {
        BigDecimal oldBalance = existing.getBalance() != null ? existing.getBalance() : BigDecimal.ZERO;
        BigDecimal newBalance = account.getBalance() != null ? account.getBalance() : BigDecimal.ZERO;
        return newBalance.subtract(oldBalance);
    }
}
