package dataaccess;

import model.UserData;
import java.util.List;
import java.util.Optional;

public interface UserDAO {

    void insertUser(UserData user) throws DataAccessException;
    Optional<UserData> getUserByUsername(String username) throws DataAccessException;
    List<UserData> getAllUsers() throws DataAccessException;
    void updateUser(UserData user) throws DataAccessException;
    void deleteUser(String username) throws DataAccessException;
    public void clear() throws DataAccessException;


}
