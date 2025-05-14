package com.cmu02.airmeet_be.domain.model;

import lombok.*;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {
    private String uuid;
    private String nickname;
}
