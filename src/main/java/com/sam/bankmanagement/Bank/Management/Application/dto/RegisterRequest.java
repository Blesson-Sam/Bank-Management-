package com.sam.bankmanagement.Bank.Management.Application.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank
    @Size(min = 2, max = 100)
    private String firstName;

    @NotBlank
    @Size(min = 2, max = 100)
    private String lastName;

    @Email
    @NotBlank
    private String email;

    @NotBlank
    private String gender;

    @NotBlank
    @Size(min = 6, max = 100)
    private String password;

    @NotBlank
    @Size(min = 10, max = 15)
    private String phone;

    @NotBlank
    private String address;

    @NotBlank
    @Size(min = 5, max = 20)
    private String nationalId;
}
