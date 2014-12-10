package com.slyco.lucidity;

import android.app.Activity;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;

public class SignalGenerator extends Activity {
	
	private Thread signalThread = null;
	private AudioTrack audioTrack  = null;            
	private boolean playSignal = false;
	private double frequency = 40; 
	private double phase = Math.PI/2.0; 
	private int sampleRate = 8000;
	private double duration = 1;
	private float leftVol = 1.0f, rightVol = 1.0f;
	
	Runnable runSignal = new Runnable(){       
        public void run(){
            Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
            byte generatedSnd[] = getSignal();
            audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, 
    				sampleRate, 
    				AudioFormat.CHANNEL_OUT_MONO,
    	            AudioFormat.ENCODING_PCM_16BIT, 
    	            2*generatedSnd.length,
    	            AudioTrack.MODE_STREAM); 
            audioTrack.setStereoVolume(leftVol, rightVol);  
            audioTrack.play(); 
	        while(playSignal){
	        	audioTrack.write(generatedSnd, 0, generatedSnd.length);
            }
        }
    };
        	
	public byte [] getSignal(){
		int numSamples = (int) Math.ceil(duration * sampleRate);
	    double sample[] = new double[numSamples];
	    byte generatedSnd[] = new byte[2 * numSamples];

	    for (int i = 0; i < numSamples; ++i) 
	    	sample[i] = Math.cos(frequency * 2 * Math.PI * i / (sampleRate) + phase);

	    // convert to 16 bit pcm sound array
	    // assumes the sample buffer is normalized.
	    int idx = 0;
	    for (final double dVal : sample) {
            // scale to maximum amplitude
            final short val = (short) ((dVal * 32767));
            // in 16 bit wav PCM, first byte is the low order byte
            generatedSnd[idx++] = (byte) (val & 0x00ff);
            generatedSnd[idx++] = (byte) ((val & 0xff00) >>> 8);
        }
	    return generatedSnd;
	}
	
	public void startSignal() {
        Log.d("SignalGen", "Starting signal...");
        playSignal = true;
		signalThread = new Thread(runSignal);
		signalThread.start();
	}
	
	public void stopSignal(){
        Log.d("SignalGen", "Stopping signal...");
        playSignal = false;
		if (audioTrack != null){
			audioTrack.stop();
			audioTrack.release();
		}
	}

    public void startSignal(double freq, double phase, float leftVol, float rightVol) {
        this.setFrequency(freq);
        this.setPhase(phase);
        this.setLeftVol(leftVol);
        this.setRightVol(rightVol);
        this.startSignal();
    }

	public boolean isPlaying(){
		return playSignal;
	}

	public void setFrequency(double val){
		frequency = val;
	}
	
	public void setPhase(double val){
		phase = val;
	}
	
	public void setLeftVol(float val){
		leftVol = val;
	}
	
	public void setRightVol(float val){
		rightVol = val;
	}
}
