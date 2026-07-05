package com.portfolio.controller;

import com.portfolio.entity.Ticker;
import com.portfolio.service.TickerService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/tickers")
@RequiredArgsConstructor
public class TickerController {

    private final TickerService tickerService;

    /**
     * Search tickers by symbol or company name (auto-complete).
     * Example: /api/tickers/search?q=HDFC
     */
    @GetMapping("/search")
    public List<Ticker> search(@RequestParam("q") String query) {
        return tickerService.searchTickers(query);
    }
}
