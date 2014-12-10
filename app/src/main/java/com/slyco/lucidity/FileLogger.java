package com.slyco.lucidity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import android.os.Environment;

public class FileLogger {
	String filename = "lucidity.log";
	FileOutputStream outputStream = null;
	
	public FileLogger(String name){
		filename = name;
	}
		
	public void write(String string){
		try {
			outputStream.write((Utilities.getDateString() +"\t"+ Utilities.getTimeSinceEpoch() +"\t"+ string +"\n").getBytes());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void open(){
		File sdCard = Environment.getExternalStorageDirectory();
		File dir = new File (sdCard.getAbsolutePath() + "/lucidity");
		dir.mkdirs();
		File file = new File(dir, Utilities.getDateString()+"-"+filename);
		try {
			outputStream = new FileOutputStream(file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public void close(){
		try {
			outputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
