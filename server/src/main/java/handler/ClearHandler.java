package handler;

import com.google.gson.Gson;
import dataaccess.BadRequestException;
import dataaccess.DataAccessException;
import dataaccess.ForbiddenException;
import dataaccess.UnauthorizedException;
import io.javalin.http.Handler;
import result.ClearResult;
import result.ErrorResult;
import service.ClearService;

public class ClearHandler {

    public final Handler clearAll;

    public ClearHandler(ClearService clearService, Gson gson) {

        // Clear DB - DELETE /db
        clearAll = ctx -> {
            try {
                ClearResult result = clearService.clearAll();
                ctx.status(200).json(result);
            } catch (BadRequestException e) {
                ctx.status(400).json(new ErrorResult("error: bad request"));
            } catch (ForbiddenException e) {
                ctx.status(403).json(new ErrorResult("error: forbidden"));
            } catch (UnauthorizedException e) {
                ctx.status(401).json(new ErrorResult("error: unauthorized"));
            } catch (DataAccessException e) {
                ctx.status(500).json(new ErrorResult("error: " + e.getMessage()));
            }
        };

    }
}
