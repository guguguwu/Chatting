import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.scene.Scene;

import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

import javafx.stage.Stage;

public class MainForm extends Application {
	
	// Underscore character for keyboard mnemonic.
	private static final String[][] priMenuStrings =
	{
		{ "����(_C)" , "����(_C).." },
		{ "����(_T)", "����(_P).." }
	};
	
	/*
	// method reference array
	private final Runnable[][] menuItemMethods =
	{
		{ null, () -> BeginConnect() },
		{ null, () -> Preferences() }
	};
	
	// private methods
	private EventHandler<ActionEvent> BeginConnect()
	{
		return null;
		
	}
	
	private void Preferences()
	{
		
	}
	*/
	
	// Start capturing audio.
	public void StartCapture() {
		CaptureAudio ca = new CaptureAudio();

		try {
			ca.Capture();
		} catch (Exception e) {
			new Alert(Alert.AlertType.ERROR, e.getMessage()).showAndWait();
		}
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		
		// Start constructing main form. 
		BorderPane pane 	= new BorderPane();
		
		TextField textfield = new TextField();
		VBox vbox			= new VBox();
		
		// Part managing menus
		MenuBar menubar 	= new MenuBar();
		Menu[] priMenu		= new Menu[priMenuStrings.length];

		for (int i = 0 ; i < priMenuStrings.length ; i++)
		{
			priMenu[i] = new Menu(priMenuStrings[i][0]);
			menubar.getMenus().add(priMenu[i]);
			
			for (int j = 1 ; j < priMenuStrings[i].length ; j++)
			{
				MenuItem tempItem = new MenuItem(priMenuStrings[i][j]);
				//tempItem.setOnAction((ActionEvent e) -> menuItemMethods[i][j] );
				priMenu[i].getItems().add(tempItem);
			}
		}
		
		// custom code
		priMenu[0].getItems().get(0).setOnAction((ActionEvent e) -> { StartCapture(); });
		
		Scene scene = new Scene(pane, 640, 480);
		
		pane.setTop(menubar);
		pane.setLeft(vbox);
		pane.setBottom(textfield);
		
		primaryStage.setScene(scene);
		primaryStage.setTitle(ConstString.defaultTitle);
		primaryStage.centerOnScreen();
		//primaryStage.setOnCloseRequest(e -> Platform.exit());
		
		primaryStage.show();
	}
}