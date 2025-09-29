package com.vcbackend.service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.vcbackend.config.JwtProvider;
import com.vcbackend.exceptions.UserException;
import com.vcbackend.model.User;
import com.vcbackend.repository.UserRepository;
import com.vcbackend.request.RegisterRequest;
import com.vcbackend.type.UserRole;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class UserServiceImp implements UserService {

    @Autowired
    private EmailService emailService;

    @Autowired
    UserRepository userRepository;

    @Value("${app.verification.token-expiry}")
    private long tokenExpiryMs;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public User findUserByEmail(String email) {
        Optional<User> user = userRepository.findByEmail(email);
        if (user.isEmpty()) {
            return null;
        } else {
            return user.get();
        }
    }

    @Override
    public User findUserByPhoneNumber(String phoneNumber) {
        User user = userRepository.findByPhoneNumber(phoneNumber);
        return user;
    }

    @Override
    public User findUserById(Integer userId) throws UserException {
        Optional<User> user = userRepository.findById(userId);
        if (user.isPresent()) {
            return user.get();
        } else {
            throw new UserException("User not found");
        }
    }

    private String generateVerificationToken() {
        return UUID.randomUUID().toString();
    }

    @Override
    public User registerUser(RegisterRequest user) throws Exception {
        // findByEmail trả về Optional<User> nên không cần check != null
        Optional<User> isExist = userRepository.findByEmail(user.getEmail());
        if (isExist.isPresent()) {
            throw new Exception("User already exists");
        }
        User newUser = new User();
        String token = generateVerificationToken();
        newUser.setFullName(user.getFullName());
        newUser.setEmail(user.getEmail());
        newUser.setRole(UserRole.USER);
        newUser.setPassword(passwordEncoder.encode(user.getPassword()));
        newUser.setVerificationToken(token);
        newUser.setTokenExpiry(LocalDateTime.now().plus(tokenExpiryMs, ChronoUnit.MILLIS));

        User savedUser = userRepository.save(newUser);
        emailService.sendVerificationEmailWithAsync(
                user.getEmail(),
                token,
                user.getFullName()).exceptionally(ex -> {
                    log.error("Email sending failed for user: " + user.getEmail(), ex);
                    // Có thể thêm logic retry hoặc notification
                    return null;
                });
        return savedUser;
    }

    @Override
    public List<User> searchUser(String query) {
        return userRepository.searchUsers(query);
    }

    @Override
    public User updateUser(User user, Integer userId) throws UserException {
        Optional<User> user1 = userRepository.findById(userId);
        if (user1.isEmpty()) {
            throw new UserException("User not found");
        }

        User oldUser = user1.get();

        if (user.getFullName() != null) {
            oldUser.setFullName(user.getFullName());
        }

        if (user.getEmail() != null) {
            oldUser.setEmail(user.getEmail());
        }

        if (user.getGender() != null) {
            oldUser.setGender(user.getGender());
        }

        User updatedUser = userRepository.save(oldUser);
        return updatedUser;
    }

    @Override
    public User findUserByJwt(String Jwt) {
        String email = JwtProvider.getEmailFromJwtToken(Jwt);
        Optional<User> user = userRepository.findByEmail(email);
        if (user.isEmpty()) {
            return null;
        } else {
            return user.get();
        }
    }

    @Override
    public User getUserById(Integer id) {
        return userRepository.findById(id).orElse(null);
    }

    @Override
    public boolean verifyEmail(String token) {
        Optional<User> userOpt = userRepository.findByVerificationToken(token);

        if (userOpt.isEmpty()) {
            return false; // Token không tồn tại
        }

        User user = userOpt.get();

        // Kiểm tra token đã hết hạn
        if (user.getTokenExpiry().isBefore(LocalDateTime.now()) || user.getIsVerified()) {
            return false; // Token đã hết hạn hoặc email đã được xác nhận trước đó
        }

        // Cập nhật trạng thái verified
        user.setIsVerified(true);
        user.setVerificationToken(null);
        user.setTokenExpiry(null);
        userRepository.save(user);

        return true;
    }

    @Override
    public void resendVerificationEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User không tồn tại"));

        if (user.getIsVerified()) {
            throw new RuntimeException("Email đã được xác nhận");
        }

        // Tạo token mới
        String newToken = generateVerificationToken();
        user.setVerificationToken(newToken);
        user.setTokenExpiry(LocalDateTime.now().plus(tokenExpiryMs, ChronoUnit.MILLIS));
        userRepository.save(user);

        // Gửi lại email
        emailService.sendVerificationEmailWithAsync(
                user.getEmail(),
                newToken,
                user.getFullName()).exceptionally(ex -> {
                    log.error("Email sending failed for user: " + user.getEmail(), ex);
                    // Có thể thêm logic retry hoặc notification
                    return null;
        });
    }

}