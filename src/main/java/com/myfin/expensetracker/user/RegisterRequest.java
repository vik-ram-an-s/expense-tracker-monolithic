package com.myfin.expensetracker.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RegisterRequest {

    @NotBlank
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Password should not be empty")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
            message = "Password must be at least 8 characters and include uppercase, lowercase, number, and special character"
    )
    private String password;

    @NotBlank(message = "Password should not be empty")
    private String confirmPassword;

    @NotBlank(message = "Name should not be empty")
    private String name;

    @NotNull(message = "Currency cannot be empty")
    private Currency currency;

    @NotBlank(message = "Mobile should not be empty")
    @Pattern(regexp = "^[0-9]{10}$")
    private String mobile;

}
