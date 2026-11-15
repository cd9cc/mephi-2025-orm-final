package org.mephi.learning.service;

import jakarta.validation.Valid;
import java.util.*;
import org.mephi.learning.api.Requests.*;
import org.mephi.learning.api.Views;
import org.mephi.learning.api.Views.*;
import org.mephi.learning.entity.*;
import org.mephi.learning.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

@Service
@Validated
@Transactional(readOnly = true)
public class UserService {
    private final UserRepository users;
    private final ProfileRepository profiles;
    private final EntityAccess access;

    public UserService(UserRepository users, ProfileRepository profiles, EntityAccess access) {
        this.users = users;
        this.profiles = profiles;
        this.access = access;
    }

    public List<UserView> list() {
        return users.findAll().stream().map(Views::user).toList();
    }

    public UserView get(Long id) {
        return Views.user(access.user(id));
    }

    @Transactional
    public UserView create(@Valid UserInput input) {
        String email = input.email().trim().toLowerCase(Locale.ROOT);
        if (users.existsByEmail(email))
            throw DomainException.conflict("Email is already registered");
        User u = new User();
        u.setName(input.name().trim());
        u.setEmail(email);
        u.setRole(input.role());
        return Views.user(users.save(u));
    }

    @Transactional
    public UserView update(Long id, @Valid UserInput input) {
        User u = access.user(id);
        String email = input.email().trim().toLowerCase(Locale.ROOT);
        if (!email.equals(u.getEmail()) && users.existsByEmail(email))
            throw DomainException.conflict("Email is already registered");
        if (input.role() != u.getRole())
            throw DomainException.conflict("Role cannot be changed after registration");
        u.setName(input.name().trim());
        u.setEmail(email);
        return Views.user(u);
    }

    @Transactional
    public void delete(Long id) {
        users.delete(access.user(id));
        users.flush();
    }

    public ProfileView profile(Long id) {
        Profile p = access.user(id).getProfile();
        if (p == null) throw DomainException.missing("Profile for user", id);
        return new ProfileView(p.getId(), id, p.getBio(), p.getAvatarUrl());
    }

    @Transactional
    public ProfileView putProfile(Long id, @Valid ProfileInput input) {
        User u = access.user(id);
        Profile p = u.getProfile();
        if (p == null) {
            p = new Profile();
            p.setUser(u);
            u.setProfile(p);
        }
        p.setBio(input.bio());
        p.setAvatarUrl(input.avatarUrl());
        profiles.saveAndFlush(p);
        return new ProfileView(p.getId(), id, p.getBio(), p.getAvatarUrl());
    }
}
