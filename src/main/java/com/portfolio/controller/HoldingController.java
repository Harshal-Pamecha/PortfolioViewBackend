package com.portfolio.controller;

import com.portfolio.dto.HoldingDto;
import com.portfolio.dto.SellRequestDto;
import com.portfolio.dto.TransactionDto;
import com.portfolio.entity.Holding;
import com.portfolio.service.HoldingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/holdings")
@RequiredArgsConstructor
public class HoldingController {
    private final HoldingService service;

    @GetMapping
    public List<HoldingDto> getAll() {
        return service.getAll().stream()
                .map(HoldingDto::from)
                .toList();
    }

    @GetMapping("/{id}")
    public HoldingDto getById(@PathVariable final Integer id) {
        return HoldingDto.from(service.getById(id));
    }

    @GetMapping("/ticker/{tickerSymbol}")
    public List<HoldingDto> getByTickerSymbol(@PathVariable final String tickerSymbol) {
        return service.getByTickerSymbol(tickerSymbol).stream()
                .map(HoldingDto::from)
                .toList();
    }

    @PostMapping
    public HoldingDto create(@RequestBody final Holding holding) {
        return HoldingDto.from(service.create(holding));
    }

    @PutMapping("/{id}")
    public HoldingDto update(@PathVariable final Integer id, @RequestBody final Holding holding) {
        return HoldingDto.from(service.update(id, holding));
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable final Integer id) {
        service.delete(id);
    }

    @DeleteMapping("/ticker/{tickerSymbol}")
    public void deleteByTickerSymbol(@PathVariable final String tickerSymbol) {
        service.deleteByTickerSymbol(tickerSymbol);
    }

    @PostMapping("/{id}/sell")
    public TransactionDto sell(@PathVariable final Integer id, @RequestBody final SellRequestDto sellRequest) {
        return TransactionDto.from(service.sell(id, sellRequest.getQuantitySold(), sellRequest.getSellPrice()));
    }
}
