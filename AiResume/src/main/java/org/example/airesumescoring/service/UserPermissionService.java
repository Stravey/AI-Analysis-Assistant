package org.example.airesumescoring.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.airesumescoring.exception.ResourceNotFoundException;
import org.example.airesumescoring.model.Permission;
import org.example.airesumescoring.model.UserPermission;
import org.example.airesumescoring.model.Users;
import org.example.airesumescoring.repository.PermissionRepository;
import org.example.airesumescoring.repository.UserPermissionRepository;
import org.example.airesumescoring.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserPermissionService {
    private static final Logger log = LoggerFactory.getLogger(UserPermissionService.class);
    private final UserPermissionRepository userPermissionRepository;
    private final PermissionRepository permissionRepository;
    private final UserRepository userRepository;
    private final EntityManager entityManager;

    @Transactional
    public void assignPermissionToUser(Long userId, Long permissionId) {
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("用户不存在"));
        Permission permission = permissionRepository.findById(permissionId)
                .orElseThrow(() -> new ResourceNotFoundException("权限不存在"));

        if (userPermissionRepository.existsByUserAndPermission(user, permission)) {
            throw new IllegalArgumentException("用户已拥有该权限");
        }

        UserPermission userPermission = new UserPermission();
        userPermission.setUser(user);
        userPermission.setPermission(permission);
        userPermissionRepository.save(userPermission);
    }

    @Transactional
    public void assignPermissionsToUser(Long userId, List<Long> permissionIds) {
        // 参数验证
        Objects.requireNonNull(userId, "用户ID不能为空");
        Objects.requireNonNull(permissionIds, "权限ID列表不能为空");

        log.debug("开始为用户 {} 分配权限: {}", userId, permissionIds);

        // 获取用户
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("用户不存在: " + userId));

        // 删除现有权限
        int deletedCount = userPermissionRepository.deleteByUser(user);
        log.debug("已删除 {} 条现有权限", deletedCount);

        // 添加新权限 - 移除不必要的存在性检查
        for (Long permissionId : permissionIds) {
            Permission permission = permissionRepository.findById(permissionId)
                    .orElseThrow(() -> new ResourceNotFoundException("权限不存在: " + permissionId));

            // 直接创建新的权限关联
            UserPermission userPermission = new UserPermission();
            userPermission.setUser(user);
            userPermission.setPermission(permission);
            userPermissionRepository.save(userPermission);
            log.debug("添加权限: {}", permission.getName());
        }

        log.info("成功为用户 {} 分配了 {} 个权限", userId, permissionIds.size());
    }

    @Transactional
    public void removePermissionFromUser(Long userId, Long permissionId) {
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("用户不存在"));
        Permission permission = permissionRepository.findById(permissionId)
                .orElseThrow(() -> new ResourceNotFoundException("权限不存在"));

        userPermissionRepository.deleteByUserAndPermission(user, permission);
    }

    @Transactional
    public List<Permission> getUserPermissions(Long userId) {
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("用户不存在"));

        List<UserPermission> userPermissions = userPermissionRepository.findByUserId(userId);


        // 调试输出
        System.out.println("用户 " + userId + " 的权限记录数量: " + userPermissions.size());
        userPermissions.forEach(up ->
                System.out.println("权限ID: " + up.getPermission().getId())
        );

        return userPermissions.stream()
                .map(UserPermission::getPermission)
                .collect(Collectors.toList());
    }

    @Transactional
    public List<Permission> getUserPermissionsDirect(Long userId) {
        try {
            String sql = "SELECT p.* FROM permissions p " +
                    "INNER JOIN user_permission up ON p.id = up.permission_id " +
                    "WHERE up.user_id = ? " +  // 使用位置参数
                    "ORDER BY p.id";

            Query query = entityManager.createNativeQuery(sql, Permission.class);
            query.setParameter(1, userId); // 设置第一个参数

            @SuppressWarnings("unchecked")
            List<Permission> permissions = query.getResultList();

            System.out.println("原生SQL查询结果: " + permissions.size() + " 条记录");
            return permissions;
        } catch (Exception e) {
            System.err.println("原生SQL查询错误: " + e.getMessage());
            throw new RuntimeException("查询用户权限失败", e);
        }
    }


}
