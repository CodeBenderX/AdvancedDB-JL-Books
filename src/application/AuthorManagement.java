package application;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.sql.*;

public class AuthorManagement {
    private Connection connection;

    public AuthorManagement(Connection connection) {
        this.connection = connection;
    }

    public VBox createAuthorsContent() {
        VBox content = new VBox(10);
        content.setPadding(new Insets(20));

        Label titleLabel = new Label("Author Management");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        Button registerButton = new Button("Register New Author");
        registerButton.setOnAction(e -> showAuthorRegistrationForm());

        Button assignButton = new Button("Assign Author to Book");
        assignButton.setOnAction(e -> showAuthorAssignmentForm());

        content.getChildren().addAll(titleLabel, registerButton, assignButton);
        return content;
    }

    private void showAuthorRegistrationForm() {
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

        stage.setScene(new javafx.scene.Scene(grid));
        stage.show();
    }

    private void registerAuthor(String authorId, String lname, String fname) {
        String sql = "{CALL sp_register_author(?, ?, ?)}";
        try (CallableStatement stmt = connection.prepareCall(sql)) {
            stmt.setString(1, authorId);
            stmt.setString(2, lname);
            stmt.setString(3, fname);
            stmt.execute();
            connection.commit();
            showAlert("Success", "Author registered successfully!");
        } catch (SQLException e) {
            showAlert("Error", "Failed to register author: " + e.getMessage());
        }
    }

    private void showAuthorAssignmentForm() {
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

        stage.setScene(new javafx.scene.Scene(grid));
        stage.show();
    }

    private void loadISBNs(ComboBox<String> comboBox) {
        String sql = "SELECT ISBN FROM JL_BOOKS";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                comboBox.getItems().add(rs.getString("ISBN"));
            }
        } catch (SQLException e) {
            showAlert("Error", "Failed to load ISBNs: " + e.getMessage());
        }
    }

    private void loadAuthorIds(ComboBox<String> comboBox) {
        String sql = "SELECT AUTHOR_ID FROM JL_AUTHOR";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                comboBox.getItems().add(rs.getString("AUTHOR_ID"));
            }
        } catch (SQLException e) {
            showAlert("Error", "Failed to load Author IDs: " + e.getMessage());
        }
    }

    private void assignAuthorToBook(String isbn, String authorId) {
        String sql = "UPDATE JL_BOOKS SET AUTHOR_ID = ? WHERE ISBN = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, authorId);
            stmt.setString(2, isbn);
            int rowsAffected = stmt.executeUpdate();
            connection.commit();
            if (rowsAffected > 0) {
                showAlert("Success", "Author assigned to book successfully!");
            } else {
                showAlert("Info", "No changes made. Please check the ISBN and Author ID.");
            }
        } catch (SQLException e) {
            showAlert("Error", "Failed to assign author to book: " + e.getMessage());
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}