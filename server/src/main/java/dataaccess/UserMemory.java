package dataaccess;

import model.UserData;
import java.util.Optional;
import java.util.HashMap;

public class UserMemory implements UserDAO {

    private final HashMap<String, UserData> usersStore = new HashMap<>();

    @Override
    public void insertUser(UserData user) throws DataAccessException {
        if (user == null) { throw new BadRequestException(); }
        String username = user.username();
        if (usersStore.containsKey(username)) { throw new ForbiddenException(); }
        usersStore.put(username, user);
    }

    @Override
    public Optional<UserData> getUserByUsername(String username) throws DataAccessException {
        if (username == null || username.isEmpty()) { throw new BadRequestException(); }
        return Optional.ofNullable(usersStore.get(username));
    }

    @Override
    public void clear() throws DataAccessException { usersStore.clear(); }

}
