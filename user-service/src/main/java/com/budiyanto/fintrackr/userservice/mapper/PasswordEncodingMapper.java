package com.budiyanto.fintrackr.userservice.mapper;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PasswordEncodingMapper {

    private final PasswordEncoder passwordEncoder;

    @EncodedMapping
    public String encode(String password) {
        if (password == null) {
            return null;
        }
        return passwordEncoder.encode(password);
    }
}
