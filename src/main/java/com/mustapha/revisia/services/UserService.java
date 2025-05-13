package com.mustapha.revisia.services;

import com.mustapha.revisia.models.User;
import java.util.List;

public interface UserService {
    void registerUser(String username, String password, String email);
    boolean authenticateUser(String username, String password);
    User getUserByUsername(String username);
    List<User> getAllUsers();
    void updateUser(User user);
    void deleteUser(User user);
}