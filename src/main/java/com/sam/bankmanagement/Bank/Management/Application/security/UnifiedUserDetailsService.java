package com.sam.bankmanagement.Bank.Management.Application.security;

import com.sam.bankmanagement.Bank.Management.Application.entity.Customer;
import com.sam.bankmanagement.Bank.Management.Application.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UnifiedUserDetailsService implements UserDetailsService {

    private final CustomerRepository customerRepository;

    private static final String ADMIN_EMAIL = "admin@bankapp.com";
    private static final String ADMIN_PASSWORD_ENCODED = "$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.";

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.info("Loading user details for: {}", username);


        if (ADMIN_EMAIL.equals(username)) {
            return User.builder()
                    .username(ADMIN_EMAIL)
                    .password(ADMIN_PASSWORD_ENCODED)
                    .authorities(List.of(new SimpleGrantedAuthority("ROLE_ADMIN")))
                    .build();
        }

        Customer customer = customerRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + username));

        return User.builder()
                .username(customer.getEmail())
                .password(customer.getPassword())
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_CUSTOMER")))
                .accountLocked(customer.getStatus() != Customer.CustomerStatus.ACTIVE)
                .build();
    }
}
