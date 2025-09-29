package com.sam.bankmanagement.Bank.Management.Application.util;

import com.sam.bankmanagement.Bank.Management.Application.entity.Customer;
import com.sam.bankmanagement.Bank.Management.Application.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;

@Component
public class SecurityUtil {

    private static CustomerRepository customerRepository;

    @Autowired
    public void setCustomerRepository(CustomerRepository customerRepository) {
        SecurityUtil.customerRepository = customerRepository;
    }

    public static Customer getCurrentCustomer() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof User) {
            User userDetails = (User) authentication.getPrincipal();
            String email = userDetails.getUsername();

            // Check if it's not an admin user
            if (!"admin@bankapp.com".equals(email)) {
                return customerRepository.findByEmail(email).orElse(null);
            }
        }
        return null;
    }

    public static Long getCurrentCustomerId() {
        Customer customer = getCurrentCustomer();
        return customer != null ? customer.getId() : null;
    }

    public static String getCurrentCustomerEmail() {
        Customer customer = getCurrentCustomer();
        return customer != null ? customer.getEmail() : null;
    }

    public static boolean isCurrentUserAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof User) {
            User userDetails = (User) authentication.getPrincipal();
            return "admin@bankapp.com".equals(userDetails.getUsername()) ||
                   authentication.getAuthorities().stream()
                       .anyMatch(auth -> "ROLE_ADMIN".equals(auth.getAuthority()));
        }
        return false;
    }

    public static String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof User) {
            User userDetails = (User) authentication.getPrincipal();
            return userDetails.getUsername();
        }
        return null;
    }
}
