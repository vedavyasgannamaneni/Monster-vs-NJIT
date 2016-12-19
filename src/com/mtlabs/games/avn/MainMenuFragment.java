/* Copyright (C) 2013 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mtlabs.games.avn;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.mtlabs.games.avn.R;

/**
 * Fragment with the main menu for the game. The main menu allows the player
 * to choose a gameplay mode (Easy or Hard), and click the buttons to
 * show view achievements/leaderboards.
 *
 * @author Bruno Oliveira (Google)
 *
 */
public class MainMenuFragment extends Fragment implements OnClickListener {
    String mGreeting = "Hello, anonymous user (not signed in)";

    public interface Listener {
        public void onStartGameRequested(boolean hardMode);
        public void onShowAchievementsRequested();
        public void onShowLeaderboardsRequested();
        public void onSettings();
        public void onAbout();
        public void onSignInButtonClicked();
        public void onSignOutButtonClicked();
        public void onShowingWinScore();
    }

    Listener mListener = null;
    boolean mShowSignIn = true;
	private SharedPreferences mPrefs;
	private Editor mEditor;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
    	   	
        View v = inflater.inflate(R.layout.fragment_mainmenu, container, false);
        final int[] CLICKABLES = new int[] {
                R.id.easy_mode_button,
                R.id.show_achievements_button, R.id.show_leaderboards_button, R.id.settings_button, R.id.about_button,
                R.id.sign_in_button, R.id.sign_out_button
        };
        for (int i : CLICKABLES) {
            v.findViewById(i).setOnClickListener(this);
        }
        return v;
    }

    public void setListener(Listener l) {
        mListener = l;
    }

    @Override
    public void onStart() {
        super.onStart();
        updateUi();
    }

    public void setGreeting(String greeting) {
        mGreeting = greeting;
        updateUi();
    }

    void updateUi() {
    	if (getActivity() == null) return;
    	// Open the shared preferences
    	mPrefs = getActivity().getSharedPreferences(AppConstants.PREFS_NAME, Context.MODE_PRIVATE);
    	// Get a SharedPreferences editor
    	mEditor = mPrefs.edit();
    	boolean showWinScore = mPrefs.getBoolean(AppConstants.SHOW_WIN_SCORE, false);
    	

    	TextView trackNextTv = (TextView) getActivity().findViewById(R.id.track_next);
		Button playButton = (Button) getActivity().findViewById(R.id.easy_mode_button);
		TextView scoreTV = (TextView) getActivity().findViewById(R.id.score_display);
		TextView youWinTV = (TextView) getActivity().findViewById(R.id.you_win_display);
		
		if (trackNextTv != null) trackNextTv.setText(getString(R.string.choose_difficulty));
		if (playButton != null) playButton.setText(getString(R.string.easy));
		//TODO remove boolean true after testing
//		showWinScore = true;
    	if(showWinScore){
			
    		int score = mPrefs.getInt(AppConstants.TOTAL_SCORE, 0);
//    		Toast.makeText(getActivity(), "Current Score: "+score, Toast.LENGTH_SHORT).show();
    		if (scoreTV != null){ 
    			scoreTV.setVisibility(View.VISIBLE);
    			scoreTV.setText(""+score);
    		}
    		
    		if (trackNextTv != null) trackNextTv.setText(getString(R.string.easy_mode_explanation));    		
    		if (playButton != null) playButton.setText(getString(R.string.next));

    		if (youWinTV != null) youWinTV.setVisibility(View.VISIBLE);
  		

    	} else {
    		
    		if (youWinTV != null) youWinTV.setVisibility(View.GONE);
    		if (scoreTV != null) scoreTV.setVisibility(View.GONE);
    	}   
    	if(!mShowSignIn)
    		mListener.onShowingWinScore();
    	
        TextView tv = (TextView) getActivity().findViewById(R.id.hello);
        if (tv != null) tv.setText(mGreeting);

        getActivity().findViewById(R.id.sign_in_bar).setVisibility(mShowSignIn ?
                View.VISIBLE : View.GONE);
        getActivity().findViewById(R.id.sign_out_bar).setVisibility(mShowSignIn ?
                View.GONE : View.VISIBLE);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
        case R.id.easy_mode_button:
            mListener.onStartGameRequested(false);
            break;
        case R.id.show_achievements_button:
            mListener.onShowAchievementsRequested();
            break;
        case R.id.show_leaderboards_button:
            mListener.onShowLeaderboardsRequested();
            break;
        case R.id.settings_button:
            mListener.onSettings();
            break;
        case R.id.about_button:
            mListener.onAbout();
            break;
        case R.id.sign_in_button:
            mListener.onSignInButtonClicked();
            break;
        case R.id.sign_out_button:
            mListener.onSignOutButtonClicked();
            break;
        }
    }

    public void setShowSignInButton(boolean showSignIn) {
        mShowSignIn = showSignIn;
        updateUi();
    }
}
