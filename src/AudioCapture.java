import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;

public class AudioCapture {
	
	private AudioFormat format;
	private TargetDataLine trgDataLine;

	public AudioCapture(NetworkConnection nc) {
		this.format = null;
		this.trgDataLine = null;
	}
	
	public void start() throws Exception, LineUnavailableException {
		// It looks like that Java searches default audio device in AudioSystem.
		// See the source code :
		// http://hg.openjdk.java.net/jdk8u/jdk8u/jdk/file/8c93eb3fa1c0/src/share/classes/javax/sound/sampled/AudioSystem.java
		
		// format is an AudioFormat object
		// It's okay though AudioFormat object is null at this time.
		DataLine.Info info = new DataLine.Info(TargetDataLine.class, format); 
		if (!AudioSystem.isLineSupported(info)) {
		    // Handle the error
			throw new Exception(ConstString.Error.NoInput);
		}
		
		// Obtain and open the line. Cast received line to TargetDataLine.
		// In this point we cannot use Port.Info.MICROPHONE instance.
		// (PortMixer class has no definition of read & write method)
		trgDataLine = (TargetDataLine) AudioSystem.getLine(info);
		trgDataLine.open(format);
		trgDataLine.start();
	}
	
	public int available() { return trgDataLine.available(); }
	public int read(byte[] dst, int offset, int length) {
		return trgDataLine.read(dst, offset, length);
	}
	
	public void stop() {
		trgDataLine.close();
	}
	
	protected void finalize() throws Throwable {
		if (trgDataLine.isOpen()) trgDataLine.close();
	}
}
