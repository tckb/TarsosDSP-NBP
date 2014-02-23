package be.hogent.tarsos.dsp.ui.layers;

import java.awt.Color;
import java.awt.Graphics2D;
import java.io.File;
import java.io.IOException;

import javax.sound.sampled.UnsupportedAudioFileException;

import be.hogent.tarsos.dsp.AudioDispatcher;
import be.hogent.tarsos.dsp.AudioEvent;
import be.hogent.tarsos.dsp.AudioProcessor;
import be.hogent.tarsos.dsp.ui.Axis;
import be.hogent.tarsos.dsp.ui.CoordinateSystem;


public class WaveFormLayer implements Layer {

	private final Color waveFormColor;
	private final CoordinateSystem cs; 
	private final File audioFile;
	
	private float[] samples;
	private float sampleRate;
	

	public WaveFormLayer(CoordinateSystem cs,File audioFile) {
		waveFormColor = Color.black;
		this.cs = cs;
		this.audioFile = audioFile;
		initialise();
	}

	public void draw(Graphics2D graphics) {
		graphics.setColor(waveFormColor);
		this.drawWaveForm(graphics);
	}

	private void drawWaveForm(Graphics2D graphics) {
		final int waveFormXMin = (int) cs.getMin(Axis.X);
		final int waveFormXMax = (int) cs.getMax(Axis.X);
		graphics.setColor(Color.GRAY);
		graphics.drawLine(waveFormXMin, 0, waveFormXMax,0);
		graphics.setColor(Color.BLACK);
		if (samples != null && samples.length > 0) {
			//graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
		
			final int waveFormHeightInUnits = (int) cs.getDelta(Axis.Y);
			final float lengthInMs = samples.length/sampleRate*1000;
			final int amountOfSamples = samples.length;
			
			float sampleCalculateFactor = amountOfSamples / lengthInMs;
			
			int amplitudeFactor = waveFormHeightInUnits / 2;
			
			//every millisecond:
			int step = 1;
			
			for (int i = Math.max(0, waveFormXMin); i < Math.min(waveFormXMax, lengthInMs); i+= step) {
				int index = (int) (i * sampleCalculateFactor);
				if (index < samples.length) {
					graphics.drawLine(i, 0, i,(int) (samples[index] * amplitudeFactor));
				}
			}
			//graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_OFF);
		}
	}

	public void initialise() {
		try {			
			AudioDispatcher adp = AudioDispatcher.fromFile(audioFile,1024, 0);
			int amountOfSamples = (int) adp.durationInFrames();
			adp = AudioDispatcher.fromFile(audioFile,amountOfSamples, 0);
			sampleRate = adp.getFormat().getSampleRate();
			adp.addAudioProcessor(new AudioProcessor() {
				public void processingFinished() {
				}
				public boolean process(AudioEvent audioEvent) {
					float[] audioFloatBuffer = audioEvent.getFloatBuffer();
					WaveFormLayer.this.samples = audioFloatBuffer.clone();
					return true;
				}
			});
			new Thread(adp).start();
		} catch (UnsupportedAudioFileException e) {
			e.printStackTrace();
		} catch (IOException e2) {
			e2.printStackTrace();
		}
	}

	@Override
	public String getName() {
		return "Waveform layer";
	}
}
