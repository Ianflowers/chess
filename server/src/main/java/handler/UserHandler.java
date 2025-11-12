package handler;

import com.google.gson.Gson;
import io.javalin.http.Handler;
import request.UserRequest;
import result.ErrorResult;
import result.UserResult;
import service.UserService;

public class UserHandler {

    public final Handler registerUser;

    public UserHandler(UserService userService, Gson gson) {

        // User Registration - POST /user
        registerUser = ctx -> {
            UserRequest req;
            try {
                req = gson.fromJson(ctx.body(), UserRequest.class);
            } catch (Exception e) {
                ctx.status(400).json(new ErrorResult("Error: bad request"));
                return;
            }

            if (req == null || req.username() == null || req.password() == null || req.email() == null) {
                ctx.status(400).json(new ErrorResult("Error: bad request"));
                return;
            }

            try {
                UserResult result = userService.registerUser(req);
                ctx.status(200).json(result);
            } catch (Exception e) {
                String msg = e.getMessage() == null ? "" : e.getMessage().toLowerCase();
                if (msg.contains("already taken")) {
                    ctx.status(403).json(new ErrorResult("Error: already taken"));
                } else {
                    ctx.status(500).json(new ErrorResult("Error: " + e.getMessage()));
                }
            }
        };

    }

}
