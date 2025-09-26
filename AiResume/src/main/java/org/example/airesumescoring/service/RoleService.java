package org.example.airesumescoring.service;

import lombok.RequiredArgsConstructor;
import org.example.airesumescoring.exception.ResourceNotFoundException;
import org.example.airesumescoring.model.Role;
import org.example.airesumescoring.repository.RoleRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RoleService {
    private final RoleRepository roleRepository;

    public Role createRole(Role role) {
        if (roleRepository.existsByName(role.getName())) {
            throw new IllegalArgumentException("角色名称已存在");
        }
        return roleRepository.save(role);
    }

    public Role updateRole(Long id, Role roleDetails) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("角色不存在"));

        if (!role.getName().equals(roleDetails.getName()) &&
                roleRepository.existsByName(roleDetails.getName())) {
            throw new IllegalArgumentException("角色名称已存在");
        }

        role.setName(roleDetails.getName());
        role.setDisplayName(roleDetails.getDisplayName());
        role.setDescription(roleDetails.getDescription());
        role.setUpdatedAt(LocalDateTime.now());

        return roleRepository.save(role);
    }

    public void deleteRole(Long id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("角色不存在"));

        // 检查是否有用户关联该角色
        // 这里需要添加检查逻辑

        roleRepository.delete(role);
    }

    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }

    public Role getRoleById(Long id) {
        return roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("角色不存在"));
    }
}
