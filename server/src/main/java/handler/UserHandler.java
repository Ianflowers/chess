package handler;

import com.google.gson.Gson;
import io.javalin.http.Handler;
import request.*;
import result.*;
import service.*;

public class UserHandler {

    public final Handler registerUser;
    public final Handler loginUser;


    public UserHandler(UserService userService, AuthService authService, Gson gson) {

        // User Registration - POST /user
        this.registerUser = ctx -> {
            try {
                UserRequest request = gson.fromJson(ctx.body(), UserRequest.class);
                UserResult result = userService.registerUser(request);
                ctx.status(200).json(result);
            } catch (Exception e) {
                ctx.status(500).result("Internal server error: " + e.getMessage());
            }
        };


        this.loginUser = ctx -> {
            var request = gson.fromJson(ctx.body(), LoginRequest.class);
            var result = authService.login(request);
            ctx.json(result);
        };




    }
}
