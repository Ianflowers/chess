package dataaccess;

import model.AuthData;

import java.sql.*;
import java.util.Optional;

public class AuthMySQL implements AuthDAO {

    @Override
    public void insertAuth(AuthData auth) throws DataAccessException {
        if (auth == null) {
            throw new BadRequestException();
        }

        String sql = "INSERT INTO auth (authToken, username) VALUES (?, ?)";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, auth.authToken());
            stmt.setString(2, auth.username());
            stmt.executeUpdate();

        } catch (SQLIntegrityConstraintViolationException e) {
            throw new ForbiddenException("Auth token already exists or invalid username");
        } catch (SQLException e) {
            throw new DataAccessException("Error inserting auth", e);
        }
    }

    @Override
    public Optional<AuthData> getAuthByToken(String authToken) throws DataAccessException {
        if (authToken == null || authToken.isEmpty()) {
            throw new UnauthorizedException();
        }

        String sql = "SELECT authToken, username FROM auth WHERE authToken = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, authToken);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String token = rs.getString("authToken");
                String username = rs.getString("username");
                return Optional.of(new AuthData(token, username));
            } else {
                return Optional.empty();
            }

        } catch (SQLException e) {
            throw new DataAccessException("Error retrieving auth token", e);
        }
    }

    @Override
    public void deleteAuth(String authToken) throws DataAccessException {
        if (authToken == null || authToken.isEmpty()) {
            throw new UnauthorizedException();
        }

        String sql = "DELETE FROM auth WHERE authToken = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, authToken);
            int affectedRows = stmt.executeUpdate();

            if (affectedRows == 0) {
                throw new UnauthorizedException("Auth token not found");
            }

        } catch (SQLException e) {
            throw new DataAccessException("Error deleting auth token", e);
        }
    }

    @Override
    public void clear() throws DataAccessException {
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute("SET FOREIGN_KEY_CHECKS = 0");
            stmt.executeUpdate("TRUNCATE TABLE auth");
            stmt.execute("SET FOREIGN_KEY_CHECKS = 1");

        } catch (SQLException e) {
            throw new DataAccessException("Error clearing auth table", e);
        }
    }

}
