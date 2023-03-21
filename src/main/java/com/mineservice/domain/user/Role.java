package com.mineservice.domain.user;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Role {

    GUEST("GUEST", "손님"),
    USER("USER", "사용자");

    private final String key;
    private final String value;
}
