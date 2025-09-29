package com.vcbackend.service;

import java.util.List;

import com.vcbackend.exceptions.UserException;
import com.vcbackend.model.User;
import com.vcbackend.request.RegisterRequest;

public interface UserService {
    public User registerUser(RegisterRequest user) throws Exception;

    public User findUserById(Integer userId) throws UserException;

    public User findUserByEmail(String email);

    public User findUserByPhoneNumber(String phoneNumber);

    public User updateUser(User user, Integer userId) throws UserException;

    public List<User> searchUser(String query);

    public User findUserByJwt(String jwt);

    public User getUserById(Integer id);

    public boolean verifyEmail(String token);

    public void resendVerificationEmail(String email);

}
