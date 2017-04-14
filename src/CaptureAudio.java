import java.util.ListIterator;

import javax.sound.sampled.DataLine;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Port;

import gov.nist.javax.sip.*;

public class CaptureAudio {
	
	AudioFormat format;
	ListIterator<SourceDataLine> srcLineIter;
	TargetDataLine line;
	
	public void Capture() throws Exception {
		
		// It looks like that Java searches default audio device in AudioSystem.
		// See the source code :
		// http://hg.openjdk.java.net/jdk8u/jdk8u/jdk/file/8c93eb3fa1c0/src/share/classes/javax/sound/sampled/AudioSystem.java
		
		// format is an AudioFormat object
		// It's okay though AudioFormat object is null at this time.
		DataLine.Info info = new DataLine.Info(TargetDataLine.class, format); 
		if (!AudioSystem.isLineSupported(info)) {
		    // Handle the error
			throw new Exception(ConstString.Error.NoOutput);
		}
		
		// Obtain and open the line. Cast received line to TargetDataLine.
		// In this point we cannot use Port.Info.MICROPHONE instance.
		// (PortMixer class has no definition of read & write method)
		try {
			line = (TargetDataLine) AudioSystem.getLine(info);
			int t = line.getFormat().getSampleSizeInBits();
			line.open();
			line.start();

		} catch (LineUnavailableException ex) {
			// Handle the error ...
			throw new Exception(ConstString.Error.LineUnavailable);
		}

	}
	
	public void StartCapture() {
		
	}
	
	protected void finalize() throws Throwable {
		if (line.isOpen()) line.close();
	}
}
