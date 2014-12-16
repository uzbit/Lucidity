package com.slyco.lucidity;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

//Notes:
//  +0.4v DC offset for when signal off across wet electrodes while wearing.
//  This offset was determined to be caused by the voltage of mah brain.
//  At max volume the signal is 5.5v p-p when signal on, 9V battery reads 7.96v

public class LucidController extends Service{

	public AccelerationMonitor accelMon = null;
    public static float DEFAULT_FREQUENCY = 25;

    private Thread lucidThread = null;
    private SignalGenerator signalGen = null;
	private FileLogger fileLogger = null;
	private boolean isStarted = false;
	private long startTime = 0;
	private float frequency = DEFAULT_FREQUENCY;
	//private short runCount = 0;
    private static boolean DO_FREQ_SCAN = true;
    private static float MAX_FREQ = 43.0f; //43Hz.
    private static float FREQ_INC = 3.0f; //3Hz increments.
    private static float MIN_START = 2.0f; //2:00 am. Set to 0 for testing.
	private static float MAX_START = 5.5f; //5:30 am. Set to 24 for testing.
	private static float RUN_TIME = 1.0f*60; //1.0 mins running.
    //private static short MAX_RUNS_PER_SESSION = 3;
    private IBinder binder = new LocalBinder();

	public LucidController(){
		accelMon = new AccelerationMonitor(this);
		signalGen = new SignalGenerator();
		fileLogger = new FileLogger("lucidity.log");
	}

	Runnable runLucid = new Runnable(){       
		public void run(){
		Thread.currentThread().setPriority(Thread.NORM_PRIORITY);
		Utilities.sleepThread(1);
		while(!lucidThread.isInterrupted()){
			accelMon.clearExpired();
			Log.d("LucidController", "accelLen "+accelMon.accelList.size());
			if (checkStartLucid()){
				fileLogger.write("Starting "+frequency+"Hz signal...");
				startTime = Utilities.getTimeSinceEpoch();
				signalGen.startSignal(frequency, 0.0, 1.0f, 1.0f);
				if (DO_FREQ_SCAN){
                    frequency += FREQ_INC;
                    if (frequency > MAX_FREQ) frequency = DEFAULT_FREQUENCY;
                }
                //runCount++;
			}
			if (checkStopLucid()){
                fileLogger.write("Stopping "+frequency+"Hz signal...");
                signalGen.stopSignal();
			}
			Utilities.sleepThread(30);
		}
		}
	};
	
	public boolean checkStartLucid(){
		float currHour = Utilities.getCurrentHour();
		if (currHour >= MIN_START && 
			currHour <= MAX_START && 
			//runCount < MAX_RUNS_PER_SESSION &&
			!signalGen.isPlaying() &&
			isInREM()){
			return true;
		}
		return false;
	}
	
	public boolean checkStopLucid(){
		boolean doStop = (signalGen.isPlaying() &&
				(Utilities.getTimeSinceEpoch() - startTime) > RUN_TIME);
		/*if (doStop && runCount == MAX_RUNS_PER_SESSION){
			accelMon.addNoEvent();
			runCount = 0;
		}*/
		return doStop;
	}
	
	public boolean isInREM(){
		return (accelMon.accelList.size() == 0);
	}
	
	public void start(){
		accelMon.startMonitor();
		fileLogger.open();
		lucidThread = new Thread(runLucid);
		lucidThread.start();
		isStarted = true;
	}
	
	public void stop(){
		accelMon.stopMonitor();
		fileLogger.close();
		if (signalGen.isPlaying()){ 
			signalGen.stopSignal();
		}
		if (lucidThread != null){
			lucidThread.interrupt();
		}
		isStarted = false;
	}

	@Override
	public IBinder onBind(Intent intent) {
		return binder;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (!isStarted)start();
		return Service.START_STICKY;
	}

	@Override
	public void onDestroy() {
		stop();
	}

	public class LocalBinder extends Binder {
		public LucidController getServiceInstance() {
			return LucidController.this;
		}
	}

}
