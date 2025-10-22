package dataaccess;

import model.UserData;
import java.util.Optional;

public interface UserDAO {

    void insertUser(UserData user) throws DataAccessException;
    Optional<UserData> getUserByUsername(String username) throws DataAccessException;
    void clear() throws DataAccessException;

}
