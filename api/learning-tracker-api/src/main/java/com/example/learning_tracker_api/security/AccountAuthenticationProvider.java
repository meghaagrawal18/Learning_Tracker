package com.example.learning_tracker_api.security;

import com.example.learning_tracker_api.entity.Account;
import com.example.learning_tracker_api.repository.AccountRepository;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AccountAuthenticationProvider implements AuthenticationProvider {

    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final String dummyHash;

    public AccountAuthenticationProvider(AccountRepository accountRepository, PasswordEncoder passwordEncoder) {
        this.accountRepository = accountRepository;
        this.passwordEncoder = passwordEncoder;
        this.dummyHash = passwordEncoder.encode("not-a-real-password");
    }

    @Override
    public Authentication authenticate(Authentication authentication) {
        String email = authentication.getName().trim().toLowerCase();
        String password = String.valueOf(authentication.getCredentials());

        Account account = accountRepository.findByEmail(email).orElse(null);
        // Always run one password check, so an unknown email takes as long as a wrong password
        String hash = account == null ? dummyHash : account.getPasswordHash();
        boolean passwordMatches = passwordEncoder.matches(password, hash);

        if (account == null || !passwordMatches || account.isTemporaryPasswordExpired()) {
            throw new BadCredentialsException("Invalid email or password.");
        }
        return AccountPrincipal.toAuthentication(account);
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}