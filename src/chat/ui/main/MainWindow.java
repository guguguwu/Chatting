package chat.ui.main;

import java.net.URL;
import java.util.ResourceBundle;

import chat.ui.Controller;
import chat.ui.i18n.I18N;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class MainWindow extends VBox implements Controller{
	
	@FXML
    private Button btn2;
	
	public MainWindow() {
		loadFxml(getClass().getResource("Main.fxml"), I18N.getBundle());
	}
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		btn2.setText(I18N.getString("ui.main.btn2", "param1"));
	}
	
	@Override
	public void setStage(Stage stage) {
		// TODO Auto-generated method stub
	}

}
