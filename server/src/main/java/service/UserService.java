package service;

import dataaccess.DataAccessException;
import dataaccess.UserDAO;
import model.UserData;
import request.UserRequest;
import request.UsernameRequest;
import result.UserResult;
import result.SimpleResult;

import java.util.Optional;

public class UserService {

    private final UserDAO userDAO;

    public UserService(UserDAO userDAO) { this.userDAO = userDAO; }

    public UserResult registerUser(UserRequest request) throws DataAccessException {
        if (request == null || request.username() == null || request.username().isEmpty()
                || request.password() == null || request.password().isEmpty()
                || request.email() == null || request.email().isEmpty()) {
            throw new DataAccessException("Invalid user registration request");
        }

        if (userDAO.getUserByUsername(request.username()).isPresent()) {
            throw new DataAccessException("Username already taken");
        }

        UserData user = new UserData(request.username(), request.password(), request.email());
        userDAO.insertUser(user);
        return new UserResult("User registered successfully", user);
    }

    public UserResult getUser(UsernameRequest request) throws DataAccessException {
        if (request == null || request.username() == null || request.username().isEmpty()) {
            throw new DataAccessException("Invalid username request");
        }

        Optional<UserData> userOpt = userDAO.getUserByUsername(request.username());
        if (userOpt.isEmpty()) {
            return new UserResult("User not found");
        }

        return new UserResult("User retrieved successfully", userOpt.get());
    }

    public SimpleResult updateUser(UserRequest request) throws DataAccessException {
        if (request == null || request.username() == null || request.username().isEmpty()
                || request.password() == null || request.password().isEmpty()
                || request.email() == null || request.email().isEmpty()) {
            throw new DataAccessException("Invalid user update request");
        }

        UserData user = new UserData(request.username(), request.password(), request.email());
        userDAO.updateUser(user);
        return new SimpleResult("User updated successfully");
    }

    public SimpleResult deleteUser(UsernameRequest request) throws DataAccessException {
        if (request == null || request.username() == null || request.username().isEmpty()) {
            throw new DataAccessException("Invalid username request");
        }

        userDAO.deleteUser(request.username());
        return new SimpleResult("User deleted successfully");
    }

}
