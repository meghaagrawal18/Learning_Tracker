package com.example.learning_tracker_api.security;

import com.example.learning_tracker_api.entity.Account;
import com.example.learning_tracker_api.entity.Role;
import com.example.learning_tracker_api.repository.AccountRepository;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:guardtest",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@Transactional
class SessionGuardFilterTest {

    @Autowired AccountRepository accountRepository;
    @Autowired PasswordEncoder passwordEncoder;

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    private Account newAccount(boolean mustChange) {
        Account account = new Account();
        account.setEmail("guard" + System.nanoTime() + "@example.com");
        account.setPasswordHash(passwordEncoder.encode("Password-12345"));
        account.setRole(Role.CANDIDATE);
        account.setMustChangePassword(mustChange);
        return accountRepository.save(account);
    }

    private MockHttpServletRequest signedInRequest(Account account, String uri) {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", uri);
        request.setRequestURI(uri);
        request.getSession(true).setAttribute(SessionGuardFilter.SESSION_VERSION, account.getSessionVersion());
        SecurityContextHolder.getContext().setAuthentication(AccountPrincipal.toAuthentication(account));
        return request;
    }

    private MockFilterChain run(MockHttpServletRequest request, MockHttpServletResponse response)
            throws ServletException, IOException {
        MockFilterChain chain = new MockFilterChain();
        new SessionGuardFilter(accountRepository).doFilter(request, response, chain);
        return chain;
    }

    @Test
    void normalSessionPasses() throws Exception {
        Account account = newAccount(false);
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = run(signedInRequest(account, "/api/v1/me/assignments"), response);
        assertNotNull(chain.getRequest());
        assertEquals(200, response.getStatus());
    }

    @Test
    void temporaryPasswordBlocksOtherEndpoints() throws Exception {
        Account account = newAccount(true);
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = run(signedInRequest(account, "/api/v1/me/assignments"), response);
        assertNull(chain.getRequest());
        assertEquals(403, response.getStatus());
    }

    @Test
    void temporaryPasswordCanStillChangePassword() throws Exception {
        Account account = newAccount(true);
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = run(signedInRequest(account, "/api/v1/auth/change-password"), response);
        assertNotNull(chain.getRequest());
    }

    @Test
    void oldSessionIsRejectedAfterReset() throws Exception {
        Account account = newAccount(false);
        MockHttpServletRequest request = signedInRequest(account, "/api/v1/me/assignments");
        account.setSessionVersion(account.getSessionVersion() + 1);
        accountRepository.save(account);

        run(request, new MockHttpServletResponse());
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }
}