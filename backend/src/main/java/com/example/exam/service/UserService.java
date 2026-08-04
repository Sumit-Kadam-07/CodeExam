package com.example.exam.service;

import com.example.exam.dto.UserDTO;
import com.example.exam.model.User;

import java.util.Optional;

public interface UserService {
    void saveStudent(User user);

    User findByUsername(String username);

    // DTO-based lookup (additive; does not affect existing MVC/auth behavior)
    Optional<UserDTO> findUserDTOByUsername(String username);
}

