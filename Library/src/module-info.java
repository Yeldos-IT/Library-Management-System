module library {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires java.sql;

    exports application to javafx.graphics;
    opens application to javafx.fxml;

    exports controller to javafx.fxml;
    opens controller to javafx.fxml;

    opens library to javafx.base;
}