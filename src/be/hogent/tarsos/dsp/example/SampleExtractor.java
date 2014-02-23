package be.hogent.tarsos.dsp.example;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.KeyEventPostProcessor;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import be.hogent.tarsos.dsp.AudioDispatcher;
import be.hogent.tarsos.dsp.AudioPlayer;
import be.hogent.tarsos.dsp.StopAudioProcessor;
import be.hogent.tarsos.dsp.WaveformSimilarityBasedOverlapAdd;
import be.hogent.tarsos.dsp.WaveformSimilarityBasedOverlapAdd.Parameters;
import be.hogent.tarsos.dsp.WaveformWriter;
import be.hogent.tarsos.dsp.resample.RateTransposer;

public class SampleExtractor  extends JFrame {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -8749523013657700525L;
	private final JFileChooser fileChooser;
	private final JSpinner startSelectionSpinner;
	private final JSpinner endSelectionSpinner;
	private final JSpinner[] centsSpinner;
	private final JSpinner[] durationSpinner;
	private final JLabel[] sampleLabel;
	private final JButton playSelection;
	private final JCheckBox[] saveSampleCheckboxes;
	private File file;
	private final char[] codes = {'e','r','t','y','u','i','o','p'};
	
	private double currentDuration = 0;
	private ChangeListener selectionChangedListener = new ChangeListener() {
		@Override
		public void stateChanged(ChangeEvent event) {
			double endValue = (Double) endSelectionSpinner.getValue();
			double startValue = (Double) startSelectionSpinner.getValue();
			if(endValue < startValue){
				startSelectionSpinner.removeChangeListener(this);
				startSelectionSpinner.setValue(endValue);
				startSelectionSpinner.addChangeListener(this);
			}
			for(int i = 0 ; i < durationSpinner.length;i++){
				double duration = (Double) durationSpinner[i].getValue();
				if(duration == 0 || duration == currentDuration){
					durationSpinner[i].setValue(selectionDuration());
				}
			}
			currentDuration = selectionDuration();
		}
	};
	public SampleExtractor(){
		this.setLayout(new BorderLayout());
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setTitle("Sample Exctractor: Extract & Modify Samples");
		
		
		fileChooser = new JFileChooser();
		startSelectionSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 10000, 0.1));
		startSelectionSpinner.setEnabled(false);
		startSelectionSpinner.addChangeListener(selectionChangedListener);
		endSelectionSpinner = new JSpinner(new SpinnerNumberModel(0,0,10000,0.1));
		endSelectionSpinner.setEnabled(false);
		endSelectionSpinner.addChangeListener(selectionChangedListener);
		centsSpinner = new JSpinner[codes.length];
		durationSpinner = new JSpinner[codes.length];
		sampleLabel = new JLabel[codes.length];
		saveSampleCheckboxes = new JCheckBox[codes.length];
		
		playSelection = new JButton("Play");
		playSelection.setEnabled(false);
		playSelection.addActionListener(new ActionListener() {
			AudioDispatcher dispatcher = null;
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if(((Double)startSelectionSpinner.getValue())<((Double)endSelectionSpinner.getValue())){
					try {
						dispatcher = AudioDispatcher.fromFile(file, 1024, 0);
						dispatcher.skip((Double)startSelectionSpinner.getValue());
						dispatcher.addAudioProcessor(new StopAudioProcessor((Double)endSelectionSpinner.getValue()));
						dispatcher.addAudioProcessor(new AudioPlayer(dispatcher.getFormat()));
					} catch (UnsupportedAudioFileException e1) {
					
					} catch (IOException e1) {

					} catch (LineUnavailableException e) {
					}
					new Thread(dispatcher).start();
				}
			}
		});
		
		JPanel fileChooserPanel = new JPanel(new GridLayout(2,4));
		fileChooserPanel.setBorder(new TitledBorder("1. Choose your audio to extract samples from (wav mono)"));		
		JButton chooseFileButton = new JButton("Load a file...");
		chooseFileButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				int returnVal = fileChooser.showOpenDialog(SampleExtractor.this);
	            if (returnVal == JFileChooser.APPROVE_OPTION) {
	                File file = fileChooser.getSelectedFile();
	                setFile(file);	                
	            } else {
	                //canceled
	            }
			}		
		});
		fileChooserPanel.add(new JLabel("Load a file"));
		fileChooserPanel.add(new JLabel("Start (s)"));
		fileChooserPanel.add(new JLabel("End (s)"));
		fileChooserPanel.add(new JLabel("Play Selection"));
		fileChooserPanel.add(chooseFileButton);
		fileChooserPanel.add(startSelectionSpinner);
		fileChooserPanel.add(endSelectionSpinner);
		fileChooserPanel.add(playSelection);
		
		
		this.add(fileChooserPanel,BorderLayout.NORTH);
		
		JPanel samplePanel = new JPanel(new GridLayout(0,1));
		JPanel sampleSubPanel = new JPanel(new GridLayout(1,0));
		sampleSubPanel.add(new JLabel("Key"));
		sampleSubPanel.add(new JLabel("Cents"));
		sampleSubPanel.add(new JLabel("Duration"));
		samplePanel.add(sampleSubPanel);
		samplePanel.add(sampleSubPanel);
		
		final KeyboardFocusManager manager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
		for(int i = 0;i<centsSpinner.length;i++){
			sampleSubPanel = new JPanel(new GridLayout(1,0));
			centsSpinner[i] = new JSpinner(new SpinnerNumberModel(i*100,-1200,1200,1));
			centsSpinner[i].setEnabled(false);
			durationSpinner[i] = new JSpinner(new SpinnerNumberModel(0,0,1200,0.1));
			durationSpinner[i].setEnabled(false);
			sampleLabel[i] = new JLabel("Press " + codes[i] + " to play.");
			saveSampleCheckboxes[i] = new JCheckBox("Save?");
			saveSampleCheckboxes[i].setEnabled(false);
			sampleSubPanel.add(sampleLabel[i]);
			sampleSubPanel.add(centsSpinner[i]);
			sampleSubPanel.add(durationSpinner[i]);
			sampleSubPanel.add(durationSpinner[i]);
			sampleSubPanel.add(saveSampleCheckboxes[i]);
			samplePanel.add(sampleSubPanel);
			final Integer index = i;
		    manager.addKeyEventPostProcessor(new KeyEventPostProcessor() {
				@Override
				public boolean postProcessKeyEvent(KeyEvent e) {
					boolean consumed = false;
					if(e.getKeyChar()== codes[index] && e.getID() == KeyEvent.KEY_TYPED && file != null){
						try{
							int cents = (Integer) centsSpinner[index].getValue();
							double duration = (Double) durationSpinner[index].getValue();
							double originalDuration = selectionDuration();
							double pitchFactor = PitchShiftingExample.centToFactor(cents);
							double durationFactor = originalDuration/duration * pitchFactor;
							boolean saveWav = saveSampleCheckboxes[index].isSelected();
							AudioFormat format = AudioSystem.getAudioFileFormat(file).getFormat();
							double sampleRate = format.getSampleRate();
							double endValue = (Double) endSelectionSpinner.getValue();
							double startValue = (Double) startSelectionSpinner.getValue();
							WaveformSimilarityBasedOverlapAdd wsola;
							AudioPlayer audioPlayer;
							RateTransposer rateTransposer;
							rateTransposer = new RateTransposer(pitchFactor);
							wsola = new WaveformSimilarityBasedOverlapAdd(Parameters.musicDefaults(durationFactor,sampleRate));
							AudioDispatcher dispatcher = AudioDispatcher.fromFile(file,wsola.getInputBufferSize(), wsola.getOverlap());
							audioPlayer = new AudioPlayer(format);
							wsola.setDispatcher(dispatcher);
							dispatcher.skip(startValue);
							dispatcher.addAudioProcessor(new StopAudioProcessor(endValue));
							dispatcher.addAudioProcessor(wsola);
							dispatcher.addAudioProcessor(rateTransposer);
							dispatcher.addAudioProcessor(audioPlayer);
							if(saveWav){
								String filename = String.format("%s_%.2fs-%.2fs_modified_%dcents_%.2fs.wav", file.getName(),startValue,endValue,cents,duration);
								WaveformWriter wfw = new WaveformWriter(format,filename);
								dispatcher.addAudioProcessor(wfw);
							}
							
							Thread t = new Thread(dispatcher);
							t.start();
							consumed = true;
						} catch (Exception ex) {
							ex.printStackTrace();
						}
					} 
					return consumed;
				}
			});
		    
		}
		samplePanel.setBorder(new TitledBorder("2. Adapt and play the samples."));
		samplePanel.setFocusable(true);
		this.add(samplePanel,BorderLayout.CENTER);
		
		try{
			final String tempDir = System.getProperty("java.io.tmpdir");
			String path = new File(tempDir,"flute_sample.wav").getAbsolutePath();
			String resource = "/be/hogent/tarsos/dsp/example/resources/flute_sample.wav";
			copyFileFromJar(resource,path);
			setFile(new File(path));
		}catch (Exception e){
			e.printStackTrace();
		}
	}
	
	/**
	 * Copy a file from a jar.
	 * 
	 * @param source
	 *            The path to read e.g. /package/name/here/help.html
	 * @param target
	 *            The target to save the file to.
	 */
	private void copyFileFromJar(final String source, final String target) {
		try {
			final InputStream inputStream = this.getClass().getResourceAsStream(source);
			OutputStream out;
			out = new FileOutputStream(target);
			final byte[] buffer = new byte[4096];
			int len = inputStream.read(buffer);
			while (len != -1) {
				out.write(buffer, 0, len);
				len = inputStream.read(buffer);
			}
			out.close();
			inputStream.close();
		} catch (final FileNotFoundException e) {
			
		} catch (final IOException e) {
			
		}
	}
	
	private double selectionDuration(){
		return (Double) endSelectionSpinner.getValue() - (Double) startSelectionSpinner.getValue();
	}
	
	private void setFile(File file) {
		this.file=file;
		endSelectionSpinner.setEnabled(true);
        startSelectionSpinner.setEnabled(true);
        playSelection.setEnabled(true);
        for(int i = 0;i<centsSpinner.length;i++){
			centsSpinner[i].setEnabled(true);
			durationSpinner[i].setEnabled(true);
			saveSampleCheckboxes[i].setEnabled(true);
		}
        
        try {
        	AudioDispatcher dispatcher = AudioDispatcher.fromFile(file, 1024, 0);
        	endSelectionSpinner.setValue(dispatcher.durationInSeconds());
		} catch (UnsupportedAudioFileException e) {
		} catch (IOException e) {
		}
        
	}	
	
	public static void main(String...strings) throws InterruptedException, InvocationTargetException{
		SwingUtilities.invokeAndWait(new Runnable() {
			@Override
			public void run() {
				try {
					UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
				} catch (Exception e) {
					//ignore failure to set default look en feel;
				}
				JFrame frame = new SampleExtractor();
				frame.pack();
				frame.setSize(450,400);
				frame.setVisible(true);
			}
		});
	}
}
