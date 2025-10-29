package dataaccess;

import model.UserData;
import java.sql.*;
import java.util.Optional;
import org.mindrot.jbcrypt.BCrypt;

public class UserMySQL implements UserDAO {

    @Override
    public void insertUser(UserData user) throws DataAccessException {
        if (user == null) {
            throw new BadRequestException();
        }

        String sql = "INSERT INTO users (username, password, email) VALUES (?, ?, ?)";
        String hashedPassword = BCrypt.hashpw(user.password(), BCrypt.gensalt());

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, user.username());
            stmt.setString(2, hashedPassword);
            stmt.setString(3, user.email());
            stmt.executeUpdate();

        } catch (SQLIntegrityConstraintViolationException e) {
            throw new ForbiddenException("Username already exists");
        } catch (SQLException e) {
            throw new DataAccessException("Error inserting user", e);
        }
    }

    @Override
    public Optional<UserData> getUserByUsername(String username) throws DataAccessException {
        if (username == null || username.isEmpty()) {
            throw new BadRequestException();
        }

        String sql = "SELECT username, password, email FROM users WHERE username = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String uname = rs.getString("username");
                String password = rs.getString("password");
                String email = rs.getString("email");
                return Optional.of(new UserData(uname, password, email));
            } else {
                return Optional.empty();
            }

        } catch (SQLException e) {
            throw new DataAccessException("Error retrieving user", e);
        }
    }

    @Override
    public void clear() throws DataAccessException {
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("SET FOREIGN_KEY_CHECKS = 0");
            stmt.executeUpdate("TRUNCATE TABLE users");
            stmt.execute("SET FOREIGN_KEY_CHECKS = 1");
        } catch (SQLException e) {
            throw new DataAccessException("Error clearing users table", e);
        }
    }

}
