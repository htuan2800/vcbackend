package com.vcbackend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.vcbackend.model.User;
import com.vcbackend.type.UserRole;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

    Optional<User> findByEmail(String email);

    User findByPhoneNumber(String phoneNumber);

    List<User> findAllByRoleNot(UserRole role); 

    Optional<User> findByVerificationToken(String token);

    @Query("SELECT u FROM User u WHERE u.fullName LIKE %:query%")
    public List<User> searchUsers(@Param("query") String query);
}
 