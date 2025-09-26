package org.example.airesumescoring.service;

import lombok.RequiredArgsConstructor;
import org.example.airesumescoring.exception.ResourceNotFoundException;
import org.example.airesumescoring.model.Permission;
import org.example.airesumescoring.repository.PermissionRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PermissionService {
    private final PermissionRepository permissionRepository;

    public Permission createPermission(Permission permission) {
        if (permissionRepository.existsByName(permission.getName())) {
            throw new IllegalArgumentException("权限名称已存在");
        }
        return permissionRepository.save(permission);
    }

    public Permission updatePermission(Long id, Permission permissionDetails) {
        Permission permission = permissionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("权限不存在"));

        if (!permission.getName().equals(permissionDetails.getName()) &&
                permissionRepository.existsByName(permissionDetails.getName())) {
            throw new IllegalArgumentException("权限名称已存在");
        }

        permission.setName(permissionDetails.getName());
        permission.setDisplayName(permissionDetails.getDisplayName());
        permission.setDescription(permissionDetails.getDescription());
        permission.setUpdatedAt(LocalDateTime.now());

        return permissionRepository.save(permission);
    }

    public void deletePermission(Long id) {
        Permission permission = permissionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("权限不存在"));

        // 检查是否有角色或用户关联该权限
        // 这里需要添加检查逻辑

        permissionRepository.delete(permission);
    }

    public List<Permission> getAllPermissions() {
        return permissionRepository.findAll();
    }

    public Permission getPermissionById(Long id) {
        return permissionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("权限不存在"));
    }
}
