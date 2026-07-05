package com.portfolio.service;

import com.portfolio.entity.Ticker;
import com.portfolio.repository.TickerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TickerService {
    
    private final TickerRepository tickerRepository;

    @Cacheable("tickers")
    public Optional<Ticker> findByTicker(String ticker) {
        return tickerRepository.findByTicker(ticker);
    }

    @Cacheable("tickerSearch")
    public List<Ticker> searchTickers(String query) {
        List<Ticker> results = tickerRepository.findByTickerContainingIgnoreCaseOrCompanyNameContainingIgnoreCase(query, query);
        
        return results.stream()
            .sorted((t1, t2) -> {
                boolean t1Exact = t1.getTicker().equalsIgnoreCase(query);
                boolean t2Exact = t2.getTicker().equalsIgnoreCase(query);
                
                if (t1Exact && !t2Exact) return -1; // t1 goes first
                if (!t1Exact && t2Exact) return 1;  // t2 goes first
                
                boolean c1Exact = t1.getCompanyName().equalsIgnoreCase(query);
                boolean c2Exact = t2.getCompanyName().equalsIgnoreCase(query);
                
                if (c1Exact && !c2Exact) return -1;
                if (!c1Exact && c2Exact) return 1;
                
                // As a tie-breaker, sort shorter tickers first (e.g. "AAPL" comes before "AAPL.BR")
                return Integer.compare(t1.getTicker().length(), t2.getTicker().length());
            })
            .limit(50)
            .toList();
    }
}
