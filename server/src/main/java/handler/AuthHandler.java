package handler;

import com.google.gson.Gson;
import io.javalin.http.Handler;
import request.LoginRequest;
import result.ErrorResult;
import result.LoginResult;
import result.LogoutResult;
import service.AuthService;
import dataaccess.UnauthorizedException;

public class AuthHandler {

    public final Handler loginUser;
    public final Handler logoutUser;

    public AuthHandler(AuthService authService, Gson gson) {

        // User Login - POST /user
        loginUser = ctx -> {
            LoginRequest req;
            try {
                req = gson.fromJson(ctx.body(), LoginRequest.class);
            } catch (Exception e) {
                ctx.status(400).json(new ErrorResult("Error: bad request"));
                return;
            }

            if (req == null || req.username() == null || req.password() == null) {
                ctx.status(400).json(new ErrorResult("Error: bad request"));
                return;
            }

            try {
                LoginResult result = authService.login(req);
                ctx.status(200).json(result);
            } catch (UnauthorizedException e) {
                ctx.status(401).json(new ErrorResult("Error: unauthorized"));
            } catch (Exception e) {
                ctx.status(500).json(new ErrorResult("Error: " + e.getMessage()));
            }
        };

        // User Logout - DELETE /session
        logoutUser = ctx -> {
            String authToken = ctx.header("Authorization");
            if (authToken == null || authToken.trim().isEmpty()) {
                ctx.status(401).json(new ErrorResult("Error: unauthorized"));
                return;
            }
            try {
                LogoutResult result = authService.logout(authToken);
                ctx.status(200).json(result);
            } catch (UnauthorizedException e) {
                ctx.status(401).json(new ErrorResult("Error: unauthorized"));
            } catch (Exception e) {
                ctx.status(500).json(new ErrorResult("Error: " + e.getMessage()));
            }
        };

    }
}
