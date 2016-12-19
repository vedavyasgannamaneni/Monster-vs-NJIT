package com.mtlabs.games.avn;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.Toast;

public class SettingsActivity extends Activity {
	
	private CheckBox alienAlert;
	private RadioGroup radioPlayerIconGroup;
	protected RadioButton playerIconButton;
	private SharedPreferences mPrefs;
    private SharedPreferences.Editor mEditor;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.settings);
        mPrefs = getApplicationContext().getSharedPreferences(AppConstants.PREFS_NAME,
                Context.MODE_PRIVATE);
    	mEditor = mPrefs.edit();
        
        addListenerOnRadioButtons();
        
        addListenerOnAvatarRadioButtons();
	}

	@Override
	public void onBackPressed() {
		this.finish();
		super.onBackPressed();
	}
	
	private void addListenerOnRadioButtons() {
		alienAlert = (CheckBox) findViewById(R.id.alienAlert);
		boolean alienAlertValue = mPrefs.getBoolean(AppConstants.ALIEN_ALERT, true);
		
		if(alienAlertValue)
			alienAlert.setChecked(true);
		else
			alienAlert.setChecked(false);
		
		alienAlert.setOnClickListener(new OnClickListener() {
	 
		  @Override
		  public void onClick(View v) {
	                //is chkIos checked?
			if (!((CheckBox) v).isChecked()) {
				Toast.makeText(SettingsActivity.this,
			 	   "Disabled monster alerts!!", Toast.LENGTH_LONG).show();
				mEditor.putBoolean(AppConstants.ALIEN_ALERT, false);
				mEditor.putBoolean(AppConstants.ALIEN_UPDATES_ON, false);
				mEditor.commit();
			} else {
				Toast.makeText(SettingsActivity.this,
					 	   "Enabled monster alerts!!", Toast.LENGTH_LONG).show();
				mEditor.putBoolean(AppConstants.ALIEN_ALERT, true);
				mEditor.putBoolean(AppConstants.ALIEN_UPDATES_ON, true);
				mEditor.commit();
			}
	 
		  }
		});

	}
	

	
	private void addListenerOnAvatarRadioButtons() {
		radioPlayerIconGroup = (RadioGroup) findViewById(R.id.radioPlayerIcon);
		
		radioPlayerIconGroup
				.setOnCheckedChangeListener(new OnCheckedChangeListener() {

					public void onCheckedChanged(RadioGroup group, int checkedId) {
						// get selected radio button from radioGroup
						int selectedId = radioPlayerIconGroup.getCheckedRadioButtonId();
			 
						// find the radiobutton by returned id
					    playerIconButton = (RadioButton) findViewById(selectedId);
					    
					    savePlayerIcon(playerIconButton.getText().toString());
					}
				});
	 
		
	}

	protected void savePlayerIcon(String text) {
		int playerIconId = R.drawable.ic_pl_gandalf;
		if(getString(R.string.player_avatar_1).equalsIgnoreCase(text)){
			playerIconId = R.drawable.ic_pl_gandalf;
		} else if(getString(R.string.player_avatar_2).equalsIgnoreCase(text)){
			playerIconId = R.drawable.ic_pl_gladiator;
		} else if(getString(R.string.player_avatar_3).equalsIgnoreCase(text)){
			playerIconId = R.drawable.ic_pl_hunter_bow_arrow;
		} else if(getString(R.string.player_avatar_4).equalsIgnoreCase(text)){
			playerIconId = R.drawable.ic_pl_vader;
		} else if(getString(R.string.player_avatar_5).equalsIgnoreCase(text)){
			playerIconId = R.drawable.ic_pl_girl_bow_arrow;
		} else if(getString(R.string.player_avatar_6).equalsIgnoreCase(text)){
			playerIconId = R.drawable.ic_pl_girl_knife;
		} else if(getString(R.string.player_avatar_7).equalsIgnoreCase(text)){
			playerIconId = R.drawable.ic_pl_girl_knight;
		}	
		
		storeIntValue(AppConstants.PLAYER_ICON, playerIconId); 
	}

	protected void storeValue(String checkBox, String value) {
		SharedPreferences settings = getSharedPreferences(AppConstants.PREFS_NAME,
				Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = settings.edit();
		editor.putString(checkBox, value);
		editor.commit();
	}
	
	protected void storeIntValue(String checkBox, int value) {
		SharedPreferences settings = getSharedPreferences(AppConstants.PREFS_NAME,
				Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = settings.edit();
		editor.putInt(checkBox, value);
		editor.commit();
	}

}
