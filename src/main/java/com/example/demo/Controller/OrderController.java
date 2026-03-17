package com.example.demo.Controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class OrderController {

    @GetMapping("/order")
    @ResponseBody
    public String order(Authentication authentication) {
        return "Order page for " + authentication.getName();
    }
}
