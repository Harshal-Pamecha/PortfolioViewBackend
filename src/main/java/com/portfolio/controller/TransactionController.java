package com.portfolio.controller;

import com.portfolio.dto.TransactionDto;
import com.portfolio.entity.Transaction;
import com.portfolio.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.portfolio.dto.request.TransactionRequestDto;
import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {
    private final TransactionService service;

    @GetMapping
    public org.springframework.data.domain.Page<TransactionDto> getAll(
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "0") int page,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "50") int size) {
        
        org.springframework.data.domain.Pageable pageable = 
            org.springframework.data.domain.PageRequest.of(page, size, org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "date"));
            
        return service.getAll(pageable)
                .map(TransactionDto::from);
    }

    @GetMapping("/{id}")
    public TransactionDto getById(@PathVariable final Integer id) {
        return TransactionDto.from(service.getById(id));
    }

    @PostMapping
    public TransactionDto create(@RequestBody final TransactionRequestDto dto) {
        Transaction tx = new Transaction();
        tx.setAmount(dto.amount());
        tx.setType(dto.type());
        tx.setDate(dto.date());
        tx.setNotes(dto.notes());
        if (dto.sourceAccountId() != null) {
            com.portfolio.entity.Account source = new com.portfolio.entity.Account();
            source.setId(dto.sourceAccountId());
            tx.setSourceAccount(source);
        }
        if (dto.destAccountId() != null) {
            com.portfolio.entity.Account dest = new com.portfolio.entity.Account();
            dest.setId(dto.destAccountId());
            tx.setDestAccount(dest);
        }
        if (dto.holdingId() != null) {
            com.portfolio.entity.Holding holding = new com.portfolio.entity.Holding();
            holding.setId(dto.holdingId());
            tx.setHolding(holding);
        }
        return TransactionDto.from(service.create(tx));
    }

    @PutMapping("/{id}")
    public TransactionDto update(@PathVariable final Integer id, @RequestBody final TransactionRequestDto dto) {
        Transaction tx = new Transaction();
        tx.setAmount(dto.amount());
        tx.setType(dto.type());
        tx.setDate(dto.date());
        tx.setNotes(dto.notes());
        if (dto.sourceAccountId() != null) {
            com.portfolio.entity.Account source = new com.portfolio.entity.Account();
            source.setId(dto.sourceAccountId());
            tx.setSourceAccount(source);
        }
        if (dto.destAccountId() != null) {
            com.portfolio.entity.Account dest = new com.portfolio.entity.Account();
            dest.setId(dto.destAccountId());
            tx.setDestAccount(dest);
        }
        if (dto.holdingId() != null) {
            com.portfolio.entity.Holding holding = new com.portfolio.entity.Holding();
            holding.setId(dto.holdingId());
            tx.setHolding(holding);
        }
        return TransactionDto.from(service.update(id, tx));
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable final Integer id) {
        service.delete(id);
    }
}
