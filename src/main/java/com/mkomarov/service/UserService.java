package com.mkomarov.service;

import com.mkomarov.entity.UserEntity;
import com.mkomarov.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;

public class UserService {
    private final UserRepository userRepository;

    public UserService() {
        this.userRepository = new UserRepository("users");
    }

    public void registerUser(HttpServletRequest req) {
        UserEntity user = new UserEntity();
        user.setEmail(req.getAttribute("email").toString());
        user.setPasswordHash(req.getAttribute("hashedPassword").toString());

        userRepository.create(user);
    }
}
