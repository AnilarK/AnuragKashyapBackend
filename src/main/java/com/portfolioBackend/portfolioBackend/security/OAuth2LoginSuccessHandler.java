package com.portfolioBackend.portfolioBackend.security;

import com.portfolioBackend.portfolioBackend.model.User;
import com.portfolioBackend.portfolioBackend.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

@Component
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    @Value("${app.oauth2.frontend-redirect-url:http://localhost:3000/auth/callback}")
    private String frontendRedirectUrl;

    public OAuth2LoginSuccessHandler(UserRepository userRepository, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                         Authentication authentication) throws IOException, ServletException {
        OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
        Map<String, Object> attributes = oauth2User.getAttributes();

        String email = (String) attributes.get("email");
        String name = (String) attributes.get("name");
        String picture = (String) attributes.get("picture");
        String sub = (String) attributes.get("sub"); // Google subject id

        if (email == null || email.isBlank()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Email not provided by Google");
            return;
        }

        User user = userRepository.findByGoogleSubjectId(sub)
                .or(() -> userRepository.findByEmail(email))
                .orElseGet(() -> {
                    User newUser = User.fromGoogle(email, name, picture, sub);
                    return userRepository.save(newUser);
                });

        if (user.getGoogleSubjectId() == null) {
            user.setGoogleSubjectId(sub);
            user.setName(name != null ? name : user.getName());
            user.setPictureUrl(picture);
            user.setEmailVerified(true);
            user.touch();
            userRepository.save(user);
        }

        String token = jwtUtil.generateToken(user);
        String redirectUrl = frontendRedirectUrl + (frontendRedirectUrl.contains("?") ? "&" : "?") + "token=" + token;
        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }
}
