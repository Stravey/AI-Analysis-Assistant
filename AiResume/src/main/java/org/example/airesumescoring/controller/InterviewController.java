package org.example.airesumescoring.controller;

import org.example.airesumescoring.util.JwtTokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.servlet.http.HttpServletRequest;

@Controller
public class InterviewController {

    private final JwtTokenUtil jwtTokenUtil;
    
    @Autowired
    public InterviewController(JwtTokenUtil jwtTokenUtil) {
        this.jwtTokenUtil = jwtTokenUtil;
    }

    @GetMapping("/interview")
    public String interviewPage() {
        // 前端JavaScript会负责检查和验证token
        // 控制器只负责返回页面，不进行token验证
        return "Interview";
    }
    
    // 从请求头中提取token的辅助方法
    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }
}