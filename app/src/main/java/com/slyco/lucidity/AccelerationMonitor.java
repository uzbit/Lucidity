package com.slyco.lucidity;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

public class AccelerationMonitor implements SensorEventListener{

	private Context context = null;
	private SensorManager sensorManager = null;
	private boolean observe = false;
	private FileLogger fileLogger = null;
	private float prex = 0, prey = 0, prez = 0;
	private static float MIN_ACCEL = 0.15f;
	private static long EXP_TIME = 1800; //expire after 30 mins
	public List<AccelEvent> accelList = new CopyOnWriteArrayList<AccelEvent>();
	
	public AccelerationMonitor(Context c){
		context = c;
		fileLogger = new FileLogger("accelMon.log");
	}
	
	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {		
	}
	
	public void startMonitor(){
		Log.d("AccelMon", "Starting accel...");
		observe = true;
		fileLogger.open();
		if(sensorManager == null && context != null) {
			sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
			sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
		}
	}
	
	public void stopMonitor(){
		Log.d("AccelMon", "Stopping accel...");
		observe = false;
		if(sensorManager != null) 
			sensorManager.unregisterListener(this);
		sensorManager = null;
		fileLogger.close();
	}
	
	@Override
	public void onSensorChanged(SensorEvent arg0) {
		if (!observe)
			return;
		if (arg0.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
			float dx = arg0.values[0] - prex;
			float dy = arg0.values[1] - prey;
			float dz = arg0.values[2] - prez;
			float mag = (float)Math.sqrt(dx*dx + dy*dy +dz*dz);
			if (mag > MIN_ACCEL){
				accelList.add(new AccelEvent(mag));
				fileLogger.write("\t"+mag);
				//Log.d("AccelMon", "\t"+mag+"\t"+dx+"\t"+dy+"\t"+dz);
			}
			prex = arg0.values[0]; prey = arg0.values[1]; prez = arg0.values[2];
		}
	}

	public void clearEvents(){
		Log.d("AccelMon", "Clearing Events...");
		accelList.removeAll(accelList);
		Log.d("AccelMon", "accelLen "+accelList.size());

	}

	public void addNoEvent(){
		accelList.add(new AccelEvent(1.0f));
		fileLogger.write("No event added.");
	}
	
	public void clearExpired(){
		Iterator<AccelEvent> iter = accelList.iterator();
		List<AccelEvent> removeList = new ArrayList<AccelEvent>();
		while (iter.hasNext()) {
			AccelEvent ae = iter.next();
			if (ae.expire(EXP_TIME)) {
				removeList.add(ae);
			}
		}
		accelList.removeAll(removeList);
	}
	
}
