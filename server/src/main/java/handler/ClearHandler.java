package handler;

import com.google.gson.Gson;
import io.javalin.http.Handler;
import result.ClearResult;
import service.ClearService;

public class ClearHandler {

    public final Handler clearAll;

    public ClearHandler(ClearService clearService, Gson gson) {

        // Clear DB - DELETE /db
        this.clearAll = ctx -> {
            try {
                ClearResult result = clearService.clearAll();
                ctx.status(200).json(result);
            } catch (Exception e) {
                ctx.status(500).result("Internal server error: " + e.getMessage());
            }
        };
    }
}
