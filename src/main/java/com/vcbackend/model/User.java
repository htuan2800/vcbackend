package com.vcbackend.model;
import java.time.LocalDateTime;
import java.util.List;

import com.vcbackend.type.Gender;
import com.vcbackend.type.UserRole;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(nullable = false)
    private String fullName;
    
    @Column(nullable = true, unique = true)
    private String email;
    
    @Column(nullable = false)
    private String password;

    @Column(name = "is_verified")
    private Boolean isVerified = false;

    @Column(name = "AvatarURL", nullable = true)
    private String avatar;

    @Column(name = "PhoneNumber", nullable = true, unique = true)
    private String phoneNumber;
    
    // private String gender;

    @Column(name = "CoverPhotoURL", nullable = true)
    private String coverPhotoURL;

    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "Role", nullable = true)
    private UserRole role;

    @Column(name="gender", nullable = true)
    private Gender gender;

    @Column(name = "is_active")
    private Boolean isActive = true;

    // Relationships

    @OneToMany(mappedBy = "donor", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Donation> donations;
}
