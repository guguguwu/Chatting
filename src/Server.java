import java.io.IOException;
import java.net.InetSocketAddress;

import java.nio.ByteOrder;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.CompletionHandler;

// Classes for broadcaster
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentLinkedQueue;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Server implements Callable<Boolean> {

    private class NetworkConnection {
    	
    	private AsynchronousSocketChannel channel;
    	private long id;
    	
    	public NetworkConnection(AsynchronousSocketChannel chan) throws IOException {
    		
    		this.channel = chan;
    		InetSocketAddress a = ((InetSocketAddress) chan.getRemoteAddress());
    		
    		ByteBuffer bb = ByteBuffer.wrap(new byte[4]);
    		bb.put(a.getAddress().getAddress(), 0, 4);
    		if (ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN)
    			bb.order(ByteOrder.LITTLE_ENDIAN);
    		bb.flip();
    		
    		// At the view point of server side,
    		// IPv4 address and port number of remote client cannot be same.
    		// So, it can be used to generate unique identification number.
    		this.id = (((long) bb.getInt()) << 16) + a.getPort();
    		
    		bb = null;
    	}
    	
    	public long getId() { return this.id; }
    	
    	public boolean isOpen() {
    		return this.channel.isOpen();
    	}
    	
    	public void send(AudioData a) {
    		this.channel.write(a.getMessageAsByteBuffer());
    	}
    	
    	protected void finalize() throws Throwable {
    		try {
    			this.channel.close();
    		} finally {
    			this.channel = null;
    		}
    	}
    	
    }
	
    // broadcaster
    private class Broadcaster implements Callable<Boolean> {

    	private Boolean isTerminatedNormally = Boolean.TRUE;

    	// Reference pointer for accessing
    	private ArrayList<NetworkConnection> 		clients;
    	private ConcurrentLinkedQueue<AudioData> 	queue;

    	public Broadcaster(
    			ArrayList<NetworkConnection> clients,
    			ConcurrentLinkedQueue<AudioData> queue) {
    		this.clients = clients;
    		this.queue = queue;
    	}
    	
		@Override
		public Boolean call() throws Exception {
			
			while (isAlive) {
				NetworkConnection client;
				AudioData dataMustBeSent = this.queue.poll();

				// Descending order due to obtaining performance
				for (int i = this.clients.size() - 1 ; 0 <= i ; i--) {
					client = this.clients.get(i);
					
					if (client.isOpen() &&
							dataMustBeSent.getId() != client.id) client.send(dataMustBeSent);
					else this.clients.remove(i);
				}
			}
			
			return this.isTerminatedNormally;
		}
    	
    }
    
	// Using ArrayList is more beneficial in common situations,
    // But in this situation we need to use ConcurrentLinkedDeque for assuring concurrency.
	// ref.) http://stackoverflow.com/questions/322715/when-to-use-linkedlist-over-arraylist
    
    //private ConcurrentLinkedDeque<NetworkConnection> clients
    //	= new ConcurrentLinkedDeque<NetworkConnection>();
    private ArrayList<NetworkConnection> clients = new ArrayList<NetworkConnection>();
    
    // I decided to use ConcurrentLinkedQueue instead of LinkedTransferQueue.
    // (LinkedTransferQueue is blocking queue)
    // perf. ref.) https://gist.github.com/normanmaurer/3180812
    private ConcurrentLinkedQueue<AudioData> broadcastQueue
    	= new ConcurrentLinkedQueue<AudioData>();

    private AsynchronousServerSocketChannel sSockChan;
    private boolean isAlive = true;
    private int 	port;
    
    public Server(int port) { this.port = port; }
    public int getPort() 	{ return this.port; }
    
    @Override
    public Boolean call() throws Exception {
    	
        try {
        	
        	this.sSockChan = AsynchronousServerSocketChannel.open().bind(new InetSocketAddress(this.port));
        	this.sSockChan.accept(null, new CompletionHandler<AsynchronousSocketChannel, Void> () {
            	
	            @Override
	            public void completed(AsynchronousSocketChannel ch, Void attachment) {
	            	
	            	// Allocate receiving data buffer, and handle this connection
	            	Logger.getGlobal().log(Level.INFO, "Established a connection ");
					try {
						clients.add(new NetworkConnection(ch));
					} catch (IOException e) {
						Logger.getGlobal().log(Level.SEVERE,
	            			"Error on receiving remote address of connection", e.getMessage());
					}
					
	            	ByteBuffer buf = ByteBuffer.allocate(88200);
	            	ch.read(buf, attachment, new CompletionHandler<Integer, Void>() {
	            	
	            		@Override
	            		public void completed(Integer res, Void attachment) {

	            			// End of Stream ; connection is disconnected
	            			if (res < 0) {
	            				
	            			}
	            			else {
	            				// If there is any read data, copy it to the broadcast queue
	            				if (1 < res) {
	            					broadcastQueue.offer(new AudioData(buf));
	            					buf.clear();
	            				}
	            				
	            				// Continue reading data on channel
	            				ch.read(buf, attachment, this);
	            			}
	            		}
	            			
	            		@Override
	            		public void failed(Throwable exc, Void attachment) {
	            			if (exc instanceof ClosedChannelException) {
	            				Logger.getGlobal().log(Level.INFO,
	    	            			"A channel connection was closed", exc.getMessage());
	            				clients.remove(ch);
	            			}
	            			else Logger.getGlobal().log(Level.SEVERE,
	            				"Error on reading data with a client", exc.getMessage());
	            		}
	            	});

	            	// Accept the next connection
	            	sSockChan.accept(null, this);
	            }
            
	            @Override
	            public void failed(Throwable exc, Void attachment) {
	            	Logger.getGlobal().log(Level.SEVERE, "Error on establishing connection with a client", exc.getMessage());
	            }
        	});

            ExecutorService executor = Executors.newSingleThreadExecutor();
            Future<Boolean> future = executor.submit(new Broadcaster(this.clients, this.broadcastQueue));
            Logger.getGlobal().log(Level.INFO, "Server started with port number " + port + ".");
            Boolean result = future.get();
            
        } catch (IOException exc) {
        	Logger.getGlobal().log(Level.SEVERE, "Error on initiating socket (port number " + port + ")");
            throw new Exception("Error " + exc);
        }
        
    }
    
    public Boolean start() {
        ExecutorService executor = Executors.newCachedThreadPool();
        Future<Boolean> future = executor.submit(new Broadcaster(this.clients, this.broadcastQueue));
        Boolean result = future.get();
    }
}
