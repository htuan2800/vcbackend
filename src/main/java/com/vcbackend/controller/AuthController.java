package com.vcbackend.controller;

import java.lang.foreign.Linker.Option;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.HttpHeaders;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.vcbackend.config.JwtProvider;
import com.vcbackend.model.User;
import com.vcbackend.repository.UserRepository;
import com.vcbackend.request.LoginRequest;
import com.vcbackend.request.RegisterRequest;
import com.vcbackend.response.ApiResponse;
import com.vcbackend.response.AuthResponse;
import com.vcbackend.response.ErrorResponse;
import com.vcbackend.response.RegisterResponse;
import com.vcbackend.service.CustomerUserDetailsService;
import com.vcbackend.service.UserService;
import com.vcbackend.type.UserRole;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/auth")
public class AuthController {
    @Autowired
    UserService userService;
    @Autowired
    UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private CustomerUserDetailsService customerUserDetailsService;
    @PostMapping("/signup")
    public RegisterResponse createUser(@RequestBody RegisterRequest user) throws Exception {
        User savedUser = userService.registerUser(user);

        RegisterResponse registerResponse = new RegisterResponse("User registered successfully", savedUser.getVerificationToken(), true);
        return registerResponse;
    }

    @GetMapping("/verify-email")
    public ResponseEntity<?> verifyEmail(@RequestParam String token) {
        boolean isVerified = userService.verifyEmail(token);
        
        if (isVerified) {
            return ResponseEntity.ok(new ApiResponse(
                "Email đã được xác nhận thành công!", 
                true
            ));
        } else {
            return ResponseEntity.badRequest()
                .body(new ApiResponse(
                    "Token không hợp lệ hoặc đã hết hạn hoặc email đã được xác nhận trước đó.", 
                    false
                ));
        }
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<?> resendVerificationEmail(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        userService.resendVerificationEmail(email);
        return ResponseEntity.ok(new ApiResponse("Verification email resent successfully", true));
    }
    

    @PostMapping("/signin")
    public ResponseEntity<?> signin(@RequestBody LoginRequest loginRequest, HttpServletResponse response) {
        Authentication authentication = authenticate(loginRequest.getEmail(), loginRequest.getPassword());
        User user = userService.findUserByEmail(loginRequest.getEmail());

        if (user == null) {
            throw new BadCredentialsException("User not found");
        } else if (!user.getIsVerified()) {
            AuthResponse res = new AuthResponse(null, "Login failed", user.getIsVerified());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(res);
        }

        // Tạo Access Token (ngắn hạn)
        String accessToken = JwtProvider.generateToken(authentication, user.getId());

        // Tạo Refresh Token (dài hạn)
        String refreshToken = JwtProvider.generateRefreshToken(authentication, user.getId());

        // ---- Set refreshToken vào HttpOnly cookie ----
        ResponseCookie cookie = ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true) // Không cho JS truy cập
                .secure(false) // Chỉ gửi qua HTTPS
                .path("/") // Cookie có hiệu lực toàn bộ app
                .maxAge(365 * 24 * 60 * 60) // 365 ngày
                .sameSite("Lax") // Ngăn CSRF (có thể dùng "Lax" nếu cần cross-site)
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString()); //Thêm một header Set-Cookie vào trong HTTP Response.

        // Trả accessToken về cho frontend
        AuthResponse res = new AuthResponse(accessToken, "Login successfully", user.getIsVerified());
        return ResponseEntity.ok(res);
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(HttpServletRequest request, HttpServletResponse response) {
        // Lấy refresh token từ cookie
        Cookie[] cookies = request.getCookies(); // lấy tất cả cookie gửi kèm request
        String refreshToken = null;
        if (cookies != null) {
            for (Cookie c : cookies) {
                System.out.println("Found refresh token in cookie: " + c.getName());
                if (c.getName().equals("refreshToken")) {
                    refreshToken = c.getValue();
                }
            }
        }

        System.out.println("Refresh token from cookie: " + refreshToken);
        if (refreshToken == null || !JwtProvider.validateRefreshToken(refreshToken)) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse("Invalid or expired refresh token", 401));
        }

        // Giải mã refresh token → tạo access token mới
        String userEmail = JwtProvider.getEmailFromJwtToken(refreshToken);
        User user = userService.findUserByEmail(userEmail);

        String newAccessToken = JwtProvider.refreshAccessToken(user);
        return ResponseEntity.ok(new AuthResponse(newAccessToken, "Token refreshed", user.getIsVerified()));
    }

    private Authentication authenticate(String email, String password) {
        UserDetails userDetails = customerUserDetailsService.loadUserByUsername(email);
        if (userDetails == null) {
            throw new BadCredentialsException("Invalid username");
        }
        if (!passwordEncoder.matches(password, userDetails.getPassword())) {
            throw new BadCredentialsException("Password not matched");
        }
        return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletResponse response) {
        ResponseCookie deleteCookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(0) // Xóa ngay lập tức
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, deleteCookie.toString());

        return ResponseEntity.ok("Logged out successfully");
    }
}
