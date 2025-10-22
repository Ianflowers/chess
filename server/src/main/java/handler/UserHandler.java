package handler;

import com.google.gson.Gson;
import io.javalin.http.Handler;
import request.*;
import result.*;
import service.UserService;

public class UserHandler {

    public final Handler registerUser;

    public UserHandler(UserService userService, Gson gson) {

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

    }

}
