package com.cmu02.airmeet_be.utils;

import lombok.Getter;

@Getter
public enum KeyPreFix {
    USER_KEY_PREFIX("user:"),
    ROOM_KEY_PREFIX("room:"),
    CODE_KEY_PREFIX("code:");

    private final String keyPrefix;

    KeyPreFix(String keyPrefix) {
        this.keyPrefix = keyPrefix;
    }
}
