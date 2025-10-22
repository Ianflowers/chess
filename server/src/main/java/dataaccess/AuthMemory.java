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
        String token = auth.authToken();
        if (authStore.containsKey(token)) {
            throw new DataAccessException("Auth token " + token + " already exists");
        }
        authStore.put(token, auth);
    }

    @Override
    public Optional<AuthData> getAuthByToken(String authToken) throws DataAccessException {
        if (authToken.isEmpty()) { throw new DataAccessException("Invalid or expired auth token"); }

        for (AuthData auth : authStore.values()) {
            if (auth.authToken().equals(authToken)) {
                return Optional.of(auth);
            }
        }
        return Optional.empty();
    }

    @Override
    public void deleteAuth(String authToken) throws DataAccessException {
        if (authToken == null || authToken.isEmpty()) {
            throw new DataAccessException("Invalid auth token");
        }
        if (!authStore.containsKey(authToken)) {
            throw new DataAccessException("No auth data found for auth token " + authToken);
        }
        authStore.remove(authToken);
    }

    @Override
    public void clear() throws DataAccessException { authStore.clear(); }

}
