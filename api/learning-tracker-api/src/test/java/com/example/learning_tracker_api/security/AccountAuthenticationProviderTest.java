package com.example.learning_tracker_api.security;

import com.example.learning_tracker_api.entity.Account;
import com.example.learning_tracker_api.entity.Role;
import com.example.learning_tracker_api.repository.AccountRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:providertest",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@Transactional
class AccountAuthenticationProviderTest {

    @Autowired AccountAuthenticationProvider provider;
    @Autowired AccountRepository accountRepository;
    @Autowired PasswordEncoder passwordEncoder;

    private Account save(String email, boolean mustChange, Instant expiry) {
        Account account = new Account();
        account.setEmail(email);
        account.setPasswordHash(passwordEncoder.encode("Correct-Pass-1"));
        account.setRole(Role.CANDIDATE);
        account.setMustChangePassword(mustChange);
        account.setTempPasswordExpiresAt(expiry);
        return accountRepository.save(account);
    }

    private Authentication login(String email, String password) {
        return provider.authenticate(UsernamePasswordAuthenticationToken.unauthenticated(email, password));
    }

    @Test
    void correctCredentialsSucceedAndEmailIsCaseInsensitive() {
        save("Mixed.Case@Example.com", false, null);
        Authentication result = login("mixed.case@example.com", "Correct-Pass-1");
        assertTrue(result.isAuthenticated());
        assertTrue(result.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_CANDIDATE")));
    }

    @Test
    void wrongPasswordAndUnknownEmailGiveTheSameError() {
        save("known@example.com", false, null);
        BadCredentialsException wrongPassword = assertThrows(BadCredentialsException.class,
                () -> login("known@example.com", "Wrong-Pass-1"));
        BadCredentialsException unknownEmail = assertThrows(BadCredentialsException.class,
                () -> login("nobody@example.com", "Wrong-Pass-1"));
        assertEquals(wrongPassword.getMessage(), unknownEmail.getMessage());
    }

    @Test
    void expiredTemporaryPasswordIsRejected() {
        save("expired@example.com", true, Instant.now().minusSeconds(60));
        assertThrows(BadCredentialsException.class, () -> login("expired@example.com", "Correct-Pass-1"));
    }

    @Test
    void validTemporaryPasswordStillLogsIn() {
        save("fresh@example.com", true, Instant.now().plusSeconds(3600));
        assertTrue(login("fresh@example.com", "Correct-Pass-1").isAuthenticated());
    }
}