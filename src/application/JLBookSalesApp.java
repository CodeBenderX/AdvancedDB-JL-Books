package application;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
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
import java.util.regex.Pattern;

import javafx.animation.PauseTransition;
import javafx.util.Duration;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;

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

			booksTab.setDisable(true);
			authorsTab.setDisable(true);
			customersTab.setDisable(true);

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
		bookListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

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
		Button deleteButton = new Button("Delete Selected");

		updateButton.setOnAction(e -> handleUpdate());
		revertAllButton.setOnAction(e -> handleRevertAll());
		cancelButton.setOnAction(e -> handleCancel(null, (Stage) cancelButton.getScene().getWindow()));
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
						updateBookProperty(getItem(), property, field.getText());
					}
				});

				field.setOnAction(event -> {
					updateBookProperty(getItem(), property, field.getText());
				});
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

				container.getChildren().addAll(isbnLabel, titleLabel, pubdateLabel, pubidLabel, costField, retailField,
						discountLabel, categoryField, updateButton, revertButton);
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

	private void deleteSelectedBooks() {
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
		String sql = "{CALL sp_Book_update(?, ?, ?, ?)}";

		try (CallableStatement stmt = connection.prepareCall(sql)) {
			stmt.setString(1, book.getIsbn());
			stmt.setString(2, book.getCategory());
			stmt.setDouble(3, book.getCost());
			stmt.setDouble(4, book.getRetail());

			stmt.execute();
			System.out.println("Book updated successfully: " + book.getIsbn());

			originalBooks.put(book.getIsbn(), book.copy());

			book.setChanged(false);
			book.setCategoryChanged(false);
			book.setCostChanged(false);
			book.setRetailChanged(false);

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
		if (book == null)
			return;

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

	// validation
	private boolean validateInputs(TextField isbnField, TextField titleField, DatePicker pubdatePicker,
			ComboBox<String> pubidComboBox, TextField costField, TextField retailField, TextField discountField,
			TextField categoryField) {
		StringBuilder errorMessage = new StringBuilder();

		// ISBN validation
		String isbn = isbnField.getText().trim();
		if (isbn.isEmpty()) {
			errorMessage.append("ISBN is required.\n");
		} else if (isbn.length() > 10) {
			errorMessage.append("ISBN must not exceed 10 characters.\n");
		}

		// Title validation
		if (titleField.getText().trim().isEmpty()) {
			errorMessage.append("Title is required.\n");
		} else if (titleField.getText().length() > 30) {
			errorMessage.append("Title must not exceed 30 characters.\n");
		}

		// Publication date validation
		if (pubdatePicker.getValue() == null) {
			errorMessage.append("Publication date is required.\n");
		}

		// Publisher ID validation
		if (pubidComboBox.getValue() == null) {
			errorMessage.append("Publisher ID is required.\n");
		}

		// Cost validation
		if (costField.getText().trim().isEmpty()) {
			errorMessage.append("Cost is required.\n");
		} else if (!isValidNumber(costField.getText(), 5, 2)) {
			errorMessage.append("Cost must be a valid number with up to 5 digits and 2 decimal places.\n");
		}

		// Retail price validation
		if (retailField.getText().trim().isEmpty()) {
			errorMessage.append("Retail price is required.\n");
		} else if (!isValidNumber(retailField.getText(), 5, 2)) {
			errorMessage.append("Retail price must be a valid number with up to 5 digits and 2 decimal places.\n");
		}

		// Discount validation (optional field)
		if (!discountField.getText().trim().isEmpty() && !isValidNumber(discountField.getText(), 4, 2)) {
			errorMessage.append("Discount must be a valid number with up to 4 digits and 2 decimal places.\n");
		}

		// Category validation
		if (categoryField.getText().trim().isEmpty()) {
			errorMessage.append("Category is required.\n");
		} else if (categoryField.getText().length() > 12) {
			errorMessage.append("Category must not exceed 12 characters.\n");
		}

		if (errorMessage.length() > 0) {
			showAlert("Validation Error", errorMessage.toString());
			return false;
		}

		return true;
	}

	private boolean isValidNumber(String value, int totalDigits, int decimalPlaces) {
		String regex = "^\\d{1," + (totalDigits - decimalPlaces) + "}(\\.\\d{1," + decimalPlaces + "})?$";
		return Pattern.matches(regex, value);
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
			user = "COMP214_F24_er_13";
			password = "password";
		} else { // Remote
			url = "jdbc:oracle:thin:@199.212.26.208:1521:SQLD";
			user = "COMP214_F24_er_13";
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
