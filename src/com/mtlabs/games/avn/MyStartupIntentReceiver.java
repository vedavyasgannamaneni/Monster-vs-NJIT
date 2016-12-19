package com.mtlabs.games.avn;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;
 
 
 
public class MyStartupIntentReceiver extends BroadcastReceiver  {
 
        @Override
        public void onReceive(Context context, Intent intent) {
            // call pending sensing service here ....
                SharedPreferences mPrefs = context.getSharedPreferences(AppConstants.PREFS_NAME, 0);
                //check is sentinel exist and start its alarm
                iniSentinelAlarm(context);
        }
        
     
        private void iniSentinelAlarm(Context context) {
                Bundle bundle = new Bundle();
                // add extras here..
                SentinelAlarm alarm = new SentinelAlarm(context, bundle, 120);
//                showToast("Sentinel searching for monster.",context);//TODO remove this
        }
        
        public void showToast(String msg, Context context) {
                CharSequence text = msg;
                int duration = Toast.LENGTH_SHORT;

                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
        }
}