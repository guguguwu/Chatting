import java.nio.channels.CompletionHandler;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;

import javafx.scene.control.Alert;
//import javafx.scene.control.Button;
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
		{ "연결(_C)" , "접속(_C).." },
		{ "도구(_T)", "설정(_P).." }
	};
	
	
	// method reference array
	@SuppressWarnings("rawtypes")
	private final EventHandler[][] menuItemMethods =
	{
		{ new BeginConnect() },
		{ new Preferences() }
	};
	
	// private methods
	private class BeginConnect implements EventHandler<ActionEvent> {
		@Override
		public void handle(ActionEvent e) {
			StartCapture();
		}
	}
	
	private class Preferences implements EventHandler<ActionEvent> {
		@Override
		public void handle(ActionEvent e) {
			
		}
	}
	/// end of private methods
	
	// Start capturing audio.
	public void StartCapture() {
		try {
			Server server = new Server(0);
			NetworkConnection nc = new NetworkConnection
				(new java.net.InetSocketAddress(java.net.InetAddress.getLoopbackAddress(), server.getPort()));
			nc.beginConnect(new CompletionHandler<Void, Void>() {

				@Override
				public void completed(Void result, Void attachment) {
					AudioDataHandler dataHandler = new AudioDataHandler(nc);
				}

				@Override
				public void failed(Throwable exc, Void attachment) {
					
				}
			});
		} catch (Exception e) {
			new Alert(Alert.AlertType.ERROR, e.getMessage()).showAndWait();
		}
	}

	@Override
	@SuppressWarnings("unchecked")
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
				priMenu[i].getItems().get(j - 1).setOnAction
					((EventHandler<ActionEvent>) menuItemMethods[i][j - 1]);
			}
		}
		
		// custom code
		//priMenu[0].getItems().get(0).setOnAction((ActionEvent e) -> { StartCapture(); });
		
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
