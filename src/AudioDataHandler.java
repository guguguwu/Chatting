import java.nio.ByteBuffer;
import java.nio.channels.CompletionHandler;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import java.util.HashMap;
import java.util.Map;

import java.util.logging.Level;
import java.util.logging.Logger;

public class AudioDataHandler {

	private class ActiveUser {

		private long 	id;
		private String 	username;
		private double	volume;
	
		public void setUsername(String name) { this.username = name; }
		
		public ActiveUser(long rID, String rName) {
			this.id = rID;
			this.username = rName;
			this.volume = 1.0;
		}
		
		public void putData(AudioData data, AudioPlayback outputObject) {
			// process received data
			switch (data.getType()) {
				case (AudioData.MessageType.TEXT):
			
					break;
				case (AudioData.MessageType.AUDIO):
					outputObject.write(data.getData(), AudioData.BytePos.VAL_POS, data.getLength());
				// may place visually 'decorating' method after.
			}
		}
		
	}
	
	private boolean isAlive = true;
	
	private NetworkConnection connection;
	private AudioCapture ac;
	private AudioPlayback ap;
	
	private Map<Long, ActiveUser> userlist;
	
	public AudioDataHandler(NetworkConnection nc) {
		
		this.connection = nc;
		this.userlist = new HashMap<Long, ActiveUser>();
		
		byte[] buf = new byte[88200];
		ByteBuffer playbackBuf = ByteBuffer.wrap(buf);
		
		// write my captured audio data
		try {
			this.ac = new AudioCapture(nc);
	        ExecutorService executor = Executors.newSingleThreadExecutor();
	        executor.submit(new Callable<Boolean>() {
	        	byte[] nBuf = new byte[88200];
	        	ByteBuffer captureBuf = ByteBuffer.wrap(this.nBuf);
	        	
	        	@Override
	        	public Boolean call() {
	        		while (isAlive) {
	        			ac.read(this.nBuf, 0, ac.available());
	        			connection.write(captureBuf);
	        			this.captureBuf.clear();
	        		}
	        		return Boolean.TRUE;
	        	}
	        });
	        
			// read other clients' data
			this.ap = new AudioPlayback();
			this.connection.read(playbackBuf, new CompletionHandler<Integer, Void>() {
				
				@Override 
				public void completed(Integer res, Void attachment) {
					AudioData ad = new AudioData(playbackBuf);
					
					long id = ad.getId();
					ActiveUser user = userlist.get(id);
					if (user == null) {
						user = userlist.put(id, new ActiveUser(id, "Anonymous"));
					}
					user.putData(ad, ap);
					
					playbackBuf.clear();
					connection.read(playbackBuf, this);
				}
			
				@Override
				public void failed(Throwable exc, Void attachment) {
					Logger.getGlobal().log(Level.SEVERE, "Could not read playback data: ", exc.getMessage());
				}
			});
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
