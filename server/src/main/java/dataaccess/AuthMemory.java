package dataaccess;

import model.AuthData;
import java.util.Optional;
import java.util.HashMap;

public class AuthMemory implements AuthDAO {

    private final HashMap<String, AuthData> authStore = new HashMap<>();

    @Override
    public void insertAuth(AuthData auth) throws DataAccessException {
        if (auth == null) {
            throw new DataAccessException("Auth data cannot be null");
        }
        String username = auth.username();
        if (authStore.containsKey(username)) {
            throw new DataAccessException("Auth data for username " + username + " already exists");
        }
        authStore.put(username, auth);
    }

    @Override
    public Optional<AuthData> getAuthByUsername(String username) throws DataAccessException {
        if (username == null || username.isEmpty()) {
            throw new DataAccessException("Invalid username");
        }
        AuthData auth = authStore.get(username);
        return Optional.ofNullable(auth);
    }

    @Override
    public Optional<AuthData> getAuthByToken(String authToken) throws DataAccessException {
        for (AuthData auth : authStore.values()) {
            if (auth.authToken().equals(authToken)) {
                return Optional.of(auth);
            }
        }
        return Optional.empty();
    }

    @Override
    public void updateAuth(AuthData auth) throws DataAccessException {
        if (auth == null) {
            throw new DataAccessException("Auth data cannot be null");
        }
        String username = auth.username();
        if (!authStore.containsKey(username)) {
            throw new DataAccessException("No auth data found for username " + username);
        }
        authStore.put(username, auth);
    }

    @Override
    public void deleteAuth(String username) throws DataAccessException {
        if (username == null || username.isEmpty()) {
            throw new DataAccessException("Invalid username");
        }
        if (!authStore.containsKey(username)) {
            throw new DataAccessException("No auth data found for username " + username);
        }
        authStore.remove(username);
    }

    @Override
    public void clear() throws DataAccessException { authStore.clear(); }

}
