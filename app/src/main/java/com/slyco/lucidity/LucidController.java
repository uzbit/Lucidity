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
	public static float DEFAULT_FREQUENCY = 40;

    private static boolean DO_FREQ_SCAN = true;
    private static float MIN_FREQ = 37.0f;
    private static float MAX_FREQ = 43.0f;
    private static float FREQ_INC = 1.0f; //3Hz increments.

    private static float MIN_START = 2.0f; //2:00 am. Set to 0 for testing.
    private static float MAX_START = 6.0f; //6:00 am. Set to 24 for testing.
    private static float RUN_TIME = 1.0f*60; //1.0 mins running.
    private static short MAX_RUNS_PER_SESSION = 2;
    private short runCount = 0;
    private Thread lucidThread = null;
	private SignalGenerator signalGen = null;
	private FileLogger fileLogger = null;
	private boolean isStarted = false;
	private long startTime = 0;
    private float frequency = DEFAULT_FREQUENCY;
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
						if (frequency > MAX_FREQ) frequency = MIN_FREQ;
					}
					runCount++;
				}
				if (checkStopLucid()){
					fileLogger.write("Stopping signal...");
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
				runCount < MAX_RUNS_PER_SESSION &&
				!signalGen.isPlaying() &&
				isInREM()){
			return true;
		}
		return false;
	}

	public boolean checkStopLucid(){
		boolean doStop = (signalGen.isPlaying() &&
				(Utilities.getTimeSinceEpoch() - startTime) > RUN_TIME);
		if (doStop && runCount == MAX_RUNS_PER_SESSION){
			accelMon.addNoEvent();
			runCount = 0;
		}
		return doStop;
	}

	public boolean isInREM(){
		return (accelMon.accelList.size() == 0);
	}

	public void start(){
		accelMon.startMonitor();
		fileLogger.open();
        if (DO_FREQ_SCAN) {
            frequency = MIN_FREQ;
        } else{
            frequency = DEFAULT_FREQUENCY;
        }
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
