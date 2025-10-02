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

}
