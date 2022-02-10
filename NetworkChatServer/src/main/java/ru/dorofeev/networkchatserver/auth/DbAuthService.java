package ru.dorofeev.networkchatserver.auth;

import ru.dorofeev.networkchatcommon.auth.User;

import java.sql.*;

public class DbAuthService implements IAuthService {

    private final Connection connection;
    PreparedStatement getUserStatement;
    PreparedStatement setUserNameStatement;

    public DbAuthService(String connectionString) {
        try {
            connection = DriverManager.getConnection(connectionString);
            System.out.println("Служба аутентификации: соединение с БД установлено");
            getUserStatement = connection.prepareStatement("SELECT id, login, username FROM users WHERE login = ? AND password = ?");
            setUserNameStatement = connection.prepareStatement("UPDATE users SET username = ? WHERE id = ?");
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при запуске службы аутентификации", e);
        }
    }

    @Override
    public User auth(String login, String password) {
        try {
            getUserStatement.setString(1, login);
            getUserStatement.setString(2, password);
            ResultSet resultSet = getUserStatement.executeQuery();

            while (resultSet.next()) {

                return new User(
                        resultSet.getLong("id"),
                        resultSet.getString("username"),
                        resultSet.getString("login")
                );
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при аутентификации пользователя", e);
        }
        return null;
    }

    @Override
    public void setNewUserName(User user, String newUserName) {
        try {
            setUserNameStatement.setString(1, newUserName);
            setUserNameStatement.setLong(2, user.getId());
            int result = setUserNameStatement.executeUpdate();
            if (result == 1) {
                user.setUserName(newUserName);
            } else {
                throw new RuntimeException("Ошибка при изменении имени пользователя");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при изменении имени пользователя", e);
        }
    }

    @Override
    public void releaseResources() {
        try {
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при освобождении ресурсов службы аутентификации", e);
        }
    }
}