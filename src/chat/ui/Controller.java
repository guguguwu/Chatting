package chat.ui;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;

public interface Controller {

	default void loadFxml(URL location, ResourceBundle resources) {
		try {
			FXMLLoader loader = new FXMLLoader(location);
			loader.setController(this);
			loader.setRoot(this);
			loader.setResources(resources);
			loader.load();
			initialize(location, resources);
		} catch (IOException exc) {
			// ignore
		}
	}

	default void loadFxml(URL location) {
		loadFxml(location, null);
	}

	void initialize(URL location, ResourceBundle resources);
	void setStage(Stage stage);
	
}
