package dataaccess;

import model.AuthData;
import java.util.Optional;

public interface AuthDAO {

    void insertAuth(AuthData auth) throws DataAccessException;
    Optional<AuthData> getAuthByUsername(String username) throws DataAccessException;
    Optional<AuthData> getAuthByToken(String authToken) throws DataAccessException;
    void updateAuth(AuthData auth) throws DataAccessException;
    void deleteAuth(String username) throws DataAccessException;

}
