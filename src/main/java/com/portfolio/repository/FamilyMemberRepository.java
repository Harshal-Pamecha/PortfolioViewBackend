package com.portfolio.repository;

import com.portfolio.entity.FamilyMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for FamilyMember entity database operations.
 */
public interface FamilyMemberRepository extends JpaRepository<FamilyMember, Integer> {
    /**
     * Retrieves all family members owned by the specified user.
     *
     * @param userId the ID of the user
     * @return a list of family members
     */
    List<FamilyMember> findByUserId(Integer userId);

    /**
     * Retrieves a family member by its ID, ensuring it belongs to the specified user.
     *
     * @param id the ID of the family member
     * @param userId the ID of the user
     * @return an Optional containing the family member if found and owned by the user
     */
    Optional<FamilyMember> findByIdAndUserId(Integer id, Integer userId);

    /**
     * Checks if a family member with the given name (case-insensitive) already exists for the user.
     *
     * @param userId the ID of the user
     * @param name the name to check
     * @return true if a family member with that name exists, false otherwise
     */
    boolean existsByUserIdAndNameIgnoreCase(Integer userId, String name);

    /**
     * Retrieves a family member by name (case-insensitive) and user ID.
     *
     * @param userId the ID of the user
     * @param name the name of the family member
     * @return an Optional containing the family member if found
     */
    Optional<FamilyMember> findByUserIdAndNameIgnoreCase(Integer userId, String name);
}
