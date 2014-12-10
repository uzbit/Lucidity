package com.slyco.lucidity;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.os.SystemClock;
import android.annotation.SuppressLint;

public class Utilities {
	@SuppressLint("SimpleDateFormat")
	public static float getCurrentHour(){
		SimpleDateFormat h = new SimpleDateFormat("HH");
		SimpleDateFormat m = new SimpleDateFormat("mm");
		float hour = Float.parseFloat(h.format(new Date()));
		float min = Float.parseFloat(m.format(new Date()));
		return hour+min/60.0f;
	}
	
	@SuppressLint("SimpleDateFormat")
	public static String getDateString(){
		SimpleDateFormat s = new SimpleDateFormat("yyyyMMddHHmmss");
		return s.format(new Date());
	}
	
	public static long getTimeSinceEpoch(){
		return (long)(System.currentTimeMillis()/1000.0);
	}
	
	public static void sleepThread(int sec){
		SystemClock.sleep(sec*1000L); //Thread.sleep(ms);
	}
}
