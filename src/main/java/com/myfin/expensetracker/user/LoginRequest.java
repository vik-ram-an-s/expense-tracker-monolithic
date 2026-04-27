package com.myfin.expensetracker.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class LoginRequest {

    @NotBlank(message = "Phone no cannot be empty")
    @Pattern(regexp = "^[0-9]{10}$")
    private String mobile;

    @NotBlank(message = "Password cannot be empty")
    private String password;
}
