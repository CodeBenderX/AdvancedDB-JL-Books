package application;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class BookRegistrationForm extends Application {
    @Override
    public void start(Stage primaryStage) {
    	
    	//Test - Bianca
    	
        // Create main container
        VBox root = new VBox(15);
        root.setStyle("-fx-background-color: #2B2B2B;");
        root.setPadding(new Insets(20));

        // Title
        Label titleLabel = new Label("Book registration Form");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 24));
        titleLabel.setTextFill(Color.RED);

        // Create grid for form fields
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(15);
        grid.setStyle("-fx-background-color: #2B2B2B;");

        // Create form elements
        Label isbnLabel = new Label("ISBN :");
        TextField isbnField = new TextField();
        
        Label titleFieldLabel = new Label("Title:");
        TextField bookTitleField = new TextField();
        
        Label pubdateLabel = new Label("Pubdate:");
        TextField pubdateField = new TextField();
        
        Label pubIdLabel = new Label("PubID: < It can be drop down>");
        ComboBox<String> pubIdComboBox = new ComboBox<>();
        pubIdComboBox.getItems().addAll("Publisher 1", "Publisher 2", "Publisher 3");
        
        Label costLabel = new Label("Cost:");
        TextField costField = new TextField();
        
        Label retailLabel = new Label("Retail price :");
        TextField retailField = new TextField();
        
        Label discountLabel = new Label("Discount:");
        TextField discountField = new TextField();
        
        Label categoryLabel = new Label("Category :");
        TextField categoryField = new TextField();

        // Style all labels
        for (Label label : new Label[]{isbnLabel, titleFieldLabel, pubdateLabel, pubIdLabel, 
                                     costLabel, retailLabel, discountLabel, categoryLabel}) {
            label.setTextFill(Color.WHITE);
            label.setFont(Font.font("System", FontWeight.BOLD, 12));
        }

        // Add elements to grid
        grid.add(isbnLabel, 0, 0);
        grid.add(isbnField, 0, 1);
        grid.add(titleFieldLabel, 1, 0);
        grid.add(bookTitleField, 1, 1);
        grid.add(pubdateLabel, 2, 0);
        grid.add(pubdateField, 2, 1);
        
        grid.add(pubIdLabel, 0, 2);
        grid.add(pubIdComboBox, 0, 3);
        grid.add(costLabel, 1, 2);
        grid.add(costField, 1, 3);
        grid.add(retailLabel, 2, 2);
        grid.add(retailField, 2, 3);
        
        grid.add(discountLabel, 0, 4);
        grid.add(discountField, 0, 5, 3, 1);
        
        grid.add(categoryLabel, 0, 6);
        grid.add(categoryField, 0, 7, 3, 1);

        // Create buttons
        HBox buttonBox = new HBox(20);
        buttonBox.setAlignment(Pos.CENTER);
        
        Button cancelButton = new Button("Cancel");
        cancelButton.setStyle("-fx-background-color: #000000; -fx-text-fill: white; -fx-border-color: white;");
        cancelButton.setPrefWidth(100);
        
        Button registerButton = new Button("Register");
        registerButton.setStyle("-fx-background-color: #0000FF; -fx-text-fill: red; -fx-border-color: white;");
        registerButton.setPrefWidth(100);

        buttonBox.getChildren().addAll(cancelButton, registerButton);

        // Add all components to root
        root.getChildren().addAll(titleLabel, grid, buttonBox);

        // Create scene
        Scene scene = new Scene(root);
        primaryStage.setTitle("Book Registration");
        primaryStage.setScene(scene);
        primaryStage.show();

        // Button actions
        cancelButton.setOnAction(e -> primaryStage.close());
        registerButton.setOnAction(e -> handleRegistration());
    }

    private void handleRegistration() {
        // Add registration logic here
        System.out.println("Registration button clicked");
    }

    public static void main(String[] args) {
        launch(args);
    }
}