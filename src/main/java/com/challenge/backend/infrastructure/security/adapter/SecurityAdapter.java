package com.challenge.backend.infrastructure.security.adapter;

import com.challenge.backend.application.port.out.SecurityPort;
import com.challenge.backend.domain.model.User;
import com.challenge.backend.domain.repository.UserRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SecurityAdapter implements SecurityPort, UserDetailsService {

    private final UserRepositoryPort userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user;
        try {
            Long id = Long.parseLong(username);
            user = userRepository.findById(id)
                    .orElseGet(() -> userRepository.findByEmail(username)
                            .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username)));
        } catch (NumberFormatException e) {
            user = userRepository.findByEmail(username)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        }

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getId().toString())
                .password(user.getPasswordHash())
                .roles(user.getRole().name())
                .build();
    }

    @Override
    public String generateToken(User user) {
        return jwtService.generateToken(loadUserByUsername(user.getId().toString()));
    }

    @Override
    public boolean validatePassword(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

    @Override
    public String encodePassword(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }
}