module JL_Books_Project {
	requires javafx.controls;
	requires java.sql;
	requires javafx.graphics;
	requires ojdbc6;
	
	opens application to javafx.graphics, javafx.fxml;
}
