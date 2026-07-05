package com.portfolio.controller;

import com.portfolio.dto.LoanDto;
import com.portfolio.entity.Loan;
import com.portfolio.service.LoanService;
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
@RequestMapping("/api/loans")
@RequiredArgsConstructor
public class LoanController {
    private final LoanService service;

    @GetMapping
    public List<LoanDto> getAll() {
        return service.getAll().stream()
                .map(LoanDto::from)
                .toList();
    }

    @GetMapping("/{id}")
    public LoanDto getById(@PathVariable final Integer id) {
        return LoanDto.from(service.getById(id));
    }

    @PostMapping
    public LoanDto create(@RequestBody final Loan loan) {
        return LoanDto.from(service.create(loan));
    }

    @PutMapping("/{id}")
    public LoanDto update(@PathVariable final Integer id, @RequestBody final Loan loan) {
        return LoanDto.from(service.update(id, loan));
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable final Integer id) {
        service.delete(id);
    }
}
