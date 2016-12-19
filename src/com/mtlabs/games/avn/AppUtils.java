package com.mtlabs.games.avn;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.telephony.TelephonyManager;
import android.util.Log;

public class AppUtils {

	public static void loadLoginUser(Context context) {
		SharedPreferences settings = context.getSharedPreferences(
				AppConstants.PREFS_NAME, 0);
		String login = settings.getString("login", "");
		// check for non student IDs
		if (!login.contains("njit")) {
			if (login.contains("@"))
				login = login.substring(0, login.indexOf("@"));
			// and truncate to 20char
			if (login.length() > 20) {
				login = login.substring(0, 20);
			}
		}

	}

	public static void setShowSignIn(Context context, boolean showSignIn) {
		SharedPreferences settings = context.getSharedPreferences(
				AppConstants.PREFS_NAME, Context.MODE_PRIVATE);

		SharedPreferences.Editor editor = settings.edit();
		editor.putBoolean("showSignIn", showSignIn);
		editor.commit();
	}

	public static String registrationWithServer(Context context, String emailId,
			String meid, String reqType) {
		// http servlet call
		HttpClient httpclient = new DefaultHttpClient();
		String providerURL = AppConstants.ip + "/PlaysWEB/LoginServlet";
		HttpPost httppost = new HttpPost(providerURL);
		HttpResponse response = null;
		InputStream is = null;
		StringBuilder sb = new StringBuilder();

		// Add your data
		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
		// nameValuePairs.add(new BasicNameValuePair("taskDesc",
		// currentTask.getTaskDescription()));
		nameValuePairs.add(new BasicNameValuePair("emailId", emailId));
		nameValuePairs.add(new BasicNameValuePair("meid", meid));
		nameValuePairs.add(new BasicNameValuePair("requestType", reqType));

		// Execute HTTP Get Request
		try {
			httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

			response = httpclient.execute(httppost);
			Log.d(AppConstants.TAG, "Reading response...");
			HttpEntity entity = response.getEntity();
			is = entity.getContent();

			BufferedReader reader = new BufferedReader(new InputStreamReader(
					is, "iso-8859-1"), 8);

			String line = null;
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
				Log.d(AppConstants.TAG, "" + sb);
			}
			is.close();
		} catch (ClientProtocolException e) {

			e.printStackTrace();
		} catch (IOException e) {

			e.printStackTrace();
		}

		// read task from servlet
		String resp = sb.toString().trim();
		Log.d(AppConstants.TAG, resp);

		return resp;
		// showToast("Uploaded: \r\n");
	}
	
	public static String getMeid(Context context){
        TelephonyManager telephonyManager = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
      String MEID = telephonyManager.getDeviceId();
      if(MEID==null){
    	  MEID = android.provider.Settings.Secure.getString(context.getContentResolver(),
                  android.provider.Settings.Secure.ANDROID_ID);
          if (MEID == null) {
        	  MEID = "NoAndroidId";
          }
      }
      return MEID;
	}
	
	public static boolean isSentinelAlarmExist(Context context){
        AlarmManager alarms = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE); 
        Intent intentToFire = new Intent(context, SentinelAlarm.class);
        boolean alarmUp = (PendingIntent.getBroadcast(context,0, intentToFire, PendingIntent.FLAG_NO_CREATE) != null) ;
        
        return alarmUp;
	}

}
