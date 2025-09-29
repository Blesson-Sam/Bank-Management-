package com.sam.bankmanagement.Bank.Management.Application.controller;

import com.sam.bankmanagement.Bank.Management.Application.dto.LoginRequest;
import com.sam.bankmanagement.Bank.Management.Application.dto.LoginResponse;
import com.sam.bankmanagement.Bank.Management.Application.dto.RegisterRequest;
import com.sam.bankmanagement.Bank.Management.Application.entity.Customer;
import com.sam.bankmanagement.Bank.Management.Application.repository.CustomerRepository;
import com.sam.bankmanagement.Bank.Management.Application.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
@Tag(name = "Authentication", description = "Authentication management APIs")
public class AuthController {

    private static final String ADMIN_EMAIL = "admin@bankapp.com";
    private static final String ADMIN_PASSWORD = "admin123";
    private static final Long ADMIN_ID = 0L;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/login")
    @Operation(summary = "User login", description = "Authenticate user and return JWT token")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful login", content = @Content(schema = @Schema(implementation = LoginResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid request"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            if ("ADMIN".equalsIgnoreCase(loginRequest.getUserType())) {
                if (ADMIN_EMAIL.equals(loginRequest.getEmail()) &&
                    ADMIN_PASSWORD.equals(loginRequest.getPassword())) {

                    String jwt = jwtUtil.generateToken(loginRequest.getEmail());

                    return ResponseEntity.ok(new LoginResponse(
                        jwt,
                        ADMIN_ID,
                        ADMIN_EMAIL,
                        "Admin",
                        "User"
                    ));
                } else {
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("Invalid admin credentials");
                }
            }
            else if ("CUSTOMER".equalsIgnoreCase(loginRequest.getUserType())) {
                Authentication authentication = authenticationManager
                    .authenticate(new UsernamePasswordAuthenticationToken(
                        loginRequest.getEmail(),
                        loginRequest.getPassword()));

                String jwt = jwtUtil.generateToken(loginRequest.getEmail());

                Customer customer = customerRepository.findByEmail(loginRequest.getEmail())
                    .orElseThrow(() -> new RuntimeException("Customer not found"));

                return ResponseEntity.ok(new LoginResponse(
                    jwt,
                    customer.getId(),
                    customer.getEmail(),
                    customer.getFirstName(),
                    customer.getLastName(),
                    customer.getGender()
                ));
            } else {
                return ResponseEntity.badRequest()
                    .body("Invalid user type. Must be CUSTOMER or ADMIN");
            }
        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body("Invalid email or password");
        }
    }

    @PostMapping("/register")
    @Operation(summary = "User registration", description = "Register a new user and return JWT token")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful registration", content = @Content(schema = @Schema(implementation = LoginResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid request"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest registerRequest) {
        try {
            if (customerRepository.existsByEmail(registerRequest.getEmail())) {
                return ResponseEntity.badRequest()
                    .body("Email is already in use!");
            }

            if (customerRepository.existsByNationalId(registerRequest.getNationalId())) {
                return ResponseEntity.badRequest()
                    .body("National ID is already in use!");
            }

            Customer customer = Customer.builder()
                .firstName(registerRequest.getFirstName())
                .lastName(registerRequest.getLastName())
                .gender(registerRequest.getGender())
                .email(registerRequest.getEmail())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .phone(registerRequest.getPhone())
                .address(registerRequest.getAddress())
                .nationalId(registerRequest.getNationalId())
                .status(Customer.CustomerStatus.ACTIVE)
                .build();

            Customer savedCustomer = customerRepository.save(customer);

            String jwt = jwtUtil.generateToken(savedCustomer.getEmail());

            return ResponseEntity.ok(new LoginResponse(
                jwt,
                savedCustomer.getId(),
                savedCustomer.getEmail(),
                savedCustomer.getFirstName(),
                savedCustomer.getLastName(),
                savedCustomer.getGender()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Registration failed: " + e.getMessage());
        }
    }

    @PostMapping("/logout")
    @Operation(summary = "User logout", description = "Logout the authenticated user")
    public ResponseEntity<?> logout() {
        return ResponseEntity.ok("Logged out successfully");
    }
}
