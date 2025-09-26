package org.example.airesumescoring.controller;

import org.example.airesumescoring.component.TokenBlacklist;
import org.example.airesumescoring.model.Users;
import org.example.airesumescoring.repository.UserPermissionRepository;
import org.example.airesumescoring.repository.UserRepository;
import org.example.airesumescoring.repository.UserRoleRepository;
import org.example.airesumescoring.service.UserService;
import org.example.airesumescoring.util.JwtTokenUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;

    private final JwtTokenUtil jwtTokenUtil;

    private final TokenBlacklist tokenBlacklist;

    private final UserRepository userRepository;

    private final UserRoleRepository userRoleRepository;

    private final UserPermissionRepository userPermissionRepository;


    public AuthController(UserService userService, JwtTokenUtil jwtTokenUtil, TokenBlacklist tokenBlacklist, UserRepository userRepository, UserRoleRepository userRoleRepository, UserPermissionRepository userPermissionRepository) {
        this.userService = userService;
        this.jwtTokenUtil = jwtTokenUtil;
        this.tokenBlacklist = tokenBlacklist;
        this.userRepository = userRepository;
        this.userRoleRepository = userRoleRepository;
        this.userPermissionRepository = userPermissionRepository;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> user) {
        String username = user.get("username");
        String password = user.get("password");
        String email = user.get("email");
        boolean success = userService.registerUser(username, password, email);

        Map<String, Object> result = new HashMap<>();
        if (success) {
            result.put("message", "注册成功");
            return ResponseEntity.ok(result);
        } else {
            result.put("message", "用户名已存在");
            return ResponseEntity.badRequest().body(result);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
        String username = credentials.get("username");
        String password = credentials.get("password");

        Users user = userService.authenticate(username, password);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "用户名或密码错误"));
        }

        // 查询用户角色
        List<String> roles = userRoleRepository.findByUser(user)
                .stream()
                .map(userRole -> userRole.getRole().getName())
                .toList();

        // 查询用户权限（使用UserPermissionRepository）
        List<String> permissions = userPermissionRepository.findByUser(user)
                .stream()
                .map(userPermission -> userPermission.getPermission().getName())
                .toList();

        String token = jwtTokenUtil.generateToken(username);

        return ResponseEntity.ok(Map.of(
                "token", token,
                "user", Map.of(
                        "id", user.getId(),
                        "username", user.getUsername(),
                        "email", user.getEmail(),
                        "roles", roles,
                        "permissions", permissions
                ),
                "expiresIn", jwtTokenUtil.getExpiration()
        ));
    }

    @GetMapping("/verify")
    public ResponseEntity<?> verifyToken(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).build();
        }

        String token = authHeader.substring(7);
        if (jwtTokenUtil.validateToken(token)) {
            String username = jwtTokenUtil.getUsernameFromToken(token);
            Users user = userService.getUserByUsername(username);

            // 查询用户角色
            List<String> roles = userRoleRepository.findByUser(user)
                    .stream()
                    .map(userRole -> userRole.getRole().getName())
                    .toList();

            // 查询用户权限（使用UserPermissionRepository）
            List<String> permissions = userPermissionRepository.findByUser(user)
                    .stream()
                    .map(userPermission -> userPermission.getPermission().getName())
                    .toList();

            return ResponseEntity.ok(Map.of(
                    "valid", true,
                    "user", Map.of(
                            "id", user.getId(),
                            "username", user.getUsername(),
                            "email", user.getEmail(),
                            "roles", roles,
                            "permissions", permissions
                    )
            ));
        }
        return ResponseEntity.ok(Map.of("valid", false));
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of(
                            "error", "未授权",
                            "message", "需要认证令牌"
                    ));
        }

        String token = authHeader.substring(7);

        try {
            if (!jwtTokenUtil.validateToken(token)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of(
                                "error", "无效令牌",
                                "message", "令牌验证失败"
                        ));
            }

            String username = jwtTokenUtil.getUsernameFromToken(token);
            if (username == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of(
                                "error", "无效令牌",
                                "message", "令牌中未包含用户信息"
                        ));
            }

            Users user = userRepository.findByUsername(username);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of(
                                "error", "用户不存在",
                                "message", "令牌中的用户未找到"
                        ));
            }

            // 查询用户角色
            List<String> roles = userRoleRepository.findByUser(user)
                    .stream()
                    .map(userRole -> userRole.getRole().getName())
                    .collect(Collectors.toList());

            // 查询用户权限（使用UserPermissionRepository）
            List<String> permissions = userPermissionRepository.findByUser(user)
                    .stream()
                    .map(userPermission -> userPermission.getPermission().getName())
                    .collect(Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            response.put("username", user.getUsername());
            response.put("email", user.getEmail());
            response.put("roles", roles);
            response.put("permissions", permissions);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of(
                            "error", "无效令牌",
                            "message", e.getMessage()
                    ));
        }
    }


    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).build();
        }

        String token = authHeader.substring(7);
        if (jwtTokenUtil.canTokenBeRefreshed(token)) {
            String username = jwtTokenUtil.getUsernameFromToken(token);
            String newToken = jwtTokenUtil.refreshToken(token);

            return ResponseEntity.ok(Map.of(
                    "token", newToken,
                    "expiresIn", jwtTokenUtil.getExpiration()
            ));
        }
        return ResponseEntity.status(401).build();
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader("Authorization") String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            tokenBlacklist.add(token);
        }
        return ResponseEntity.ok(Map.of("message", "登出成功"));
    }

}