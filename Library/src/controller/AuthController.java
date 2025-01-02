package controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;

import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;

public class AuthController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label errorLabel;

    private Connection connection;

    public void initialize() {
        connectToDatabase();
    }

    private void connectToDatabase() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            String url = "jdbc:mysql://localhost:3306/library?useSSL=false&serverTimezone=UTC";
            connection = DriverManager.getConnection(url, "root", "Yeldos210406");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Ошибка", "Не удалось подключиться к базе данных.");
            e.printStackTrace();
        }
    }

    public void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        if (!validateInput(username, password)) {
            return;
        }

        try {
            String hashedPassword = hashPassword(password);
            String query = "SELECT role FROM users WHERE username = ? AND password = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, username);
            statement.setString(2, hashedPassword);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                String role = resultSet.getString("role");
                if ("admin".equalsIgnoreCase(role)) {
                    loadPane("/view/AdminPane.fxml", "Admin Dashboard");
                } else if ("user".equalsIgnoreCase(role)) {
                    loadPane("/view/UserPane.fxml", "User Dashboard");
                }
            } else {
                errorLabel.setText("Неверный логин или пароль.");
            }
        } catch (SQLException e) {
            errorLabel.setText("Ошибка авторизации.");
            e.printStackTrace();
        }
    }

    public void handleRegister() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        if (!validateInput(username, password)) {
            return;
        }

        try {
            String checkQuery = "SELECT COUNT(*) FROM users WHERE username = ?";
            PreparedStatement checkStatement = connection.prepareStatement(checkQuery);
            checkStatement.setString(1, username);
            ResultSet resultSet = checkStatement.executeQuery();
            resultSet.next();

            if (resultSet.getInt(1) > 0) {
                errorLabel.setText("Пользователь с таким именем уже существует.");
                return;
            }

            String hashedPassword = hashPassword(password);
            String query = "INSERT INTO users (username, password, role) VALUES (?, ?, 'user')";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, username);
            statement.setString(2, hashedPassword);
            statement.executeUpdate();

            errorLabel.setText("Регистрация успешна.");
        } catch (SQLException e) {
            errorLabel.setText("Ошибка регистрации.");
            e.printStackTrace();
        }
    }

    private boolean validateInput(String username, String password) {
        if (username.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Введите логин и пароль.");
            return false;
        }

        if (username.length() < 3) {
            errorLabel.setText("Имя пользователя должно быть не менее 3 символов.");
            return false;
        }

        if (password.length() < 6) {
            errorLabel.setText("Пароль должен быть не менее 6 символов.");
            return false;
        }

        if (!username.matches("[a-zA-Z0-9._-]+")) {
            errorLabel.setText("Имя пользователя содержит недопустимые символы.");
            return false;
        }

        return true;
    }

    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] messageDigest = md.digest(password.getBytes());
            BigInteger number = new BigInteger(1, messageDigest);
            String hashedPassword = number.toString(16);

            while (hashedPassword.length() < 32) {
                hashedPassword = "0" + hashedPassword;
            }
            return hashedPassword;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Ошибка хэширования пароля.", e);
        }
    }

    private void loadPane(String fxmlPath, String title) {
        try {
            Stage stage = (Stage) usernameField.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Scene scene = new Scene(loader.load());
            stage.setScene(scene);
            stage.setTitle(title);
        } catch (IOException e) {
            errorLabel.setText("Не удалось загрузить панель.");
            e.printStackTrace();
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}