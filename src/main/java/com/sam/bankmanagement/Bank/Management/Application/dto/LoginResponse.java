package com.sam.bankmanagement.Bank.Management.Application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

    private String token;
    private String type = "Bearer";
    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private String gender;
    private String userType;

    public LoginResponse(String token, Long id, String email, String firstName, String lastName, String gender) {
        this.token = token;
        this.type = "Bearer";
        this.id = id;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.gender = gender;
        this.userType = "CUSTOMER";
    }

    public LoginResponse(String token, Long id, String email, String firstName, String lastName) {
        this.token = token;
        this.type = "Bearer";
        this.id = id;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.gender = null;
        this.userType = "ADMIN";
    }
}
