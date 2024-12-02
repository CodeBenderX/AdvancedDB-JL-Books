package application;

import java.sql.*;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

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
        TextField firstNameField = new TextField();
        TextField lastNameField = new TextField();
        
        setupAuthorIdValidation(authorIdField);
        setupLastNameValidation(lastNameField);
        setupFirstNameValidation(firstNameField);

        grid.addRow(0, new Label("Author ID:"), authorIdField);
        grid.addRow(1, new Label("First Name:"), firstNameField);
        grid.addRow(2, new Label("Last Name:"), lastNameField);

        Button registerButton = new Button("Register");
        registerButton.setOnAction(e -> {
            if (validateInputs(authorIdField, firstNameField, lastNameField)) {
                registerAuthor(authorIdField.getText().trim(), firstNameField.getText().trim(), lastNameField.getText().trim());
                stage.close();
            }
        });

        Button cancelButton = new Button("Cancel");
        cancelButton.setOnAction(e -> stage.close());

        HBox buttonBox = new HBox(10, registerButton, cancelButton);
        grid.add(buttonBox, 1, 3);

        Scene scene = new Scene(grid);
        stage.setScene(scene);
        stage.show();
    }
    
    private void setupAuthorIdValidation(TextField authorIdField) {
        authorIdField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null) return;
            
            String filtered = newValue.replaceAll("[^a-zA-Z0-9]", "");
            if (filtered.length() > 4) {
                filtered = filtered.substring(0, 4);
                Platform.runLater(() -> showAlert("Author ID Limit Reached", "Author ID can only have 4 characters."));
            }
            if (!filtered.equals(newValue)) {
                authorIdField.setText(filtered);
            }
        });
    }

    private void setupLastNameValidation(TextField lastNameField) {
        lastNameField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null) return;
            
            String filtered = newValue.replaceAll("[^a-zA-Z]", "");
            if (filtered.length() > 10) {
                filtered = filtered.substring(0, 10);
                Platform.runLater(() -> showAlert("Last Name Limit Reached", "Last name can only have 10 characters."));
            }
            if (!filtered.equals(newValue)) {
                lastNameField.setText(filtered);
            }
        });
    }

    private void setupFirstNameValidation(TextField firstNameField) {
        firstNameField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null) return;
            
            String filtered = newValue.replaceAll("[^a-zA-Z]", "");
            if (filtered.length() > 10) {
                filtered = filtered.substring(0, 10);
                Platform.runLater(() -> showAlert("First Name Limit Reached", "First name can only have 10 characters."));
            }
            if (!filtered.equals(newValue)) {
                firstNameField.setText(filtered);
            }
        });
    }

    private boolean validateInputs(TextField authorIdField, TextField lastNameField, TextField firstNameField) {
        String authorId = authorIdField.getText().trim();
        String lastName = lastNameField.getText().trim();
        String firstName = firstNameField.getText().trim();

        if (authorId.isEmpty()) {
            showAlert("Validation Error", "Author ID is required.");
            return false;
        }
        
        if (firstName.isEmpty()) {
            showAlert("Validation Error", "First Name is required.");
            return false;
        }
        
        if (lastName.isEmpty()) {
            showAlert("Validation Error", "Last Name is required.");
            return false;
        }

        return true;
    }

    private void registerAuthor(String authorId, String firstName, String lastName) {
    	String sql = "{CALL sp_register_author(?, ?, ?)}";
    	try (CallableStatement cstmt = connection.prepareCall(sql)) {
            cstmt.setString(1, authorId);
            cstmt.setString(2, lastName);  // Note: LNAME is the second parameter in the stored procedure
            cstmt.setString(3, firstName); // FNAME is the third parameter
            cstmt.execute();
            showAlert("Success", "Author registered successfully!");
        } catch (SQLException e) {
            String errorMessage = "Failed to register author: " + e.getMessage();
            if (e.getErrorCode() == 1) {  // ORA-00001: unique constraint violated
                errorMessage = "An author with this ID already exists. Please use a different ID.";
            } else if (e.getErrorCode() == 2290) {  // ORA-02290: check constraint violated
                errorMessage = "Invalid input. Please check the author details and try again.";
            }
            showAlert("Error", errorMessage);
            e.printStackTrace();  // Log the full stack trace for debugging
        }
    }

    public void showAuthorAssignmentForm() {
        if (connection == null) {
            showAlert("Error", "Please connect to the database first.");
            return;
        }

        Stage stage = new Stage();
        stage.setTitle("Assign Author to Book");

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(20));
        grid.setVgap(10);
        grid.setHgap(10);

        ComboBox<String> isbnComboBox = new ComboBox<>();
        loadISBNs(isbnComboBox);

        ComboBox<String> authorIdComboBox = new ComboBox<>();
        try {
            loadAuthorIds(authorIdComboBox);
        } catch (SQLException e) {
            showAlert("Error", "Failed to load author IDs: " + e.getMessage());
            e.printStackTrace();
        }

        grid.addRow(0, new Label("Choose Book (ISBN):"), isbnComboBox);
        grid.addRow(1, new Label("Choose Author (ID):"), authorIdComboBox);

        Button assignButton = new Button("Assign");
        assignButton.setOnAction(e -> {
            if (isbnComboBox.getValue() != null && authorIdComboBox.getValue() != null) {
                try {
                    assignAuthorToBook(isbnComboBox.getValue(), authorIdComboBox.getValue());
                    stage.close();
                } catch (SQLException ex) {
                    showAlert("Error", "Failed to assign author to book: " + ex.getMessage());
                    ex.printStackTrace();
                }
            } else {
                showAlert("Error", "Please select both a book and an author.");
            }
        });

        grid.addRow(2, assignButton);

        stage.setScene(new Scene(grid));
        stage.show();
    }

    private void assignAuthorToBook(String isbn, String authorId) throws SQLException {
        String insertSql = "INSERT INTO JL_BOOKAUTHOR (ISBN, AUTHORID) VALUES (?, ?)";
        String deleteSql = "DELETE FROM JL_BOOKAUTHOR WHERE ISBN = ?";

        try {
            // First, delete any existing author assignments for this book
            try (PreparedStatement deleteStmt = connection.prepareStatement(deleteSql)) {
                deleteStmt.setString(1, isbn);
                deleteStmt.executeUpdate();
            }

            // Then, insert the new author assignment
            try (PreparedStatement insertStmt = connection.prepareStatement(insertSql)) {
                insertStmt.setString(1, isbn);
                insertStmt.setString(2, authorId);
                int rowsAffected = insertStmt.executeUpdate();

                if (rowsAffected > 0) {
                    connection.commit(); // Commit the transaction
                    showAlert("Success", "Author assigned to book successfully!");
                } else {
                    connection.rollback(); // Rollback if no rows were affected
                    showAlert("Error", "Failed to assign author to book. No rows affected.");
                }
            }
        } catch (SQLException e) {
            connection.rollback(); // Rollback in case of any error
            showAlert("Error", "Failed to assign author to book: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadISBNs(ComboBox<String> isbnComboBox) {
        String query = "SELECT ISBN FROM JL_BOOKS";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                isbnComboBox.getItems().add(rs.getString("ISBN"));
            }
        } catch (SQLException e) {
            showAlert("Error", "Failed to load ISBNs: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadAuthorIds(ComboBox<String> authorIdComboBox) throws SQLException {
        String query = "SELECT AUTHORID FROM JL_AUTHOR";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
        	authorIdComboBox.getItems().clear();
            while (rs.next()) {
            	authorIdComboBox.getItems().add(rs.getString("AUTHORID"));
            }
        }
    }

    private void showAlert(String title, String message) {
        Platform.runLater(() -> {
            Alert.AlertType type = title.toLowerCase().contains("error") ? Alert.AlertType.ERROR
                    : Alert.AlertType.INFORMATION;
            Alert alert = new Alert(type);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }
}