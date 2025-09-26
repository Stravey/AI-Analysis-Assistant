package org.example.airesumescoring.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.airesumescoring.exception.ResourceNotFoundException;
import org.example.airesumescoring.model.Role;
import org.example.airesumescoring.model.UserRole;
import org.example.airesumescoring.model.Users;
import org.example.airesumescoring.repository.RoleRepository;
import org.example.airesumescoring.repository.UserRepository;
import org.example.airesumescoring.repository.UserRoleRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserRoleService {
    private final UserRoleRepository userRoleRepository;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;

    public void assignRoleToUser(Long userId, Long roleId) {
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("用户不存在"));
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("角色不存在"));

        if (userRoleRepository.existsByUserAndRole(user, role)) {
            throw new IllegalArgumentException("用户已拥有该角色");
        }

        UserRole userRole = new UserRole();
        userRole.setUser(user);
        userRole.setRole(role);
        userRoleRepository.save(userRole);
    }

    public void removeRoleFromUser(Long userId, Long roleId) {
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("用户不存在"));
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("角色不存在"));

        userRoleRepository.deleteByUserAndRole(user, role);
    }

    public List<Role> getUserRoles(Long userId) {
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("用户不存在"));

        return userRoleRepository.findByUser(user).stream()
                .map(UserRole::getRole)
                .collect(Collectors.toList());
    }

    @Transactional
    public void removeAllRolesFromUser(Long userId) {
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("用户不存在"));

        userRoleRepository.deleteByUser(user.getId());
    }
}