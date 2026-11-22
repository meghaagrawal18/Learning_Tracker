package com.example.learning_tracker_api.service;

import com.example.learning_tracker_api.dto.CandidateCreatedResponse;
import com.example.learning_tracker_api.dto.LearnerRequest;
import com.example.learning_tracker_api.entity.Account;
import com.example.learning_tracker_api.entity.Role;
import com.example.learning_tracker_api.exception.ConflictException;
import com.example.learning_tracker_api.repository.AccountRepository;
import com.example.learning_tracker_api.security.AccountAuthenticationProvider;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:learnercreation",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@Transactional
class LearnerCreationTest {

    @Autowired LearnerService learnerService;
    @Autowired AccountRepository accountRepository;
    @Autowired AccountAuthenticationProvider provider;

    @Test
    void createsCandidateWithTemporaryPassword() {
        CandidateCreatedResponse created = learnerService.create(
                new LearnerRequest("Ada Lovelace", "Ada@Example.com", "Backend cohort"));

        assertEquals("ada@example.com", created.credentials().email());
        assertEquals(14, created.credentials().temporaryPassword().length());

        Account account = accountRepository.findByEmail("ada@example.com").orElseThrow();
        assertEquals(Role.CANDIDATE, account.getRole());
        assertTrue(account.isMustChangePassword());
        assertNotEquals(created.credentials().temporaryPassword(), account.getPasswordHash());
        assertTrue(account.getTempPasswordExpiresAt().isAfter(Instant.now().plus(Duration.ofHours(23))));
    }

    @Test
    void duplicateEmailIgnoresCapitalLetters() {
        learnerService.create(new LearnerRequest("Ada Lovelace", "ada@example.com", "Backend cohort"));
        assertThrows(ConflictException.class, () -> learnerService.create(
                new LearnerRequest("Someone Else", "ADA@EXAMPLE.COM", "Backend cohort")));
    }

    @Test
    void seededEmailCannotBeReused() {
        assertThrows(ConflictException.class, () -> learnerService.create(
                new LearnerRequest("Another Jordan", "Jordan.Lee@example.com", "Frontend cohort")));
    }

    @Test
    void newCandidateCanSignInWithTheTemporaryPassword() {
        CandidateCreatedResponse created = learnerService.create(
                new LearnerRequest("Grace Hopper", "grace@example.com", "Backend cohort"));

        assertTrue(provider.authenticate(UsernamePasswordAuthenticationToken.unauthenticated(
                "grace@example.com", created.credentials().temporaryPassword())).isAuthenticated());
    }
}