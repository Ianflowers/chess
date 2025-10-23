package service;

import dataaccess.AuthMemory;
import dataaccess.DataAccessException;
import dataaccess.UnauthorizedException;
import dataaccess.UserMemory;
import model.UserData;
import org.junit.jupiter.api.*;
import request.*;
import result.*;

import static org.junit.jupiter.api.Assertions.*;

public class AuthServiceTest {

    private AuthService authService;

    @BeforeEach
    void setUp() throws DataAccessException {
        UserMemory userDAO = new UserMemory();
        AuthMemory authDAO = new AuthMemory();
        authService = new AuthService(userDAO, authDAO);

        UserData user = new UserData("tester", "password123", "test@example.com");
        userDAO.insertUser(user);
    }

    // Login Tests
    @Test
    void loginSuccess() throws DataAccessException {
        LoginRequest request = new LoginRequest("tester", "password123");
        LoginResult result = authService.login(request);

        assertNotNull(result);
        assertEquals("tester", result.username());
        assertNotNull(result.authToken());
    }

    @Test
    void loginWrongPasswordThrowsException() {
        LoginRequest request = new LoginRequest("tester", "wrong-password");
        DataAccessException exception = assertThrows(DataAccessException.class, () -> authService.login(request));

        assertEquals("Error: unauthorized", exception.getMessage());
    }

    @Test
    void loginUserNotFoundThrowsException() {
        LoginRequest request = new LoginRequest("nonexistent", "password123");
        UnauthorizedException exception = assertThrows(UnauthorizedException.class, () -> authService.login(request));

        assertEquals("Error: unauthorized", exception.getMessage());
    }

    @Test
    void loginMissingFieldsThrowsException() {
        LoginRequest request = new LoginRequest(null, null);
        DataAccessException exception = assertThrows(DataAccessException.class, () -> authService.login(request));

        assertEquals("Error: bad request", exception.getMessage());
    }

     // Logout Tests
    @Test
    void logoutSuccess() throws DataAccessException {
        LoginRequest loginRequest = new LoginRequest("tester", "password123");
        LoginResult loginResult = authService.login(loginRequest);
        String authToken = loginResult.authToken();
        LogoutResult logoutResult = authService.logout(authToken);

        assertNotNull(logoutResult);
        assertEquals("Logout successful", logoutResult.message());


    }

    @Test
    void logoutInvalidThrowsException() {
        String invalidAuthToken = "nonexistent-token";
        UnauthorizedException exception = assertThrows(UnauthorizedException.class, () -> authService.logout(invalidAuthToken));

        assertEquals("Error: unauthorized", exception.getMessage());
    }

}
