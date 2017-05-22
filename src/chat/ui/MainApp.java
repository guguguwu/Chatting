package chat.ui;

import chat.ui.main.MainWindow;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {
	

	@Override
	public void start(Stage primaryStage) throws Exception {

		MainWindow mainWindow = new MainWindow();

		Scene scene = new Scene(mainWindow, 400, 400);
		primaryStage.setScene(scene);
		primaryStage.show();

	}

	public static void main(String[] args) {
		launch(args);
	}

}
