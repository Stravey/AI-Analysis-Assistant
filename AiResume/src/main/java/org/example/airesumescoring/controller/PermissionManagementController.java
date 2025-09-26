package org.example.airesumescoring.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/permissions")
public class PermissionManagementController {

    @GetMapping
    public String permissionManagementPage() {
        return "permission-management";
    }
}