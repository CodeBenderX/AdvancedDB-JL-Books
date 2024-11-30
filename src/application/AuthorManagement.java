package application;

import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.geometry.Insets;
import java.sql.*;

public class AuthorManagement {
    private Connection connection;

    public AuthorManagement(Connection connection) {
        this.connection = connection;
    }

    public void showAuthorRegistrationForm() {
        Stage stage = new Stage();
        stage.setTitle("Register New Author");

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(20));
        grid.setVgap(10);
        grid.setHgap(10);

        TextField authorIdField = new TextField();
        TextField lnameField = new TextField();
        TextField fnameField = new TextField();

        grid.addRow(0, new Label("Author ID:"), authorIdField);
        grid.addRow(1, new Label("Last Name:"), lnameField);
        grid.addRow(2, new Label("First Name:"), fnameField);

        Button registerButton = new Button("Register");
        registerButton.setOnAction(e -> {
            registerAuthor(authorIdField.getText(), lnameField.getText(), fnameField.getText());
            stage.close();
        });

        grid.addRow(3, registerButton);

        stage.setScene(new Scene(grid));
        stage.show();
    }

    public void showAuthorAssignmentForm() {
        Stage stage = new Stage();
        stage.setTitle("Assign Author to Book");

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(20));
        grid.setVgap(10);
        grid.setHgap(10);

        ComboBox<String> isbnComboBox = new ComboBox<>();
        loadISBNs(isbnComboBox);

        ComboBox<String> authorIdComboBox = new ComboBox<>();
        loadAuthorIds(authorIdComboBox);

        grid.addRow(0, new Label("Choose Book (ISBN):"), isbnComboBox);
        grid.addRow(1, new Label("Choose Author:"), authorIdComboBox);

        Button assignButton = new Button("Assign");
        assignButton.setOnAction(e -> {
            assignAuthorToBook(isbnComboBox.getValue(), authorIdComboBox.getValue());
            stage.close();
        });

        grid.addRow(2, assignButton);

        stage.setScene(new Scene(grid));
        stage.show();
    }

    private void registerAuthor(String authorId, String lname, String fname) {
        // Implementation of author registration
    }

    private void assignAuthorToBook(String isbn, String authorId) {
        // Implementation of author assignment to book
    }

    private void loadISBNs(ComboBox<String> comboBox) {
        // Implementation of loading ISBNs
    }

    private void loadAuthorIds(ComboBox<String> comboBox) {
        // Implementation of loading Author IDs
    }

    // Other necessary methods...
}