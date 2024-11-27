//package application;
//
//import javafx.application.Application;
//import javafx.application.Platform;
//import javafx.collections.FXCollections;
//import javafx.collections.ObservableList;
//import javafx.geometry.Insets;
//import javafx.geometry.Pos;
//import javafx.scene.Scene;
//import javafx.scene.control.*;
//import javafx.scene.layout.*;
//import javafx.stage.Stage;
//import javafx.util.Callback;
////import javafx.util.StringConverter;
//import java.sql.*;
//import java.text.NumberFormat;
//import java.util.Locale;
//import java.time.LocalDate;
//import java.time.format.DateTimeFormatter;
//import java.util.HashMap;
//import java.util.Map;
//import java.util.function.BiConsumer;
//import java.util.function.Function;
//
//
//
//public class JLBookSalesApp extends Application {
//
//    private Connection connection;
//    private ComboBox<String> databaseSelector;
//    private Label connectionStringLabel;
//    private ListView<Book> bookListView;
//    private TabPane tabPane;
//    private Label homeInstructionLabel;
//    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yy-MM-dd");
//    private Button updateButton;
//    private Button revertAllButton;
//    private Button cancelButton;
//    private Map<String, Book> originalBooks = new HashMap<>();
//    private boolean hasChanges = false;
//
//    @Override
//    public void start(Stage primaryStage) {
//        try {
//            primaryStage.setTitle("JL Book Sales");
//
//            VBox root = new VBox(10);
//            root.setPadding(new Insets(10));
//
//            databaseSelector = new ComboBox<>();
//            databaseSelector.getItems().addAll("School", "Remote");
//            databaseSelector.setValue("School");
//
//            Button connectButton = new Button("Connect to Database");
//            connectButton.setOnAction(e -> connectToDatabase());
//
//            connectionStringLabel = new Label("Not connected");
//            connectionStringLabel.setStyle("-fx-font-style: italic; -fx-text-fill: gray;");
//
//            HBox connectionControls = new HBox(10);
//            connectionControls.setAlignment(Pos.CENTER_LEFT);
//            connectionControls.getChildren().addAll(
//                new Label("Select Database:"), 
//                databaseSelector, 
//                connectButton, 
//                connectionStringLabel
//            );
//
//            tabPane = new TabPane();
//
//            Tab homeTab = new Tab("Home");
//            homeTab.setContent(createHomeContent());
//
//            Tab booksTab = new Tab("Books");
//            booksTab.setContent(createBooksContent());
//            booksTab.setDisable(true);
//
//            Tab authorsTab = new Tab("Authors");
//            authorsTab.setContent(createAuthorsContent());
//            authorsTab.setDisable(true);
//
//            Tab customersTab = new Tab("Customers");
//            customersTab.setContent(createCustomersContent());
//            customersTab.setDisable(true);
//
//            tabPane.getTabs().addAll(homeTab, booksTab, authorsTab, customersTab);
//
//            root.getChildren().addAll(connectionControls, tabPane);
//
//            Scene scene = new Scene(root, 1000, 600);
//            primaryStage.setScene(scene);
//            primaryStage.show();
//        } catch (Exception e) {
//            e.printStackTrace();
//            showAlert("Error", "Failed to start application: " + e.getMessage());
//            Platform.exit();
//        }
//    }
//
//    private VBox createHomeContent() {
//        VBox content = new VBox(20);
//        content.setPadding(new Insets(20));
//        content.setAlignment(Pos.CENTER);
//
//        homeInstructionLabel = new Label("Please use the connection controls above to connect to the database.");
//        homeInstructionLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: #4a4a4a;");
//        homeInstructionLabel.setWrapText(true);
//        homeInstructionLabel.setMaxWidth(600);
//
//        Label welcomeLabel = new Label("Welcome to JL Book Sales");
//        welcomeLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
//
//        Label descriptionLabel = new Label("Manage your books, authors, and customers with ease.");
//        descriptionLabel.setStyle("-fx-font-size: 16px;");
//
//        content.getChildren().addAll(homeInstructionLabel, welcomeLabel, descriptionLabel);
//        welcomeLabel.setVisible(false);
//        descriptionLabel.setVisible(false);
//
//        return content;
//    }
//
//    private VBox createBooksContent() {
//        VBox content = new VBox(10);
//        content.setPadding(new Insets(20));
//
//        Label titleLabel = new Label("Book Management");
//        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
//
//        Button registerButton = new Button("Register New Book");
//        registerButton.setOnAction(e -> showBookRegistrationForm());
//
////        Button listUpdateButton = new Button("List and Update Books");
////        listUpdateButton.setOnAction(e -> showBookListAndUpdateForm());
//        
//        Button showBooksButton = new Button("Show Books");
//        showBooksButton.setOnAction(e -> showBookListForm());
//
//        registerButton.setDisable(true);
////        listUpdateButton.setDisable(true);
//        showBooksButton.setDisable(true);
//
//        content.getChildren().addAll(titleLabel, registerButton, showBooksButton);
//        return content;
//    }
//
////    private void showBookListAndUpdateForm() {
////        if (connection == null) {
////            showAlert("Error", "No database connection. Please connect to a database first.");
////            return;
////        }
////
////        Stage stage = new Stage();
////        stage.setTitle("List Books");
////
////        VBox layout = new VBox(10);
////        layout.setPadding(new Insets(20));
////
////        bookListView = new ListView<>();
////        bookListView.setCellFactory(lv -> new ListCell<Book>() {
////            @Override
////            protected void updateItem(Book book, boolean empty) {
////                super.updateItem(book, empty);
////                if (empty || book == null) {
////                    setText(null);
////                } else {
////                    setText(book.toString());
////                }
////            }
////        });
////
////        Button refreshButton = new Button("Refresh Book List");
////        refreshButton.setOnAction(e -> loadBooksFromDatabase());
////
////        layout.getChildren().addAll(bookListView, refreshButton);
////
////        Scene scene = new Scene(layout, 800, 600);
////        stage.setScene(scene);
////        stage.show();
////
////        loadBooksFromDatabase();
////    }
//    
//    private void showBookListForm() {
//        if (connection == null) {
//            showAlert("Error", "No database connection. Please connect to a database first.");
//            return;
//        }
//
//        Stage stage = new Stage();
//        stage.setTitle("Book List");
//
//        VBox layout = new VBox(10);
//        layout.setPadding(new Insets(20));
//
//        // Create column headers
//        HBox header = createColumnHeaders();
//        layout.getChildren().add(header);
//
//        bookListView = new ListView<>();
//        bookListView.setCellFactory(createBookCellFactory());
//        bookListView.setPrefHeight(600);
//
//        HBox buttonBox = createButtonBox();
//        layout.getChildren().addAll(bookListView, buttonBox);
//        VBox.setVgrow(bookListView, Priority.ALWAYS);
//
//        Scene scene = new Scene(layout, 1200, 700);
//        stage.setScene(scene);
//        stage.show();
//
//        loadBooksFromDatabase();
//    }
//
//    private HBox createColumnHeaders() {
//        HBox header = new HBox(10);
//        header.setAlignment(Pos.CENTER_LEFT);
//        header.setPadding(new Insets(5));
//        header.setStyle("-fx-background-color: #f0f0f0; -fx-font-weight: bold;");
//
//        Label isbnHeader = new Label("ISBN");
//        Label titleHeader = new Label("Title");
//        Label pubdateHeader = new Label("Pub Date");
//        Label pubidHeader = new Label("Pub ID");
//        Label costHeader = new Label("Cost");
//        Label retailHeader = new Label("Retail");
//        Label discountHeader = new Label("Discount");
//        Label categoryHeader = new Label("Category");
//
//        isbnHeader.setPrefWidth(100);
//        titleHeader.setPrefWidth(200);
//        pubdateHeader.setPrefWidth(100);
//        pubidHeader.setPrefWidth(60);
//        costHeader.setPrefWidth(80);
//        retailHeader.setPrefWidth(80);
//        discountHeader.setPrefWidth(80);
//        categoryHeader.setPrefWidth(100);
//
//        header.getChildren().addAll(
//                isbnHeader, titleHeader, pubdateHeader, pubidHeader,
//                costHeader, retailHeader, discountHeader, categoryHeader
//            );
//            return header;
//    }
//
//    private HBox createButtonBox() {
//        HBox buttonBox = new HBox(10);
//        buttonBox.setAlignment(Pos.CENTER);
//
//        updateButton = new Button("Update");
//        revertAllButton = new Button("Revert All");
//        cancelButton = new Button("Cancel");
//
//        updateButton.setDisable(true);
//        revertAllButton.setDisable(true);
//        cancelButton.setDisable(true);
//
//        updateButton.setOnAction(e -> handleUpdate());
//        revertAllButton.setOnAction(e -> handleRevertAll());
//        cancelButton.setOnAction(e -> handleCancel(null, (Stage) cancelButton.getScene().getWindow()));
//
//        buttonBox.getChildren().addAll(updateButton, revertAllButton, cancelButton);
//        return buttonBox;
//    }
//
//    
//    
//    private Callback<ListView<Book>, ListCell<Book>> createBookCellFactory() {
//        return listView -> new ListCell<Book>() {
//            private TextField categoryField;
//            private TextField costField;
//            private TextField retailField;
//
//            @Override
//            protected void updateItem(Book book, boolean empty) {
//                super.updateItem(book, empty);
//                if (empty || book == null) {
//                    setText(null);
//                    setGraphic(null);
//                } else {
//                    if (categoryField == null) {
//                        createFields();
//                    }
//                    updateFields(book);
//                    HBox container = createContainer(book);
//                    setGraphic(container);
//                }
//            }
//
//            private void createFields() {
//                categoryField = new TextField();
//                costField = new TextField();
//                retailField = new TextField();
//
//                categoryField.setPrefWidth(100);
//                costField.setPrefWidth(80);
//                retailField.setPrefWidth(80);
//
//                setupEditableFields();
//            }
//
//            private void updateFields(Book book) {
//                categoryField.setText(book.getCategory());
//                costField.setText(formatCurrency(book.getCost()));
//                retailField.setText(formatCurrency(book.getRetail()));
//                
//                resetFieldStyle(categoryField);
//                resetFieldStyle(costField);
//                resetFieldStyle(retailField);
//
//                Book originalBook = originalBooks.get(book.getIsbn());
//                if (originalBook != null) {
//                    if (!book.getCategory().equals(originalBook.getCategory())) {
//                        setChangedFieldStyle(categoryField);
//                    }
//                    if (book.getCost() != originalBook.getCost()) {
//                        setChangedFieldStyle(costField);
//                    }
//                    if (book.getRetail() != originalBook.getRetail()) {
//                        setChangedFieldStyle(retailField);
//                    }
//                }
//            }
//
//            private HBox createContainer(Book book) {
//                HBox container = new HBox(10);
//                container.setAlignment(Pos.CENTER_LEFT);
//                container.setPadding(new Insets(5));
//
//                Label isbnLabel = new Label(book.getIsbn());
//                Label titleLabel = new Label(book.getTitle());
//                Label pubdateLabel = new Label(book.getPubdate().format(dateFormatter));
//                Label pubidLabel = new Label(String.valueOf(book.getPubid()));
//                Label discountLabel = new Label(formatCurrency(book.getDiscount()));
//
//                isbnLabel.setPrefWidth(100);
//                titleLabel.setPrefWidth(200);
//                pubdateLabel.setPrefWidth(100);
//                pubidLabel.setPrefWidth(60);
//                discountLabel.setPrefWidth(80);
//
//                container.getChildren().addAll(
//                    isbnLabel, titleLabel, pubdateLabel, pubidLabel,
//                    costField, retailField, discountLabel, categoryField
//                );
//                return container;
//            }
//
//            private void setupEditableFields() {
//                categoryField.textProperty().addListener((observable, oldValue, newValue) -> 
//                    handleFieldChange(categoryField, oldValue, newValue, Book::getCategory, Book::setCategory));
//                costField.textProperty().addListener((observable, oldValue, newValue) -> 
//                    handleFieldChange(costField, oldValue, newValue, Book::getCost, (book, value) -> book.setCost(parseCurrency(value))));
//                retailField.textProperty().addListener((observable, oldValue, newValue) -> 
//                    handleFieldChange(retailField, oldValue, newValue, Book::getRetail, (book, value) -> book.setRetail(parseCurrency(value))));
//            }
//            
//            private <T> void handleFieldChange(TextField field, String oldValue, String newValue, 
//                                               Function<Book, T> getter, BiConsumer<Book, String> setter) {
//                Book book = getItem();
//                if (book != null) {
//                    Book originalBook = originalBooks.get(book.getIsbn());
//                    if (originalBook != null) {
//                        T originalValue = getter.apply(originalBook);
//                        T newValueConverted = (T) (field == categoryField ? newValue : parseCurrency(newValue));
//                        
//                        if (!newValueConverted.equals(originalValue)) {
//                            setter.accept(book, newValue);
//                            setChangedFieldStyle(field);
//                            enableButtons();
//                        } else {
//                            resetFieldStyle(field);
//                        }
//                        checkForChanges();
//                    }
//                }
//            }
//            
//            private void setChangedFieldStyle(TextField field) {
//                field.setStyle("-fx-background-color: #FFFF00; -fx-text-fill: #FF0000;");
//            }
//
//            private void resetFieldStyle(TextField field) {
//                field.setStyle("");
//            }
//        };
//    }
//    
//    private void handleUpdate() {
//        ObservableList<Book> books = bookListView.getItems();
//        for (Book book : books) {
//            if (isBookChanged(book)) {
//                updateBookInDatabase(book);
//            }
//        }
//        originalBooks.clear();
//        for (Book book : books) {
//            originalBooks.put(book.getIsbn(), book.copy());
//        }
//        hasChanges = false;
//        disableButtons();
//        bookListView.refresh();
//    }
//
//    private void handleRevertAll() {
//        ObservableList<Book> books = bookListView.getItems();
//        for (Book book : books) {
//            Book originalBook = originalBooks.get(book.getIsbn());
//            book.setCategory(originalBook.getCategory());
//            book.setCost(originalBook.getCost());
//            book.setRetail(originalBook.getRetail());
//        }
//        hasChanges = false;
//        disableButtons();
//        bookListView.refresh();
//    }
//
//    private void handleCancel(Stage primaryStage, Stage currentStage) {
//        if (hasChanges) {
//            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
//            alert.setTitle("Unsaved Changes");
//            alert.setHeaderText("There are unsaved changes.");
//            alert.setContentText("Do you want to discard these changes?");
//
//            alert.showAndWait().ifPresent(response -> {
//                if (response == ButtonType.OK) {
//                    closeWindow(primaryStage, currentStage);
//                }
//            });
//        } else {
//            closeWindow(primaryStage, currentStage);
//        }
//    }
//
//    private void closeWindow(Stage primaryStage, Stage currentStage) {
//        currentStage.close();
//        if (primaryStage != null) {
//            primaryStage.show();
//        }
//    }
//
////            private void updateBook() {
////                Book book = getItem();
////                if (book != null) {
////                    book.setCategory(categoryField.getText());
////                    book.setCost(parseCurrency(costField.getText()));
////                    book.setRetail(parseCurrency(retailField.getText()));
////                    updateBookInDatabase(book);
////                }
////            }
////        };
////    }
//    
//    
//    private String formatCurrency(double amount) {
//        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.US);
//        return currencyFormat.format(amount);
//    }
//
//
//    private void loadBooksFromDatabase() {
//        if (connection == null) {
//            showAlert("Error", "No database connection. Please connect to a database first.");
//            return;
//        }
//
//        ObservableList<Book> books = FXCollections.observableArrayList();
//        String query = "SELECT ISBN, TITLE, PUBDATE, PUBID, COST, RETAIL, DISCOUNT, CATEGORY FROM JL_BOOKS";
//
//        try (Statement stmt = connection.createStatement();
//             ResultSet rs = stmt.executeQuery(query)) {
//
//            while (rs.next()) {
//                books.add(new Book(
//                		rs.getString("ISBN"),
//                        rs.getString("TITLE"),
//                        rs.getDate("PUBDATE").toLocalDate(),
//                        rs.getString("PUBID"),
//                        rs.getDouble("COST"),
//                        rs.getDouble("RETAIL"),
//                        rs.getDouble("DISCOUNT"),
//                        rs.getString("CATEGORY")
//                ));
//            }
//
//            Platform.runLater(() -> {
//            	bookListView.setItems(books);
//                System.out.println("Loaded " + books.size() + " books from the database.");
//            });
//        } catch (SQLException e) {
//            e.printStackTrace();
//            Platform.runLater(() -> {
//                showAlert("Error", "Failed to load books: " + e.getMessage());
//                System.err.println("SQL Error: " + e.getMessage());
//            });
//        }
//    }
//
//    private void updateBookInDatabase(Book book) {
//        String query = "UPDATE JL_BOOKS SET CATEGORY = ?, COST = ?, RETAIL = ? WHERE ISBN = ?";
//
//        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
//            pstmt.setString(1, book.getCategory());
//            pstmt.setDouble(2, book.getCost());
//            pstmt.setDouble(3, book.getRetail());
//            pstmt.setString(4, book.getIsbn());
//
//            int affectedRows = pstmt.executeUpdate();
//            if (affectedRows > 0) {
//                System.out.println("Book updated successfully!");
//            } else {
//                System.out.println("Failed to update book. No rows affected.");
//            }
//        } catch (SQLException e) {
//            e.printStackTrace();
//            showAlert("Error", "Failed to update book: " + e.getMessage());
//        }
//    }
//    
//    private boolean isBookChanged(Book book) {
//        Book originalBook = originalBooks.get(book.getIsbn());
//        if (originalBook == null) {
//            return false;
//        }
//        return !book.getCategory().equals(originalBook.getCategory()) ||
//               book.getCost() != originalBook.getCost() ||
//               book.getRetail() != originalBook.getRetail();
//    }
//
//    private void checkForChanges() {
//        boolean changes = bookListView.getItems().stream().anyMatch(this::isBookChanged);
//        hasChanges = changes;
//        updateButtonStates();
//    }
//
//    private void updateButtonStates() {
//        updateButton.setDisable(!hasChanges);
//        revertAllButton.setDisable(!hasChanges);
//        cancelButton.setDisable(!hasChanges);
//    }
//
//    private void enableButtons() {
//        updateButton.setDisable(false);
//        revertAllButton.setDisable(false);
//        cancelButton.setDisable(false);
//    }
//    
//    private void disableButtons() {
//        updateButton.setDisable(true);
//        revertAllButton.setDisable(true);
//        cancelButton.setDisable(true);
//    }
//
//    
//    private double parseCurrency(String value) {
//        try {
//            return Double.parseDouble(value.replaceAll("[^\\d.]", ""));
//        } catch (NumberFormatException e) {
//            return 0.0;
//        }
//    }
//    
////    private void updateBookInDatabase(Book book) {
////        String query = "UPDATE JL_BOOKS SET CATEGORY = ?, COST = ?, RETAIL = ? WHERE ISBN = ?";
////
////        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
////            pstmt.setString(1, book.getCategory());
////            pstmt.setDouble(2, book.getCost());
////            pstmt.setDouble(3, book.getRetail());
////            pstmt.setString(4, book.getIsbn());
////
////            int affectedRows = pstmt.executeUpdate();
////            if (affectedRows > 0) {
////                showAlert("Success", "Book updated successfully!");
////            } else {
////                showAlert("Error", "Failed to update book. No rows affected.");
////            }
////        } catch (SQLException e) {
////            e.printStackTrace();
////            showAlert("Error", "Failed to update book: " + e.getMessage());
////        }
////    }
//
//    private void showBookRegistrationForm() {
//        Stage stage = new Stage();
//        stage.setTitle("Register New Book");
//
//        GridPane grid = new GridPane();
//        grid.setPadding(new Insets(20));
//        grid.setVgap(10);
//        grid.setHgap(10);
//
//        TextField isbnField = new TextField();
//        TextField titleField = new TextField();
//        DatePicker pubdatePicker = new DatePicker();
//        ComboBox<String> pubidComboBox = new ComboBox<>();
//        pubidComboBox.getItems().addAll("1", "2", "3");
//        TextField costField = new TextField();
//        TextField retailField = new TextField();
//        TextField discountField = new TextField();
//        TextField categoryField = new TextField();
//
//        grid.addRow(0, new Label("ISBN:"), isbnField);
//        grid.addRow(1, new Label("Title:"), titleField);
//        grid.addRow(2, new Label("Pubdate:"), pubdatePicker);
//        grid.addRow(3, new Label("PubID:"), pubidComboBox);
//        grid.addRow(4, new Label("Cost:"), costField);
//        grid.addRow(5, new Label("Retail price:"), retailField);
//        grid.addRow(6, new Label("Discount:"), discountField);
//        grid.addRow(7, new Label("Category:"), categoryField);
//
//        Button registerButton = new Button("Register");
//        registerButton.setOnAction(e -> {
//            registerBook(isbnField.getText(), titleField.getText(), pubdatePicker.getValue(),
//                    pubidComboBox.getValue(), costField.getText(), retailField.getText(),
//                    discountField.getText(), categoryField.getText());
//            stage.close();
//        });
//
//        Button cancelButton = new Button("Cancel");
//        cancelButton.setOnAction(e -> stage.close());
//
//        HBox buttonBox = new HBox(10, registerButton, cancelButton);
//        grid.add(buttonBox, 1, 8);
//
//        Scene scene = new Scene(grid);
//        stage.setScene(scene);
//        stage.show();
//    }
//
//    private void registerBook(String isbn, String title, LocalDate pubdate, String pubid, String cost,
//                              String retail, String discount, String category) {
//        try {
//            String sql = "CALL sp_Book_register(?, ?, ?, ?, ?, ?, ?, ?)";
//            CallableStatement stmt = connection.prepareCall(sql);
//            stmt.setString(1, isbn);
//            stmt.setString(2, title);
//            stmt.setDate(3, Date.valueOf(pubdate));
//            stmt.setString(4, pubid);
//            stmt.setDouble(5, Double.parseDouble(cost));
//            stmt.setDouble(6, Double.parseDouble(retail));
//            stmt.setDouble(7, Double.parseDouble(discount));
//            stmt.setString(8, category);
//
//            stmt.execute();
//            showAlert("Success", "Book registered successfully!");
//        } catch (SQLException e) {
//            showAlert("Error", "Failed to register book: " + e.getMessage());
//        }
//    }
//
//    private VBox createAuthorsContent() {
//        VBox content = new VBox(10);
//        content.setPadding(new Insets(20));
//
//        Label titleLabel = new Label("Author Management");
//        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
//
//        Button registerButton = new Button("Register New Author");
//        registerButton.setOnAction(e -> showAuthorRegistrationForm());
//
//        Button assignButton = new Button("Assign Author to Book");
//        assignButton.setOnAction(e -> showAuthorAssignmentForm());
//
//        registerButton.setDisable(true);
//        assignButton.setDisable(true);
//
//        content.getChildren().addAll(titleLabel, registerButton, assignButton);
//        return content;
//    }
//
//    private void showAuthorRegistrationForm() {
//        showAlert("Info", "Author registration functionality not implemented yet.");
//    }
//
//    private void showAuthorAssignmentForm() {
//        showAlert("Info", "Author assignment functionality not implemented yet.");
//    }
//
//    private VBox createCustomersContent() {
//        VBox content = new VBox(10);
//        content.setPadding(new Insets(20));
//
//        Label titleLabel = new Label("Customer Management");
//        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
//
//        Button registerButton = new Button("Register New Customer");
//        registerButton.setOnAction(e -> showCustomerRegistrationForm());
//
//        Button updateButton = new Button("Update Customer Information");
//        updateButton.setOnAction(e -> showCustomerUpdateForm());
//
//        registerButton.setDisable(true);
//        updateButton.setDisable(true);
//
//        content.getChildren().addAll(titleLabel, registerButton, updateButton);
//        return content;
//    }
//
//    private void showCustomerRegistrationForm() {
//        showAlert("Info", "Customer registration functionality not implemented yet.");
//    }
//
//    private void showCustomerUpdateForm() {
//        showAlert("Info", "Customer update functionality not implemented yet.");
//    }
//
//    private boolean isDatabaseConnected() {
//        if (connection == null) {
//            return false;
//        }
//        try {
//            return !connection.isClosed();
//        } catch (SQLException e) {
//            e.printStackTrace();
//            return false;
//        }
//    }
//
//    private void connectToDatabase() {
//        String selectedDatabase = databaseSelector.getValue();
//        String url, user, password;
//
//        if ("School".equals(selectedDatabase)) {
//            url = "jdbc:oracle:thin:@oracle1.centennialcollege.ca:1521:SQLD";
//            user = "COMP214_F24_er_13";
//            password = "password";
//        } else { // Remote
//            url = "jdbc:oracle:thin:@199.212.26.208:1521:SQLD";
//            user = "COMP214_F24_er_13";
//            password = "password";
//        }
//
//        new Thread(() -> {
//            try {
//                if (isDatabaseConnected()) {
//                    connection.close();
//                }
//                connection = DriverManager.getConnection(url, user, password);
//            
//                Platform.runLater(() -> {
//                    connectionStringLabel.setText("Connected: " + url);
//                    connectionStringLabel.setStyle("-fx-font-style: normal; -fx-text-fill: green;");
//                    System.out.println("Connection successful. Label updated to: " + connectionStringLabel.getText());
//
//                    // Enable all tabs
//                    for (Tab tab : tabPane.getTabs()) {
//                        tab.setDisable(false);
//                    }
//
//                    // Update Home tab content
//                    VBox homeContent = (VBox) tabPane.getTabs().get(0).getContent();
//                    homeContent.getChildren().get(0).setVisible(false); // Hide instruction label
//                    homeContent.getChildren().get(1).setVisible(true); // Show welcome label
//                    homeContent.getChildren().get(2).setVisible(true); // Show description label
//
//                    // Enable buttons in all tabs
//                    enableTabButtons(tabPane.getTabs().get(1)); // Books tab
//                    enableTabButtons(tabPane.getTabs().get(2)); // Authors tab
//                    enableTabButtons(tabPane.getTabs().get(3)); // Customers tab
//                });
//            
//                showAlert("Success", "Successfully connected to " + selectedDatabase + " database!");
//            } catch (SQLException e) {
//                Platform.runLater(() -> {
//                    connectionStringLabel.setText("Not connected");
//                    connectionStringLabel.setStyle("-fx-font-style: italic; -fx-text-fill: red;");
//                    System.out.println("Connection failed. Label updated to: " + connectionStringLabel.getText());
//
//                    // Disable all tabs except Home
//                    for (int i = 1; i < tabPane.getTabs().size(); i++) {
//                        tabPane.getTabs().get(i).setDisable(true);
//                    }
//
//                    // Update Home tab content
//                    VBox homeContent = (VBox) tabPane.getTabs().get(0).getContent();
//                    homeContent.getChildren().get(0).setVisible(true); // Show instruction label
//                    homeContent.getChildren().get(1).setVisible(false); // Hide welcome label
//                    homeContent.getChildren().get(2).setVisible(false); // Hide description label
//
//                    showAlert("Connection Error", "Failed to connect to the database: " + e.getMessage());
//                });
//                e.printStackTrace();
//            }
//        }).start();
//    }
//
//    private void enableTabButtons(Tab tab) {
//        VBox content = (VBox) tab.getContent();
//        for (javafx.scene.Node node : content.getChildren()) {
//            if (node instanceof Button) {
//                ((Button) node).setDisable(false);
//            }
//        }
//    }
//
//    private void showAlert(String title, String message) {
//        Platform.runLater(() -> {
//            Alert.AlertType type = title.toLowerCase().contains("error") ? Alert.AlertType.ERROR : Alert.AlertType.INFORMATION;
//            Alert alert = new Alert(type);
//            alert.setTitle(title);
//            alert.setHeaderText(null);
//            alert.setContentText(message);
//            alert.showAndWait();
//        });
//    }
//
//    public static void main(String[] args) {
//        launch(args);
//    }
//}
//

package application;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Callback;
import java.sql.*;
import java.text.NumberFormat;
import java.util.Locale;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class JLBookSalesApp extends Application {
	// Lorenzo//Angelo//Bianca
	private Connection connection;
	private ComboBox<String> databaseSelector;
	private Label connectionStringLabel;
	private ListView<Book> bookListView;
	private TabPane tabPane;
	private Label homeInstructionLabel;
	private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yy-MM-dd");
	private Button updateButton;
	private Button revertAllButton;
	private Button cancelButton;
	private Map<String, Book> originalBooks = new HashMap<>();

	@Override
	public void start(Stage primaryStage) {
		try {
			primaryStage.setTitle("JL Book Sales");

			VBox root = new VBox(10);
			root.setPadding(new Insets(10));

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

			tabPane = new TabPane();

			Tab homeTab = new Tab("Home");
			homeTab.setContent(createHomeContent());

			Tab booksTab = new Tab("Books");
			booksTab.setContent(createBooksContent());

			Tab authorsTab = new Tab("Authors");
			authorsTab.setContent(createAuthorsContent());

			Tab customersTab = new Tab("Customers");
			customersTab.setContent(createCustomersContent());

			tabPane.getTabs().addAll(homeTab, booksTab, authorsTab, customersTab);

			root.getChildren().addAll(connectionControls, tabPane);

			Scene scene = new Scene(root, 1000, 600);
			primaryStage.setScene(scene);
			primaryStage.show();
		} catch (Exception e) {
			e.printStackTrace();
			showAlert("Error", "Failed to start application: " + e.getMessage());
			Platform.exit();
		}
	}

	private VBox createHomeContent() {
		VBox content = new VBox(20);
		content.setPadding(new Insets(20));
		content.setAlignment(Pos.CENTER);

		homeInstructionLabel = new Label("Please use the connection controls above to connect to the database.");
		homeInstructionLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: #4a4a4a;");
		homeInstructionLabel.setWrapText(true);
		homeInstructionLabel.setMaxWidth(600);

		Label welcomeLabel = new Label("Welcome to JL Book Sales");
		welcomeLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

		Label descriptionLabel = new Label("Manage your books, authors, and customers with ease.");
		descriptionLabel.setStyle("-fx-font-size: 16px;");

		content.getChildren().addAll(homeInstructionLabel, welcomeLabel, descriptionLabel);
		welcomeLabel.setVisible(false);
		descriptionLabel.setVisible(false);

		return content;
	}

	private VBox createBooksContent() {
		VBox content = new VBox(10);
		content.setPadding(new Insets(20));

		Label titleLabel = new Label("Book Management");
		titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

		Button registerButton = new Button("Register New Book");
		registerButton.setOnAction(e -> showBookRegistrationForm());

		Button showBooksButton = new Button("Show Books");
		showBooksButton.setOnAction(e -> showBookListForm());

		content.getChildren().addAll(titleLabel, registerButton, showBooksButton);
		return content;
	}

	private void showBookListForm() {
		if (connection == null) {
			showAlert("Error", "No database connection. Please connect to a database first.");
			return;
		}

		Stage stage = new Stage();
		stage.setTitle("Book List");

		VBox layout = new VBox(10);
		layout.setPadding(new Insets(20));

		HBox header = createColumnHeaders();
		layout.getChildren().add(header);

		bookListView = new ListView<>();
		bookListView.setCellFactory(createBookCellFactory());
		bookListView.setPrefHeight(600);

		HBox buttonBox = createButtonBox();
		layout.getChildren().addAll(bookListView, buttonBox);
		VBox.setVgrow(bookListView, Priority.ALWAYS);

		Scene scene = new Scene(layout, 1200, 700);
		stage.setScene(scene);
		stage.show();

		loadBooksFromDatabase();
	}

	private HBox createColumnHeaders() {
		HBox header = new HBox(10);
		header.setAlignment(Pos.CENTER_LEFT);
		header.setPadding(new Insets(5));
		header.setStyle("-fx-background-color: #f0f0f0; -fx-font-weight: bold;");

		Label isbnHeader = new Label("ISBN");
		Label titleHeader = new Label("Title");
		Label pubdateHeader = new Label("Pub Date");
		Label pubidHeader = new Label("Pub ID");
		Label costHeader = new Label("Cost");
		Label retailHeader = new Label("Retail");
		Label discountHeader = new Label("Discount");
		Label categoryHeader = new Label("Category");

		isbnHeader.setPrefWidth(100);
		titleHeader.setPrefWidth(200);
		pubdateHeader.setPrefWidth(100);
		pubidHeader.setPrefWidth(60);
		costHeader.setPrefWidth(80);
		retailHeader.setPrefWidth(80);
		discountHeader.setPrefWidth(80);
		categoryHeader.setPrefWidth(100);

		header.getChildren().addAll(isbnHeader, titleHeader, pubdateHeader, pubidHeader, costHeader, retailHeader,
				discountHeader, categoryHeader);
		return header;
	}

	private HBox createButtonBox() {
		HBox buttonBox = new HBox(10);
		buttonBox.setAlignment(Pos.CENTER);

		updateButton = new Button("Update");
		revertAllButton = new Button("Revert All");
		cancelButton = new Button("Cancel");

		updateButton.setOnAction(e -> handleUpdate());
		revertAllButton.setOnAction(e -> handleRevertAll());
		cancelButton.setOnAction(e -> handleCancel(null, (Stage) cancelButton.getScene().getWindow()));

		buttonBox.getChildren().addAll(updateButton, revertAllButton, cancelButton);
		return buttonBox;
	}

	private Callback<ListView<Book>, ListCell<Book>> createBookCellFactory() {
		return listView -> new ListCell<Book>() {
			private TextField categoryField;
			private TextField costField;
			private TextField retailField;

			@Override
			protected void updateItem(Book book, boolean empty) {
				super.updateItem(book, empty);
				if (empty || book == null) {
					setText(null);
					setGraphic(null);
				} else {
					if (categoryField == null) {
						createFields();
					}
					updateFields(book);
					HBox container = createContainer(book);
					setGraphic(container);
				}
			}

			private void createFields() {
				categoryField = new TextField();
				costField = new TextField();
				retailField = new TextField();

				categoryField.setPrefWidth(100);
				costField.setPrefWidth(80);
				retailField.setPrefWidth(80);
			}

			private void updateFields(Book book) {
				categoryField.setText(book.getCategory());
				costField.setText(formatCurrency(book.getCost()));
				retailField.setText(formatCurrency(book.getRetail()));
			}

			private HBox createContainer(Book book) {
				HBox container = new HBox(10);
				container.setAlignment(Pos.CENTER_LEFT);
				container.setPadding(new Insets(5));

				Label isbnLabel = new Label(book.getIsbn());
				Label titleLabel = new Label(book.getTitle());
				Label pubdateLabel = new Label(book.getPubdate().format(dateFormatter));
				Label pubidLabel = new Label(String.valueOf(book.getPubid()));
				Label discountLabel = new Label(formatCurrency(book.getDiscount()));

				isbnLabel.setPrefWidth(100);
				titleLabel.setPrefWidth(200);
				pubdateLabel.setPrefWidth(100);
				pubidLabel.setPrefWidth(60);
				discountLabel.setPrefWidth(80);

				container.getChildren().addAll(isbnLabel, titleLabel, pubdateLabel, pubidLabel, costField, retailField,
						discountLabel, categoryField);
				return container;
			}
		};
	}

	private void handleUpdate() {
		ObservableList<Book> books = bookListView.getItems();
		for (Book book : books) {
			updateBookInDatabase(book);
		}
		originalBooks.clear();
		for (Book book : books) {
			originalBooks.put(book.getIsbn(), book.copy());
		}
		bookListView.refresh();
	}

	private void handleRevertAll() {
		ObservableList<Book> books = bookListView.getItems();
		for (Book book : books) {
			Book originalBook = originalBooks.get(book.getIsbn());
			book.setCategory(originalBook.getCategory());
			book.setCost(originalBook.getCost());
			book.setRetail(originalBook.getRetail());
		}
		bookListView.refresh();
	}

	private void handleCancel(Stage primaryStage, Stage currentStage) {
		currentStage.close();
		if (primaryStage != null) {
			primaryStage.show();
		}
	}

	private String formatCurrency(double amount) {
		NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.US);
		return currencyFormat.format(amount);
	}

	private void loadBooksFromDatabase() {
		if (connection == null) {
			showAlert("Error", "No database connection. Please connect to a database first.");
			return;
		}

		ObservableList<Book> books = FXCollections.observableArrayList();
		String query = "SELECT ISBN, TITLE, PUBDATE, PUBID, COST, RETAIL, DISCOUNT, CATEGORY FROM JL_BOOKS";

		try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(query)) {

			while (rs.next()) {
				Book book = new Book(rs.getString("ISBN"), rs.getString("TITLE"), rs.getDate("PUBDATE").toLocalDate(),
						rs.getString("PUBID"), rs.getDouble("COST"), rs.getDouble("RETAIL"), rs.getDouble("DISCOUNT"),
						rs.getString("CATEGORY"));
				books.add(book);
				originalBooks.put(book.getIsbn(), book.copy());
			}

			Platform.runLater(() -> {
				bookListView.setItems(books);
				System.out.println("Loaded " + books.size() + " books from the database.");
			});
		} catch (SQLException e) {
			e.printStackTrace();
			Platform.runLater(() -> {
				showAlert("Error", "Failed to load books: " + e.getMessage());
				System.err.println("SQL Error: " + e.getMessage());
			});
		}
	}

	private void updateBookInDatabase(Book book) {
		String query = "UPDATE JL_BOOKS SET CATEGORY = ?, COST = ?, RETAIL = ? WHERE ISBN = ?";

		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, book.getCategory());
			pstmt.setDouble(2, book.getCost());
			pstmt.setDouble(3, book.getRetail());
			pstmt.setString(4, book.getIsbn());

			int affectedRows = pstmt.executeUpdate();
			if (affectedRows > 0) {
				System.out.println("Book updated successfully!");
			} else {
				System.out.println("Failed to update book. No rows affected.");
			}
		} catch (SQLException e) {
			e.printStackTrace();
			showAlert("Error", "Failed to update book: " + e.getMessage());
		}
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
			if (validateInputs(isbnField, titleField, pubdatePicker, pubidComboBox, costField, retailField,
					discountField, categoryField)) {
				registerBook(isbnField.getText(), titleField.getText(), pubdatePicker.getValue(),
						pubidComboBox.getValue(), costField.getText(), retailField.getText(), discountField.getText(),
						categoryField.getText());
				stage.close();
			}
		});

		Button cancelButton = new Button("Cancel");
		cancelButton.setOnAction(e -> stage.close());

		HBox buttonBox = new HBox(10, registerButton, cancelButton);
		grid.add(buttonBox, 1, 8);

		Scene scene = new Scene(grid);
		stage.setScene(scene);
		stage.show();
	}

	// Added this for validation (Bianca)
	private boolean validateInputs(TextField isbnField, TextField titleField, DatePicker pubdatePicker,
			ComboBox<String> pubidComboBox, TextField costField, TextField retailField, TextField discountField,
			TextField categoryField) {
		StringBuilder errorMessage = new StringBuilder();

		if (isbnField.getText().trim().isEmpty()) {
			errorMessage.append("ISBN is required.\n");
		}
		if (titleField.getText().trim().isEmpty()) {
			errorMessage.append("Title is required.\n");
		}
		if (pubdatePicker.getValue() == null) {
			errorMessage.append("Publication date is required.\n");
		}
		if (pubidComboBox.getValue() == null) {
			errorMessage.append("Publisher ID is required.\n");
		}
		if (costField.getText().trim().isEmpty()) {
			errorMessage.append("Cost is required.\n");
		}
		if (retailField.getText().trim().isEmpty()) {
			errorMessage.append("Retail price is required.\n");
		}
		if (categoryField.getText().trim().isEmpty()) {
			errorMessage.append("Category is required.\n");
		}

		if (errorMessage.length() > 0) {
			showAlert("Validation Error", errorMessage.toString());
			return false;
		}

		return true;
	}

	// End of UPDATE
	//////////////////
	

	private void registerBook(String isbn, String title, LocalDate pubdate, String pubid, String cost, String retail,
			String discount, String category) {
		try {
			String sql = "CALL sp_Book_register(?, ?, ?, ?, ?, ?, ?, ?)";
			CallableStatement stmt = connection.prepareCall(sql);
			stmt.setString(1, isbn);
			stmt.setString(2, title);
			stmt.setDate(3, Date.valueOf(pubdate));
			stmt.setString(4, pubid);
//			stmt.setDouble(5, Double.parseDouble(cost));
//			stmt.setDouble(6, Double.parseDouble(retail));
//			stmt.setDouble(7, Double.parseDouble(discount));
	        
			// Handle empty cost
	        if (cost.trim().isEmpty()) {
	            stmt.setNull(5, Types.DOUBLE);
	        } else {
	            stmt.setDouble(5, Double.parseDouble(cost));
	        }
	        
	        // Handle empty retail
	        if (retail.trim().isEmpty()) {
	            stmt.setNull(6, Types.DOUBLE);
	        } else {
	            stmt.setDouble(6, Double.parseDouble(retail));
	        }
	        
			
			// Handle empty discount
	        if (discount.trim().isEmpty()) {
	            stmt.setNull(7, Types.DOUBLE);
	        } else {
	            stmt.setDouble(7, Double.parseDouble(discount));
	        }
	        
			stmt.setString(8, category);

			stmt.execute();
			showAlert("Success", "Book registered successfully!");
		} catch (SQLException e) {
			showAlert("Error", "Failed to register book: " + e.getMessage());
		} catch (NumberFormatException e) {
	        showAlert("Error", "Invalid number format. Please check cost, retail, and discount fields.");
	    }
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
		showAlert("Info", "Author registration functionality not implemented yet.");
	}

	private void showAuthorAssignmentForm() {
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
		showAlert("Info", "Customer registration functionality not implemented yet.");
	}

	private void showCustomerUpdateForm() {
		showAlert("Info", "Customer update functionality not implemented yet.");
	}

	private boolean isDatabaseConnected() {
		if (connection == null) {
			return false;
		}
		try {
			return !connection.isClosed();
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	private void connectToDatabase() {
		String selectedDatabase = databaseSelector.getValue();
		String url, user, password;

		if ("School".equals(selectedDatabase)) {
			url = "jdbc:oracle:thin:@oracle1.centennialcollege.ca:1521:SQLD";
			user = "COMP214_F24_er_19";
			password = "password";
		} else { // Remote
			url = "jdbc:oracle:thin:@199.212.26.208:1521:SQLD";
			user = "COMP214_F24_er_19";
			password = "password";
		}

		new Thread(() -> {
			try {
				if (isDatabaseConnected()) {
					connection.close();
				}
				connection = DriverManager.getConnection(url, user, password);

				Platform.runLater(() -> {
					connectionStringLabel.setText("Connected: " + url);
					connectionStringLabel.setStyle("-fx-font-style: normal; -fx-text-fill: green;");
					System.out.println("Connection successful. Label updated to: " + connectionStringLabel.getText());

					// Enable all tabs
					for (Tab tab : tabPane.getTabs()) {
						tab.setDisable(false);
					}

					// Update Home tab content
					VBox homeContent = (VBox) tabPane.getTabs().get(0).getContent();
					homeContent.getChildren().get(0).setVisible(false); // Hide instruction label
					homeContent.getChildren().get(1).setVisible(true); // Show welcome label
					homeContent.getChildren().get(2).setVisible(true); // Show description label

					// Enable buttons in all tabs
					enableTabButtons(tabPane.getTabs().get(1)); // Books tab
					enableTabButtons(tabPane.getTabs().get(2)); // Authors tab
					enableTabButtons(tabPane.getTabs().get(3)); // Customers tab
				});

				showAlert("Success", "Successfully connected to " + selectedDatabase + " database!");
			} catch (SQLException e) {
				Platform.runLater(() -> {
					connectionStringLabel.setText("Not connected");
					connectionStringLabel.setStyle("-fx-font-style: italic; -fx-text-fill: red;");
					System.out.println("Connection failed. Label updated to: " + connectionStringLabel.getText());

					// Disable all tabs except Home
					for (int i = 1; i < tabPane.getTabs().size(); i++) {
						tabPane.getTabs().get(i).setDisable(true);
					}

					// Update Home tab content
					VBox homeContent = (VBox) tabPane.getTabs().get(0).getContent();
					homeContent.getChildren().get(0).setVisible(true); // Show instruction label
					homeContent.getChildren().get(1).setVisible(false); // Hide welcome label
					homeContent.getChildren().get(2).setVisible(false); // Hide description label

					showAlert("Connection Error", "Failed to connect to the database: " + e.getMessage());
				});
				e.printStackTrace();
			}
		}).start();
	}

	private void enableTabButtons(Tab tab) {
		VBox content = (VBox) tab.getContent();
		for (javafx.scene.Node node : content.getChildren()) {
			if (node instanceof Button) {
				((Button) node).setDisable(false);
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

	public static void main(String[] args) {
		launch(args);
	}
}
