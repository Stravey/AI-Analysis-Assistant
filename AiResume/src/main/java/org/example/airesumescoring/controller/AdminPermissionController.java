package org.example.airesumescoring.controller;


import lombok.RequiredArgsConstructor;
import org.example.airesumescoring.model.Permission;
import org.example.airesumescoring.model.Users;
import org.example.airesumescoring.service.PermissionService;
import org.example.airesumescoring.service.UserPermissionService;
import org.example.airesumescoring.service.UserService;
import org.example.airesumescoring.util.JwtTokenUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/permissions")
@RequiredArgsConstructor
public class AdminPermissionController {

    private final UserPermissionService userPermissionService;
    private final PermissionService permissionService;
    private final UserService userService;
    private final JwtTokenUtil jwtTokenUtil;

    // 获取当前用户的权限（需要从token中解析用户ID）
    @GetMapping("/my-permissions")
    public ResponseEntity<List<Permission>> getCurrentUserPermissions(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        // 直接返回测试数据或所有权限，跳过token验证
        return ResponseEntity.ok(permissionService.getAllPermissions());
        
    }

    // 获取所有权限（用于权限选择器）
    @GetMapping("/all")
    public ResponseEntity<List<Permission>> getAllPermissions() {
        return ResponseEntity.ok(permissionService.getAllPermissions());
    }

    // 获取所有用户（用于权限分配）
    @GetMapping("/users")
    public ResponseEntity<List<Users>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    // 为用户分配权限（支持批量）
    @PostMapping("/assign")
    public ResponseEntity<Void> assignPermissionsToUser(
            @RequestParam Long userId,
            @RequestBody List<Long> permissionIds) {
        userPermissionService.assignPermissionsToUser(userId, permissionIds);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    private Long parseUserIdFromToken(String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                throw new IllegalArgumentException("无效的Authorization头格式");
            }
            String token = authHeader.substring(7).trim();
            return jwtTokenUtil.getUserIdFromToken(token);
        } catch (Exception e) {
            throw new IllegalArgumentException("token解析失败: " + e.getMessage());
        }
    }

}
