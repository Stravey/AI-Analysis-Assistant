package org.example.airesumescoring.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.example.airesumescoring.model.Users;
import org.example.airesumescoring.repository.UserRepository;
import org.example.airesumescoring.repository.UserRoleRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;

    public AuthService(UserRepository userRepository, UserRoleRepository userRoleRepository) {
        this.userRepository = userRepository;
        this.userRoleRepository = userRoleRepository;
    }

    public boolean authenticate(String username, String password, HttpServletRequest request) {
        Users user = userRepository.findByUsername(username);
        if (user == null) {
            return false;
        }

        boolean isAuthenticated = password.equals(user.getPassword());

        if (isAuthenticated) {
            // 查询用户角色
            List<String> roles = userRoleRepository.findByUser(user)
                    .stream()
                    .map(userRole -> userRole.getRole().getName())
                    .collect(Collectors.toList());

            // 创建会话
            HttpSession session = request.getSession();
            session.setAttribute("username", username);
            session.setAttribute("userId", user.getId());
            session.setAttribute("userEmail", user.getEmail());
            session.setAttribute("userRoles", roles); // 添加角色信息
            session.setMaxInactiveInterval(30 * 60);
        }

        return isAuthenticated;
    }
}