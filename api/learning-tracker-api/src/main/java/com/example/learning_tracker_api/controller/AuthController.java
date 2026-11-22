package com.example.learning_tracker_api.controller;

import com.example.learning_tracker_api.dto.ChangePasswordRequest;
import com.example.learning_tracker_api.dto.LoginRequest;
import com.example.learning_tracker_api.dto.LoginResponse;
import com.example.learning_tracker_api.entity.Account;
import com.example.learning_tracker_api.repository.AccountRepository;
import com.example.learning_tracker_api.security.AccountPrincipal;
import com.example.learning_tracker_api.security.LoginRateLimiter;
import com.example.learning_tracker_api.security.SessionGuardFilter;
import com.example.learning_tracker_api.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository securityContextRepository;
    private final AccountRepository accountRepository;
    private final AuthService authService;
    private final LoginRateLimiter rateLimiter;
    private final String sessionCookieName;

    public AuthController(AuthenticationManager authenticationManager,
                          SecurityContextRepository securityContextRepository,
                          AccountRepository accountRepository,
                          AuthService authService,
                          LoginRateLimiter rateLimiter,
                          @Value("${server.servlet.session.cookie.name:JSESSIONID}") String sessionCookieName) {
        this.authenticationManager = authenticationManager;
        this.securityContextRepository = securityContextRepository;
        this.accountRepository = accountRepository;
        this.authService = authService;
        this.rateLimiter = rateLimiter;
        this.sessionCookieName = sessionCookieName;
    }

    @GetMapping("/csrf")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void csrf() {
        // The CSRF cookie is sent by CsrfCookieFilter; nothing else to do
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest body,
                               HttpServletRequest request, HttpServletResponse response) {
        String email = body.email().trim().toLowerCase();
        String ip = request.getRemoteAddr();
        String accountKey = email + "|" + ip;

        rateLimiter.checkAllowed(accountKey, 5);
        rateLimiter.checkAllowed(ip, 20);

        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    UsernamePasswordAuthenticationToken.unauthenticated(email, body.password()));
        } catch (AuthenticationException ex) {
            rateLimiter.recordFailure(accountKey);
            rateLimiter.recordFailure(ip);
            throw ex;
        }
        rateLimiter.reset(accountKey);

        AccountPrincipal principal = (AccountPrincipal) authentication.getPrincipal();
        Account account = accountRepository.findById(principal.accountId()).orElseThrow();
        startFreshSession(authentication, account.getSessionVersion(), request, response);

        return new LoginResponse(account.getRole(), account.isMustChangePassword());
    }

    @PostMapping("/change-password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changePassword(@AuthenticationPrincipal AccountPrincipal principal,
                               @Valid @RequestBody ChangePasswordRequest body,
                               HttpServletRequest request, HttpServletResponse response) {
        Account account = authService.changePassword(
                principal.accountId(), body.currentPassword(), body.newPassword());
        // New session id and new version: the old session cannot be reused
        startFreshSession(AccountPrincipal.toAuthentication(account),
                account.getSessionVersion(), request, response);
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        SecurityContextHolder.clearContext();

        ResponseCookie expired = ResponseCookie.from(sessionCookieName, "")
                .path("/").httpOnly(true).maxAge(0).build();
        response.addHeader(HttpHeaders.SET_COOKIE, expired.toString());
    }

    private void startFreshSession(Authentication authentication, int sessionVersion,
                                   HttpServletRequest request, HttpServletResponse response) {
        HttpSession old = request.getSession(false);
        if (old != null) {
            old.invalidate();
        }
        HttpSession session = request.getSession(true);
        session.setAttribute(SessionGuardFilter.SESSION_VERSION, sessionVersion);

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        securityContextRepository.saveContext(context, request, response);
    }
}