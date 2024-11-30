package application;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.sql.*;

public class Main extends Application {
    private Connection connection;
    private ComboBox<String> databaseSelector;
    private Label connectionStringLabel;
    private TabPane tabPane;
    private BookManagement bookManagement;
    private AuthorManagement authorManagement;

    @Override
    public void start(Stage primaryStage) {
        try {
            primaryStage.setTitle("JL Book Sales");

            VBox root = new VBox(10);
            root.setPadding(new Insets(10));

            setupDatabaseControls(root);
            setupTabPane(root);

            Scene scene = new Scene(root, 1000, 600);
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to start application: " + e.getMessage());
            Platform.exit();
        }
    }

    private void setupDatabaseControls(VBox root) {
        databaseSelector = new ComboBox<>();
        databaseSelector.getItems().addAll("School", "Remote");
        databaseSelector.setValue("School");

        Button connectButton = new Button("Connect to Database");
        connectButton.setOnAction(e -> connectToDatabase());

        connectionStringLabel = new Label("Not connected");
        connectionStringLabel.setStyle("-fx-font-style: italic; -fx-text-fill: gray;");

        HBox connectionControls = new HBox(10);
        connectionControls.setAlignment(Pos.CENTER_LEFT);
        connectionControls.getChildren().addAll(new Label("Select Database:"), databaseSelector, connectButton,
                connectionStringLabel);

        root.getChildren().add(connectionControls);
    }

    private void setupTabPane(VBox root) {
        tabPane = new TabPane();

        Tab homeTab = new Tab("Home");
        homeTab.setContent(createHomeContent());

        Tab booksTab = new Tab("Books");
        booksTab.setContent(createBooksContent());

        Tab authorsTab = new Tab("Authors");
        authorsTab.setContent(createAuthorsContent());

        Tab customersTab = new Tab("Customers");
        customersTab.setContent(createCustomersContent());
        
        booksTab.setDisable(true);
		authorsTab.setDisable(true);
		customersTab.setDisable(true);

        tabPane.getTabs().addAll(homeTab, booksTab, authorsTab, customersTab);
        root.getChildren().add(tabPane);
    }

    private VBox createHomeContent() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(20));
        content.setAlignment(Pos.CENTER);

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
        registerButton.setOnAction(e -> bookManagement.showBookRegistrationForm());

        Button showBooksButton = new Button("Show Books");
        showBooksButton.setOnAction(e -> bookManagement.showBookListForm(null));

        content.getChildren().addAll(titleLabel, registerButton, showBooksButton);
        return content;
    }

    private VBox createAuthorsContent() {
        VBox content = new VBox(10);
        content.setPadding(new Insets(20));

        Label titleLabel = new Label("Author Management");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        Button registerButton = new Button("Register New Author");
        registerButton.setOnAction(e -> authorManagement.showAuthorRegistrationForm());

        Button assignButton = new Button("Assign Author to Book");
        assignButton.setOnAction(e -> authorManagement.showAuthorAssignmentForm());

        content.getChildren().addAll(titleLabel, registerButton, assignButton);
        return content;
    }

    private VBox createCustomersContent() {
        VBox content = new VBox(10);
        content.setPadding(new Insets(20));

        Label titleLabel = new Label("Customer Management");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        Button registerButton = new Button("Register New Customer");
        registerButton.setOnAction(e -> showAlert("Info", "Customer registration functionality not implemented yet."));

        Button updateButton = new Button("Update Customer Information");
        updateButton.setOnAction(e -> showAlert("Info", "Customer update functionality not implemented yet."));

        content.getChildren().addAll(titleLabel, registerButton, updateButton);
        return content;
    }

    private void connectToDatabase() {
        String selectedDatabase = databaseSelector.getValue();
        String url, user, password;

        if ("School".equals(selectedDatabase)) {
            url = "jdbc:oracle:thin:@oracle1.centennialcollege.ca:1521:SQLD";
            user = "COMP214_F24_er_13";
            password = "password";
        } else { // Remote
            url = "jdbc:oracle:thin:@199.212.26.208:1521:SQLD";
            user = "COMP214_F24_er_13";
            password = "password";
        }

        new Thread(() -> {
            try {
                if (connection != null && !connection.isClosed()) {
                    connection.close();
                }
                connection = DriverManager.getConnection(url, user, password);

                Platform.runLater(() -> {
                    connectionStringLabel.setText("Connected: " + url);
                    connectionStringLabel.setStyle("-fx-font-style: normal; -fx-text-fill: green;");
                    System.out.println("Connection successful. Label updated to: " + connectionStringLabel.getText());

                    for (Tab tab : tabPane.getTabs()) {
                        tab.setDisable(false);
                    }

                    bookManagement = new BookManagement(connection);
                    authorManagement = new AuthorManagement(connection);
                });

                showAlert("Success", "Successfully connected to " + selectedDatabase + " database!");
            } catch (SQLException e) {
                Platform.runLater(() -> {
                    connectionStringLabel.setText("Not connected");
                    connectionStringLabel.setStyle("-fx-font-style: italic; -fx-text-fill: red;");
                    System.out.println("Connection failed. Label updated to: " + connectionStringLabel.getText());

                    for (int i = 1; i < tabPane.getTabs().size(); i++) {
                        tabPane.getTabs().get(i).setDisable(true);
                    }

                    showAlert("Connection Error", "Failed to connect to the database: " + e.getMessage());
                });
                e.printStackTrace();
            }
        }).start();
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

    public static void main(String[] args) {
        launch(args);
    }
}