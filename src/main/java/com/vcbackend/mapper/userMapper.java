package com.vcbackend.mapper;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.vcbackend.DTO.userDTO;
import com.vcbackend.model.User;

@Component
public class userMapper {
    public userDTO toDTO(User user) {
        if (user == null)
            return null;

        userDTO dto = new userDTO();
        dto.setId(user.getId());
        dto.setFullName(user.getFullName());
        dto.setEmail(user.getEmail());
        dto.setGender(user.getGender());
        dto.setAvatar(user.getAvatar());
        dto.setCoverPhotoURL(user.getCoverPhotoURL());
        dto.setRole(user.getRole());
        dto.setCreatedAt(user.getCreatedAt());
        return dto;
    }

    public List<userDTO> toDTOList(List<User> users) {
        if (users == null)
            return Collections.emptyList();

        return users.stream()
                .map(user -> toDTO(user))
                .collect(Collectors.toList());
    }
}