package com.portfolio.repository;

import com.portfolio.entity.PortfolioSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PortfolioSnapshotRepository extends JpaRepository<PortfolioSnapshot, Integer> {

    List<PortfolioSnapshot> findByUserIdAndDateGreaterThanEqualOrderByDateAsc(Integer userId, LocalDate startDate);
    Optional<PortfolioSnapshot> findByUserIdAndDate(Integer userId, LocalDate date);
    void deleteByUserId(Integer userId);
}
