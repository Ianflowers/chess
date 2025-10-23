package service;

import dataaccess.*;
import model.AuthData;
import model.UserData;
import request.*;
import result.*;

import java.util.UUID;

public class UserService {

    private final UserDAO userDAO;
    private final AuthDAO authDAO;


    public UserService(UserDAO userDAO, AuthDAO authDAO) {
        this.userDAO = userDAO;
        this.authDAO = authDAO;
    }

    public UserResult registerUser(UserRequest request) throws DataAccessException {
        if (request == null
                || request.username() == null || request.username().isEmpty()
                || request.password() == null || request.password().isEmpty()
                || request.email() == null || request.email().isEmpty()) {
            throw new BadRequestException();
        }

        if (userDAO.getUserByUsername(request.username()).isPresent()) {
            throw new ForbiddenException();
        }

        UserData user = new UserData(request.username(), request.password(), request.email());
        userDAO.insertUser(user);

        String authToken = UUID.randomUUID().toString();
        AuthData authData = new AuthData(authToken, user.username());
        authDAO.insertAuth(authData);

        return new UserResult(user.username(), authToken);
    }

}
