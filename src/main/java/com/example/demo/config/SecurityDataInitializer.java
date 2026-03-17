package com.example.demo.config;

import java.util.HashSet;
import java.util.Set;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.model.Account;
import com.example.demo.model.Role;
import com.example.demo.repository.AccountRepository;
import com.example.demo.repository.RoleRepository;

@Component
public class SecurityDataInitializer implements CommandLineRunner {
    private final RoleRepository roleRepository;
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;

    public SecurityDataInitializer(
        RoleRepository roleRepository,
        AccountRepository accountRepository,
        PasswordEncoder passwordEncoder
    ) {
        this.roleRepository = roleRepository;
        this.accountRepository = accountRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        Role roleAdmin = roleRepository.findByName("ROLE_ADMIN")
            .orElseGet(() -> roleRepository.save(new Role(null, "ROLE_ADMIN")));

        Role roleUser = roleRepository.findByName("ROLE_USER")
            .orElseGet(() -> roleRepository.save(new Role(null, "ROLE_USER")));

        createAccountIfMissing("admin", "admin123", roleAdmin);
        createAccountIfMissing("user", "123456", roleUser);
    }

    private void createAccountIfMissing(String loginName, String rawPassword, Role role) {
        if (accountRepository.findByLoginName(loginName).isPresent()) {
            return;
        }

        Account account = new Account();
        account.setLoginName(loginName);
        account.setPassword(passwordEncoder.encode(rawPassword));

        Set<Role> roles = new HashSet<>();
        roles.add(role);
        account.setRoles(roles);

        accountRepository.save(account);
    }
}
