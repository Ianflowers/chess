package service;

import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import dataaccess.UserDAO;
import model.AuthData;
import model.UserData;
import request.LoginRequest;
import request.LogoutRequest;
import result.LoginResult;
import result.LogoutResult;

import java.util.Optional;
import java.util.UUID;

public class AuthService {

    private final UserDAO userDAO;
    private final AuthDAO authDAO;

    public AuthService(UserDAO userDAO, AuthDAO authDAO) {
        this.userDAO = userDAO;
        this.authDAO = authDAO;
    }

    public LoginResult login(LoginRequest request) throws DataAccessException {
        if (request == null || request.username() == null || request.password() == null) {
            throw new DataAccessException("Error: bad request");
        }

        Optional<UserData> userOpt = userDAO.getUserByUsername(request.username());

        if (userOpt.isEmpty() || !userOpt.get().password().equals(request.password())) {
            throw new DataAccessException("Error: unauthorized");
        }

        String authToken = UUID.randomUUID().toString();
        AuthData authData = new AuthData(authToken, request.username());
        authDAO.insertAuth(authData);
        return new LoginResult(request.username(), authToken);
    }


    public LogoutResult logout(LogoutRequest request) throws DataAccessException {
        if (request == null || request.authToken() == null || request.authToken().isEmpty()) {
            throw new DataAccessException("Error: unauthorized");
        }

        Optional<AuthData> authOpt = authDAO.getAuthByToken(request.authToken());

        if (authOpt.isEmpty()) {
            throw new DataAccessException("Error: unauthorized");
        }

        authDAO.deleteAuth(request.authToken());
        return new LogoutResult("Logout successful");
    }

}
