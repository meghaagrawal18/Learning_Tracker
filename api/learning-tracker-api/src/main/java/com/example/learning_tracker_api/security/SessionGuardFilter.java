package com.example.learning_tracker_api.security;

import com.example.learning_tracker_api.entity.Account;
import com.example.learning_tracker_api.repository.AccountRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;

public class SessionGuardFilter extends OncePerRequestFilter {

    public static final String SESSION_VERSION = "sessionVersion";

    private static final Set<String> ALLOWED_WITH_TEMPORARY_PASSWORD = Set.of(
            "/api/v1/auth/change-password",
            "/api/v1/auth/logout",
            "/api/v1/auth/csrf");

    private final AccountRepository accountRepository;

    public SessionGuardFilter(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.getPrincipal() instanceof AccountPrincipal principal) {
            Account account = accountRepository.findById(principal.accountId()).orElse(null);
            HttpSession session = request.getSession(false);
            Object sessionVersion = session == null ? null : session.getAttribute(SESSION_VERSION);

            boolean valid = account != null
                    && sessionVersion != null
                    && sessionVersion.equals(account.getSessionVersion())
                    && !account.isTemporaryPasswordExpired();

            if (!valid) {
                // Old, reset or expired session: treat the caller as not signed in
                if (session != null) {
                    session.invalidate();
                }
                SecurityContextHolder.clearContext();
            } else if (account.isMustChangePassword()
                    && !ALLOWED_WITH_TEMPORARY_PASSWORD.contains(request.getRequestURI())) {
                JsonSecurityHandlers.write(response, 403, "Password change required.");
                return;
            }
        }
        chain.doFilter(request, response);
    }
}