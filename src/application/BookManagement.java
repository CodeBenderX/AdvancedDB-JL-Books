package application;

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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class BookManagement {
    private Connection connection;
    private ListView<Book> bookListView;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yy-MM-dd");
    private Button updateButton;
    private Button revertAllButton;
    private Button cancelButton;
    private Map<String, Book> originalBooks = new HashMap<>();

    public BookManagement(Connection connection) {
        this.connection = connection;
    }

    public void showBookListForm(Stage primaryStage) {
        Stage stage = new Stage();
        stage.setTitle("Book List");

        VBox layout = new VBox(10);
        layout.setPadding(new Insets(20));

        HBox header = createColumnHeaders();
        layout.getChildren().add(header);

        bookListView = new ListView<>();
        bookListView.setCellFactory(createBookCellFactory());
        bookListView.setPrefHeight(600);

        HBox buttonBox = createButtonBox(stage, primaryStage);
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

    private HBox createButtonBox(Stage currentStage, Stage primaryStage) {
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);

        updateButton = new Button("Update");
        revertAllButton = new Button("Revert All");
        cancelButton = new Button("Cancel");
        Button deleteButton = new Button("Delete Selected");

        updateButton.setOnAction(e -> handleUpdate());
        revertAllButton.setOnAction(e -> handleRevertAll());
        cancelButton.setOnAction(e -> handleCancel(primaryStage, currentStage));
        deleteButton.setOnAction(e -> deleteSelectedBooks());

        buttonBox.getChildren().addAll(updateButton, revertAllButton, deleteButton, cancelButton);
        return buttonBox;
    }

    private Callback<ListView<Book>, ListCell<Book>> createBookCellFactory() {
        return listView -> new ListCell<Book>() {
            private TextField categoryField;
            private TextField costField;
            private TextField retailField;
            private Button updateButton;
            private Button revertButton;

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
                updateButton = new Button("Update");
                revertButton = new Button("Revert");

                categoryField.setPrefWidth(100);
                costField.setPrefWidth(80);
                retailField.setPrefWidth(80);

                setupFieldListeners(categoryField, "category");
                setupFieldListeners(costField, "cost");
                setupFieldListeners(retailField, "retail");
                
                categoryField.textProperty().addListener((observable, oldValue, newValue) -> {
                    if (newValue != null) {
                        String upperCase = newValue.toUpperCase();
                        if (!newValue.equals(upperCase)) {
                            categoryField.setText(upperCase);
                        }
                    }
                });

                updateButton.setOnAction(e -> {
                    Book book = getItem();
                    if (book != null) {
                        updateBookInDatabase(book);
                    }
                });
                
                revertButton.setOnAction(e -> {
                    Book book = getItem();
                    if (book != null) {
                        revertBookChanges(book);
                    }
                });
                
                updateButton.setVisible(false);
                revertButton.setVisible(false);
            }

            private void setupFieldListeners(TextField field, String property) {
                field.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
                    if (!isFocused) {
                        validateAndUpdateField(field, property);
                    }
                });

                field.setOnAction(event -> validateAndUpdateField(field, property));

                field.textProperty().addListener((observable, oldValue, newValue) -> {
                    if (field == categoryField) {
                        if (newValue.length() > 12) {
                            field.setText(oldValue);
                            Platform.runLater(() -> showAlert("Category Limit Reached", "Category can only have 12 characters."));
                        } else {
                            field.setText(newValue.toUpperCase());
                        }
                    } else if (field == costField || field == retailField) {
                        if (!newValue.matches("\\$?\\d{0,3}(\\.\\d{0,2})?")) {
                            field.setText(oldValue);
                            Platform.runLater(() -> showAlert("Invalid Input", 
                                field == costField ? "Cost must be a valid number with up to 3 digits and 2 decimal places (max 999.99)." 
                                                   : "Retail price must be a valid number with up to 3 digits and 2 decimal places (max 999.99)."));
                        }
                    }
                });
            }

            private void validateAndUpdateField(TextField field, String property) {
                Book book = getItem();
                if (book != null) {
                    String newValue = field.getText();
                    boolean isValid = true;

                    if (field == categoryField) {
                        if (newValue.length() > 12) {
                            isValid = false;
                            showAlert("Invalid Category", "Category must be 12 characters or less.");
                        }
                    } else if (field == costField || field == retailField) {
                        if (!newValue.matches("\\$?\\d{1,3}(\\.\\d{0,2})?")) {
                            isValid = false;
                            showAlert("Invalid Input", 
                                field == costField ? "Cost must be a valid number with up to 3 digits and 2 decimal places (max 999.99)." 
                                                   : "Retail price must be a valid number with up to 3 digits and 2 decimal places (max 999.99).");
                        }
                    }

                    if (isValid) {
                        updateBookProperty(book, property, newValue);
                    } else {
                        updateFields(book);
                    }
                }
            }

            private void updateFields(Book book) {
                categoryField.setText(book.getCategory());
                costField.setText(formatCurrency(book.getCost()));
                retailField.setText(formatCurrency(book.getRetail()));

                categoryField.setStyle(book.isCategoryChanged() ? "-fx-background-color: lightgreen;" : "");
                costField.setStyle(book.isCostChanged() ? "-fx-background-color: lightgreen;" : "");
                retailField.setStyle(book.isRetailChanged() ? "-fx-background-color: lightgreen;" : "");

                updateButton.setVisible(book.isChanged());
                revertButton.setVisible(book.isChanged());
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

                container.getChildren().addAll(
                    isbnLabel, titleLabel, pubdateLabel, pubidLabel,
                    costField, retailField, discountLabel, categoryField, updateButton, revertButton
                );
                return container;
            }
            
            private void revertBookChanges(Book book) {
                Book originalBook = originalBooks.get(book.getIsbn());
                book.setCategory(originalBook.getCategory());
                book.setCost(originalBook.getCost());
                book.setRetail(originalBook.getRetail());
                book.setChanged(false);
                book.setCategoryChanged(false);
                book.setCostChanged(false);
                book.setRetailChanged(false);
                updateFields(book);
                bookListView.refresh();
            }
        };
    }

    public void deleteSelectedBooks() {
        ObservableList<Book> selectedBooks = bookListView.getSelectionModel().getSelectedItems();
        if (selectedBooks.isEmpty()) {
            showAlert("Info", "No books selected for deletion.");
            return;
        }

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Deletion");
        confirmAlert.setHeaderText("Delete Selected Books");
        confirmAlert.setContentText("Are you sure you want to delete the selected books?");

        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                for (Book book : selectedBooks) {
                    deleteBookFromDatabase(book);
                }
                loadBooksFromDatabase();
            }
        });
    }

    private void deleteBookFromDatabase(Book book) {
        String sql = "{CALL sp_Book_delete(?)}";
        try (CallableStatement stmt = connection.prepareCall(sql)) {
            stmt.setString(1, book.getIsbn());
            stmt.execute();
            System.out.println("Book deleted successfully: " + book.getIsbn());
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to delete book: " + e.getMessage());
        }
    }

    private void handleUpdate() {
        ObservableList<Book> books = bookListView.getItems();
        for (Book book : books) {
            if (book.isChanged()) {
                updateBookInDatabase(book);
                book.setChanged(false);
            }
        }
        bookListView.refresh();
        showAlert("Success", "All changes have been saved to the database.");
    }

    private void handleRevertAll() {
        ObservableList<Book> books = bookListView.getItems();
        for (Book book : books) {
            Book originalBook = originalBooks.get(book.getIsbn());
            book.setCategory(originalBook.getCategory());
            book.setCost(originalBook.getCost());
            book.setRetail(originalBook.getRetail());
            book.setChanged(false);
            book.setCategoryChanged(false);
            book.setCostChanged(false);
            book.setRetailChanged(false);
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

    public void loadBooksFromDatabase() {
        ObservableList<Book> books = FXCollections.observableArrayList();
        String query = "SELECT ISBN, TITLE, PUBDATE, PUBID, COST, RETAIL, DISCOUNT, CATEGORY " +
                       "FROM JL_BOOKS " +
                       "ORDER BY PUBDATE DESC, ISBN DESC";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                Book book = new Book(
                    rs.getString("ISBN"),
                    rs.getString("TITLE"),
                    rs.getDate("PUBDATE").toLocalDate(),
                    rs.getString("PUBID"),
                    rs.getDouble("COST"),
                    rs.getDouble("RETAIL"),
                    rs.getDouble("DISCOUNT"),
                    rs.getString("CATEGORY")
                );
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
        String sql = "{CALL sp_Book_update(?, ?, ?, ?)}";

        try (CallableStatement stmt = connection.prepareCall(sql)) {
            stmt.setString(1, book.getIsbn());
            stmt.setString(2, book.getCategory());
            stmt.setDouble(3, book.getCost());
            stmt.setDouble(4, book.getRetail());

            stmt.execute();
            System.out.println("Book updated successfully: " + book.getIsbn());

            // Update the original book in the map
            originalBooks.put(book.getIsbn(), book.copy());
            
            // Reset the changed flags
            book.setChanged(false);
            book.setCategoryChanged(false);
            book.setCostChanged(false);
            book.setRetailChanged(false);

            // Refresh the ListView
            Platform.runLater(() -> {
                bookListView.refresh();
                showAlert("Success", "Book updated successfully: " + book.getIsbn());
            });
            
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to update book: " + e.getMessage());
        }
    }

    private void updateBookProperty(Book book, String property, String newValue) {
        if (book == null) return;

        boolean changed = false;
        switch (property) {
            case "category":
                if (!newValue.equals(book.getCategory())) {
                    book.setCategory(newValue);
                    book.setCategoryChanged(true);
                    book.setChanged(true);
                    changed = true;
                }
                break;
            case "cost":
                double newCost = parseCurrency(newValue);
                if (newCost != book.getCost()) {
                    book.setCost(newCost);
                    book.setCostChanged(true);
                    book.setChanged(true);
                    changed = true;
                }
                break;
            case "retail":
                double newRetail = parseCurrency(newValue);
                if (newRetail != book.getRetail()) {
                    book.setRetail(newRetail);
                    book.setRetailChanged(true);
                    book.setChanged(true);
                    changed = true;
                }
                break;
        }

        if (changed) {
            book.setChanged(true);
            bookListView.refresh();
        }
    }

    private double parseCurrency(String value) {
        try {
            return Double.parseDouble(value.replaceAll("[^\\d.]", ""));
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    public void showBookRegistrationForm() {
        Stage stage = new Stage();
        stage.setTitle("Register New Book");

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(30));
        grid.setVgap(10);
        grid.setHgap(10);

        TextField isbnField = new TextField();
        TextField titleField = new TextField();
        DatePicker pubdatePicker = new DatePicker();
        pubdatePicker.setStyle("-fx-pref-width: 120px;");
        setupPubdateValidation(pubdatePicker);
        Label dateFormatLabel = new Label("(yyyy-MM-dd)");
        HBox pubdateBox = new HBox(7, pubdatePicker, dateFormatLabel);
        pubdateBox.setAlignment(Pos.CENTER_LEFT);
        ComboBox<String> pubidComboBox = new ComboBox<>();
        try {
            String query = "SELECT PUBID, NAME FROM JL_PUBLISHER ORDER BY PUBID";
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                String pubid = rs.getString("PUBID");
                String name = rs.getString("NAME");
                pubidComboBox.getItems().add(pubid + " - " + name);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load publishers: " + e.getMessage());
        }
        TextField costField = new TextField();
        TextField retailField = new TextField();
        TextField discountField = new TextField();
        TextField categoryField = new TextField();
        
        // Field validations
        setupFieldValidations(isbnField, titleField, costField, retailField, discountField, categoryField);

        grid.addRow(0, new Label("ISBN:"), isbnField);
        grid.addRow(1, new Label("Title:"), titleField);
        grid.addRow(2, new Label("Pubdate:"), pubdateBox);
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

    private void setupFieldValidations(TextField isbnField, TextField titleField, TextField costField,
                                       TextField retailField, TextField discountField, TextField categoryField) {
        // ISBN validation
        isbnField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                String filtered = newValue.replaceAll("[^a-zA-Z0-9]", "");
                if (filtered.length() > 10) {
                    filtered = filtered.substring(0, 10);
                    Platform.runLater(() -> showAlert("ISBN Limit Reached", "ISBN can only have 10 characters."));
                }
                if (!filtered.equals(newValue)) {
                    isbnField.setText(filtered);
                }
            }
        });

        // Title validation
        titleField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && newValue.length() > 30) {
                titleField.setText(oldValue);
                Platform.runLater(() -> showAlert("Title Limit Reached", "Title can only have 30 characters."));
            }
        });

        // Cost validation
        costField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !newValue.matches("\\d{0,3}(\\.\\d{0,2})?")) {
                costField.setText(oldValue);
                Platform.runLater(() -> showAlert("Invalid Cost", "Cost must be a valid number with up to 3 digits and 2 decimal places (max 999.99)."));
            }
        });

        // Retail validation
        retailField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !newValue.matches("\\d{0,3}(\\.\\d{0,2})?")) {
                retailField.setText(oldValue);
                Platform.runLater(() -> showAlert("Invalid Retail Price", "Retail price must be a valid number with up to 3 digits and 2 decimal places (max 999.99)."));
            }
        });

        // Discount validation
        discountField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !newValue.isEmpty() && !newValue.matches("\\d{0,2}(\\.\\d{0,2})?")) {
                discountField.setText(oldValue);
                Platform.runLater(() -> showAlert("Invalid Discount", "Discount must be a valid number with up to 2 digits and 2 decimal places (max 99.99) or empty."));
            }
        });

        // Category validation
        categoryField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                String upperCase = newValue.replaceAll("[^a-zA-Z0-9]", "").toUpperCase();
                if (upperCase.length() > 12) {
                    upperCase = upperCase.substring(0, 12);
                    Platform.runLater(() -> showAlert("Category Limit Reached", "Category can only have 12 characters."));
                }
                if (!newValue.equals(upperCase)) {
                    categoryField.setText(upperCase);
                }
            }
        });
    }
    
    private void setupPubdateValidation(DatePicker pubdatePicker) {
        pubdatePicker.getEditor().textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null) return;

            String filtered = newValue.replaceAll("[^0-9-]", "");
            if (!filtered.equals(newValue)) {
                pubdatePicker.getEditor().setText(filtered);
            }

            // Ensure correct format (yyyy-MM-dd)
            if (filtered.length() > 10) {
                filtered = filtered.substring(0, 10);
                pubdatePicker.getEditor().setText(filtered);
            }

            if (filtered.length() == 4 || filtered.length() == 7) {
                filtered += "-";
                pubdatePicker.getEditor().setText(filtered);
            }
        });
    }

    private boolean validateInputs(TextField isbnField, TextField titleField, DatePicker pubdatePicker,
                                   ComboBox<String> pubidComboBox, TextField costField, TextField retailField,
                                   TextField discountField, TextField categoryField) {
        StringBuilder errorMessage = new StringBuilder();

        String isbn = isbnField.getText().trim();
        if (isbn.isEmpty()) {
            errorMessage.append("ISBN is required.\n");
        } else if (isbn.length() > 10) {
            errorMessage.append("ISBN must be 10 characters or less.\n");
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
        if (!isValidNumber(costField.getText())) {
            errorMessage.append("Cost must be a valid number.\n");
        }
        if (!isValidNumber(retailField.getText())) {
            errorMessage.append("Retail price must be a valid number.\n");
        }
        if (!discountField.getText().isEmpty() && !isValidNumber(discountField.getText())) {
            errorMessage.append("Discount must be a valid number or empty.\n");
        }

        String category = categoryField.getText().trim();
        if (category.isEmpty()) {
            errorMessage.append("Category is required.\n");
        } else if (category.length() > 12) {
            errorMessage.append("Category must be 12 characters or less.\n");
        }

        if (errorMessage.length() > 0) {
            showAlert("Validation Error", errorMessage.toString());
            return false;
        }

        return true;
    }

    private boolean isValidNumber(String value) {
        if (value == null || value.trim().isEmpty()) {
            return true; // Allow empty fields
        }
        try {
            Double.parseDouble(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public void registerBook(String isbn, String title, LocalDate pubdate, String pubid, String cost, String retail,
                             String discount, String category) {
        try {
            String sql = "CALL sp_Book_register(?, ?, ?, ?, ?, ?, ?, ?)";
            CallableStatement stmt = connection.prepareCall(sql);
            stmt.setString(1, isbn);
            stmt.setString(2, title);
            stmt.setDate(3, Date.valueOf(pubdate));
            stmt.setString(4, pubid.split(" - ")[0]);
            
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