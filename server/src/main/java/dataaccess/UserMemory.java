package dataaccess;

import model.UserData;
import java.util.List;
import java.util.ArrayList;
import java.util.Optional;
import java.util.HashMap;

public class UserMemory implements UserDAO {

    private final HashMap<String, UserData> usersStore = new HashMap<>();

    @Override
    public void insertUser(UserData user) throws DataAccessException {
        if (user == null) {
            throw new DataAccessException("User cannot be null");
        }
        String username = user.username();
        if (usersStore.containsKey(username)) {
            throw new DataAccessException("User with username " + username + " already exists");
        }
        usersStore.put(username, user);
    }

    @Override
    public Optional<UserData> getUserByUsername(String username) throws DataAccessException {
        if (username == null || username.isEmpty()) {
            throw new DataAccessException("Invalid username");
        }
        UserData user = usersStore.get(username);
        return Optional.ofNullable(user);
    }

    @Override
    public List<UserData> getAllUsers() throws DataAccessException {
        return new ArrayList<>(usersStore.values());
    }

    @Override
    public void updateUser(UserData user) throws DataAccessException {
        if (user == null) {
            throw new DataAccessException("User cannot be null");
        }
        String username = user.username();
        if (!usersStore.containsKey(username)) {
            throw new DataAccessException("User with username " + username + " does not exist");
        }
        usersStore.put(username, user);
    }

    @Override
    public void deleteUser(String username) throws DataAccessException {
        if (username == null || username.isEmpty()) {
            throw new DataAccessException("Invalid username");
        }
        if (!usersStore.containsKey(username)) {
            throw new DataAccessException("User with username " + username + " does not exist");
        }
        usersStore.remove(username);
    }
}
