package com.portfolio.repository;

import com.portfolio.entity.Ticker;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TickerRepository extends JpaRepository<Ticker, Integer> {
    List<Ticker> findByTickerContainingIgnoreCaseOrCompanyNameContainingIgnoreCase(String ticker, String name);

    java.util.Optional<Ticker> findByTicker(String ticker);
    List<Ticker> findByTickerIn(java.util.Collection<String> tickers);
}
