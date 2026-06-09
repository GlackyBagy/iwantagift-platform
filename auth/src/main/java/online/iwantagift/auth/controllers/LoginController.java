package online.iwantagift.auth.controllers;

import lombok.RequiredArgsConstructor;
import online.iwantagift.auth.config.IwagProperties;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class LoginController {
    private final IwagProperties iwagProperties;

    @GetMapping("/login")
    public String login(Model model) {
        model.addAttribute("uiBaseUrl", iwagProperties.getUiBaseUrl());
        return "login";
    }
}
