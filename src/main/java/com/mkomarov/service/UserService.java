package com.mkomarov.service;

import com.mkomarov.entity.UserEntity;
import com.mkomarov.repository.UserRepository;

import java.util.Optional;

public class UserService {
    private final UserRepository userRepository;

    public UserService() {
        this.userRepository = new UserRepository("users");
    }

    public Optional<UserEntity> getUserById(long id) {
        return Optional.ofNullable(userRepository.getById(id));
    }
}
