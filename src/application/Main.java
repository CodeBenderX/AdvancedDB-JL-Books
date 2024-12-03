package application;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
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
    
    
    private static final String TAB_COLOR = "#E8CBA0";
    private static final String ACTIVE_TAB_COLOR = "#A67C4D";
    private static final String BACKGROUND_COLOR = "#FDFDFD";
    private static final String PAGE_COLOR = "#D9B68D";

    //Lorenzo
    @Override
    public void start(Stage primaryStage) {
        VBox root = new VBox();
        root.setPadding(new Insets(10)); // Add padding to the root
        root.setStyle("-fx-background-color: " + PAGE_COLOR + ";");
        
        setupDatabaseControls(root);
        setupTabPane();
        HBox tabPaneContainer = new HBox(10);
        tabPaneContainer.setPadding(new Insets(0, 50, 0, 50)); // Add left and right padding
        tabPaneContainer.getChildren().add(tabPane);

        root.getChildren().add(tabPaneContainer);

        root.getChildren().add(tabPane);

        Scene scene = new Scene(root, 1000, 600);

        primaryStage.setScene(scene);


        primaryStage.show();
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

    private void setupTabPane() {
        tabPane = new TabPane();
        tabPane.setPrefWidth(800);
        tabPane.setPrefHeight(550);
        

        Tab homeTab = createTab("Home", createHomeContent());
        Tab booksTab = createTab("Books", createBooksContent());
        Tab authorsTab = createTab("Authors", createAuthorsContent());
        Tab customersTab = createTab("Customers", createCustomersContent());

        booksTab.setDisable(true);
        authorsTab.setDisable(true);
        customersTab.setDisable(true);

        tabPane.getTabs().addAll(homeTab, booksTab, authorsTab, customersTab);
        tabPane.setStyle("-fx-background-color: " + BACKGROUND_COLOR + ";");

        // Add a listener to style the selected tab
        tabPane.getSelectionModel().selectedItemProperty().addListener((observable, oldTab, newTab) -> {
            if (oldTab != null) {
                oldTab.setStyle("-fx-background-color: " + TAB_COLOR + ";");
            }
            if (newTab != null) {
                newTab.setStyle("-fx-background-color: " + ACTIVE_TAB_COLOR + "; -fx-background-insets: 0 1 0 1;");
            }
        });

        // Initially style the first tab as selected
        homeTab.setStyle("-fx-background-color: " + ACTIVE_TAB_COLOR + "; -fx-background-insets: 0 1 0 1;");
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
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-alignment: center");

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
        
        Button showListButton = new Button("Show Book-Author List");
        showListButton.setOnAction(e -> authorManagement.showBookAuthorList());	

        content.getChildren().addAll(titleLabel, registerButton, assignButton, showListButton);
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
    
    private Tab createTab(String title, Node content) {
        Tab tab = new Tab(title);
        tab.setContent(content);
        tab.setStyle("-fx-background-color: " + TAB_COLOR + ";");
        return tab;
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