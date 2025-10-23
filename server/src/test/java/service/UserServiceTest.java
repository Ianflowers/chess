package service;

import dataaccess.AuthMemory;
import dataaccess.DataAccessException;
import dataaccess.UserMemory;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import static org.junit.jupiter.api.Assertions.*;

import request.UserRequest;
import result.UserResult;

import java.util.stream.Stream;

public class UserServiceTest {

    private UserService userService;

    @BeforeEach
    void setUp() { userService = new UserService(new UserMemory(), new AuthMemory()); }

    //  Register Tests
    @Test
    void registerUser_success() throws DataAccessException {
        UserRequest request = new UserRequest("newuser", "password123", "email@example.com");
        UserResult result = userService.registerUser(request);

        assertNotNull(result);
        assertNotNull(result.username());
        assertEquals("newuser", result.username());
    }

    @Test
    void registerUser_userAlreadyExists_throwsException() throws DataAccessException {
        UserRequest request = new UserRequest("existinguser", "pass", "mail@x.com");
        userService.registerUser(request);
        DataAccessException ex = assertThrows(DataAccessException.class, () -> { userService.registerUser(request); });

        assertEquals(ex.getMessage(), "Error: already taken");
    }

    @ParameterizedTest
    @MethodSource("invalidRequests")
    void registerUser_invalidFields_throwsException(UserRequest request) {
        assertThrows(DataAccessException.class, () -> { userService.registerUser(request); });
    }

    private static Stream<UserRequest> invalidRequests() {
        return Stream.of(
                new UserRequest(null, "password123", "email@example.com"),
                new UserRequest("", "password123", "email@example.com"),
                new UserRequest("username", null, "email@example.com"),
                new UserRequest("username", "", "email@example.com"),
                new UserRequest("username", "password123", null),
                new UserRequest("username", "password123", "")
        );
    }





}
