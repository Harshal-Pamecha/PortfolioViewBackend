package com.portfolio.controller;

import com.portfolio.dto.FamilyMemberDto;
import com.portfolio.entity.FamilyMember;
import com.portfolio.service.FamilyMemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.portfolio.dto.request.FamilyMemberRequestDto;
import java.util.List;

@RestController
@RequestMapping("/api/family-members")
@RequiredArgsConstructor
public class FamilyMemberController {
    private final FamilyMemberService service;

    @GetMapping
    public List<FamilyMemberDto> getAll() {
        return service.getAll().stream()
                .map(FamilyMemberDto::from)
                .toList();
    }

    @GetMapping("/{id}")
    public FamilyMemberDto getById(@PathVariable final Integer id) {
        return FamilyMemberDto.from(service.getById(id));
    }

    @PostMapping
    public FamilyMemberDto create(@RequestBody final FamilyMemberRequestDto dto) {
        FamilyMember familyMember = new FamilyMember();
        familyMember.setName(dto.name());
        familyMember.setBalance(dto.balance());
        familyMember.setCurrency(dto.currency());
        return FamilyMemberDto.from(service.create(familyMember));
    }

    @PutMapping("/{id}")
    public FamilyMemberDto update(@PathVariable final Integer id, @RequestBody final FamilyMemberRequestDto dto) {
        FamilyMember familyMember = new FamilyMember();
        familyMember.setName(dto.name());
        familyMember.setBalance(dto.balance());
        familyMember.setCurrency(dto.currency());
        return FamilyMemberDto.from(service.update(id, familyMember));
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable final Integer id) {
        service.delete(id);
    }
}
