package com.portfolio.controller;

import com.portfolio.dto.UserDto;
import com.portfolio.entity.User;
import com.portfolio.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.portfolio.dto.request.UpdateUserRequestDto;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService service;

    @GetMapping("/me")
    public UserDto getCurrentUser() {
        Integer userId = com.portfolio.security.UserContext.getCurrentUserId();
        if (userId == null) {
            throw new SecurityException("User is not authenticated");
        }
        return UserDto.from(service.getById(userId));
    }

    @PutMapping("/me")
    public UserDto updateCurrentUser(@RequestBody final UpdateUserRequestDto dto) {
        Integer userId = com.portfolio.security.UserContext.getCurrentUserId();
        if (userId == null) {
            throw new SecurityException("User is not authenticated");
        }
        User user = new User();
        user.setBaseCurrency(dto.baseCurrency());
        user.setSubscriptionPlan(dto.subscriptionPlan());
        user.setLongTermThresholds(dto.longTermThresholds());
        return UserDto.from(service.update(userId, user));
    }

    @DeleteMapping("/me")
    public void deleteCurrentUser() {
        Integer userId = com.portfolio.security.UserContext.getCurrentUserId();
        if (userId == null) {
            throw new SecurityException("User is not authenticated");
        }
        service.delete(userId);
    }
}
