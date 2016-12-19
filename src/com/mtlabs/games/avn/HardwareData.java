package com.mtlabs.games.avn;

import java.io.IOException;
import java.lang.reflect.Type;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
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

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.location.Location;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.util.Log;
import android.widget.Toast;

public class HardwareData {

	private static final String TAG = "HardwareData";
	
	private BatteryReceiver mBatteryReceiver;
	private boolean mShouldTurnOffWifi;
	private WifiManager mWifiManager;
	private WifiDataAvailableReceiver mWifiDataReceiver;
//	private JTask currentTask;
	private SharedPreferences mPrefs;

	private Editor mEditor;
	private Context context;
	private Location location;
	private List<JWiFiData> jWiFiDataList;
	private static final int NOTIFICATION_EX = 1;
    private NotificationManager notificationManager;


	/**
	 * Turns on wifi interface (if it is off) and start a wifi scan. WiFi
	 * interface will be turned off after scan if it was off.
	 * 
	 * @param context
	 *            Application context.
	 * @param location 
	 * @param currentTask 
	 */
	public HardwareData() {

	}
	
	public HardwareData(Context context, Location location) {
		this.location = location;
		this.context = context;
		// Open the shared preferences
		 		mPrefs = context.getSharedPreferences(AppConstants.PREFS_NAME, Context.MODE_PRIVATE);
		 		// Get a SharedPreferences editor
		 		mEditor = mPrefs.edit();
	}
	
//	public HardwareData(JTask currTask) {
//		currentTask = currTask;
//	}
	
	public void startWifiScan() {
		mWifiDataReceiver = new WifiDataAvailableReceiver();
		context.getApplicationContext().registerReceiver(mWifiDataReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
		if (null == mWifiManager) {
			mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		}
		if (null != mWifiManager) {
			if (!mWifiManager.isWifiEnabled()) {
				mWifiManager.setWifiEnabled(true);
				mShouldTurnOffWifi = true;
			} else {
				mShouldTurnOffWifi = false;
			}
			mWifiManager.startScan();
		}
	}

	private class WifiDataAvailableReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			context.unregisterReceiver(this);
			List<ScanResult> scanResults = mWifiManager.getScanResults();
			
			Timestamp currentTimestamp = new Timestamp(Calendar.getInstance().getTime().getTime());
//			AppUtils.writeToFile(context, "Timestamp:"+currentTimestamp+";wifi scan\n","sensing_file"+currentTask.getTaskId());
			jWiFiDataList = new ArrayList<JWiFiData>();
//			List<String> listResult = new LinkedList<String>();
			if(scanResults!=null){
				for (ScanResult sr : scanResults) {
					String srStr = String.format("Timestamp:%s;BSSID:%s;SSID:%s;capabilities:%s;freq:%d;level:%d\n", currentTimestamp.toString(),
							sr.BSSID.replace(':', '-'), sr.SSID, sr.capabilities, sr.frequency, sr.level);
//					listResult.add(srStr);
					
					JWiFiData jWiFiData = new JWiFiData(); 
	           	 	jWiFiData.setBSSID(sr.BSSID.replace(':', '-'));
	           	 	jWiFiData.setSSID(sr.SSID);
	           	 	jWiFiData.setCapabilities(sr.capabilities);
	           	 	jWiFiData.setFrequency(sr.frequency);
	           	 	jWiFiData.setLevel(sr.level);
	           	 	jWiFiData.setAltitude(mPrefs.getInt(AppConstants.ALTITUDE , 0));
	           	 	
	           	 	jWiFiDataList.add(jWiFiData);
//					Log.d(TAG, "Wifi scan: " + srStr);
//					Toast.makeText(context.getApplicationContext(), "Wifi scan: " + srStr, Toast.LENGTH_SHORT).show();
				}
				
				String serverURL = AppConstants.ip+"/PlaysWEB/AlienServlet?requestType=alienSearch";
			      // Create Object and call AsyncTask execute Method
					new LongOperation().execute(serverURL);
			}
				
			
//			Toast.makeText(context.getApplicationContext(), "Wifi scan complete. " + scanResults.size(), Toast.LENGTH_SHORT).show();

			if (mShouldTurnOffWifi) {
				mWifiManager.setWifiEnabled(false);
			}
			
		}
	}

	/**
	 * Starts a one-shot battery measurement. Battery level is asynchronously
	 * provided by Android. This method listens for the first battery level
	 * change and then stops.
	 * 
	 * @param context
	 */
	public void startBatteryMonitoring(Context context) {
		mBatteryReceiver = new BatteryReceiver();
		context.getApplicationContext().registerReceiver(mBatteryReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
	}

	private class BatteryReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			context.unregisterReceiver(this);
			int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
			int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
			int temp = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1);
			int voltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1);
			int plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
			int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
			String statusStr = null;
			switch (status) {
			case BatteryManager.BATTERY_STATUS_CHARGING:
				statusStr = "charging";
				break;
			case BatteryManager.BATTERY_STATUS_DISCHARGING:
				statusStr = "discharging";
				break;
			case BatteryManager.BATTERY_STATUS_FULL:
				statusStr = "full";
				break;
			case BatteryManager.BATTERY_STATUS_NOT_CHARGING:
				statusStr = "not charging";
				break;
			default:
				statusStr = "unknown";
			}

			String pluggedStr = null;
			switch (plugged) {
			case BatteryManager.BATTERY_PLUGGED_AC:
				pluggedStr = "ac";
				break;
			case BatteryManager.BATTERY_PLUGGED_USB:
				pluggedStr = "usb";
				break;
			default:
				pluggedStr = "no";
			}
			Timestamp currentTimestamp = new Timestamp(Calendar.getInstance().getTime().getTime());
			String stBatt = String.format("Timestamp:%s;level:%d;scale:%d;temp:%d;voltage:%d,plugged:%s,status:%s"+" \n", currentTimestamp, level, scale, temp, voltage, pluggedStr, statusStr);
			Log.d(TAG, stBatt);
//			AppUtils.writeToFile(context, stBatt,"sensing_file"+currentTask.getTaskId());
		}
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
                 
                // Server url call by GET method
//                HttpGet httpget = new HttpGet(urls[0]);
            	 HttpPost httppost = new HttpPost(urls[0]);
//            	 JSONObject json = new JSONObject();
//                 json.put("email", "tmanoop@gmail.com");
//                 json.put("wifi", "wifimac");
            	 JData jData = new JData();
            	 jData.setEmail(mPrefs.getString(AppConstants.ACCOUNT_NAME, "noAccountName"));
            	 jData.setMeid(AppUtils.getMeid(context));
            	 jData.setCollectedPowerCount(mPrefs.getInt(AppConstants.COLLECTED_POWER, AppConstants.DEFAULT_POWER));
         		 jData.setScore(mPrefs.getInt(AppConstants.TOTAL_SCORE, 0));
         		 jData.setBustedAliens(mPrefs.getInt(AppConstants.BUSTED_ALIENS, 0));	
         		 jData.setLevel(mPrefs.getInt(AppConstants.PLAYER_GAME_LEVEL, 1));
         		 jData.setCurrentLat(location.getLatitude());
           	     jData.setCurrentLng(location.getLongitude());
            	 jData.setjWiFiData(jWiFiDataList);
            	 
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
                 
//                uiUpdate.setText("Output : "+Error);
//                Toast.makeText(ReconSenseActivity.this, "Game server not reachable..",
//                      Toast.LENGTH_LONG).show();
            	//TODO Cache the wifi data
                 
            } else {
//Content
//            	Toast.makeText(ReconSenseActivity.this, Content,
//                        Toast.LENGTH_LONG).show();
            	//Parse Response into our object
                Type collectionType = new TypeToken<JData>() {
                }.getType();
                JData jData = new Gson().fromJson(Content, collectionType);	  
                
                double prevAlienLat = Double.longBitsToDouble(mPrefs.getLong(
        				AppConstants.ALIEN_LAT, Double.doubleToLongBits(0)));
        		double prevAlienLng = Double.longBitsToDouble(mPrefs.getLong(
        				AppConstants.ALIEN_LNG, Double.doubleToLongBits(0)));
                
                if(jData!=null && jData.getRequestType() != null && jData.getCurrentLat()!=0 && jData.getCurrentLng() != 0){
                	String requestType = jData.getRequestType();         	
         			
         			float dist = getDistance(new LatLng(prevAlienLat, prevAlienLng), new LatLng(jData.getCurrentLat(), jData.getCurrentLng()));
         			
         			boolean alienUpdatesOn = mPrefs.getBoolean(AppConstants.ALIEN_UPDATES_ON, true);
                	if(alienUpdatesOn && dist > 5){
                		mEditor.putLong(AppConstants.ALIEN_LAT, Double.doubleToRawLongBits(jData.getCurrentLat()));
             			mEditor.putLong(AppConstants.ALIEN_LNG, Double.doubleToRawLongBits(jData.getCurrentLng()));
             			mEditor.putInt(AppConstants.ALIEN_FLOOR, jData.getFloorNum());
             			mEditor.putBoolean(AppConstants.ALIEN_FOUND , true);
                		notifyUser(context);
                	}
                }
    			mEditor.commit();
             }
        }
        
        protected void notifyUser(Context context) {
			notificationManager = (NotificationManager) context
					.getSystemService(Context.NOTIFICATION_SERVICE);

			int icon = R.drawable.ic_alien;
			CharSequence tickerText = "Monster vs NJIT";
			long when = System.currentTimeMillis();

			Notification notification = new Notification(icon, tickerText, when);

			// Context context = getApplicationContext();
			CharSequence contentTitle = "Monster vs NJIT";
			CharSequence contentText = "Monster found at current location.";
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
    
    private float getDistance(LatLng alien, LatLng location) {
		// Location l = new Location(l);
		// l.setLatitude(alienMarker.latitude);
		// l.setLongitude(alienMarker.longitude);
		float[] results = new float[1];
		Location.distanceBetween(alien.latitude, alien.longitude,
				location.latitude, location.longitude, results);

		return results[0];
	}
}