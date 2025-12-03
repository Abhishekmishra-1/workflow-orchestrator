package com.workflow.userservice.dto;

import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateDto {
    @Email(message = "Email must be valid")
    private String email;

    private Set<Long> roleIds;
}

