import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.channels.CompletionHandler;
import java.util.NoSuchElementException;
import java.util.Optional;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.event.EventTarget;
import javafx.scene.Scene;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class MainForm extends Application {
	
	// fields
	private NetworkConnection	connection	= null;
	private AudioDataHandler	dataHandler = null;
	private Server				localServer = null;
	
	private BorderPane 	pane;	
	private TextField 	textfield;
	private VBox 		vbox;
	private Scene 		scene;
	
	private MenuBar 	menubar; 
	private Menu[] 		priMenu;

	private MenuItemProperties[][] menuItemProperties = {
		{ new ConnectMenuItemProperty() },
		{ new PreferencesMenuItemProperty() }
	};
	
	// Underscore character for keyboard mnemonic.
	private static final String[] priMenuStrings = { "연결(_C)" , "도구(_T)" };
	/*
	private static final String[][] priMenuStrings =
	{
		{ "연결(_C)" , "접속(_C).." },
		{ "도구(_T)", "설정(_P).." }
	};
	*/
	
	// method reference array
	/*
	@SuppressWarnings("rawtypes")
	private EventHandler[][] menuItemMethods =
	{
		{ new BeginConnect() },
		{ new Preferences() }
	};
	*/


	private class MenuItemProperties {

		private String menuItemText;
		private EventHandler<ActionEvent> eventHandler;
		private MenuItemProperties toggleProperty;
		
		public String getMenuItemText() { return this.menuItemText; }
		public EventHandler<ActionEvent> getEventHandler() { return this.eventHandler; }
		

		public MenuItemProperties getToggleProperty() { return this.toggleProperty; }
		public void setToggleProperty(MenuItemProperties callee) {
			this.setToggleProperty(this, callee);
		}
		private void setToggleProperty(MenuItemProperties caller, MenuItemProperties callee) {
			this.toggleProperty = callee;
			callee.setToggleProperty(callee, caller);
		}
		
		MenuItemProperties(String itemText, EventHandler<ActionEvent> handler) {
			this.menuItemText = itemText;
			this.eventHandler = handler;
			assert (this.getMenuItemText() != null && this.getEventHandler() != null);
		}
	}
	
	private class ConnectMenuItemProperty extends MenuItemProperties {
		ConnectMenuItemProperty() {
			super("연결(_C)...", new EventHandler<ActionEvent>() {
				@Override
				public void handle(ActionEvent event) {
					try {
						Optional<ConnectDialogResult> result = new ConnectDialogForm().showAndWait();
						ConnectDialogResult resObj = result.get();
							
						if (resObj.isRequiredRunningServer()) {
							if (localServer == null)
								localServer = new Server(ConstString.defaultPort);
							localServer.start();
						}
						connection = new NetworkConnection(resObj.getSockAddr());
						connection.beginConnect(new CompletionHandler<Void, Object>() {

							@Override
							public void completed(Void result, Object attachment) {
								// change menu item detail
								//Class<? extends Object> srcClass = e.getSource().getClass();
								((MenuItem) event.getSource()).setOnAction
									(((MenuItemProperties) attachment).getToggleProperty().getEventHandler());
								dataHandler = new AudioDataHandler(connection);
							}

							@Override
							public void failed(Throwable exc, Object attachment) {
								
							}
						});
					} catch (IOException ioe) {
						
					} catch (NoSuchElementException nsee) {};
				}
			});
			
			// link toggling-menu item
			DisconnectMenuItemProperty disconnMenu = new DisconnectMenuItemProperty();
			this.setToggleProperty(disconnMenu);
			disconnMenu.setToggleProperty(this);
		}
	}
	
	private class DisconnectMenuItemProperty extends MenuItemProperties {
		DisconnectMenuItemProperty() {
			super("연결 해제(_D)", new EventHandler<ActionEvent>() {
				@Override
				public void handle(ActionEvent event) {
					
				}
			});
		}
	}
	
	// not implemented yet
	private class PreferencesMenuItemProperty extends MenuItemProperties {
		PreferencesMenuItemProperty() {
			super("설정(_P)...", new EventHandler<ActionEvent>() {
				@Override
				public void handle(ActionEvent event) {
					
				}
			});
		}
	}
	
	
	/*
	// private methods
	private class BeginConnect implements EventHandler<ActionEvent> {
		
		@Override
		public void handle(ActionEvent e) {
			try {
				Optional<ConnectDialogResult> result = new ConnectDialogForm().showAndWait();
				ConnectDialogResult resObj = result.get(); 
					
				if (resObj.isRequiredRunningServer()) {
					if (localServer == null)
						localServer = new Server(ConstString.defaultPort);
					localServer.start();
				}
				connection = new NetworkConnection(resObj.getSockAddr());
				connection.beginConnect(new CompletionHandler<Void, Void>() {

					@Override
					public void completed(Void result, Void attachment) {
						// change menu item detail
						//Class<? extends Object> srcClass = e.getSource().getClass();
						((MenuItem) e.getSource()).setOnAction(new Disconnect());
						dataHandler = new AudioDataHandler(connection);
					}

					@Override
					public void failed(Throwable exc, Void attachment) {
						
					}
				});
			} catch (IOException ioe) {
				
			} catch (NoSuchElementException nsee) {};
		}
	}
	
	private class Disconnect implements EventHandler<ActionEvent> {
		@Override
		public void handle(ActionEvent event) {

		}
	}
	
	// not implemented yet
	private class Preferences implements EventHandler<ActionEvent> {
		@Override
		public void handle(ActionEvent e) {
			
		}
	}
	/// end of private methods
	 */

	@Override
	@SuppressWarnings("unchecked")
	public void start(Stage primaryStage) throws Exception {
		
		// Start constructing main form. 
		pane 		= new BorderPane();
		textfield 	= new TextField();
		vbox		= new VBox();
		
		scene 		= new Scene(pane, 640, 480);
		
		// Part managing menus
		menubar = new MenuBar();
		priMenu	= new Menu[priMenuStrings.length];

		/*
		for (int i = 0 ; i < priMenuStrings.length ; i++)
		{
			priMenu[i] = new Menu(priMenuStrings[i][0]);
			menubar.getMenus().add(priMenu[i]);
			
			for (int j = 1 ; j < priMenuStrings[i].length ; j++)
			{
				MenuItem tempItem = new MenuItem(priMenuStrings[i][j]);
				priMenu[i].getItems().add(tempItem);
				priMenu[i].getItems().get(j - 1).setOnAction
					((EventHandler<ActionEvent>) menuItemMethods[i][j - 1]);
			}
		}
		*/
		
		for (int i = 0 ; i < priMenuStrings.length ; i++)
		{
			priMenu[i] = new Menu(priMenuStrings[i]);
			menubar.getMenus().add(priMenu[i]);
			
			for (int j = 0 ; j < menuItemProperties[i].length ; j++)
			{
				MenuItem tempItem = new MenuItem(menuItemProperties[i][j].getMenuItemText());
				priMenu[i].getItems().add(tempItem);
				priMenu[i].getItems().get(j).setOnAction(menuItemProperties[i][j].getEventHandler());
			}
		}
		
		pane.setTop(menubar);
		pane.setLeft(vbox);
		pane.setBottom(textfield);
		
		primaryStage.setScene(scene);
		primaryStage.setTitle(ConstString.defaultTitle);
		primaryStage.centerOnScreen();
		primaryStage.setOnCloseRequest(e -> stop(true));
		
		primaryStage.show();
	}
	
	public void stop(boolean isFormClosing) {
		try {
			if (dataHandler != null) { dataHandler.stop(); dataHandler = null; }
			if (connection != null) { connection.close(); connection = null; }
			if (localServer != null) {
				if (isFormClosing) { localServer.close(); localServer = null; }
				else if (showConfirmation("서버가 동작 중입니다. 서버를 비활성화할까요?")) { localServer.close(); }
			}
		} catch (IOException ioe) { }
	}
	
	// method showing alert type dialog
	public static boolean showConfirmation(Exception e) {
		return (new Alert(Alert.AlertType.CONFIRMATION, e.getMessage(),
			ButtonType.YES, ButtonType.NO).showAndWait().get() == ButtonType.YES);
	}
	public static boolean showConfirmation(String str) {
		return (new Alert(Alert.AlertType.CONFIRMATION, str,
			ButtonType.YES, ButtonType.NO).showAndWait().get() == ButtonType.YES);
	}
	public static void showError(Exception e) {
		new Alert(Alert.AlertType.ERROR, e.getMessage()).showAndWait();
	}
	public static void showError(String str) {
		new Alert(Alert.AlertType.ERROR, str).showAndWait();
	}
}
