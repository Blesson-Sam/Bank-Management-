package com.sam.bankmanagement.Bank.Management.Application.controller;

import com.sam.bankmanagement.Bank.Management.Application.dto.LoginRequest;
import com.sam.bankmanagement.Bank.Management.Application.dto.LoginResponse;
import com.sam.bankmanagement.Bank.Management.Application.dto.RegisterRequest;
import com.sam.bankmanagement.Bank.Management.Application.entity.Customer;
import com.sam.bankmanagement.Bank.Management.Application.repository.CustomerRepository;
import com.sam.bankmanagement.Bank.Management.Application.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Auth Controller Tests")
class AuthControllerTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthController authController;

    private LoginRequest customerLoginRequest;
    private LoginRequest adminLoginRequest;
    private RegisterRequest registerRequest;
    private Customer customer;

    @BeforeEach
    void setUp() {
        customerLoginRequest = new LoginRequest();
        customerLoginRequest.setEmail("customer@test.com");
        customerLoginRequest.setPassword("password123");
        customerLoginRequest.setUserType("CUSTOMER");

        adminLoginRequest = new LoginRequest();
        adminLoginRequest.setEmail("admin@bankapp.com");
        adminLoginRequest.setPassword("admin123");
        adminLoginRequest.setUserType("ADMIN");

        registerRequest = new RegisterRequest();
        registerRequest.setFirstName("John");
        registerRequest.setLastName("Doe");
        registerRequest.setEmail("john.doe@test.com");
        registerRequest.setPassword("password123");
        registerRequest.setPhone("+1234567890");
        registerRequest.setAddress("123 Test St");
        registerRequest.setNationalId("ID123456789");
        registerRequest.setGender("MALE"); // Added gender field

        customer = Customer.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .email("customer@test.com")
                .password("hashedPassword")
                .phone("+1234567890")
                .address("123 Test St")
                .nationalId("ID123456789")
                .status(Customer.CustomerStatus.ACTIVE)
                .gender("MALE") // Added gender field
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Should login customer successfully")
    void loginCustomer_Success() throws Exception {
        Authentication authentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(jwtUtil.generateToken(anyString())).thenReturn("jwt-token");
        when(customerRepository.findByEmail(anyString())).thenReturn(Optional.of(customer));

        ResponseEntity<?> response = authController.login(customerLoginRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof LoginResponse);

        LoginResponse loginResponse = (LoginResponse) response.getBody();
        assertEquals("jwt-token", loginResponse.getToken());
        assertEquals("CUSTOMER", loginResponse.getUserType());
        assertEquals("customer@test.com", loginResponse.getEmail());

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtUtil).generateToken("customer@test.com");
        verify(customerRepository).findByEmail("customer@test.com");
    }

    @Test
    @DisplayName("Should login admin successfully")
    void loginAdmin_Success() throws Exception {
        when(jwtUtil.generateToken(anyString())).thenReturn("admin-jwt-token");

        ResponseEntity<?> response = authController.login(adminLoginRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof LoginResponse);

        LoginResponse loginResponse = (LoginResponse) response.getBody();
        assertEquals("admin-jwt-token", loginResponse.getToken());
        assertEquals("ADMIN", loginResponse.getUserType());
        assertEquals("admin@bankapp.com", loginResponse.getEmail());
        assertEquals("Admin", loginResponse.getFirstName());
        assertEquals("User", loginResponse.getLastName());

        verify(jwtUtil).generateToken("admin@bankapp.com");
        verify(authenticationManager, never()).authenticate(any());
    }

    @Test
    @DisplayName("Should fail admin login with wrong credentials")
    void loginAdmin_WrongCredentials() throws Exception {
        LoginRequest wrongAdminLogin = new LoginRequest();
        wrongAdminLogin.setEmail("admin@bankapp.com");
        wrongAdminLogin.setPassword("wrongpassword");
        wrongAdminLogin.setUserType("ADMIN");

        ResponseEntity<?> response = authController.login(wrongAdminLogin);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Invalid admin credentials", response.getBody());

        verify(jwtUtil, never()).generateToken(anyString());
    }

    @Test
    @DisplayName("Should register customer successfully")
    void register_Success() throws Exception {
        when(customerRepository.existsByEmail(anyString())).thenReturn(false);
        when(customerRepository.existsByNationalId(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashedPassword");
        when(customerRepository.save(any(Customer.class))).thenReturn(customer);
        when(jwtUtil.generateToken(anyString())).thenReturn("jwt-token");

        ResponseEntity<?> response = authController.register(registerRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof LoginResponse);

        LoginResponse loginResponse = (LoginResponse) response.getBody();
        assertEquals("jwt-token", loginResponse.getToken());
        assertEquals("customer@test.com", loginResponse.getEmail());
        assertEquals("John", loginResponse.getFirstName());
        assertEquals("Doe", loginResponse.getLastName());

        verify(customerRepository).existsByEmail("john.doe@test.com");
        verify(customerRepository).existsByNationalId("ID123456789");
        verify(passwordEncoder).encode("password123");
        verify(customerRepository).save(any(Customer.class));
        verify(jwtUtil).generateToken("customer@test.com");
    }

    @Test
    @DisplayName("Should fail registration with duplicate email")
    void register_DuplicateEmail() throws Exception {
        when(customerRepository.existsByEmail(anyString())).thenReturn(true);

        ResponseEntity<?> response = authController.register(registerRequest);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Email is already in use!", response.getBody());

        verify(customerRepository).existsByEmail("john.doe@test.com");
        verify(customerRepository, never()).save(any(Customer.class));
    }

    @Test
    @DisplayName("Should fail registration with duplicate national ID")
    void register_DuplicateNationalId() throws Exception {
        when(customerRepository.existsByEmail(anyString())).thenReturn(false);
        when(customerRepository.existsByNationalId(anyString())).thenReturn(true);

        ResponseEntity<?> response = authController.register(registerRequest);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("National ID is already in use!", response.getBody());

        verify(customerRepository).existsByNationalId("ID123456789");
        verify(customerRepository, never()).save(any(Customer.class));
    }

    @Test
    @DisplayName("Should logout successfully")
    void logout_Success() throws Exception {
        ResponseEntity<?> response = authController.logout();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Logged out successfully", response.getBody());
    }

    @Test
    @DisplayName("Should fail login with invalid user type")
    void login_InvalidUserType() throws Exception {
        LoginRequest invalidTypeRequest = new LoginRequest();
        invalidTypeRequest.setEmail("test@test.com");
        invalidTypeRequest.setPassword("password123");
        invalidTypeRequest.setUserType("INVALID");

        ResponseEntity<?> response = authController.login(invalidTypeRequest);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Invalid user type. Must be CUSTOMER or ADMIN", response.getBody());

        verify(authenticationManager, never()).authenticate(any());
        verify(jwtUtil, never()).generateToken(anyString());
    }
}
