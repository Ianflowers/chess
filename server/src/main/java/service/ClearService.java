package service;

import dataaccess.*;
import result.ClearResult;

public class ClearService {
    private final UserDAO userDAO;
    private final GameDAO gameDAO;
    private final AuthDAO authDAO;

    public ClearService(UserDAO userDAO, GameDAO gameDAO, AuthDAO authDAO) {
        this.userDAO = userDAO;
        this.gameDAO = gameDAO;
        this.authDAO = authDAO;
    }

    public ClearResult clearAll() throws DataAccessException, UnauthorizedException {
        userDAO.clear();
        gameDAO.clear();
        authDAO.clear();
        return new ClearResult("Clear succeeded.");
    }

}
