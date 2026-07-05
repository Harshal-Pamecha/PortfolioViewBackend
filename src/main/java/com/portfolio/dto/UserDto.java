package com.portfolio.dto;

import com.portfolio.entity.User;
import com.portfolio.entity.SubscriptionPlan;
import com.portfolio.entity.Currency;

import java.util.Map;

public record UserDto(
    String email,
    SubscriptionPlan subscriptionPlan,
    Currency baseCurrency,
    Map<String, Integer> longTermThresholds
) {
    public static UserDto from(User user) {
        if (user == null) return null;
        return new UserDto(
            user.getEmail(),
            user.getSubscriptionPlan(),
            user.getBaseCurrency(),
            user.getLongTermThresholds()
        );
    }
}
