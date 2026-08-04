package com.example.exam.service;

import org.springframework.stereotype.Service;

import com.example.exam.dto.RegisterRequest;
import com.example.exam.model.User;

@Service
public class AuthService {

    public User mapRegisterRequestToUser(RegisterRequest request) {
        User user = new User();
        user.setUsername(request.getEmail());
        user.setEmail(request.getEmail());
        user.setMobileNumber(request.getMobileNumber());
        user.setPassword(request.getPassword());
        user.setProfilePicUrl(request.getProfilePhoto());
        String fullName = String.format("%s %s %s",
                request.getFirstName() != null ? request.getFirstName().trim() : "",
                request.getMiddleName() != null ? request.getMiddleName().trim() : "",
                request.getLastName() != null ? request.getLastName().trim() : ""
        ).replaceAll("\\s+", " ").trim();
        user.setFullName(fullName);
        return user;
    }

}

