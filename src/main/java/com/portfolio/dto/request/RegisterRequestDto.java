package com.portfolio.dto.request;

public record RegisterRequestDto(
    String email,
    String password,
    String authProviderId
) {}
