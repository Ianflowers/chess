package dataaccess;

import model.AuthData;
import java.util.Optional;

public interface AuthDAO {

    void insertAuth(AuthData auth) throws DataAccessException;
    Optional<AuthData> getAuthByToken(String authToken) throws DataAccessException;
    void deleteAuth(String authToken) throws DataAccessException;
    public void clear() throws DataAccessException;

}
