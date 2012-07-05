/*
    Copyright Pablo Gil Montano, 2012


    This file is part of MLogger.

    MLogger is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    MLogger is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with MLogger.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.pgil.mlogger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.os.PowerManager;

public class LoggerService extends Service {

	private static final Logger log = LoggerFactory.getLogger(LoggerService.class);

	private static final String GPS_FILENAME = "gps_log.txt";
	private static final String ACCEL_FILENAME = "accel_log.txt";
	//	private static final String LINEAR_ACCEL_FILENAME = "linear_accel_log.txt";

	private static final String SEPARATOR = ";";

	public static boolean running;

	private LocationListener locationListener;
	private LocationManager locationManager;

	private SensorManager sensorManager;


	private static class SensorListener implements SensorEventListener{

		private PrintWriter writer;

		public SensorListener(PrintWriter writer){
			this.writer = writer;
		}

		public void onAccuracyChanged(Sensor arg0, int arg1) {
			log.debug("Accurracy changed: {}, {}", arg0, arg1);

		}

		public void onSensorChanged(SensorEvent arg0) {
			log.debug("Logging sensor data...");
			StringBuilder sb = new StringBuilder();
			sb.append(System.currentTimeMillis());
			sb.append(SEPARATOR);
			sb.append(arg0.values[0]);
			sb.append(SEPARATOR);
			sb.append(arg0.values[1]);
			sb.append(SEPARATOR);
			sb.append(arg0.values[2]);
			writer.println(sb);
		}

	}

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}


	private PrintWriter gpsWriter;
	private PrintWriter accelWriter;
	//	private PrintWriter linearAccelWriter;

	private Sensor accelSensor;
	//	private Sensor linearAccelSensor;

	private SensorListener accelListener;

	PowerManager pm;
	PowerManager.WakeLock wl;

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		if(pm == null){
			pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		}

		// Acquire a reference to the system Location Manager
		if(locationManager == null){
			locationManager = (LocationManager) 
					this.getSystemService(Context.LOCATION_SERVICE);
		}
		wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MLogger - sensor");
		wl.acquire();
		try {
			openWriter();

			// Define a listener that responds to location updates
			if(locationListener == null){
				locationListener = new LocationListener() {
					public void onLocationChanged(Location location) {
						// Called when a new location is found by the network location provider.
						//					if(location.hasSpeed()){
						log.debug("Logging speed data");
						logLocation(location);
						//					}
						//					else{
						//						log.debug("Location without speed data received, not logging");
						//					}
					}

					public void onStatusChanged(String provider, int status, Bundle extras) {}

					public void onProviderEnabled(String provider) {}

					public void onProviderDisabled(String provider) {}
				};
			}
			// Register the listener with the Location Manager to receive location updates
			locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
			running = true;

			accelListener = new SensorListener(accelWriter);
			//			SensorListener linearAccelListener = new SensorListener(linearAccelWriter);

			if(sensorManager == null){
				sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
				accelSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
			}

			sensorManager.registerListener(accelListener, accelSensor, SensorManager.SENSOR_DELAY_FASTEST);

		} 
		catch (IOException e) {
			log.error("Could not open file for logging", e);
		}




		return START_STICKY;
	}

	private void openWriter() throws IOException{
		File sdFile = Environment.getExternalStorageDirectory();
		gpsWriter = new PrintWriter(new FileWriter(new File(sdFile, GPS_FILENAME), true));
		accelWriter = new PrintWriter(new FileWriter(new File(sdFile, ACCEL_FILENAME), true));
		//		linearAccelWriter = new PrintWriter(new FileWriter(new File(sdFile, LINEAR_ACCEL_FILENAME), true));
	}

	private void logLocation(Location location){
		StringBuilder sb = new StringBuilder();
		sb.append(System.currentTimeMillis());
		sb.append(SEPARATOR);
		sb.append(location.getLatitude());
		sb.append(SEPARATOR);
		sb.append(location.getLongitude());
		sb.append(SEPARATOR);
		sb.append(location.getSpeed());
		gpsWriter.println(sb);
	}

	private void closeWriter(){
		gpsWriter.close();
		accelWriter.close();
		//		linearAccelWriter.close();
	}

	@Override
	public void onDestroy() {
		wl.release();
		if(locationManager != null && locationListener != null){
			locationManager.removeUpdates(locationListener);
		}
		if(sensorManager != null && accelListener != null){
			sensorManager.unregisterListener(accelListener);
		}
		running = false;
		log.debug("Service is being destroyed. Closing writer...");
		closeWriter();
	}


}
