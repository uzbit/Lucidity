package com.slyco.lucidity;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

import com.slyco.lucidity.LucidController.LocalBinder;

public class MainActivity extends ActionBarActivity {

	static LucidController lucidController = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction()
					.add(R.id.container, new MainFragment())
					.commit();
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		Log.d("LucidMain", "Main onSaveInstanceState...");
		super.onSaveInstanceState(outState);
	}

	@Override
	public void onDestroy(){
		Log.d("LucidMain", "Main onDestroy...");
		super.onDestroy();
	}

	@Override
	public void onStop(){
		Log.d("LucidMain", "Main onStop...");
		super.onStop();
	}

	@Override
	public void onStart(){
		Log.d("LucidMain", "Main onStart...");
		super.onStart();
	}

	@Override
	public void onResume(){
		Log.d("LucidMain", "Main onResume...");
		super.onResume();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	public static class MainFragment extends Fragment {

		static Button lucidButton = null, acButton = null, clearEventsButton = null;
		static SignalGenerator signalAC = new SignalGenerator();
		static ServiceConnection connection = new ServiceConnection() {

			public void onServiceDisconnected(ComponentName name) {
				lucidController.stop();
				lucidController = null;
			}

			public void onServiceConnected(ComponentName name, IBinder service) {
				LocalBinder mLocalBinder = (LocalBinder)service;
				lucidController = mLocalBinder.getServiceInstance();
			}
		};

		public MainFragment() {
		}

		public boolean isServiceRunning(Class<?> serviceClass) {
			ActivityManager manager = (ActivityManager) getActivity().getBaseContext().getSystemService(Context.ACTIVITY_SERVICE);
			for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
				if (serviceClass.getName().equals(service.service.getClassName())) {
					return true;
				}
			}
			return false;
		}

		public void addButtons() {
			Log.d("LucidMainFragment", "Adding buttons...");
			if (clearEventsButton == null) {
				clearEventsButton = (Button) getView().findViewById(R.id.clearEvents);
				clearEventsButton.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						if (lucidController != null)
							lucidController.accelMon.clearEvents();
					}
				});
			}

			if (lucidButton == null) {
				lucidButton = (Button) getView().findViewById(R.id.lucidButton);
				lucidButton.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						Intent thisIntent = new Intent(getActivity().getBaseContext(), LucidController.class);
						if (!isServiceRunning(LucidController.class)){
							getActivity().bindService(thisIntent, connection, BIND_AUTO_CREATE);
							getActivity().startService(thisIntent);
							lucidButton.setText("Stop Lucidity");
						} else {
							if (getActivity().stopService(thisIntent)) {
								getActivity().unbindService(connection);
							}
							lucidController = null;
							lucidButton.setText("Start Lucidity");
						}
					}
				});
			}

			if (acButton == null) {
				acButton = (Button) getView().findViewById(R.id.acButton);
				acButton.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						if (signalAC.isPlaying()) {
							acButton.setText("40Hz Off");
							signalAC.stopSignal();
						} else {
							acButton.setText("40Hz On");
							signalAC.startSignal(40.0, 0.0, 1.0f, 1.0f);
						}
					}
				});
			}
		}

		@Override
		public void onSaveInstanceState(Bundle outState) {
			Log.d("LucidMainFragment", "onSaveInstanceState...");
			super.onSaveInstanceState(outState);
		}
		
		@Override
		public void onCreate(Bundle savedInstanceState)
		{
			Log.d("LucidMainFragment", "onCreate...");
			super.onCreate(savedInstanceState);
		}
		
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			Log.d("LucidMainFragment", "onCreateView...");

			View rootView = inflater.inflate(R.layout.fragment_main, container, false);
			return rootView;
		}
	   
		@Override
		public void onViewCreated(View view, Bundle savedInstanceState) {
			Log.d("LucidMainFragment", "onViewCreated...");
			super.onViewCreated(view, savedInstanceState);
			addButtons();
		}

		@Override
		public void onDestroy(){
			Log.d("LucidMainFragment", "onDestroy...");
			Intent thisIntent = new Intent(getActivity().getBaseContext(), LucidController.class);
			if (getActivity().stopService(thisIntent)) {
				getActivity().unbindService(connection);
			}
			if (lucidController != null) lucidController.onDestroy();
			super.onDestroy();
		}

		@Override
		public void onStop(){
			Log.d("LucidMainFragment", "onStop...");
			super.onStop();
		}

		@Override
		public void onStart(){
			Log.d("LucidMainFragment", "onStart...");
			super.onStart();
		}

		@Override
		public void onResume(){
			Log.d("LucidMainFragment", "onResume...");
			super.onResume();
			if (lucidButton != null){
				if (isServiceRunning(LucidController.class)) {
					Intent thisIntent = new Intent(getActivity().getBaseContext(), LucidController.class);
					getActivity().bindService(thisIntent, connection, BIND_AUTO_CREATE);
					lucidButton.setText("Stop Lucidity");
				} else{
					lucidButton.setText("Start Lucidity");
				}
			}

		}

	}
}
