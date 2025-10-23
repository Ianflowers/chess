package service;

import dataaccess.*;
import model.AuthData;
import model.UserData;
import request.*;
import result.*;

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
            throw new BadRequestException();
        }

        Optional<UserData> userOpt = userDAO.getUserByUsername(request.username());

        if (userOpt.isEmpty()) {
            throw new UnauthorizedException();
        }
        if (!userOpt.get().password().equals(request.password())) {
            throw new UnauthorizedException();
        }

        String authToken = UUID.randomUUID().toString();
        AuthData authData = new AuthData(authToken, request.username());
        authDAO.insertAuth(authData);
        return new LoginResult(request.username(), authToken);
    }

    public LogoutResult logout(String authToken) throws DataAccessException {
        if (authToken == null || authToken.isEmpty()) {
            throw new UnauthorizedException();
        }

        Optional<AuthData> authOpt = authDAO.getAuthByToken(authToken);

        if (authOpt.isEmpty()) {
            throw new UnauthorizedException();
        }

        authDAO.deleteAuth(authToken);
        return new LogoutResult("Logout successful");
    }

}
