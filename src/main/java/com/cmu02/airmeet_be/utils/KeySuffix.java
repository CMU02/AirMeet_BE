package com.cmu02.airmeet_be.utils;

import lombok.Getter;

@Getter
public enum KeySuffix {
    ROOMS_SUFFIX(":rooms"),
    USERS_SUFFIX(":users"),
    MESSAGES_SUFFIX(":messages");

    private final String keySuffix;

    KeySuffix(String keySuffix) {
        this.keySuffix = keySuffix;
    }
}
