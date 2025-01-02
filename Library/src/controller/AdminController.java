package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import library.Books;

import java.awt.Desktop;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;
import java.util.Optional;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class AdminController {

    @FXML
    private TextField titleField, authorField, yearField, pagesField, pdfPathField, searchField;

    @FXML
    private ComboBox<String> filterComboBox, yearFilterComboBox;

    @FXML
    private TableView<Books> TableView;

    @FXML
    private TableColumn<Books, Integer> idColumn, yearColumn, pagesColumn;

    @FXML
    private TableColumn<Books, String> titleColumn, authorColumn;

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
        updateFilterComboBox();
        updateYearFilterComboBox();
        TableView.setItems(filteredData);
    }


    private void updateFilterComboBox() {
        ObservableList<String> authors = getUniqueAuthors();
        authors.add(0, "Все");
        filterComboBox.setItems(authors);
        filterComboBox.setValue("Все");
    }

    private void updateYearFilterComboBox() {
        ObservableList<String> years = getUniqueYears();
        years.add(0, "Все");
        yearFilterComboBox.setItems(years);
        yearFilterComboBox.setValue("Все");
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


    public void handleAddBook() {
        String title = titleField.getText().trim();
        String author = authorField.getText().trim();
        String year = yearField.getText().trim();
        String pages = pagesField.getText().trim();
        String pdfPath = pdfPathField.getText().trim();

        if (title.isEmpty() || author.isEmpty() || year.isEmpty() || pages.isEmpty() || pdfPath.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Предупреждение", "Пожалуйста, заполните все поля.");
            return;
        }

        if (title.length() > 255) {
            showAlert(Alert.AlertType.WARNING, "Предупреждение", "Название книги слишком длинное (максимум 255 символов).");
            return;
        }
        if (author.length() > 255) {
            showAlert(Alert.AlertType.WARNING, "Предупреждение", "Имя автора слишком длинное (максимум 255 символов).");
            return;
        }

        if (title.matches(".*[<>\\{}].*")) {
            showAlert(Alert.AlertType.WARNING, "Предупреждение", "Название книги содержит недопустимые символы.");
            return;
        }
        if (author.matches(".*[<>\\{}].*")) {
            showAlert(Alert.AlertType.WARNING, "Предупреждение", "Имя автора содержит недопустимые символы.");
            return;
        }

        if (author.matches("\\d+")) {
            showAlert(Alert.AlertType.WARNING, "Предупреждение", "Имя автора не может быть числом.");
            return;
        }

        int yearValue;
        try {
            yearValue = Integer.parseInt(year);
            if (yearValue < 1500 || yearValue > 2024) {
                showAlert(Alert.AlertType.WARNING, "Предупреждение", "Год должен быть в диапазоне от 1500 до 2024.");
                return;
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.WARNING, "Предупреждение", "Год должен быть числом.");
            return;
        }

        int pagesValue;
        try {
            pagesValue = Integer.parseInt(pages);
            if (pagesValue < 1) {
                showAlert(Alert.AlertType.WARNING, "Предупреждение", "Книга должна содержать хотя бы одну страницу.");
                return;
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.WARNING, "Предупреждение", "Количество страниц должно быть числом.");
            return;
        }

        if (!pdfPath.toLowerCase().endsWith(".pdf")) {
            showAlert(Alert.AlertType.WARNING, "Предупреждение", "Путь должен указывать на PDF файл.");
            return;
        }
        File pdfFile = new File(pdfPath);
        if (!pdfFile.exists() || !pdfFile.isFile()) {
            showAlert(Alert.AlertType.WARNING, "Предупреждение", "Указанный PDF файл не существует.");
            return;
        }

        String checkQuery = "SELECT COUNT(*) FROM books WHERE Title = ? OR PdfPath = ?";
        try (PreparedStatement checkStatement = connection.prepareStatement(checkQuery)) {
            checkStatement.setString(1, title);
            checkStatement.setString(2, pdfPath);
            ResultSet rs = checkStatement.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                showAlert(Alert.AlertType.WARNING, "Предупреждение", "Книга с таким названием или PDF уже существует.");
                return;
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Ошибка", "Не удалось проверить уникальность названия или PDF.");
            e.printStackTrace();
            return;
        }

        try {
            String query = "INSERT INTO books (Title, Author, Year, Pages, PdfPath) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, title);
            statement.setString(2, author);
            statement.setInt(3, yearValue);
            statement.setInt(4, pagesValue);
            statement.setString(5, pdfPath);
            statement.executeUpdate();
            refreshData();
            showAlert(Alert.AlertType.INFORMATION, "Успех", "Книга добавлена.");
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Ошибка", "Не удалось добавить книгу.");
            e.printStackTrace();
        }
    }

    public void handleUpdateBook() {
        Books selectedBook = TableView.getSelectionModel().getSelectedItem();
        if (selectedBook == null) {
            showAlert(Alert.AlertType.WARNING, "Предупреждение", "Выберите книгу для обновления.");
            return;
        }

        String title = titleField.getText().trim();
        String author = authorField.getText().trim();
        String year = yearField.getText().trim();
        String pages = pagesField.getText().trim();
        String pdfPath = pdfPathField.getText().trim();

        if (title.isEmpty() || author.isEmpty() || year.isEmpty() || pages.isEmpty() || pdfPath.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Предупреждение", "Пожалуйста, заполните все поля.");
            return;
        }

        if (title.length() > 255) {
            showAlert(Alert.AlertType.WARNING, "Предупреждение", "Название книги слишком длинное (максимум 255 символов).");
            return;
        }
        if (author.length() > 255) {
            showAlert(Alert.AlertType.WARNING, "Предупреждение", "Имя автора слишком длинное (максимум 255 символов).");
            return;
        }

        if (title.matches(".*[<>\\{}].*")) {
            showAlert(Alert.AlertType.WARNING, "Предупреждение", "Название книги содержит недопустимые символы.");
            return;
        }
        if (author.matches(".*[<>\\{}].*")) {
            showAlert(Alert.AlertType.WARNING, "Предупреждение", "Имя автора содержит недопустимые символы.");
            return;
        }

        if (author.matches("\\d+")) {
            showAlert(Alert.AlertType.WARNING, "Предупреждение", "Имя автора не может быть числом.");
            return;
        }

        int yearValue;
        try {
            yearValue = Integer.parseInt(year);
            if (yearValue < 1500 || yearValue > 2024) {
                showAlert(Alert.AlertType.WARNING, "Предупреждение", "Год должен быть в диапазоне от 1500 до 2024.");
                return;
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.WARNING, "Предупреждение", "Год должен быть числом.");
            return;
        }

        int pagesValue;
        try {
            pagesValue = Integer.parseInt(pages);
            if (pagesValue < 1) {
                showAlert(Alert.AlertType.WARNING, "Предупреждение", "Книга должна содержать хотя бы одну страницу.");
                return;
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.WARNING, "Предупреждение", "Количество страниц должно быть числом.");
            return;
        }

        if (!pdfPath.toLowerCase().endsWith(".pdf")) {
            showAlert(Alert.AlertType.WARNING, "Предупреждение", "Путь должен указывать на PDF файл.");
            return;
        }
        File pdfFile = new File(pdfPath);
        if (!pdfFile.exists() || !pdfFile.isFile()) {
            showAlert(Alert.AlertType.WARNING, "Предупреждение", "Указанный PDF файл не существует.");
            return;
        }

        String checkQuery = "SELECT COUNT(*) FROM books WHERE (Title = ? OR PdfPath = ?) AND Id != ?";
        try (PreparedStatement checkStatement = connection.prepareStatement(checkQuery)) {
            checkStatement.setString(1, title);
            checkStatement.setString(2, pdfPath);
            checkStatement.setInt(3, selectedBook.getId());
            ResultSet rs = checkStatement.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                showAlert(Alert.AlertType.WARNING, "Предупреждение", "Книга с таким названием или PDF уже существует.");
                return;
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Ошибка", "Не удалось проверить уникальность названия или PDF.");
            e.printStackTrace();
            return;
        }

        try {
            String query = "UPDATE books SET Title = ?, Author = ?, Year = ?, Pages = ?, PdfPath = ? WHERE Id = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, title);
            statement.setString(2, author);
            statement.setInt(3, yearValue);
            statement.setInt(4, pagesValue);
            statement.setString(5, pdfPath);
            statement.setInt(6, selectedBook.getId());
            statement.executeUpdate();
            refreshData();
            showAlert(Alert.AlertType.INFORMATION, "Успех", "Книга успешно обновлена.");
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Ошибка", "Не удалось обновить книгу.");
            e.printStackTrace();
        }
    }

    public void handleDeleteBook() {
        Books selectedBook = TableView.getSelectionModel().getSelectedItem();
        if (selectedBook == null) {
            showAlert(Alert.AlertType.WARNING, "Предупреждение", "Выберите книгу для удаления.");
            return;
        }
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Удаление книги");
        confirmation.setHeaderText("Вы уверены?");
        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                String query = "DELETE FROM books WHERE Id = ?";
                PreparedStatement statement = connection.prepareStatement(query);
                statement.setInt(1, selectedBook.getId());
                statement.executeUpdate();
                refreshData();
                showAlert(Alert.AlertType.INFORMATION, "Успех", "Книга удалена.");
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Ошибка", "Не удалось удалить книгу.");
                e.printStackTrace();
            }
        }
    }

    public void handleChooseFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        File selectedFile = fileChooser.showOpenDialog(null);
        if (selectedFile != null) {
            pdfPathField.setText(selectedFile.getAbsolutePath());
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

    public void handleExport() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Сохранить как");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        File file = fileChooser.showSaveDialog(TableView.getScene().getWindow());
        if (file != null) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                writer.write("ID,Title,Author,Year,Pages,PdfPath");
                writer.newLine();

                for (Books book : masterData) {
                    writer.write(String.format("%d,\"%s\",\"%s\",%d,%d,\"%s\"",
                            book.getId(),
                            escapeSpecialCharacters(book.getTitle()),
                            escapeSpecialCharacters(book.getAuthor()),
                            book.getYear(),
                            book.getPages(),
                            escapeSpecialCharacters(book.getPdfPath())));
                    writer.newLine();
                }
                showAlert(Alert.AlertType.INFORMATION, "Успех", "Данные успешно экспортированы в CSV файл.");
            } catch (IOException e) {
                showAlert(Alert.AlertType.ERROR, "Ошибка", "Не удалось сохранить CSV файл.");
                e.printStackTrace();
            }
        }
    }

    @FXML
    public void handleLogOut(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/AuthPane.fxml"));
            Scene loginScene = new Scene(loader.load());

            Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            currentStage.setScene(loginScene);
            currentStage.setTitle("Library Management - Login");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String escapeSpecialCharacters(String data) {
        if (data == null) {
            return "";
        }
        String escapedData = data.replace("\"", "\"\"");
        if (data.contains(",") || data.contains("\n")) {
            escapedData = "\"" + escapedData + "\"";
        }
        return escapedData;
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}