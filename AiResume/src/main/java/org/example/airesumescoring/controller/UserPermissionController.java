package org.example.airesumescoring.controller;

import lombok.RequiredArgsConstructor;
import org.example.airesumescoring.dto.PermissionResponse;
import org.example.airesumescoring.exception.ResourceNotFoundException;
import org.example.airesumescoring.model.Permission;
import org.example.airesumescoring.service.UserPermissionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/user-permissions")
@RequiredArgsConstructor
public class UserPermissionController {
    private final UserPermissionService userPermissionService;

    @GetMapping("/users/{userId}")
    public ResponseEntity<List<PermissionResponse>> getUserPermissions(@PathVariable Long userId) {
        List<Permission> permissions = userPermissionService.getUserPermissions(userId);
        List<PermissionResponse> response = permissions.stream()
                .map(PermissionResponse::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<Void> assignPermissionToUser(@RequestParam Long userId, @RequestParam Long permissionId) {
        userPermissionService.assignPermissionToUser(userId, permissionId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping
    public ResponseEntity<Void> removePermissionFromUser(@RequestParam Long userId, @RequestParam Long permissionId) {
        userPermissionService.removePermissionFromUser(userId, permissionId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/users/{userId}")
    public ResponseEntity<?> updateUserPermissions(
            @PathVariable Long userId,
            @RequestBody List<Long> permissionIds) {

        try {

            if (userId == null) {
                return ResponseEntity.badRequest().body("用户ID不能为空");
            }
            if (permissionIds == null || permissionIds.isEmpty()) {
                return ResponseEntity.badRequest().body("权限ID列表不能为空");
            }

            userPermissionService.assignPermissionsToUser(userId, permissionIds);
            return ResponseEntity.ok().build();

        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("服务器内部错误");
        }
    }
}