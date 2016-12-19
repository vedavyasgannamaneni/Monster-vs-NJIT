package com.mtlabs.games.avn;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;

public class AboutActivity extends Activity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.about);
        
        TextView t2 = (TextView) findViewById(R.id.map_link);
        t2.setMovementMethod(LinkMovementMethod.getInstance());
	}
	
	@Override
	public void onBackPressed() {
		this.finish();
		super.onBackPressed();
	}

}
