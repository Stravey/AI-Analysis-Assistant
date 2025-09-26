package org.example.airesumescoring.dto;

import lombok.Data;
import org.example.airesumescoring.model.Role;

import java.util.List;

@Data
public class LoginResponse {
    private String token;
    private Long id;
    private String username;
    private String email;
    private List<Role> roles;
}
