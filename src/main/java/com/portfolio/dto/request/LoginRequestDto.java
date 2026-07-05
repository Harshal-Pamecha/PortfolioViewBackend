package com.portfolio.dto.request;

public record LoginRequestDto(
    String email,
    String password,
    String authProviderId
) {}
