package org.example.airesumescoring.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.airesumescoring.model.Permission;
import org.example.airesumescoring.model.UserRole;
import org.example.airesumescoring.model.Users;
import org.example.airesumescoring.repository.PermissionRepository;
import org.example.airesumescoring.repository.UserPermissionRepository;
import org.example.airesumescoring.repository.UserRepository;
import org.example.airesumescoring.repository.UserRoleRepository;
import org.example.airesumescoring.util.JwtTokenUtil;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.List;

@Component
public class PermissionInterceptor implements HandlerInterceptor {
    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final UserPermissionRepository userPermissionRepository;
    private final PermissionRepository permissionRepository;
    private final JwtTokenUtil jwtTokenUtil;

    public PermissionInterceptor(UserRepository userRepository,
                                 UserRoleRepository userRoleRepository,
                                 UserPermissionRepository userPermissionRepository,
                                 PermissionRepository permissionRepository, JwtTokenUtil jwtTokenUtil) {
        this.userRepository = userRepository;
        this.userRoleRepository = userRoleRepository;
        this.userPermissionRepository = userPermissionRepository;
        this.permissionRepository = permissionRepository;
        this.jwtTokenUtil = jwtTokenUtil;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 获取请求路径和方法
        String requestURI = request.getRequestURI();
        String method = request.getMethod();

        // 解析需要的权限
        String requiredPermission = resolveRequiredPermission(requestURI, method);
        if (requiredPermission == null) {
            return true; // 不需要权限验证的路径
        }

        // 获取当前用户
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "未授权");
            return false;
        }

        String token = authHeader.substring(7);
        // 这里应该有解析token获取用户ID的逻辑
        Long userId = parseUserIdFromToken(token); // 伪代码，需要实现

        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        // 检查用户是否有权限
        if (hasPermission(user, requiredPermission)) {
            return true;
        }

        response.sendError(HttpServletResponse.SC_FORBIDDEN, "没有权限");
        return false;
    }


    private boolean hasPermission(Users user, String permissionName) {
        // 1. 检查用户是否直接拥有该权限
        Permission permission = permissionRepository.findByName(permissionName)
                .orElseThrow(() -> new RuntimeException("权限不存在"));

        if (userPermissionRepository.existsByUserAndPermission(user, permission)) {
            return true;
        }

        // 2. 检查用户角色是否拥有该权限
        List<UserRole> userRoles = userRoleRepository.findByUser(user);
        for (UserRole userRole : userRoles) {
            if (userRole.getRole().getPermissions().stream()
                    .anyMatch(p -> p.getName().equals(permissionName))) {
                return true;
            }
        }

        return false;
    }

    private String resolveRequiredPermission(String requestURI, String method) {
        if (requestURI.startsWith("/admin/permissions")) {
            return "admin:access";
        }
        // 其他路径的权限检查
        return null;
    }

    private Long parseUserIdFromToken(String token) {
        try {
            return jwtTokenUtil.getUserIdFromToken(token);
        } catch (Exception e) {
            // 其他可能的异常处理
            System.out.println("Error parsing JWT token: " + e.getMessage());
            return null;
        }
    }
}