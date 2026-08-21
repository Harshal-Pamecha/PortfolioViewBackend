package com.portfolio.controller;

import com.portfolio.dto.AccountDto;
import com.portfolio.entity.Account;
import com.portfolio.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.portfolio.dto.request.AccountRequestDto;
import com.portfolio.entity.FamilyMember;
import java.util.List;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService service;

    @GetMapping
    public List<AccountDto> getAll(@RequestParam(required = false) Integer familyMemberId) {
        return service.getAll(familyMemberId).stream()
                .map(AccountDto::from)
                .toList();
    }

    @GetMapping("/{id}")
    public AccountDto getById(@PathVariable final Integer id) {
        return AccountDto.from(service.getById(id));
    }

    @PostMapping
    public AccountDto create(@RequestBody final AccountRequestDto dto) {
        Account account = new Account();
        account.setName(dto.name());
        account.setType(dto.type());
        account.setBalance(dto.balance());
        account.setCurrency(dto.currency());
        if (dto.familyMemberId() != null) {
            FamilyMember fm = new FamilyMember();
            fm.setId(dto.familyMemberId());
            account.setFamilyMember(fm);
        }
        return AccountDto.from(service.create(account));
    }

    @PutMapping("/{id}")
    public AccountDto update(@PathVariable final Integer id, @RequestBody final AccountRequestDto dto) {
        Account account = new Account();
        account.setName(dto.name());
        account.setType(dto.type());
        account.setBalance(dto.balance());
        account.setCurrency(dto.currency());
        if (dto.familyMemberId() != null) {
            FamilyMember fm = new FamilyMember();
            fm.setId(dto.familyMemberId());
            account.setFamilyMember(fm);
        }
        return AccountDto.from(service.update(id, account));
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable final Integer id) {
        service.delete(id);
    }
}
