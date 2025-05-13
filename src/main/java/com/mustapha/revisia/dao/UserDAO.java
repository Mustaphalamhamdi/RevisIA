package com.mustapha.revisia.dao;

import com.mustapha.revisia.models.User;
import java.util.List;

public interface UserDAO {
    void saveUser(User user);
    User getUserById(Long id);
    User getUserByUsername(String username);
    List<User> getAllUsers();
    void updateUser(User user);
    void deleteUser(User user);
    boolean validateLogin(String username, String password);
}