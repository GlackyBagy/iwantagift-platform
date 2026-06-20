package online.iwantagift.auth.controllers;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import online.iwantagift.auth.config.IwagProperties;
import online.iwantagift.auth.models.dto.LoginForm;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class LoginController {
    private static final String FORM_ATTRIBUTE = "loginForm";

    private final IwagProperties iwagProperties;

    private String uiBaseUrl;

    @PostConstruct
    private void init(){
        uiBaseUrl = iwagProperties.requireService("ui").requireBaseUrl().toString();
    }

    @GetMapping("/login")
    public String login(@RequestParam(required = false) String error,
                        @RequestParam(required = false) String oauthError,
                        Model model) {
        model.addAttribute("uiBaseUrl", uiBaseUrl);

        LoginForm form = new LoginForm();
        model.addAttribute(FORM_ATTRIBUTE, form);

        // Bad-credentials failures (the form login redirects here with ?error). Reported as a
        // global error so the template can show a single shared message via #fields.globalErrors().
        if (error != null) {
            BindingResult bindingResult = new BeanPropertyBindingResult(form, FORM_ATTRIBUTE);
            bindingResult.reject("login.failed", "Invalid email or password.");
            model.addAttribute(BindingResult.MODEL_KEY_PREFIX + FORM_ATTRIBUTE, bindingResult);
        }

        // OAuth2 protocol failures bounced back from the ui (state mismatch, token/userinfo errors).
        // The template surfaces this as a JS alert; see login.html.
        model.addAttribute("oauthError", oauthError != null);

        return "login";
    }
}
