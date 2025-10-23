package dataaccess;

import model.AuthData;
import java.util.Optional;
import java.util.HashMap;

public class AuthMemory implements AuthDAO {

    private final HashMap<String, AuthData> authStore = new HashMap<>();

    @Override
    public void insertAuth(AuthData auth) throws DataAccessException {
        if (auth == null) { throw new BadRequestException(); }
        String token = auth.authToken();
        if (authStore.containsKey(token)) { throw new ForbiddenException();}
        authStore.put(token, auth);
    }

    @Override
    public Optional<AuthData> getAuthByToken(String authToken) throws DataAccessException {
        if (authToken == null || authToken.isEmpty()) { throw new UnauthorizedException(); }
        return Optional.ofNullable(authStore.get(authToken));
    }

    @Override
    public void deleteAuth(String authToken) throws DataAccessException {
        if (authToken == null || authToken.isEmpty()) { throw new UnauthorizedException(); }
        if (!authStore.containsKey(authToken)) { throw new UnauthorizedException(); }
        authStore.remove(authToken);
    }

    @Override
    public void clear() throws DataAccessException { authStore.clear(); }

}
