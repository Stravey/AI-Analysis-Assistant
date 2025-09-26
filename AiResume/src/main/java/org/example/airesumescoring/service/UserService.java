package org.example.airesumescoring.service;

import org.example.airesumescoring.model.Users;
import org.example.airesumescoring.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Users authenticate(String username, String password) {
        Users user = userRepository.findByUsername(username);
        if (user != null && password.equals(user.getPassword())) {
            return user;
        }
        return null;
    }

    public List<Users> getAllUsers() {
        return userRepository.findAll();
    }

    public Users getUserById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    public Users createUser(Users user) {
        return userRepository.save(user);
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    public boolean registerUser(String username, String password, String email) {
        if (userRepository.findByUsername(username) != null) {
            return false;
        }

        Users newUser = new Users();
        newUser.setUsername(username);
        newUser.setPassword(password);
        newUser.setEmail(email);
        userRepository.save(newUser);
        return true;
    }

    public boolean loginUser(String username, String password) {
        Users user = userRepository.findByUsername(username);
        if (user == null) {
            System.out.println("用户不存在: " + username);
            return false;
        }

        boolean isMatch = password.equals(user.getPassword());
        System.out.println("密码验证结果: " + isMatch);
        return isMatch;
    }

    public Users getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }
}