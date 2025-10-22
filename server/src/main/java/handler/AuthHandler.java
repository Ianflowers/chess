package handler;

import com.google.gson.Gson;
import io.javalin.http.Handler;
import request.*;
import service.*;

public class AuthHandler {

    public final Handler loginUser;
    public final Handler logoutUser;

    public AuthHandler(AuthService authService, Gson gson) {

        // User Login - POST /user
        this.loginUser = ctx -> {
            var request = gson.fromJson(ctx.body(), LoginRequest.class);
            var result = authService.login(request);
            ctx.json(result);
        };

        // User Logout - DELETE /session
        this.logoutUser = ctx -> {
            String token = ctx.header("Authorization");
            if (token == null || token.isEmpty()) {
                ctx.status(401).result("Missing Authorization header");
                return;
            }
            var result = authService.logout(new LogoutRequest(token));
            ctx.json(result);
        };

    }

}
