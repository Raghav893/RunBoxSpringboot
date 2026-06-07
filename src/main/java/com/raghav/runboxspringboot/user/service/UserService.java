package com.raghav.runboxspringboot.user.service;

import com.raghav.runboxspringboot.user.repo.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
}
