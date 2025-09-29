package com.vcbackend.controller;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.vcbackend.DTO.userDTO;
import com.vcbackend.exceptions.UserException;
import com.vcbackend.mapper.userMapper;
import com.vcbackend.model.User;
import com.vcbackend.repository.UserRepository;
import com.vcbackend.service.UserService;
import com.vcbackend.type.UserRole;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
public class UserController {
    @Autowired
    UserRepository userRepository;

    @Autowired
    UserService userService;

    @Autowired
    private userMapper userMapper;

    @GetMapping("/api/users")
    public ResponseEntity<List<userDTO>> getUsers(
            @RequestHeader(value = "Authorization", required = false) String jwt) {
        User currentUser = null;
        if (jwt != null && !jwt.isEmpty()) {
            currentUser = userService.findUserByJwt(jwt);
        }

        List<User> users = userRepository.findAllByRoleNot(UserRole.ADMIN);
        List<userDTO> userDTOs = userMapper.toDTOList(users);
        return new ResponseEntity<>(userDTOs, HttpStatus.OK);
    }

    @GetMapping("/api/users/{userId}")
    public ResponseEntity<userDTO> getUserById(
            @PathVariable("userId") Integer id,
            @RequestHeader(value = "Authorization", required = false) String jwt) throws UserException {
        User currentUser = null;
        if (jwt != null && !jwt.isEmpty()) {
            currentUser = userService.findUserByJwt(jwt);
        }

        User user = userService.findUserById(id);
        userDTO userDTO = userMapper.toDTO(user);
        return new ResponseEntity<>(userDTO, HttpStatus.OK);
    }

    @PutMapping("/api/users")
    public ResponseEntity<userDTO> updateUser(
            @RequestHeader("Authorization") String jwt,
            @RequestBody User user) throws UserException {
        User reqUser = userService.findUserByJwt(jwt);
        User updatedUser = userService.updateUser(user, reqUser.getId());
        userDTO userDTO = userMapper.toDTO(updatedUser);
        return new ResponseEntity<>(userDTO, HttpStatus.OK);
    }

    @GetMapping("/api/users/search")
    public ResponseEntity<List<userDTO>> searchUser(
            @RequestParam("query") String param,
            @RequestHeader(value = "Authorization", required = false) String jwt) {
        User currentUser = null;
        if (jwt != null && !jwt.isEmpty()) {
            currentUser = userService.findUserByJwt(jwt);
        }

        List<User> users = userService.searchUser(param);
        List<userDTO> userDTOs = userMapper.toDTOList(users);
        return new ResponseEntity<>(userDTOs, HttpStatus.OK);
    }

    @GetMapping("/api/users/profile")
    public ResponseEntity<userDTO> getUserFromToken(@RequestHeader("Authorization") String jwt) {
        User user = userService.findUserByJwt(jwt);
        userDTO userDTO = userMapper.toDTO(user);
        return new ResponseEntity<>(userDTO, HttpStatus.OK);
    }

    @DeleteMapping("/users/{userId}")
    public ResponseEntity<Map<String, String>> deleteUser(@PathVariable("userId") Integer userId) throws UserException {
        Optional<User> user = userRepository.findById(userId);
        if (user.isEmpty()) {
            throw new UserException("User not found");
        }
        userRepository.delete(user.get());

        Map<String, String> response = new HashMap<>();
        response.put("message", "User deleted successfully with id " + userId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
