// Classes for handling IPv4 address
import java.io.IOException;
import java.net.InetSocketAddress;

// Classes for asynchronous server-side channel
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
//import java.util.concurrent.Future;

// Classes for inner containers
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

// Classes for logging
import java.util.logging.Level;
import java.util.logging.Logger;

public class Server {

    private class ClientConnection {
    	
    	private AsynchronousSocketChannel channel;
    	private long id;
    	
    	public ClientConnection(AsynchronousSocketChannel chan) throws IOException {
    		
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
    	
    	// Given unique identification number must not be modified.
    	// So we don't need to implement set method.
    	public long getId() { return this.id; }
    	
    	public boolean isOpen() {
    		return this.channel.isOpen();
    	}
    	
    	public void send(AudioData a) {
    		this.channel.write(a.getMessageAsByteBuffer());
    	}
    	
    	public void closeChannel() {
    		try {
    			this.channel.close();
    		} catch (IOException e) {
    			Logger.getGlobal().log(Level.SEVERE, "Error occurred on closing socketchannel: ", e.getMessage());
    		}
    	}
    	
    }
	
    
    // broadcaster
    private class Broadcaster implements Callable<Boolean> {

    	private Boolean isTerminatedNormally = Boolean.TRUE;

    	// Reference pointer for accessing
    	private Map<Long, ClientConnection> 	clients;
    	private Queue<AudioData> 				queue;

    	public Broadcaster(
    			Map<Long, ClientConnection> clients,
    			Queue<AudioData> queue) {
    		this.clients = clients;
    		this.queue = queue;
    	}
    	
		@Override
		public Boolean call() throws Exception {
			
            Logger.getGlobal().log(Level.INFO,
            	"Server started broadcasting received audio data.");
			
			while (isAlive) {
				AudioData dataMustBeSent = this.queue.poll();
				this.clients.forEach((id, nc) -> {
					if (nc.isOpen()) {
						if (dataMustBeSent.getId() != id) nc.send(dataMustBeSent);
					}
					else this.clients.remove(id);
				});
				
				/* deactivated
				 * 
					ClientConnection client;
					// Descending order due to obtaining performance
					for (int i = this.clients.size() - 1 ; 0 <= i ; i--) {
						client = this.clients.get(i);
						
						if (client.isOpen() &&
								dataMustBeSent.getId() != client.id) client.send(dataMustBeSent);
						else this.clients.remove(i);
					}
				*/
			}
			
			return this.isTerminatedNormally;
		}
		
		@Override
		protected void finalize() throws Throwable {
			this.isTerminatedNormally = null;
			this.clients = null;
			this.queue	 = null;
			
			super.finalize();
		}
    	
    }
    
    // Using ArrayList rather than LinkedList is more beneficial in common situations,
    // (ref.) http://stackoverflow.com/questions/322715/when-to-use-linkedlist-over-arraylist)
    // But in this situation I'd use ConcurrentHashMap.
    private Map<Long, ClientConnection> clients;
    
    // I decided to use ConcurrentLinkedQueue instead of LinkedTransferQueue.
    // (LinkedTransferQueue is blocking queue)
    // perf. ref.) https://gist.github.com/normanmaurer/3180812
    private Queue<AudioData> broadcastQueue;

    private AsynchronousServerSocketChannel sSockChan;
    private boolean isAlive = true;
    private int 	port;
    
    public Server(int port) throws IOException {

    	this.port = port;
    	this.sSockChan = AsynchronousServerSocketChannel.open().bind(new InetSocketAddress(this.port));
    	
    }
    public int getPort() 	{ return this.port; }
    
    public boolean start() {

    	this.clients 		= new ConcurrentHashMap<Long, ClientConnection>();
    	this.broadcastQueue = new ConcurrentLinkedQueue<AudioData>();
    	
    	this.sSockChan.accept(null, new CompletionHandler<AsynchronousSocketChannel, Void> () {
        	
            @Override
            public void completed(AsynchronousSocketChannel ch, Void attachment) {
            	
            	// Allocate receiving data buffer, and handle this connection
            	Logger.getGlobal().log(Level.INFO, "Established a connection ");
				try {
					ClientConnection cc = new ClientConnection(ch);
					clients.putIfAbsent(cc.getId(), cc);
					
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
	            			if (isAlive && exc instanceof ClosedChannelException) {
	            				Logger.getGlobal().log(Level.INFO,
	    	            			"A channel connection was closed", exc.getMessage());
	            				clients.remove(ch);
	            			}
	            			else Logger.getGlobal().log(Level.SEVERE,
	            				"Error on reading data with a client", exc.getMessage());
	            		}
	            	});
					
				} catch (IOException e) {
					Logger.getGlobal().log(Level.SEVERE,
            			"Error on receiving remote address of connection", e.getMessage());
				}
				
            	// Accept the next connection
            	sSockChan.accept(null, this);
            }
        
            @Override
            public void failed(Throwable exc, Void attachment) {
            	Logger.getGlobal().log(Level.SEVERE, "Error on establishing connection with a client", exc.getMessage());
            }
    	});

        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(new Broadcaster(this.clients, this.broadcastQueue));
        Logger.getGlobal().log(Level.INFO, "Server started listening with port number " + port + ".");

    	return this.isAlive;

    }
    
    public void close() {
    
    	this.isAlive = false;
    	
    	try {
			this.sSockChan.close();
			this.clients.forEach((id, nc) -> {
				if (nc != null && nc.isOpen()) nc.closeChannel();
			});
			this.clients.clear();
			
	    	// ConcurrentLinkedQueue doesn't support clear() method.
	    	while (!this.broadcastQueue.isEmpty()) this.broadcastQueue.poll();
	    	
	    	this.sSockChan = null;
	    	this.clients = null;
	    	this.broadcastQueue = null;
	    	
		} catch (IOException e) {
			Logger.getGlobal().log(Level.SEVERE,
				"Error occurred on shutdown server operation: ", e.getMessage());
		}
    }
    
    @Override
    protected void finalize() throws Throwable {
    	this.close();
    	super.finalize();
    }
}
