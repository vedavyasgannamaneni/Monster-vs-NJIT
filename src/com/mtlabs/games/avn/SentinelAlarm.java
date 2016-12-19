package com.mtlabs.games.avn;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Calendar;
import java.util.List;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import com.mtlabs.games.avn.MyLocation.LocationResult;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class SentinelAlarm extends BroadcastReceiver {

        private final String REMINDER_BUNDLE = "SentinelServiceReminderBundle";
        private SharedPreferences mPrefs;
        private SharedPreferences.Editor mEditor;
        private static final int NOTIFICATION_EX = 1;
        private NotificationManager notificationManager;
        private Context mContext;
        private static MyLocation myLocation;
        private SensorManager mSensorManager;
        // this constructor is called by the alarm manager.
        public SentinelAlarm() {
        }

        // you can use this constructor to create the alarm.
        // Just pass in the main activity as the context,
        // any extras you'd like to get later when triggered
        // and the timeout
        public SentinelAlarm(Context context, Bundle extras, int timeoutInSeconds) {
        		myLocation = new MyLocation();
        		mContext = context;
        		mPrefs = context.getSharedPreferences(AppConstants.PREFS_NAME,
                    Context.MODE_PRIVATE);
        		mEditor = mPrefs.edit();
        		
                AlarmManager alarmMgr = (AlarmManager) context
                                .getSystemService(Context.ALARM_SERVICE);
                Intent intent = new Intent(context, SentinelAlarm.class);

                PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0,
                                intent, PendingIntent.FLAG_UPDATE_CURRENT);
                Calendar time = Calendar.getInstance();
                time.setTimeInMillis(System.currentTimeMillis());
                time.add(Calendar.SECOND, timeoutInSeconds);
                //Every 5minutes - 5*60*1000 TODO change 1 to 5 - 1 is good for sensing
                alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP, time.getTimeInMillis(), 1*60*1000, 
                                pendingIntent);
                                
//                Toast.makeText(context, "Sentinel searching for monster.", Toast.LENGTH_SHORT).show();
                
        }

        @Override
        public void onReceive(Context context, Intent intent) {
//              Toast.makeText(context, "Sentinel Scan started", Toast.LENGTH_SHORT).show();
            // Create Object and call AsyncTask execute Method
        	mContext = context;
    		mPrefs = context.getSharedPreferences(AppConstants.PREFS_NAME,
                Context.MODE_PRIVATE);
    		mEditor = mPrefs.edit();
        	boolean sentinelFoundAlien = mPrefs.getBoolean(AppConstants.SENTINEL_FOUND_ALIEN , false);
        	boolean showSentinel = mPrefs.getBoolean(AppConstants.SHOW_SENTINEL, false);
        	if(showSentinel && !sentinelFoundAlien){
            	String serverURL = AppConstants.ip+"/PlaysWEB/AlienServlet?requestType=sentinelSearch";
        		new LongOperation().execute(serverURL);
        	}
        		
        	boolean alienUpdatesOn = mPrefs.getBoolean(AppConstants.ALIEN_UPDATES_ON, true);
        	if(alienUpdatesOn){
        		iniAppSensingService(context);
        	}
        	
        	getAltitude();
                        
        }

        private void getAltitude() {

            
        	// get SensorManager instance.
            mSensorManager = (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);
            
            List<Sensor> deviceSensors = mSensorManager.getSensorList(Sensor.TYPE_ALL);
            boolean hasPressureSensor = false;
            for(Sensor sensor : deviceSensors){
            	if(Sensor.TYPE_PRESSURE == sensor.getType())
            		hasPressureSensor = true;
            }
            
            if(hasPressureSensor){
//            	Toast.makeText(mContext, "Pressure Sensor Registered!!", Toast.LENGTH_SHORT).show();

                // Register listener
                   mSensorManager.registerListener(mSensorListener, mSensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE), SensorManager.SENSOR_DELAY_NORMAL);
            } else{
//            	Toast.makeText(mContext, "No pressure sesnor!!", Toast.LENGTH_LONG).show();	
            }
            	
            	
            
		}

		protected void iniAppSensingService(Context context) {
        	myLocation = new MyLocation();
        		myLocation.getLocation(context, locationResult);
                
        }
        
        private SensorEventListener mSensorListener = new SensorEventListener() {
            
            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {
              // when accuracy changed, this method will be called.
            }
            
//            @TargetApi(Build.VERSION_CODES.GINGERBREAD)
			@SuppressLint("NewApi")
			@Override
            public void onSensorChanged(SensorEvent event) {
              // when pressure value is changed, this method will be called.
              float pressure_value = 0.0f;
              int height = 0;
              
              // if you use this listener as listener of only one sensor (ex, Pressure), then you don't need to check sensor type.
              if( Sensor.TYPE_PRESSURE == event.sensor.getType() ) {
                pressure_value = event.values[0];
                height = (int) SensorManager.getAltitude(SensorManager.PRESSURE_STANDARD_ATMOSPHERE, pressure_value);
                
//                Toast.makeText(mContext, "Altitude: "+height, Toast.LENGTH_SHORT).show();
                mEditor.putInt(AppConstants.ALTITUDE , height);
                mEditor.commit();      
              }
              
           // Unregister listener
              mSensorManager.unregisterListener(mSensorListener);
//              Toast.makeText(mContext, "Pressure Sensor Un-registered!!", Toast.LENGTH_SHORT).show();
            }
          };
        
        public LocationResult locationResult = (new LocationResult(){
            @Override
            public void gotLocation(final Location loc){
            	if(isPlayerMoved(loc)){
//                	Toast.makeText(mContext, "Background Monster Scan started, Loc:"+loc.getLatitude()+","+loc.getLongitude(), Toast.LENGTH_SHORT).show();
                	HardwareData hardwareData = new HardwareData(mContext, loc);
                    hardwareData.startWifiScan();

                    saveNewLoc(loc);
            	}   
            }
        });
        
        
 
        private float getDistance(LatLng alien, LatLng location) {
    		// Location l = new Location(l);
    		// l.setLatitude(alienMarker.latitude);
    		// l.setLongitude(alienMarker.longitude);
    		float[] results = new float[1];
    		Location.distanceBetween(alien.latitude, alien.longitude,
    				location.latitude, location.longitude, results);

    		return results[0];
    	}
        
     protected boolean isPlayerMoved(Location loc) {
    	 	LatLng playerLastLoc = getPrevLoc();
     		LatLng currentLoc = new LatLng(loc.getLatitude(), loc.getLongitude());
     		
     		float dist = AppConstants.MOVEMENT_LIMIT;
     		
     		if(playerLastLoc!=null && currentLoc!=null){
     			dist = getDistance(playerLastLoc, currentLoc);
     		}
     		
     		if(dist >= AppConstants.MOVEMENT_LIMIT)
     			return true;
     		else
     			return false;
		}

	protected void saveNewLoc(Location loc) {
		mEditor.putLong(AppConstants.PLAYER_LAST_LAT,
				Double.doubleToRawLongBits(loc.getLatitude()));
		mEditor.putLong(AppConstants.PLAYER_LAST_LNG,
				Double.doubleToRawLongBits(loc.getLongitude()));
		mEditor.commit();
	}

	protected LatLng getPrevLoc() {
		LatLng playerLastLoc = null;
		double playerLastLat = Double.longBitsToDouble(mPrefs.getLong(AppConstants.PLAYER_LAST_LAT, Double.doubleToLongBits(0)));
		double playerLastLng = Double.longBitsToDouble(mPrefs.getLong(AppConstants.PLAYER_LAST_LNG, Double.doubleToLongBits(0)));
//		Toast.makeText(this, "SHOT_ALIEN: "+alienNextLat+", "+alienNextLng,
//				Toast.LENGTH_SHORT).show();
		double ZERO = 0.0;
		if(playerLastLat != ZERO || playerLastLng != ZERO){
			playerLastLoc = new LatLng(playerLastLat, playerLastLng);
		} 	
		return playerLastLoc;
	}

	// Class with extends AsyncTask class
        private class LongOperation  extends AsyncTask<String, Void, Void> {
             
            private final HttpClient Client = new DefaultHttpClient();
            private String Content;
            private String Error = null;
                      
            protected void onPreExecute() {
                // NOTE: You can call UI Element here.
                 
                //UI Element
            }
     
            // Call after onPreExecute method
            protected Void doInBackground(String... urls) {
                try {
                     
                    // Call long running operations here (perform background computation)
                    // NOTE: Don't call UI Element here.
                     
                	 HttpPost httppost = new HttpPost(urls[0]);
//                	 JSONObject json = new JSONObject();
//                     json.put("email", "tmanoop@gmail.com");
//                     json.put("wifi", "wifimac");
                	 JData jData = new JData();
                	 jData.setEmail(mPrefs.getString(AppConstants.ACCOUNT_NAME, "noAccountName"));
                	 jData.setMeid(AppUtils.getMeid(mContext));
                	 double sentinelLat = Double.longBitsToDouble(mPrefs.getLong(AppConstants.SENTINEL_LAT, Double.doubleToLongBits(0)));
             		 double sentinelLng = Double.longBitsToDouble(mPrefs.getLong(AppConstants.SENTINEL_LNG, Double.doubleToLongBits(0)));
             		 jData.setCurrentLat(sentinelLat);
               	     jData.setCurrentLng(sentinelLng);
                	 
                	 String jsonString = new Gson().toJson(jData);
                	 
                     StringEntity se = new StringEntity(jsonString);  
                     se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
                     httppost.setEntity(se);
                    
                    ResponseHandler<String> responseHandler = new BasicResponseHandler();
                    Content = Client.execute(httppost, responseHandler);
                    if(Content != null) 
                    	Content = Content.trim();
                } catch (ClientProtocolException e) {
                    Error = e.getMessage();
                    cancel(true);
                } catch (IOException e) {
                    Error = e.getMessage();
                    cancel(true);
                } 
                 
                return null;
            }
             
            protected void onPostExecute(Void unused) {
                // NOTE: You can call UI Element here.
                              
                if (Error != null) {
                     
//                    uiUpdate.setText("Output : "+Error);
//                    Toast.makeText(ReconSenseActivity.this, "Game server not reachable..",
//                          Toast.LENGTH_LONG).show();
                     
                } else {
    //Content
//                	Toast.makeText(ReconSenseActivity.this, Content,
//                            Toast.LENGTH_LONG).show();
                	//Parse Response into our object
                    Type collectionType = new TypeToken<JData>() {
                    }.getType();
                    JData jData = new Gson().fromJson(Content, collectionType);	 
                    
                    if(jData!=null && jData.getRequestType() != null && jData.getCurrentLat()!=0 && jData.getCurrentLng() != 0){
                    	String requestType = jData.getRequestType();
                        if(JData.SENTINEL_SEARCH.equalsIgnoreCase(requestType)){                			
                 			mEditor.putBoolean(AppConstants.SENTINEL_FOUND_ALIEN , true);
                			mEditor.commit();
                			
                			//alert player that monster found at sentinel location. walk to the sentinel to detect the monster.
                			notifyUser(mContext);
            			} 

                    }
                    
                       	            	
                 }
            }
            
			protected void notifyUser(Context context) {
				notificationManager = (NotificationManager) context
						.getSystemService(Context.NOTIFICATION_SERVICE);
	
				int icon = R.drawable.ic_sentinel_button;
				CharSequence tickerText = "Monster vs NJIT";
				long when = System.currentTimeMillis();
	
				Notification notification = new Notification(icon, tickerText, when);
	
				// Context context = getApplicationContext();
				CharSequence contentTitle = "Monster vs NJIT";
				CharSequence contentText = "Monster found at sentinel location.";
				Intent notificationIntent = new Intent(context, MainActivity.class);
				PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
						notificationIntent, 0);
	
				notification.setLatestEventInfo(context, contentTitle, contentText,
						contentIntent);
				notification.defaults |= Notification.DEFAULT_VIBRATE;
				notification.flags = Notification.FLAG_AUTO_CANCEL;
				// long[] vibrate = {0,100,200,300};
				// notification.vibrate = vibrate;
				notificationManager.notify(NOTIFICATION_EX, notification);
			}
             
        }
}