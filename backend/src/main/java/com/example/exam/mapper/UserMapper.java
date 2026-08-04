package com.example.exam.mapper;

import com.example.exam.dto.UserDTO;
import com.example.exam.model.User;

public class UserMapper {

    private UserMapper() {
    }

    public static UserDTO toDTO(User user) {
        if (user == null) {
            return null;
        }

        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setFullName(user.getFullName());
        dto.setEmail(user.getEmail() != null ? user.getEmail() : user.getUsername());
        dto.setRole(user.getRole());
        dto.setProfilePicUrl(user.getProfilePicUrl());
        dto.setMobileNumber(user.getMobileNumber());
        return dto;
    }
}



