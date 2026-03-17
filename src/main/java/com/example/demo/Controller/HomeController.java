package com.example.demo.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.demo.model.Student;

@Controller
public class HomeController {

    @GetMapping("/")
    public String index() {
        return "redirect:/home";
    }

    @GetMapping("/home")
    @ResponseBody
    public String home(Authentication authentication) {
        return "Hello, " + authentication.getName();
    }

    @GetMapping("/demo")
    public String demoPage(Model model) {
        Student student = new Student(1L, "Nguyễn Văn A");
        model.addAttribute("student", student);
        model.addAttribute("message", "Welcome Thymeleaf!");
        return "demo";
    }
}
