package service;

import dataaccess.AuthMemory;
import dataaccess.DataAccessException;
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
    void login_success() throws DataAccessException {
        LoginRequest request = new LoginRequest("tester", "password123");
        LoginResult result = authService.login(request);

        assertNotNull(result);
        assertEquals("tester", result.username());
        assertNotNull(result.authToken());
    }

    @Test
    void login_wrongPassword_throwsException() {
        LoginRequest request = new LoginRequest("tester", "wrong-password");
        DataAccessException exception = assertThrows(DataAccessException.class, () -> authService.login(request));

        assertEquals("Error: Incorrect password", exception.getMessage());
    }

    @Test
    void login_userNotFound_throwsException() {
        LoginRequest request = new LoginRequest("nonexistent", "password123");
        DataAccessException exception = assertThrows(DataAccessException.class, () -> authService.login(request));

        assertEquals("Error: User not found", exception.getMessage());
    }

    @Test
    void login_missingFields_throwsException() {
        LoginRequest request = new LoginRequest(null, null);
        DataAccessException exception = assertThrows(DataAccessException.class, () -> authService.login(request));

        assertEquals("Error: bad request", exception.getMessage());
    }

    // Logout Tests
    @Test
    void logout_success() throws DataAccessException {
        LoginRequest loginRequest = new LoginRequest("tester", "password123");
        LoginResult loginResult = authService.login(loginRequest);
        String authToken = loginResult.authToken();

        LogoutRequest logoutRequest = new LogoutRequest(authToken);
        LogoutResult logoutResult = authService.logout(logoutRequest);

        assertNotNull(logoutResult);
        assertEquals("Logout successful", logoutResult.message());
    }

    @Test
    void logout_invalid_throwsException() {
        LogoutRequest request = new LogoutRequest("nonexistent-token");
        DataAccessException exception = assertThrows(DataAccessException.class, () -> authService.logout(request));

        assertEquals("Error: unauthorized", exception.getMessage());
    }

}
