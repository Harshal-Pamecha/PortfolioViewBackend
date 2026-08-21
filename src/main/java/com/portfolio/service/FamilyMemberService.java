package com.portfolio.service;

import com.portfolio.entity.FamilyMember;
import com.portfolio.entity.User;
import com.portfolio.entity.Account;
import com.portfolio.entity.AccountType;
import com.portfolio.repository.AccountRepository;
import com.portfolio.repository.FamilyMemberRepository;
import com.portfolio.repository.UserRepository;
import com.portfolio.security.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * Service layer for FamilyMember entity business logic.
 */
@Service
@RequiredArgsConstructor
public class FamilyMemberService {

    private final FamilyMemberRepository repository;
    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final AccountService accountService;

    /**
     * Helper to compute dynamic balance based on BANK and WALLET accounts.
     * Fallback to stored balance if no cash accounts exist.
     */
    private BigDecimal calculateDynamicBalance(Integer familyMemberId, Integer userId, BigDecimal dbBalance) {
        List<AccountType> cashTypes = List.of(AccountType.BANK, AccountType.WALLET);
        boolean hasCashAccounts = accountRepository.existsByFamilyMemberIdAndUserIdAndTypeIn(familyMemberId, userId, cashTypes);
        if (hasCashAccounts) {
            BigDecimal sum = accountRepository.sumBalanceByFamilyMemberIdAndUserIdAndTypes(familyMemberId, userId, cashTypes);
            return sum != null ? sum : BigDecimal.ZERO;
        }
        return dbBalance != null ? dbBalance : BigDecimal.ZERO;
    }

    /**
     * Retrieves all family members for the currently authenticated user.
     *
     * @return list of family members
     * @throws SecurityException if the user is not authenticated
     */
    public List<FamilyMember> getAll() {
        Integer userId = UserContext.getCurrentUserId();
        if (userId == null) {
            throw new SecurityException("User is not authenticated");
        }
        List<FamilyMember> members = repository.findByUserId(userId);
        for (FamilyMember member : members) {
            member.setBalance(calculateDynamicBalance(member.getId(), userId, member.getBalance()));
        }
        return members;
    }

    /**
     * Retrieves a family member by its ID for the currently authenticated user.
     *
     * @param id the ID of the family member
     * @return the family member
     * @throws SecurityException if the user is not authenticated
     * @throws java.util.NoSuchElementException if the family member is not found or not owned by the user
     */
    public FamilyMember getById(final Integer id) {
        Integer userId = UserContext.getCurrentUserId();
        if (userId == null) {
            throw new SecurityException("User is not authenticated");
        }
        FamilyMember member = repository.findByIdAndUserId(id, userId).orElseThrow();
        member.setBalance(calculateDynamicBalance(member.getId(), userId, member.getBalance()));
        return member;
    }

    /**
     * Creates a new family member for the currently authenticated user.
     *
     * @param familyMember the family member to create
     * @return the created family member
     * @throws SecurityException if the user is not authenticated
     */
    public FamilyMember create(final FamilyMember familyMember) {
        Integer userId = UserContext.getCurrentUserId();
        if (userId == null) {
            throw new SecurityException("User is not authenticated");
        }
        List<FamilyMember> existingMembers = repository.findByUserId(userId);
        boolean exists = existingMembers.stream()
            .anyMatch(m -> m.getName().equalsIgnoreCase(familyMember.getName()));
        if (exists) {
            throw new IllegalArgumentException("A family member with this name already exists.");
        }
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new SecurityException("User not found"));
        familyMember.setUser(user);
        
        BigDecimal initialBalance = familyMember.getBalance();
        FamilyMember savedMember = repository.save(familyMember);
        
        // Auto-create a default "Cash" account of type BANK if there's a non-zero initial balance
        if (initialBalance != null && initialBalance.compareTo(BigDecimal.ZERO) != 0) {
            Account defaultAccount = new Account();
            defaultAccount.setName("Cash");
            defaultAccount.setType(AccountType.BANK);
            defaultAccount.setFamilyMember(savedMember);
            defaultAccount.setBalance(initialBalance);
            defaultAccount.setUser(user);
            accountRepository.save(defaultAccount);
            
            savedMember.setBalance(initialBalance);
        } else {
            savedMember.setBalance(BigDecimal.ZERO);
        }
        
        return savedMember;
    }

    /**
     * Updates an existing family member for the currently authenticated user.
     *
     * @param id the ID of the family member to update
     * @param familyMember the family member details to update
     * @return the updated family member
     * @throws SecurityException if the user is not authenticated
     * @throws java.util.NoSuchElementException if the family member is not found or not owned by the user
     */
    public FamilyMember update(final Integer id, FamilyMember familyMember) {
        Integer userId = UserContext.getCurrentUserId();
        if (userId == null) {
            throw new SecurityException("User is not authenticated");
        }
        
        List<FamilyMember> existingMembers = repository.findByUserId(userId);
        boolean exists = existingMembers.stream()
            .anyMatch(m -> !m.getId().equals(id) && m.getName().equalsIgnoreCase(familyMember.getName()));
        if (exists) {
            throw new IllegalArgumentException("A family member with this name already exists.");
        }

        FamilyMember existing = repository.findByIdAndUserId(id, userId).orElseThrow();
        existing.setName(familyMember.getName());
        
        // Distribute edited balance to BANK/WALLET accounts if they exist
        BigDecimal newBalance = familyMember.getBalance();
        List<AccountType> cashTypes = List.of(AccountType.BANK, AccountType.WALLET);
        List<Account> cashAccounts = accountRepository.findByFamilyMemberIdAndUserIdAndTypeIn(id, userId, cashTypes);
                
        if (!cashAccounts.isEmpty()) {
            if (cashAccounts.size() == 1) {
                Account singleAccount = cashAccounts.get(0);
                singleAccount.setBalance(newBalance != null ? newBalance : BigDecimal.ZERO);
                accountRepository.save(singleAccount);
            } else {
                Account targetAccount = cashAccounts.stream()
                        .filter(acc -> "Cash".equalsIgnoreCase(acc.getName()))
                        .findFirst()
                        .orElse(cashAccounts.get(0));
                
                BigDecimal otherSum = cashAccounts.stream()
                        .filter(acc -> !acc.getId().equals(targetAccount.getId()))
                        .map(acc -> acc.getBalance() != null ? acc.getBalance() : BigDecimal.ZERO)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                
                BigDecimal targetBalance = (newBalance != null ? newBalance : BigDecimal.ZERO).subtract(otherSum);
                targetAccount.setBalance(targetBalance);
                accountRepository.save(targetAccount);
            }
        } else {
            existing.setBalance(newBalance);
        }
        
        FamilyMember savedMember = repository.save(existing);
        savedMember.setBalance(calculateDynamicBalance(id, userId, savedMember.getBalance()));
        return savedMember;
    }

    /**
     * Deletes a family member by its ID for the currently authenticated user.
     *
     * @param id the ID of the family member to delete
     * @throws SecurityException if the user is not authenticated
     * @throws java.util.NoSuchElementException if the family member is not found or not owned by the user
     */
    @Transactional
    public void delete(final Integer id) {
        Integer userId = UserContext.getCurrentUserId();
        if (userId == null) {
            throw new SecurityException("User is not authenticated");
        }
        repository.findByIdAndUserId(id, userId).orElseThrow();
        
        List<Account> accounts = accountRepository.findByFamilyMemberIdAndUserId(id, userId);
        for (Account account : accounts) {
            accountService.delete(account.getId());
        }
        
        repository.deleteById(id);
    }
}
