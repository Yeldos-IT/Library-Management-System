package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import library.Books;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.sql.*;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class UserController {

    @FXML
    private TableView<Books> TableView;

    @FXML
    private TableColumn<Books, Integer> idColumn, yearColumn, pagesColumn;

    @FXML
    private TableColumn<Books, String> titleColumn, authorColumn;

    @FXML
    private ComboBox<String> filterComboBox, yearFilterComboBox;

    @FXML
    private TextField searchField;

    private ObservableList<Books> masterData = FXCollections.observableArrayList();
    private ObservableList<Books> filteredData = FXCollections.observableArrayList();

    private Connection connection;

    public void initialize() {
        connectToDatabase();
        setupTableColumns();
        setupFilters();
        refreshData();
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

    private void setupTableColumns() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        authorColumn.setCellValueFactory(new PropertyValueFactory<>("author"));
        yearColumn.setCellValueFactory(new PropertyValueFactory<>("year"));
        pagesColumn.setCellValueFactory(new PropertyValueFactory<>("pages"));
    }

    private void refreshData() {
        masterData.setAll(getBooksList());
        filteredData.setAll(masterData);
        TableView.setItems(filteredData);
    }

    private ObservableList<Books> getBooksList() {
        ObservableList<Books> booksList = FXCollections.observableArrayList();
        String query = "SELECT * FROM books";
        try (Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(query)) {
            while (rs.next()) {
                booksList.add(new Books(
                        rs.getInt("Id"),
                        rs.getString("Title"),
                        rs.getString("Author"),
                        rs.getInt("Year"),
                        rs.getInt("Pages"),
                        rs.getString("PdfPath")
                ));
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Ошибка", "Не удалось получить список книг.");
            e.printStackTrace();
        }
        return booksList;
    }

    private void setupFilters() {
        filterComboBox.setItems(getUniqueAuthors());
        filterComboBox.getItems().add(0, "Все");
        filterComboBox.setValue("Все");
        yearFilterComboBox.setItems(getUniqueYears());
        yearFilterComboBox.getItems().add(0, "Все");
        yearFilterComboBox.setValue("Все");
        filterComboBox.setOnAction(e -> applyFilters());
        yearFilterComboBox.setOnAction(e -> applyFilters());
        searchField.textProperty().addListener((observable, oldValue, newValue) -> applyFilters());
    }

    private ObservableList<String> getUniqueAuthors() {
        ObservableList<String> authors = FXCollections.observableArrayList();
        String query = "SELECT DISTINCT Author FROM books";
        try (Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(query)) {
            while (rs.next()) {
                authors.add(rs.getString("Author"));
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Ошибка", "Не удалось получить список авторов.");
            e.printStackTrace();
        }
        return authors;
    }

    private ObservableList<String> getUniqueYears() {
        ObservableList<String> years = FXCollections.observableArrayList();
        String query = "SELECT DISTINCT Year FROM books ORDER BY Year DESC";
        try (Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(query)) {
            while (rs.next()) {
                years.add(String.valueOf(rs.getInt("Year")));
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Ошибка", "Не удалось получить список годов.");
            e.printStackTrace();
        }
        return years;
    }

    private void applyFilters() {
        String selectedAuthor = filterComboBox.getValue();
        String selectedYear = yearFilterComboBox.getValue();
        String keyword = searchField.getText().toLowerCase();
        filteredData.setAll(masterData);
        if (selectedAuthor != null && !selectedAuthor.equals("Все")) {
            filteredData.removeIf(book -> !book.getAuthor().equalsIgnoreCase(selectedAuthor));
        }
        if (selectedYear != null && !selectedYear.equals("Все")) {
            try {
                int year = Integer.parseInt(selectedYear);
                filteredData.removeIf(book -> book.getYear() != year);
            } catch (NumberFormatException ignored) {
            }
        }
        if (keyword != null && !keyword.isEmpty()) {
            filteredData.removeIf(book ->
                    !book.getTitle().toLowerCase().contains(keyword) &&
                            !book.getAuthor().toLowerCase().contains(keyword)
            );
        }
    }

    public void handleReadPDF() {
        Books selectedBook = TableView.getSelectionModel().getSelectedItem();
        if (selectedBook == null) {
            showAlert(Alert.AlertType.WARNING, "Предупреждение", "Выберите книгу для чтения.");
            return;
        }
        String pdfPath = selectedBook.getPdfPath();
        if (pdfPath == null || pdfPath.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Предупреждение", "Путь к PDF не указан.");
            return;
        }
        File pdfFile = new File(pdfPath);
        if (!pdfFile.exists()) {
            showAlert(Alert.AlertType.ERROR, "Ошибка", "PDF-файл не найден.");
            return;
        }
        try {
            Desktop.getDesktop().open(pdfFile);
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Ошибка", "Не удалось открыть PDF.");
            e.printStackTrace();
        }
    }

    public void handleLogOut(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/AuthPane.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
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