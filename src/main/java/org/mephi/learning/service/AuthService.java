package org.mephi.learning.service;

import java.util.Locale;
import org.mephi.learning.api.Requests.RegisterInput;
import org.mephi.learning.api.Views.UserView;
import org.mephi.learning.entity.Role;
import org.mephi.learning.entity.User;
import org.mephi.learning.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
    private final UserRepository users;
    private final PasswordEncoder encoder;

    public AuthService(UserRepository users, PasswordEncoder encoder) {
        this.users = users;
        this.encoder = encoder;
    }

    @Transactional
    public UserView register(RegisterInput input) {
        String email = input.email().trim().toLowerCase(Locale.ROOT);
        if (users.existsByEmail(email))
            throw DomainException.conflict("Email is already registered");
        User user = new User();
        user.setName(input.name().trim());
        user.setEmail(email);
        user.setRole(Role.STUDENT);
        user.setPasswordHash(encoder.encode(input.password()));
        User saved = users.save(user);
        return new UserView(saved.getId(), saved.getName(), saved.getEmail(), saved.getRole());
    }

    @Transactional(readOnly = true)
    public UserView viewByEmail(String email) {
        User user =
                users.findByEmail(email.trim().toLowerCase(Locale.ROOT))
                        .orElseThrow(() -> DomainException.missing("User", email));
        return new UserView(user.getId(), user.getName(), user.getEmail(), user.getRole());
    }
}
