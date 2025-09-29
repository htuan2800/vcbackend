package com.vcbackend.DTO;
import java.time.LocalDateTime;

import com.vcbackend.type.Gender;
import com.vcbackend.type.UserRole;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class userDTO {
    private Integer id;
    private String fullName;
    private String email;
    private Gender gender;
    private String avatar;
    private String coverPhotoURL;
    private UserRole role;
    private LocalDateTime createdAt;
    private Boolean isVerified;    
}
