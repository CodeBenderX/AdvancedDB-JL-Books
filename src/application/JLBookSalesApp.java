package application;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.sql.*;
//Angelo

public class JLBookSalesApp extends Application {

    private Connection connection;
<<<<<<< HEAD
=======
//test
>>>>>>> branch 'master' of https://github.com/CodeBenderX/AdvancedDB-JL-Books.git

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("JL Book Sales");

        TabPane tabPane = new TabPane();

        Tab homeTab = new Tab("Home");
        homeTab.setContent(createHomeContent());

        Tab booksTab = new Tab("Books");
        booksTab.setContent(createBooksContent());

        Tab authorsTab = new Tab("Authors");
        authorsTab.setContent(createAuthorsContent());

        Tab customersTab = new Tab("Customers");
        customersTab.setContent(createCustomersContent());

        tabPane.getTabs().addAll(homeTab, booksTab, authorsTab, customersTab);

        Scene scene = new Scene(tabPane, 800, 600);
        primaryStage.setScene(scene);
        primaryStage.show();

        connectToDatabase();
    }

    private VBox createHomeContent() {
        VBox content = new VBox(10);
        content.setPadding(new Insets(20));

        Label welcomeLabel = new Label("Welcome to JL Book Sales");
        welcomeLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        Label descriptionLabel = new Label("Manage your books, authors, and customers with ease.");
        descriptionLabel.setStyle("-fx-font-size: 16px;");

        content.getChildren().addAll(welcomeLabel, descriptionLabel);
        return content;
    }

    private VBox createBooksContent() {
        VBox content = new VBox(10);
        content.setPadding(new Insets(20));

        Label titleLabel = new Label("Book Management");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        Button registerButton = new Button("Register New Book");
        registerButton.setOnAction(e -> showBookRegistrationForm());

        Button updateButton = new Button("Update Book Information");
        updateButton.setOnAction(e -> showBookUpdateForm());

        content.getChildren().addAll(titleLabel, registerButton, updateButton);
        return content;
    }

    private void showBookRegistrationForm() {
        Stage stage = new Stage();
        stage.setTitle("Register New Book");

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(20));
        grid.setVgap(10);
        grid.setHgap(10);

        TextField isbnField = new TextField();
        TextField titleField = new TextField();
        DatePicker pubdatePicker = new DatePicker();
        ComboBox<String> pubidComboBox = new ComboBox<>();
        pubidComboBox.getItems().addAll("1", "2", "3");
        TextField costField = new TextField();
        TextField retailField = new TextField();
        TextField discountField = new TextField();
        TextField categoryField = new TextField();

        grid.addRow(0, new Label("ISBN:"), isbnField);
        grid.addRow(1, new Label("Title:"), titleField);
        grid.addRow(2, new Label("Pubdate:"), pubdatePicker);
        grid.addRow(3, new Label("PubID:"), pubidComboBox);
        grid.addRow(4, new Label("Cost:"), costField);
        grid.addRow(5, new Label("Retail price:"), retailField);
        grid.addRow(6, new Label("Discount:"), discountField);
        grid.addRow(7, new Label("Category:"), categoryField);

        Button registerButton = new Button("Register");
        registerButton.setOnAction(e -> {
            registerBook(isbnField.getText(), titleField.getText(), pubdatePicker.getValue().toString(),
                    pubidComboBox.getValue(), costField.getText(), retailField.getText(),
                    discountField.getText(), categoryField.getText());
            stage.close();
        });

        Button cancelButton = new Button("Cancel");
        cancelButton.setOnAction(e -> stage.close());

        HBox buttonBox = new HBox(10, registerButton, cancelButton);
        grid.add(buttonBox, 1, 8);

        Scene scene = new Scene(grid);
        stage.setScene(scene);
        stage.show();
    }

    private void registerBook(String isbn, String title, String pubdate, String pubid, String cost,
                              String retail, String discount, String category) {
        try {
            String sql = "CALL sp_Book_register(?, ?, ?, ?, ?, ?, ?, ?)";
            CallableStatement stmt = connection.prepareCall(sql);
            stmt.setString(1, isbn);
            stmt.setString(2, title);
            stmt.setDate(3, Date.valueOf(pubdate));
            stmt.setString(4, pubid);
            stmt.setDouble(5, Double.parseDouble(cost));
            stmt.setDouble(6, Double.parseDouble(retail));
            stmt.setDouble(7, Double.parseDouble(discount));
            stmt.setString(8, category);

            stmt.execute();
            showAlert("Success", "Book registered successfully!");
        } catch (SQLException e) {
            showAlert("Error", "Failed to register book: " + e.getMessage());
        }
    }

    private void showBookUpdateForm() {
        // Implement book update functionality here
        showAlert("Info", "Book update functionality not implemented yet.");
    }

    private VBox createAuthorsContent() {
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
        // Implement author registration form here
        showAlert("Info", "Author registration functionality not implemented yet.");
    }

    private void showAuthorAssignmentForm() {
        // Implement author assignment form here
        showAlert("Info", "Author assignment functionality not implemented yet.");
    }

    private VBox createCustomersContent() {
        VBox content = new VBox(10);
        content.setPadding(new Insets(20));

        Label titleLabel = new Label("Customer Management");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        Button registerButton = new Button("Register New Customer");
        registerButton.setOnAction(e -> showCustomerRegistrationForm());

        Button updateButton = new Button("Update Customer Information");
        updateButton.setOnAction(e -> showCustomerUpdateForm());

        content.getChildren().addAll(titleLabel, registerButton, updateButton);
        return content;
    }

    private void showCustomerRegistrationForm() {
        // Implement customer registration form here
        showAlert("Info", "Customer registration functionality not implemented yet.");
    }

    private void showCustomerUpdateForm() {
        // Implement customer update form here
        showAlert("Info", "Customer update functionality not implemented yet.");
    }

    private void connectToDatabase() {
        try {
            // Replace with your actual database connection details
            String url = "jdbc:oracle:thin:@199.212.26.208:1521:SQLD";
            String user = "COMP214_F24_er_25";
            String password = "password";
            connection = DriverManager.getConnection(url, user, password);
        } catch (SQLException e) {
            showAlert("Error", "Failed to connect to the database: " + e.getMessage());
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
=======
//eyyyy
    //eyyyy
>>>>>>> branch 'master' of https://github.com/CodeBenderX/AdvancedDB-JL-Books.git
    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("JL Book Sales");

        TabPane tabPane = new TabPane();

        Tab homeTab = new Tab("Home");
        homeTab.setContent(createHomeContent());

        Tab booksTab = new Tab("Books");
        booksTab.setContent(createBooksContent());

        Tab authorsTab = new Tab("Authors");
        authorsTab.setContent(createAuthorsContent());

        Tab customersTab = new Tab("Customers");
        customersTab.setContent(createCustomersContent());

        tabPane.getTabs().addAll(homeTab, booksTab, authorsTab, customersTab);

        Scene scene = new Scene(tabPane, 800, 600);
        primaryStage.setScene(scene);
        primaryStage.show();

        connectToDatabase();
    }

    private VBox createHomeContent() {
        VBox content = new VBox(10);
        content.setPadding(new Insets(20));

        Label welcomeLabel = new Label("Welcome to JL Book Sales");
        welcomeLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        Label descriptionLabel = new Label("Manage your books, authors, and customers with ease.");
        descriptionLabel.setStyle("-fx-font-size: 16px;");

        content.getChildren().addAll(welcomeLabel, descriptionLabel);
        return content;
    }

    private VBox createBooksContent() {
        VBox content = new VBox(10);
        content.setPadding(new Insets(20));

        Label titleLabel = new Label("Book Management");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        Button registerButton = new Button("Register New Book");
        registerButton.setOnAction(e -> showBookRegistrationForm());

        Button updateButton = new Button("Update Book Information");
        updateButton.setOnAction(e -> showBookUpdateForm());

        content.getChildren().addAll(titleLabel, registerButton, updateButton);
        return content;
    }

    private void showBookRegistrationForm() {
        Stage stage = new Stage();
        stage.setTitle("Register New Book");

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(20));
        grid.setVgap(10);
        grid.setHgap(10);

        TextField isbnField = new TextField();
        TextField titleField = new TextField();
        DatePicker pubdatePicker = new DatePicker();
        ComboBox<String> pubidComboBox = new ComboBox<>();
        pubidComboBox.getItems().addAll("1", "2", "3");
        TextField costField = new TextField();
        TextField retailField = new TextField();
        TextField discountField = new TextField();
        TextField categoryField = new TextField();

        grid.addRow(0, new Label("ISBN:"), isbnField);
        grid.addRow(1, new Label("Title:"), titleField);
        grid.addRow(2, new Label("Pubdate:"), pubdatePicker);
        grid.addRow(3, new Label("PubID:"), pubidComboBox);
        grid.addRow(4, new Label("Cost:"), costField);
        grid.addRow(5, new Label("Retail price:"), retailField);
        grid.addRow(6, new Label("Discount:"), discountField);
        grid.addRow(7, new Label("Category:"), categoryField);

        Button registerButton = new Button("Register");
        registerButton.setOnAction(e -> {
            registerBook(isbnField.getText(), titleField.getText(), pubdatePicker.getValue().toString(),
                    pubidComboBox.getValue(), costField.getText(), retailField.getText(),
                    discountField.getText(), categoryField.getText());
            stage.close();
        });

        Button cancelButton = new Button("Cancel");
        cancelButton.setOnAction(e -> stage.close());

        HBox buttonBox = new HBox(10, registerButton, cancelButton);
        grid.add(buttonBox, 1, 8);

        Scene scene = new Scene(grid);
        stage.setScene(scene);
        stage.show();
    }

    private void registerBook(String isbn, String title, String pubdate, String pubid, String cost,
                              String retail, String discount, String category) {
        try {
            String sql = "CALL sp_Book_register(?, ?, ?, ?, ?, ?, ?, ?)";
            CallableStatement stmt = connection.prepareCall(sql);
            stmt.setString(1, isbn);
            stmt.setString(2, title);
            stmt.setDate(3, Date.valueOf(pubdate));
            stmt.setString(4, pubid);
            stmt.setDouble(5, Double.parseDouble(cost));
            stmt.setDouble(6, Double.parseDouble(retail));
            stmt.setDouble(7, Double.parseDouble(discount));
            stmt.setString(8, category);

            stmt.execute();
            showAlert("Success", "Book registered successfully!");
        } catch (SQLException e) {
            showAlert("Error", "Failed to register book: " + e.getMessage());
        }
    }

    private void showBookUpdateForm() {
        // Implement book update functionality here
        showAlert("Info", "Book update functionality not implemented yet.");
    }

    private VBox createAuthorsContent() {
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
        // Implement author registration form here
        showAlert("Info", "Author registration functionality not implemented yet.");
    }

    private void showAuthorAssignmentForm() {
        // Implement author assignment form here
        showAlert("Info", "Author assignment functionality not implemented yet.");
    }

    private VBox createCustomersContent() {
        VBox content = new VBox(10);
        content.setPadding(new Insets(20));

        Label titleLabel = new Label("Customer Management");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        Button registerButton = new Button("Register New Customer");
        registerButton.setOnAction(e -> showCustomerRegistrationForm());

        Button updateButton = new Button("Update Customer Information");
        updateButton.setOnAction(e -> showCustomerUpdateForm());

        content.getChildren().addAll(titleLabel, registerButton, updateButton);
        return content;
    }

    private void showCustomerRegistrationForm() {
        // Implement customer registration form here
        showAlert("Info", "Customer registration functionality not implemented yet.");
    }

    private void showCustomerUpdateForm() {
        // Implement customer update form here
        showAlert("Info", "Customer update functionality not implemented yet.");
    }

    private void connectToDatabase() {
        try {
            // Replace with your actual database connection details
            String url = "jdbc:oracle:thin:@199.212.26.208:1521:SQLD";
            String user = "COMP214_F24_er_25";
            String password = "password";
            connection = DriverManager.getConnection(url, user, password);
        } catch (SQLException e) {
            showAlert("Error", "Failed to connect to the database: " + e.getMessage());
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
>>>>>>> branch 'master' of https://github.com/CodeBenderX/AdvancedDB-JL-Books.git
}