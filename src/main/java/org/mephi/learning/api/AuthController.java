package org.mephi.learning.api;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.mephi.learning.api.Requests.LoginInput;
import org.mephi.learning.api.Requests.RegisterInput;
import org.mephi.learning.api.Views.UserView;
import org.mephi.learning.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService auth;
    private final AuthenticationManager manager;
    private final SecurityContextRepository contextRepository;

    public AuthController(
            AuthService auth,
            AuthenticationManager manager,
            SecurityContextRepository contextRepository) {
        this.auth = auth;
        this.manager = manager;
        this.contextRepository = contextRepository;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserView register(@Valid @RequestBody RegisterInput input) {
        return auth.register(input);
    }

    @PostMapping("/login")
    public UserView login(
            @Valid @RequestBody LoginInput input,
            HttpServletRequest request,
            HttpServletResponse response) {
        Authentication authentication =
                manager.authenticate(
                        UsernamePasswordAuthenticationToken.unauthenticated(
                                input.email().trim(), input.password()));
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        contextRepository.saveContext(context, request, response);
        return auth.viewByEmail(authentication.getName());
    }
}
