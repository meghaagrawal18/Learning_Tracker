package com.example.learning_tracker_api.service;

import com.example.learning_tracker_api.dto.CandidateCreatedResponse;
import com.example.learning_tracker_api.dto.CredentialsResponse;
import com.example.learning_tracker_api.dto.LearnerRequest;
import com.example.learning_tracker_api.dto.LearnerResponse;
import com.example.learning_tracker_api.entity.Account;
import com.example.learning_tracker_api.entity.Learner;
import com.example.learning_tracker_api.entity.Role;
import com.example.learning_tracker_api.exception.ConflictException;
import com.example.learning_tracker_api.exception.ResourceNotFoundException;
import com.example.learning_tracker_api.repository.AccountRepository;
import com.example.learning_tracker_api.repository.LearnerRepository;
import com.example.learning_tracker_api.security.TemporaryPasswordGenerator;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Service
public class LearnerService {

    private final LearnerRepository learnerRepository;
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;

    public LearnerService(LearnerRepository learnerRepository, AccountRepository accountRepository,
                          PasswordEncoder passwordEncoder) {
        this.learnerRepository = learnerRepository;
        this.accountRepository = accountRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<LearnerResponse> findAll() {
        return learnerRepository.findAll().stream()
                .map(learner -> new LearnerResponse(learner.getId(), learner.getName(),
                        learner.getCohort(), learner.getAccount().getEmail()))
                .toList();
    }

    @Transactional
    public CandidateCreatedResponse create(LearnerRequest request) {
        String email = request.email().trim().toLowerCase();

        if (accountRepository.existsByEmail(email)) {
            throw new ConflictException("Email already exists.");
        }

        String temporaryPassword = TemporaryPasswordGenerator.generate();

        Account account = new Account();
        account.setEmail(email);
        account.setPasswordHash(passwordEncoder.encode(temporaryPassword));
        account.setRole(Role.CANDIDATE);
        account.setMustChangePassword(true);
        account.setTempPasswordExpiresAt(Instant.now().plus(Duration.ofHours(24)));

        Learner learner = new Learner();
        learner.setName(request.name().trim());
        learner.setCohort(request.cohort().trim());

        try {
            learner.setAccount(accountRepository.saveAndFlush(account));
            learnerRepository.saveAndFlush(learner);
        } catch (DataIntegrityViolationException e) {
            // Two requests with the same email at the same moment: the database rule is the safety net
            throw new ConflictException("Email already exists.");
        }

        return new CandidateCreatedResponse(
                new LearnerResponse(learner.getId(), learner.getName(), learner.getCohort(), account.getEmail()),
                new CredentialsResponse(account.getEmail(), temporaryPassword, account.getTempPasswordExpiresAt()));
    }

    @Transactional
    public CredentialsResponse resetPassword(Long learnerId) {
        Learner learner = learnerRepository.findById(learnerId)
                .orElseThrow(() -> new ResourceNotFoundException("Learner not found."));
        Account account = learner.getAccount();

        String temporaryPassword = TemporaryPasswordGenerator.generate();
        account.setPasswordHash(passwordEncoder.encode(temporaryPassword));
        account.setMustChangePassword(true);
        account.setTempPasswordExpiresAt(Instant.now().plus(Duration.ofHours(24)));
        account.setSessionVersion(account.getSessionVersion() + 1);
        accountRepository.save(account);

        return new CredentialsResponse(account.getEmail(), temporaryPassword, account.getTempPasswordExpiresAt());
    }
}