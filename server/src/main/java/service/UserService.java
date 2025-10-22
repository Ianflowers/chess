package service;

import dataaccess.DataAccessException;
import dataaccess.UserDAO;
import model.UserData;
import request.*;
import result.*;

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

}
