import java.net.InetSocketAddress;

import javafx.beans.value.ChangeListener;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;

public class ConnectDialogForm extends Dialog<ConnectDialogResult> {

	GridPane 	gridPane;
	ToggleGroup tglGroup;
	RadioButton rbClient, rbHost;
	
	TextField 	remoteAddrField, 	nicknameField;
	Tooltip 	remoteAddrTooltip, 	nicknameTooltip;
	
	Node 		connectButton;
	ButtonType 	connectButtonType;
	
	// Create the custom dialog.
	public ConnectDialogForm() {

		this.setTitle("���� ���� �� ����");
	
		// allocating part
		gridPane = new GridPane();
		tglGroup = new ToggleGroup();
		rbClient = new RadioButton("���� Ŭ���̾�Ʈ(_R)");
		rbHost	 = new RadioButton("ȣ��Ʈ(_H)");
		
		remoteAddrField 	= new TextField();
		nicknameField 		= new TextField();
		remoteAddrTooltip 	= new Tooltip("IPv4 �ּ� �Ǵ� ������ �̸��� �Է��ϼ���.");
		nicknameTooltip 	= new Tooltip("���� ������ ����ϰ��� �� ����� �г����� �Է��ϼ���.");

		// assigning & aligning part
		connectButtonType = new ButtonType("����(_C)", ButtonData.OK_DONE);
		this.getDialogPane().getButtonTypes().addAll(connectButtonType, ButtonType.CANCEL);
		connectButton = this.getDialogPane().lookupButton(connectButtonType);
		
		rbClient.setToggleGroup(tglGroup);
		rbHost.setToggleGroup(tglGroup);
		rbClient.setSelected(true);
		rbHost.selectedProperty().addListener((obsVal, oldVal, newVal) -> {
		    remoteAddrField.setDisable(newVal);
		});
		
		gridPane.setHgap(10);
		gridPane.setVgap(10);
		gridPane.setPadding(new Insets(20, 150, 10, 10));
		
		gridPane.add(rbClient, 0, 0);
		gridPane.add(rbHost, 1, 0);
		
		gridPane.add(new Label("���� �ּ�:"), 0, 1);
		gridPane.add(remoteAddrField, 1, 1);
		gridPane.add(new Label("�г���:"), 0, 2);
		gridPane.add(nicknameField, 1, 2);
		
		remoteAddrField.setTooltip(remoteAddrTooltip);
		nicknameField.setTooltip(nicknameTooltip);
			
		// validating handler part
		ChangeListener<String> validatingListener = (obsVal, oldVal, newVal) -> {
			connectButton.setDisable(
				isInputRequired(rbHost.isSelected(), nicknameField.getText(), remoteAddrField.getText()));
		};
		remoteAddrField.textProperty().addListener(validatingListener);
		nicknameField.textProperty().addListener(validatingListener);
		
		this.getDialogPane().setContent(gridPane);
		connectButton.setDisable(true);
		
		this.setResultConverter(dialogButton -> {
		    if (dialogButton == connectButtonType) {
				try {
					String addr = null;
					int port = ConstString.defaultPort;
					
					boolean isHost = rbHost.isSelected();
					if (isHost) {
						addr = /*"127.0.0.1"*/"loopback";
					}
					else {
						String addrText = remoteAddrField.getText().trim().toLowerCase();
						int liof = addrText.lastIndexOf(':');	// last index of
						
						if (-1 < liof && liof < addrText.length() - 1) {
							// Split hostname and port number.
							// In this time, parseInt method can throw NumberFormatException.
							addr = addrText.substring(0, liof);
							port = Integer.parseInt(addrText.substring(liof + 1));
						}
						else addr = addrText;
					}
					
					ConnectDialogResult result = new ConnectDialogResult
						(isHost, port, new InetSocketAddress(addr, port), nicknameField.getText());
					return result;
					
				} catch (NumberFormatException nfe) {
					MainForm.showError("��Ʈ ��ȣ�μ� ���ڰ� �ƴ� �ٸ� ���ڰ� ���ԵǾ����ϴ�.");
				} catch (IllegalArgumentException iae) {
					MainForm.showError(iae.getMessage());
				}
		    }
		    return null;
		});
	}

	public boolean isInputRequired(boolean isHost, String nickname, String address) {
		return (isHost ? nickname.trim().isEmpty() :
			(nickname.trim().isEmpty() || address.trim().isEmpty()));
	}
}
