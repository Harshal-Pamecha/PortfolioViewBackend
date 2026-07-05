package com.portfolio.service;

import com.portfolio.entity.Holding;
import com.portfolio.entity.Transaction;
import java.math.BigDecimal;
import java.util.List;

/**
 * Interface for Holding entity business logic.
 */
public interface HoldingService {
    List<Holding> getAll();
    Holding getById(Integer id);
    List<Holding> getByTickerSymbol(String tickerSymbol);
    Holding create(Holding holding);
    Holding update(Integer id, Holding holding);
    void delete(Integer id);
    void deleteByTickerSymbol(String tickerSymbol);
    Transaction sell(Integer id, BigDecimal quantitySold, BigDecimal sellPrice);
}
