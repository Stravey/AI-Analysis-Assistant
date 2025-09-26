package org.example.airesumescoring.controller;

import lombok.RequiredArgsConstructor;
import org.example.airesumescoring.dto.RoleDTO;
import org.example.airesumescoring.model.Role;
import org.example.airesumescoring.service.UserRoleService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user-roles")
@RequiredArgsConstructor
public class UserRoleController {
    private final UserRoleService userRoleService;

    @GetMapping("/users/{userId}")
    public ResponseEntity<List<RoleDTO>> getUserRoles(@PathVariable Long userId) {
        List<Role> roles = userRoleService.getUserRoles(userId);
        List<RoleDTO> roleDTOs = roles.stream()
                .map(role -> new RoleDTO(role.getId(), role.getName()))
                .toList();
        return ResponseEntity.ok(roleDTOs);
    }

    @PostMapping
    public ResponseEntity<Void> assignRoleToUser(@RequestParam Long userId, @RequestParam Long roleId) {
        userRoleService.assignRoleToUser(userId, roleId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping
    public ResponseEntity<Void> removeRoleFromUser(@RequestParam Long userId, @RequestParam Long roleId) {
        userRoleService.removeRoleFromUser(userId, roleId);
        return ResponseEntity.noContent().build();
    }
    
    @DeleteMapping("/users/{userId}/all")
    public ResponseEntity<Void> removeAllRolesFromUser(@PathVariable Long userId) {
        userRoleService.removeAllRolesFromUser(userId);
        return ResponseEntity.noContent().build();
    }
}

