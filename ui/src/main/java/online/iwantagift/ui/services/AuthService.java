package online.iwantagift.ui.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    @Value("${iwag.services.auth.url}")
    private String authURL;
}
