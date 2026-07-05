package com.portfolio.dto.request;

import com.portfolio.entity.Currency;
import com.portfolio.entity.SubscriptionPlan;
import java.util.Map;

public record UpdateUserRequestDto(
    Currency baseCurrency,
    SubscriptionPlan subscriptionPlan,
    Map<String, Integer> longTermThresholds
) {}
