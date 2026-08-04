package com.example.exam.service;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.exam.dto.UserDTO;
import com.example.exam.mapper.UserMapper;
import com.example.exam.model.User;
import com.example.exam.repository.UserRepository;

@Service
public class UserServiceImpl implements UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void saveStudent(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole("ROLE_STUDENT");
        userRepository.save(user);
        logger.info("Student registered: username='{}', fullName='{}'", user.getUsername(), user.getFullName());
    }

    @Override
    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
    }

    @Override
    public Optional<UserDTO> findUserDTOByUsername(String username) {
        User user = findByUsername(username);
        return Optional.ofNullable(UserMapper.toDTO(user));
    }
}

