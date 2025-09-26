package org.example.airesumescoring.controller;


import org.example.airesumescoring.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class RegisterController {

    private final UserService userService;

    public RegisterController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam String email,
            Model model) {

        if (userService.registerUser(username, password, email)) {
            return "redirect:/login?registerSuccess";
        } else {
            model.addAttribute("error", "用户名已存在");
            return "register";
        }
    }
}