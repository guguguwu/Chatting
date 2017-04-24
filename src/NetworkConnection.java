import java.io.IOException;
import java.net.SocketAddress;

import java.nio.ByteBuffer;

import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

import java.nio.channels.AlreadyConnectedException;
import java.nio.channels.ConnectionPendingException;
import java.nio.channels.NotYetConnectedException;
import java.nio.channels.ReadPendingException;
import java.nio.channels.ShutdownChannelGroupException;
import java.nio.channels.UnresolvedAddressException;
import java.nio.channels.UnsupportedAddressTypeException;
import java.nio.channels.WritePendingException;

public class NetworkConnection {

	private AsynchronousSocketChannel channel;
	private SocketAddress remoteAddr;
	
	public NetworkConnection(SocketAddress addr) throws IOException {
		this.channel = AsynchronousSocketChannel.open();
		this.remoteAddr = addr;
	}
	
	public void beginConnect() throws UnresolvedAddressException,
			UnsupportedAddressTypeException, AlreadyConnectedException, ConnectionPendingException {
		this.channel.connect(remoteAddr);
	}
	
	public void beginConnect(CompletionHandler<Void, Void> handler) throws UnresolvedAddressException,
		UnsupportedAddressTypeException, AlreadyConnectedException, ConnectionPendingException, ShutdownChannelGroupException {
		this.channel.connect(remoteAddr, null, handler);
	}
	
	public void write(ByteBuffer src) throws
			WritePendingException, NotYetConnectedException {
		this.channel.write(src);
	}
	
	public void read(ByteBuffer dst, CompletionHandler<Integer, Void> handler) throws
			IllegalArgumentException, ReadPendingException, NotYetConnectedException {
		this.channel.read(dst, null, handler);
	}
	
	public void close() throws IOException {
		this.channel.close();
	}
}
