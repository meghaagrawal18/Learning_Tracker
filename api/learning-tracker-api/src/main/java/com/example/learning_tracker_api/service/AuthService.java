package com.example.learning_tracker_api.service;

import com.example.learning_tracker_api.entity.Account;
import com.example.learning_tracker_api.exception.BadRequestException;
import com.example.learning_tracker_api.exception.ResourceNotFoundException;
import com.example.learning_tracker_api.repository.AccountRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(AccountRepository accountRepository, PasswordEncoder passwordEncoder) {
        this.accountRepository = accountRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public Account changePassword(Long accountId, String currentPassword, String newPassword) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found."));

        if (!passwordEncoder.matches(currentPassword, account.getPasswordHash())) {
            throw new BadRequestException("Current password is incorrect.");
        }
        if (currentPassword.equals(newPassword)) {
            throw new BadRequestException("New password must be different from the current password.");
        }

        account.setPasswordHash(passwordEncoder.encode(newPassword));
        account.setMustChangePassword(false);
        account.setTempPasswordExpiresAt(null);
        account.setSessionVersion(account.getSessionVersion() + 1); // ends every older session
        return accountRepository.save(account);
    }
}