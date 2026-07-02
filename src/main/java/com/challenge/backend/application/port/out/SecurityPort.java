package com.challenge.backend.application.port.out;

import com.challenge.backend.domain.model.User;

public interface SecurityPort {
    String generateToken(User user);
    boolean validatePassword(String rawPassword, String encodedPassword);
    String encodePassword(String rawPassword);
}