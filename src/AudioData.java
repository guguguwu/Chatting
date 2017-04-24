import java.nio.ByteBuffer;

// TLV(Type-Length-Value) message + long integer for identification
public class AudioData {
	
	// Message type constant
	public static class MessageType {
		public static final int TEXT = 0;
		public static final int AUDIO = 1;
		
		public static final int USER_JOINED = 101;
		public static final int USER_LEFT	= 102;
		public static final int USER_CHANGED_NICK 	= 103;
		public static final int USER_CHANGED_STATUS	= 104;
	}
	
	// Byte position constant
	public static class BytePos {
		public static final int ID_POS = 0; 
		public static final int TYPE_POS = 8;
		public static final int LEN_POS = 12;
		public static final int VAL_POS = 16;
	}
	
	private long 	id;		// 32-bit IPv4 Address + 16-bit port number ; minimum 48-bit.
	private int 	type;	// message type
	private int		length;	// value length
	
	private byte[] 	data;	// array for saving actual full message data
							// (id, type, length, value)
	
	public AudioData(ByteBuffer dataBuf) {
		// First, copy data from the reference
		this.data = dataBuf.array().clone();
		
		// And separate each variable data
		ByteBuffer bb = ByteBuffer.wrap(this.data);
		this.id 	= bb.getLong();
		this.type 	= bb.getInt();
		this.length = bb.getInt();
	}
	
	public long getId() { return this.id; }
	public int getType() { return this.type; }
	public int getLength() { return this.length; }
	public byte[] getData() { return this.data; }

	public ByteBuffer getMessageAsByteBuffer() {
		return ByteBuffer.wrap(this.data);
	}
}
