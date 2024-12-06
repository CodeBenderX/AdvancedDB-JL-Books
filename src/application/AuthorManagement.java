package application;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;


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
        TextField firstNameField = new TextField();
        TextField lastNameField = new TextField();
        
        setupAuthorIdValidation(authorIdField);
        setupLastNameValidation(lastNameField);
        setupFirstNameValidation(firstNameField);
        
        authorIdField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                String upperCase = newValue.toUpperCase();
                if (!newValue.equals(upperCase)) {
                    authorIdField.setText(upperCase);
                }
            }
        });

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
                	String selectedIsbn = isbnComboBox.getValue().split(" - ")[0];
                    String selectedAuthorId = authorIdComboBox.getValue().split(" - ")[0];
                    assignAuthorToBook(selectedIsbn, selectedAuthorId);
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
        String query = "SELECT ISBN, TITLE FROM JL_BOOKS ORDER BY TITLE";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            isbnComboBox.getItems().clear(); // Clear existing items
            while (rs.next()) {
                String isbn = rs.getString("ISBN");
                String title = rs.getString("TITLE");
                String displayText = String.format("%s - %s", isbn, title);
                isbnComboBox.getItems().add(displayText);
            }
        } catch (SQLException e) {
            showAlert("Error", "Failed to load ISBNs and titles: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadAuthorIds(ComboBox<String> authorIdComboBox) throws SQLException {
    	String query = "SELECT AUTHORID, FNAME || ' ' || LNAME AS AUTHOR_NAME FROM JL_AUTHOR ORDER BY LNAME, FNAME";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            authorIdComboBox.getItems().clear(); // Clear existing items
            while (rs.next()) {
                String authorId = rs.getString("AUTHORID");
                String authorName = rs.getString("AUTHOR_NAME");
                String displayText = String.format("%s - %s", authorId, authorName);
                authorIdComboBox.getItems().add(displayText);
            }
        } catch (SQLException e) {
            showAlert("Error", "Failed to load author IDs and names: " + e.getMessage());
            e.printStackTrace();
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
    
    public void showBookAuthorList() {
        Stage stage = new Stage();
        stage.setTitle("Book-Author List");
        
        HBox headerBox = new HBox(10);
        headerBox.setPadding(new Insets(10, 10, 5, 10));
        headerBox.getChildren().addAll(
            createHeaderLabel("ISBN", 120),
            createHeaderLabel("Title", 320),
            createHeaderLabel("Author", 200),
            createHeaderLabel("Author ID", 80)
        );

        ListView<HBox> listView = new ListView<>();
//        listView.setStyle("-fx-font-family: 'Courier New'; -fx-font-size: 12px;");
//        listView.setPadding(new Insets(0, 10, 10, 10));
        listView.setStyle("-fx-font-family: 'Courier New'; -fx-font-size: 12px;");
        listView.setPrefHeight(400); // Set a fixed height
        listView.setPrefWidth(740);  // Set a fixed width
        listView.setMaxHeight(400);  // Ensure the height doesn't exceed 400
        listView.setMaxWidth(740);   // Ensure the width doesn't exceed 740
        listView.setPadding(new Insets(0, 10, 10, 10));

        String query = """
            SELECT b.ISBN, b.TITLE, a.FNAME || ' ' || a.LNAME as AUTHOR_NAME, a.AUTHORID
            FROM JL_BOOKS b
            LEFT JOIN JL_BOOKAUTHOR ba ON b.ISBN = ba.ISBN
            LEFT JOIN JL_AUTHOR a ON ba.AUTHORID = a.AUTHORID
            ORDER BY b.ISBN
        """;

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            // Add header
            //listView.getItems().add(String.format("%-12s | %-32s | %-20s | %-8s",
                //"ISBN", "Title", "Author", "Author ID"));
            //listView.getItems().add("-".repeat(80));

            while (rs.next()) {
                String isbn = rs.getString("ISBN");
                String title = rs.getString("TITLE");
                String authorName = rs.getString("AUTHOR_NAME");
                String authorId = rs.getString("AUTHORID");

//                String displayText = String.format("%-12s | %-32s | %-20s | %-8s",
//                    isbn != null ? isbn : "",
//                    title != null ? title : "",
//                    authorName != null ? authorName : "",
//                    authorId != null ? authorId : "N/A");
//
//                listView.getItems().add(displayText);
                HBox row = new HBox(0);
                row.getChildren().addAll(
                    createDataLabel(isbn != null ? isbn : "", 120),
                    createDataLabel(title != null ? title : "", 320),
                    createDataLabel(authorName != null ? authorName : "", 200),
                    createDataLabel(authorId != null ? authorId : "N/A", 80)
                );

                listView.getItems().add(row);
            }
        } catch (SQLException e) {
            showAlert("Error", "Failed to load book-author list: " + e.getMessage());
            e.printStackTrace();
            return;
        }

        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(10));
        vbox.getChildren().addAll(headerBox, listView);

        double totalHeight = listView.getItems().size() * 24; // Approximate height per row
        listView.setPrefHeight(totalHeight);

        // Set the stage size based on content
//        stage.setMinWidth(750);
//        stage.setMinHeight(totalHeight + 100); // Add extra space for header and padding
//        stage.setMaxHeight(totalHeight + 100); // Lock the maximum height
        
        stage.setWidth(760);  // ListView width (740) + padding
        stage.setHeight(480);

        stage.setScene(new Scene(vbox));
        stage.show();
    }
    
    private Label createHeaderLabel(String text, double width) {
        Label label = new Label(text);
        label.setStyle("-fx-font-weight: bold; -fx-font-family: 'Courier New'; -fx-font-size: 12px;");
        label.setPrefWidth(width);
        return label;
    }

    private Label createDataLabel(String text, double width) {
        Label label = new Label(text);
        label.setStyle("-fx-font-family: 'Courier New'; -fx-font-size: 12px;");
        label.setPrefWidth(width);
        return label;
    }

//    private void showAlert(String title, String message) {
//        Platform.runLater(() -> {
//            Alert.AlertType type = title.toLowerCase().contains("error") ? Alert.AlertType.ERROR
//                    : Alert.AlertType.INFORMATION;
//            Alert alert = new Alert(type);
//            alert.setTitle(title);
//            alert.setHeaderText(null);
//            alert.setContentText(message);
//            alert.showAndWait();
//        });
//    }
}

