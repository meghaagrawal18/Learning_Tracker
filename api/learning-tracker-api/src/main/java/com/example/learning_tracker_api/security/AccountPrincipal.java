package com.example.learning_tracker_api.security;

import com.example.learning_tracker_api.entity.Account;
import com.example.learning_tracker_api.entity.Role;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.io.Serializable;
import java.util.List;

public record AccountPrincipal(Long accountId, String email, Role role) implements Serializable {

    public static Authentication toAuthentication(Account account) {
        AccountPrincipal principal = new AccountPrincipal(account.getId(), account.getEmail(), account.getRole());
        return UsernamePasswordAuthenticationToken.authenticated(
                principal, null, List.of(new SimpleGrantedAuthority("ROLE_" + account.getRole().name())));
    }
}