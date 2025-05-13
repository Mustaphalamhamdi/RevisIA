package com.mustapha.revisia.services;

import com.mustapha.revisia.dao.UserDAO;
import com.mustapha.revisia.dao.UserDAOImpl;
import com.mustapha.revisia.models.User;

import java.util.List;

public class UserServiceImpl implements UserService {

    private final UserDAO userDAO = new UserDAOImpl();

    @Override
    public void registerUser(String username, String password, String email) {
        // In a real application, you would hash the password before saving
        User newUser = new User(username, password, email);
        userDAO.saveUser(newUser);
    }

    @Override
    public boolean authenticateUser(String username, String password) {
        return userDAO.validateLogin(username, password);
    }

    @Override
    public User getUserByUsername(String username) {
        return userDAO.getUserByUsername(username);
    }

    @Override
    public List<User> getAllUsers() {
        return userDAO.getAllUsers();
    }

    @Override
    public void updateUser(User user) {
        userDAO.updateUser(user);
    }

    @Override
    public void deleteUser(User user) {
        userDAO.deleteUser(user);
    }
}