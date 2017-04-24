import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

public class AudioPlayback {
	
	private AudioFormat 	format;
	private SourceDataLine 	srcDataLine;
	
	public AudioPlayback() {
		this.format = null;
		this.srcDataLine = null;
	}
	
	public void start() throws Exception, LineUnavailableException {
		DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
		if (!AudioSystem.isLineSupported(info)) {
		    // Handle the error
			throw new Exception(ConstString.Error.NoOutput);
		}
		
		srcDataLine = (SourceDataLine) AudioSystem.getLine(info);
		srcDataLine.open(format);
		srcDataLine.start();
	}
	
	public void write(byte[] bytes, int offset, int length) {
		srcDataLine.write(bytes, offset, length);
	}
	
	public void stop() {
		srcDataLine.close();
	}
}
