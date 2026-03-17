package com.example.demo.service;

import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.example.demo.model.Account;
import com.example.demo.repository.AccountRepository;

@Service
public class AccountService implements UserDetailsService {
    private final AccountRepository accountRepository;

    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Account account = accountRepository.findByLoginName(username)
            .orElseThrow(() -> new UsernameNotFoundException("Khong tim thay tai khoan: " + username));

        List<GrantedAuthority> authorities = account.getRoles().stream()
            .map(role -> new SimpleGrantedAuthority(role.getName()))
            .map(GrantedAuthority.class::cast)
            .toList();

        return User.withUsername(account.getLoginName())
            .password(account.getPassword())
            .authorities(authorities)
            .build();
    }
}
