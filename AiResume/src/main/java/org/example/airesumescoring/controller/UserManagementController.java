package org.example.airesumescoring.controller;

import org.example.airesumescoring.model.Users;
import org.example.airesumescoring.repository.UserRepository;
import org.example.airesumescoring.util.JwtTokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/admin")
public class UserManagementController {

    private final UserRepository userRepository;
    private final JwtTokenUtil jwtTokenUtil;

    @Autowired
    public UserManagementController(UserRepository userRepository, JwtTokenUtil jwtTokenUtil) {
        this.userRepository = userRepository;
        this.jwtTokenUtil = jwtTokenUtil;
    }

    @GetMapping("/users")
    public String userManagementPage(Model model) {
        // 前端JavaScript会负责检查和验证token
        // 控制器只负责返回页面和数据，不进行token验证
        List<Users> users = userRepository.findAll();
        model.addAttribute("users", users);
        return "user-management";
    }
    
    // 从请求头中提取token的辅助方法
    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }

    @PostMapping("/add-user")
    @ResponseBody
    public ResponseEntity<?> addUser(@RequestBody Users user) {
        try {
            System.out.println("接收到添加用户请求: " + user.toString());

            // 检查必填字段
            if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("message", "用户名不能为空"));
            }
            if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("message", "密码不能为空"));
            }

            // 检查用户名是否已存在
            if (userRepository.findByUsername(user.getUsername()) != null) {
                return ResponseEntity.badRequest().body(Map.of("message", "用户名已存在"));
            }

            Users savedUser = userRepository.save(user);
            return ResponseEntity.ok(Map.of(
                    "message", "用户添加成功",
                    "user", Map.of(
                            "id", savedUser.getId(),
                            "username", savedUser.getUsername(),
                            "email", savedUser.getEmail()
                    )
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", "添加失败: " + e.getMessage()));
        }
    }

    @GetMapping("/edit-user")
    @ResponseBody
    public ResponseEntity<?> getEditUserData(@RequestParam("id") String idParam) {
        try {
            System.out.println("接收到编辑用户请求，ID参数: " + idParam);

            // 转换ID为Long类型
            Long id;
            try {
                id = Long.parseLong(idParam);
            } catch (NumberFormatException e) {
                System.out.println("ID格式错误: " + idParam);
                return ResponseEntity.badRequest().body("ID格式错误");
            }

            System.out.println("转换后的ID: " + id);

            Optional<Users> userOptional = userRepository.findById(id);
            if (userOptional.isPresent()) {
                Users user = userOptional.get();
                System.out.println("找到用户: " + user.getUsername());
                return ResponseEntity.ok(user);
            } else {
                System.out.println("用户不存在，ID: " + id);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            System.out.println("获取用户信息异常: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("服务器错误: " + e.getMessage());
        }
    }

    @PostMapping("/edit-user")
    @ResponseBody
    public ResponseEntity<String> updateUser(@RequestBody Users user) {
        try {
            if (user.getId() == null) {
                return ResponseEntity.badRequest().body("更新失败: 用户ID不能为空");
            }

            Optional<Users> existingUserOpt = userRepository.findById(user.getId());
            if (existingUserOpt.isEmpty()) {
                return ResponseEntity.badRequest().body("更新失败: 用户不存在");
            }

            Users existingUser = existingUserOpt.get();
            existingUser.setUsername(user.getUsername());
            existingUser.setEmail(user.getEmail());

            if (user.getPassword() != null && !user.getPassword().isEmpty()) {
                existingUser.setPassword(user.getPassword());
            }

            userRepository.save(existingUser);
            return ResponseEntity.ok("用户更新成功");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("更新失败: " + e.getMessage());
        }
    }

    @RequestMapping(value = "/delete-user/{id}", method = {RequestMethod.DELETE, RequestMethod.GET})
    @ResponseBody
    public ResponseEntity<?> deleteUser(@PathVariable("id") Long id) {
        try {
            userRepository.deleteById(id);
            return ResponseEntity.ok(Map.of("message", "用户删除成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", "删除失败: " + e.getMessage()));
        }
    }
}