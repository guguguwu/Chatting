import java.net.InetSocketAddress;

// custom class for delivering essential connection-establishing data 
public class ConnectDialogResult {
	
	private boolean runServer;
	private int		serverPort;
	private InetSocketAddress 	sockAddr;
	private String 				nickname;
	
	public ConnectDialogResult
			(boolean rServ, int servPort, InetSocketAddress addr, String nick) {
		this.runServer = rServ;
		this.serverPort = servPort;
		this.sockAddr = addr;
		this.nickname = nick;
	}
	
	public boolean isRequiredRunningServer() {
		return runServer;
	}

	public int getServerPort() {
		return serverPort;
	}

	public InetSocketAddress getSockAddr() {
		return sockAddr;
	}

	public String getNickname() {
		return nickname;
	}
}