/*
 * Copyright (C) 2013 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mtlabs.games.avn;

import java.io.IOException;
import java.lang.reflect.Type;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.games.Games;
import com.google.android.gms.games.Player;
import com.google.example.games.basegameutils.BaseGameActivity;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

/**
 * Our main activity for the game.
 *
 * IMPORTANT: Before attempting to run this sample, please change
 * the package name to your own package name (not com.android.*) and
 * replace the IDs on res/values/ids.xml by your own IDs (you must
 * create a game in the developer console to get those IDs).
 *
 * This is a very simple game where the user selects "easy mode" or
 * "hard mode" and then the "gameplay" consists of inputting the
 * desired score (0 to 9999). In easy mode, you get the score you
 * request; in hard mode, you get half.
 *
 * @author Bruno Oliveira
 */
public class MainActivity extends BaseGameActivity
        implements MainMenuFragment.Listener {

    // Fragments
    MainMenuFragment mMainMenuFragment;

	private SharedPreferences mPrefs;

	private Editor mEditor;
	
	private int score;    	
	private int bustedAliens;		
	private int collectedPowerCount;
	
    // request codes we use when invoking an external activity
    final int RC_RESOLVE = 5000, RC_UNUSED = 5001;

    // tag for debug logging
    final boolean ENABLE_DEBUG = true;
    final String TAG = "TanC";

    // playing on hard mode?
    boolean mLevel1Mode = false;
    boolean mLevel2Mode = false;
    boolean mLevel3Mode = false;
    boolean mLevel4Mode = false;
    boolean mLevel5Mode = false;

    // achievements and scores we're pending to push to the cloud
    // (waiting for the user to sign in, for instance)
    AccomplishmentsOutbox mOutbox = new AccomplishmentsOutbox();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        enableDebugLog(ENABLE_DEBUG, TAG);
        // remove title
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);      
        
        setContentView(R.layout.activity_main);        
        
        // Open the shared preferences
 		mPrefs = getSharedPreferences(AppConstants.PREFS_NAME, Context.MODE_PRIVATE);
 		// Get a SharedPreferences editor
 		mEditor = mPrefs.edit();
        
 		loadLevel();
 		
        // create fragments
        mMainMenuFragment = new MainMenuFragment();

        // listen to fragment events
        mMainMenuFragment.setListener(this);

        // add initial fragment (welcome fragment)
        getSupportFragmentManager().beginTransaction().add(R.id.fragment_container,
                mMainMenuFragment).commit();

        // IMPORTANT: if this Activity supported rotation, we'd have to be
        // more careful about adding the fragment, since the fragment would
        // already be there after rotation and trying to add it again would
        // result in overlapping fragments. But since we don't support rotation,
        // we don't deal with that for code simplicity.

        // load outbox from file
        mOutbox.loadLocal(this);
        
        if(!AppUtils.isSentinelAlarmExist(getApplicationContext()))
			iniSentinelAlarm(getApplicationContext());
    }
    

	 private void iniSentinelAlarm(Context context) {
        Bundle bundle = new Bundle();
        // add extras here..
        SentinelAlarm alarm = new SentinelAlarm(context, bundle, 120);
	 }
	 
    @Override
    protected void onResume() {

    	super.onResume();
    	
    }

    @Override
    public void onSettings() {
		Intent i = new Intent(getApplicationContext(),
				SettingsActivity.class);
		startActivity(i);
	}
    
    @Override
    public void onAbout() {
		Intent i = new Intent(getApplicationContext(),
				AboutActivity.class);
		startActivity(i);
	}
    
    // Switch UI to the given fragment
    void switchToFragment(Fragment newFrag) {
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, newFrag)
                .commit();
    }

    @Override
    public void onStartGameRequested(boolean hardMode) {
//        startGame(hardMode);
    	if (isSignedIn()) {
    		if(gameAccess()){
			    startGameActivity();
    		} else {
    			//call long operation to check access
    			loginRegistration();
        	}
    	} else {
    		showAlert(getString(R.string.game_not_available));
    	}
    }

    private void startGameActivity() {
    	Intent i = new Intent(getApplicationContext(), ReconSenseActivity.class);
		startActivity(i);
	}

	private boolean gameAccess() {
		// Check with server registration locally, if registered, then return true.
    	// if not found locally, then check with server
    	// if registered in server, then save the registered ID locally
    	// 		then return true //give access.
    	// else return false
        return mPrefs.getBoolean(AppConstants.ACCESS, false); 
	}

	@Override
    public void onShowAchievementsRequested() {
        if (isSignedIn()) {
            startActivityForResult(Games.Achievements.getAchievementsIntent(getApiClient()),
                    RC_UNUSED);
        } else {
            showAlert(getString(R.string.achievements_not_available));
        }
    }

    @Override
    public void onShowLeaderboardsRequested() {
        if (isSignedIn()) {
            startActivityForResult(Games.Leaderboards.getAllLeaderboardsIntent(getApiClient()),
                    RC_UNUSED);
        } else {
            showAlert(getString(R.string.leaderboards_not_available));
        }
    }

    /**
     * Start gameplay. This means updating some status variables and switching
     * to the "gameplay" screen (the screen where the user types the score they want).
     *
     * @param hardMode whether to start gameplay in "hard mode".
     */
    void startGame(boolean hardMode) {
        mLevel1Mode = hardMode;
    }

    /**
     * Checks that the developer (that's you!) read the instructions.
     *
     * IMPORTANT: a method like this SHOULD NOT EXIST in your production app!
     * It merely exists here to check that anyone running THIS PARTICULAR SAMPLE
     * did what they were supposed to in order for the sample to work.
     */
    boolean verifyPlaceholderIdsReplaced() {
        final boolean CHECK_PKGNAME = true; // set to false to disable check
                                            // (not recommended!)

        // Did the developer forget to change the package name?
        if (CHECK_PKGNAME && getPackageName().startsWith("com.google.example.")) {
            Log.e(TAG, "*** Sample setup problem: " +
                "package name cannot be com.google.example.*. Use your own " +
                "package name.");
            return false;
        }

        // Did the developer forget to replace a placeholder ID?
        int res_ids[] = new int[] {
                R.string.app_id, R.string.achievement_power,
                R.string.achievement_monster_hunter, R.string.achievement_best_tracker,
                R.string.achievement_level_1, R.string.achievement_sentinel,
                R.string.achievement_search_remotely, R.string.achievement_level_2,
                R.string.achievement_bonus_power, R.string.achievement_great_power,
                R.string.achievement_level_3, R.string.achievement_double_power,
                R.string.achievement_level_4, R.string.achievement_extra_power,
                R.string.achievement_level_5, R.string.achievement_legendary_hunter,
                
                R.string.leaderboard_level_1, R.string.leaderboard_level_2,
                R.string.leaderboard_level_3, R.string.leaderboard_level_4,
                R.string.leaderboard_level_5
        };
        for (int i : res_ids) {
            if (getString(i).equalsIgnoreCase("ReplaceMe")) {
                Log.e(TAG, "*** Sample setup problem: You must replace all " +
                    "placeholder IDs in the ids.xml file by your project's IDs.");
                return false;
            }
        }
        return true;
    }

    @Override
    public void onShowingWinScore() {
        // Compute final score (in easy mode, it's the requested score; in hard mode, it's half)
    	if(mPrefs!=null){
    		score = mPrefs.getInt(AppConstants.TOTAL_SCORE, 0);    	
        	bustedAliens = mPrefs.getInt(AppConstants.BUSTED_ALIENS, 0);		
    		collectedPowerCount = mPrefs.getInt(AppConstants.COLLECTED_POWER, AppConstants.DEFAULT_POWER);

            // check for achievements
            checkForAchievements();

            // update leaderboards
            updateLeaderboards(score);

            // push those accomplishments to the cloud, if signed in
            pushAccomplishments();
    	}
    }
    

    // Checks if n is prime. We don't consider 0 and 1 to be prime.
    // This is not an implementation we are mathematically proud of, but it gets the job done.
    boolean isPrime(int n) {
        int i;
        if (n == 0 || n == 1) return false;
        for (i = 2; i <= n / 2; i++) {
            if (n % i == 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * Check for achievements and unlock the appropriate ones.
     *
     * @param requestedScore the score the user requested.
     * @param finalScore the score the user got.
     */
    void checkForAchievements() {
        // Check if each condition is met; if so, unlock the corresponding
        // achievement.
    	
        if (checkPowerAchievement()) {
            mOutbox.mPowerAchievement = true;
            mEditor.putBoolean(AppConstants.SHOW_GENIE , true);
    		mEditor.commit();
        }
        if (checkMonsterHunterAchievement()) {
            mOutbox.mMonsterHunterAchievement = true;
            achievementToast(getString(R.string.achievement_arrogant_toast_text));
        }
        if (checkBestTrackerAchievement()) {
            mOutbox.mBestTrackerAchievement = true;
            achievementToast(getString(R.string.achievement_humble_toast_text));
        }
        if (checkLevel1Achievement()) {
            mOutbox.mLevel1Achievement = true;
            achievementToast(getString(R.string.achievement_leet_toast_text));
            mLevel1Mode = false; 
            mLevel2Mode = true;
            saveLevel();
        }
        if (checkSentinelAchievement()) {
            mOutbox.mSentinelAchievement = true;
            mEditor.putBoolean(AppConstants.SHOW_SENTINEL , true);
    		mEditor.commit();
        }
        if (checkSearchRemotelyAchievement()) {
            mOutbox.mSearchRemotelyAchievement = true;
            achievementToast(getString(R.string.achievement_arrogant_toast_text));
        }
        if (checkLevel2Achievement()) {
            mOutbox.mLevel2Achievement = true;
            achievementToast(getString(R.string.achievement_humble_toast_text));
            mLevel2Mode = false; 
            mLevel3Mode = true;
            saveLevel();
        }
        if (checkBonusPowerAchievement()) {
            mOutbox.mBonusPowerAchievement = true;            
            collectedPowerCount = collectedPowerCount + 25;
            mEditor.putInt(AppConstants.COLLECTED_POWER , collectedPowerCount);
            mEditor.putBoolean(AppConstants.BONUS_POWER , false);
    		mEditor.commit();
        }
        if (checkGreatPowerAchievement()) {
            mOutbox.mGreatPowerAchievement = true;
            achievementToast(getString(R.string.achievement_arrogant_toast_text));
            mEditor.putBoolean(AppConstants.SHOW_GENIE_2 , true);
    		mEditor.commit();
        }
        if (checkLevel3Achievement()) {
            mOutbox.mLevel3Achievement = true;
            achievementToast(getString(R.string.achievement_humble_toast_text));
            mLevel3Mode = false; 
            mLevel4Mode = true;
            saveLevel();
        }
        if (checkDoublePowerAchievement()) {
            mOutbox.mDoublePowerAchievement = true;
            achievementToast(getString(R.string.achievement_leet_toast_text));
            collectedPowerCount = collectedPowerCount * 2;
            mEditor.putInt(AppConstants.COLLECTED_POWER , collectedPowerCount);
            mEditor.putBoolean(AppConstants.DOUBLE_POWER , false);
    		mEditor.commit();
        }
        if (checkLevel4Achievement()) {
            mOutbox.mLevel4Achievement = true;
            achievementToast(getString(R.string.achievement_prime_toast_text));
            mLevel4Mode = false; 
            mLevel5Mode = true;
            saveLevel();
        }
        if (checkExtraPowerAchievement()) {
            mOutbox.mExtraPowerAchievement = true;
            collectedPowerCount = collectedPowerCount + 50;
            mEditor.putInt(AppConstants.COLLECTED_POWER , collectedPowerCount);
            mEditor.putBoolean(AppConstants.EXTRA_POWER , false);
    		mEditor.commit();
        }
        if (checkLevel5Achievement()) {
            mOutbox.mLevel5Achievement = true;
            achievementToast(getString(R.string.achievement_humble_toast_text)); 
        }
        if (checkLegendaryHunterAchievement()) {
            mOutbox.mLegendaryHunterAchievement = true;
            achievementToast(getString(R.string.achievement_leet_toast_text));
        }
    }

    private void saveLevel() {
    	int level = 1;
    	if(mLevel2Mode) {
    		level = 2;
    	} else if(mLevel3Mode){
    		level = 3;
    	} else if(mLevel4Mode){
    		level = 4;
    	} else if(mLevel5Mode){
    		level = 5;
    	}
    	
		mEditor.putInt(AppConstants.PLAYER_GAME_LEVEL , level);
		mEditor.commit();
	}
    
    private void loadLevel() {
    	int level = mPrefs.getInt(AppConstants.PLAYER_GAME_LEVEL, 1);
    	
    	switch (level) {
        case 1:
        	mLevel1Mode = true;
            break;
        case 2:
        	mLevel2Mode = true;
            break;
        case 3:
        	mLevel3Mode = true;
            break;
        case 4:
        	mLevel4Mode = true;
            break;
        case 5:
        	mLevel5Mode = true;
            break;
        }
    }

	private boolean checkLegendaryHunterAchievement() {
		return (bustedAliens >= 75) ? true : false;
	}

	private boolean checkLevel5Achievement() {
		return (bustedAliens >= 60) ? true : false;
	}

	private boolean checkExtraPowerAchievement() {
		boolean extraPower = mPrefs.getBoolean(AppConstants.EXTRA_POWER, true);
		return (extraPower && bustedAliens >= 52 && collectedPowerCount >= AppConstants.DEFAULT_POWER) ? true : false;
	}

	private boolean checkLevel4Achievement() {
		return (bustedAliens >= 50) ? true : false;
	}

	private boolean checkDoublePowerAchievement() {
		boolean doublePower = mPrefs.getBoolean(AppConstants.DOUBLE_POWER, true);
		return (doublePower && bustedAliens >= 35) ? true : false;
	}

	private boolean checkLevel3Achievement() {
		return (bustedAliens >= 20 && collectedPowerCount >= AppConstants.DEFAULT_POWER/2) ? true : false;
	}

	private boolean checkGreatPowerAchievement() {
		return (bustedAliens >= 15) ? true : false;
	}
	
	private boolean checkBonusPowerAchievement() {
		boolean bonusPower = mPrefs.getBoolean(AppConstants.BONUS_POWER, true);
		return (bonusPower && bustedAliens >= 12 && collectedPowerCount >= AppConstants.DEFAULT_POWER) ? true : false;
	}

	private boolean checkLevel2Achievement() {
		return (bustedAliens >= 10) ? true : false;
	}

	private boolean checkSearchRemotelyAchievement() {
		return mPrefs.getBoolean(AppConstants.SENTINEL_PLACED, false);
	}

	private boolean checkSentinelAchievement() {
		return (bustedAliens >= 5) ? true : false;
	}

	private boolean checkLevel1Achievement() {
		return (bustedAliens >= 3) ? true : false;
	}

	private boolean checkBestTrackerAchievement() {
		return (bustedAliens >= 1) ? true : false;
	}

	private boolean checkMonsterHunterAchievement() {
		return (score >= 100) ? true : false;
	}

	private boolean checkPowerAchievement() {
		return (collectedPowerCount >= AppConstants.DEFAULT_POWER + 3 || score >= 100) ? true : false;
	}

	void unlockAchievement(int achievementId, String fallbackString) {
        if (isSignedIn()) {
            Games.Achievements.unlock(getApiClient(), getString(achievementId));
        } else {
//            Toast.makeText(this, getString(R.string.achievement) + ": " + fallbackString,
//                    Toast.LENGTH_LONG).show();
        }
    }

    void achievementToast(String achievement) {
        // Only show toast if not signed in. If signed in, the standard Google Play
        // toasts will appear, so we don't need to show our own.
        if (!isSignedIn()) {
//            Toast.makeText(this, getString(R.string.achievement) + ": " + achievement,
//                    Toast.LENGTH_LONG).show();
        }
    }

    void pushAccomplishments() {
        if (!isSignedIn()) {
            // can't push to the cloud, so save locally
            mOutbox.saveLocal(this);
            return;
        }
        if (mOutbox.mPowerAchievement) {
            Games.Achievements.unlock(getApiClient(), getString(R.string.achievement_power));
            mOutbox.mPowerAchievement = false;
        }
        if (mOutbox.mMonsterHunterAchievement) {
            Games.Achievements.unlock(getApiClient(), getString(R.string.achievement_monster_hunter));
            mOutbox.mMonsterHunterAchievement = false;
        }
        if (mOutbox.mBestTrackerAchievement) {
            Games.Achievements.unlock(getApiClient(), getString(R.string.achievement_best_tracker));
            mOutbox.mBestTrackerAchievement = false;
        }
        if (mOutbox.mLevel1Achievement) {
            Games.Achievements.unlock(getApiClient(), getString(R.string.achievement_level_1));
            mOutbox.mLevel1Achievement = false;
        }
        if (mOutbox.mSentinelAchievement) {
        	 Games.Achievements.unlock(getApiClient(), getString(R.string.achievement_sentinel));
             mOutbox.mSentinelAchievement = false;
        }
        if (mOutbox.mSearchRemotelyAchievement) {
            Games.Achievements.unlock(getApiClient(), getString(R.string.achievement_search_remotely));
            mOutbox.mSearchRemotelyAchievement = false;
        }
        if (mOutbox.mLevel2Achievement) {
            Games.Achievements.unlock(getApiClient(), getString(R.string.achievement_level_2));
            mOutbox.mLevel2Achievement = false;
        }
        if (mOutbox.mBonusPowerAchievement) {
            Games.Achievements.unlock(getApiClient(), getString(R.string.achievement_bonus_power));
            mOutbox.mBonusPowerAchievement = false;
        }
        if (mOutbox.mGreatPowerAchievement) {
            Games.Achievements.unlock(getApiClient(), getString(R.string.achievement_great_power));
            mOutbox.mGreatPowerAchievement = false;
        }
        if (mOutbox.mLevel3Achievement) {
            Games.Achievements.unlock(getApiClient(), getString(R.string.achievement_level_3));
            mOutbox.mLevel3Achievement = false;
        }
        if (mOutbox.mDoublePowerAchievement) {
        	 Games.Achievements.unlock(getApiClient(), getString(R.string.achievement_double_power));
             mOutbox.mDoublePowerAchievement = false;
        }
        if (mOutbox.mLevel4Achievement) {
            Games.Achievements.unlock(getApiClient(), getString(R.string.achievement_level_4));
            mOutbox.mLevel4Achievement = false;
        }
        if (mOutbox.mExtraPowerAchievement) {
            Games.Achievements.unlock(getApiClient(), getString(R.string.achievement_extra_power));
            mOutbox.mExtraPowerAchievement = false;
        }
        if (mOutbox.mLevel5Achievement) {
            Games.Achievements.unlock(getApiClient(), getString(R.string.achievement_level_5));
            mOutbox.mLevel5Achievement = false;
        }
        if (mOutbox.mLegendaryHunterAchievement) {
            Games.Achievements.unlock(getApiClient(), getString(R.string.achievement_legendary_hunter));
            mOutbox.mLegendaryHunterAchievement = false;
        }
        
        if (mOutbox.mLevel1ModeScore >= 0) {
            Games.Leaderboards.submitScore(getApiClient(), getString(R.string.leaderboard_level_1),
                    mOutbox.mLevel1ModeScore);
            mOutbox.mLevel1ModeScore = -1;
        }
        if (mOutbox.mLevel2ModeScore >= 0) {
            Games.Leaderboards.submitScore(getApiClient(), getString(R.string.leaderboard_level_2),
                    mOutbox.mLevel2ModeScore);
            mOutbox.mLevel2ModeScore = -1;
        }
        if (mOutbox.mLevel3ModeScore >= 0) {
            Games.Leaderboards.submitScore(getApiClient(), getString(R.string.leaderboard_level_3),
                    mOutbox.mLevel3ModeScore);
            mOutbox.mLevel3ModeScore = -1;
        }
        if (mOutbox.mLevel4ModeScore >= 0) {
            Games.Leaderboards.submitScore(getApiClient(), getString(R.string.leaderboard_level_4),
                    mOutbox.mLevel4ModeScore);
            mOutbox.mLevel4ModeScore = -1;
        }
        if (mOutbox.mLevel5ModeScore >= 0) {
            Games.Leaderboards.submitScore(getApiClient(), getString(R.string.leaderboard_level_5),
                    mOutbox.mLevel5ModeScore);
            mOutbox.mLevel5ModeScore = -1;
        }
        mOutbox.saveLocal(this);
    }

    /**
     * Update leaderboards with the user's score.
     *
     * @param finalScore The score the user got.
     */
    void updateLeaderboards(int finalScore) {
        if (mLevel1Mode && mOutbox.mLevel1ModeScore < finalScore) {
            mOutbox.mLevel1ModeScore = finalScore;
        } else if (mLevel2Mode && mOutbox.mLevel2ModeScore < finalScore) {
            mOutbox.mLevel2ModeScore = finalScore;
        } else if (mLevel3Mode && mOutbox.mLevel3ModeScore < finalScore) {
            mOutbox.mLevel3ModeScore = finalScore;
        } else if (mLevel4Mode && mOutbox.mLevel4ModeScore < finalScore) {
            mOutbox.mLevel4ModeScore = finalScore;
        } else if (mLevel5Mode && mOutbox.mLevel5ModeScore < finalScore) {
            mOutbox.mLevel5ModeScore = finalScore;
        }
    }


    @Override
    public void onSignInFailed() {
        // Sign-in failed, so show sign-in button on main menu
        mMainMenuFragment.setGreeting(getString(R.string.signed_out_greeting));
        mMainMenuFragment.setShowSignInButton(true);
    }

    @Override
    public void onSignInSucceeded() {
        // Show sign-out button on main menu
        mMainMenuFragment.setShowSignInButton(false);

        // Set the greeting appropriately on main menu
        Player p = Games.Players.getCurrentPlayer(getApiClient());
        String displayName;
        if (p == null) {
            Log.w(TAG, "mGamesClient.getCurrentPlayer() is NULL!");
            displayName = "???";
        } else {
            displayName = p.getDisplayName();
        }
        mMainMenuFragment.setGreeting("Hello, " + displayName);


        // if we have accomplishments to push, push them
        if (!mOutbox.isEmpty()) {
            pushAccomplishments();
//            Toast.makeText(this, getString(R.string.your_progress_will_be_uploaded),
//                    Toast.LENGTH_LONG).show();
        }
        
        boolean registration = mPrefs.getBoolean(AppConstants.REGISTRATION, false);  
        if(!registration){
        	//call long operation to server
        	loginRegistration();
        	//AppUtils.registrationWithServer(getApplicationContext(), p.getPlayerId(), AppUtils.getMeid(getApplicationContext()),AppConstants.REGISTRATION);
        }
 
        mEditor.putString(AppConstants.ACCOUNT_NAME , Games.getCurrentAccountName(getApiClient()));
 		mEditor.commit();
    }

    private void loginRegistration() {
    	// Server Request URL
    	String emailId = Games.getCurrentAccountName(getApiClient());
    	String meid = AppUtils.getMeid(getApplicationContext());
        String serverURL = AppConstants.ip+"/PlaysWEB/LoginServlet?emailId="+emailId+"&meid="+meid+"&requestType="+AppConstants.REGISTRATION;
//        Toast.makeText(this, "Connecting to "+serverURL,
//              Toast.LENGTH_LONG).show();//TODO remove this toast
        // Create Object and call AsyncTask execute Method
        new LongOperation().execute(serverURL);
	}

	@Override
    public void onSignInButtonClicked() {
        // check if developer read the documentation!
        // (Note: in a production application, this code should NOT exist)
        if (!verifyPlaceholderIdsReplaced()) {
            showAlert("Sample not set up correctly. See README.");
            return;
        }

        // start the sign-in flow
        beginUserInitiatedSignIn();
    }

    @Override
    public void onSignOutButtonClicked() {
        signOut();
        mMainMenuFragment.setGreeting(getString(R.string.signed_out_greeting));
        mMainMenuFragment.setShowSignInButton(true);
    }

    class AccomplishmentsOutbox {
        boolean mPowerAchievement = false;
        boolean mMonsterHunterAchievement = false;
        boolean mBestTrackerAchievement = false;
        boolean mLevel1Achievement = false;
        boolean mSentinelAchievement = false;
        boolean mSearchRemotelyAchievement = false;
        boolean mLevel2Achievement = false;
        boolean mBonusPowerAchievement = false;
        boolean mGreatPowerAchievement = false;
        boolean mLevel3Achievement = false;
        boolean mDoublePowerAchievement = false;
        boolean mLevel4Achievement = false;
        boolean mExtraPowerAchievement = false;
        boolean mLevel5Achievement = false;
        boolean mLegendaryHunterAchievement = false;
        
        int mLevel1ModeScore = -1;
        int mLevel2ModeScore = -1;
        int mLevel3ModeScore = -1;
        int mLevel4ModeScore = -1;
        int mLevel5ModeScore = -1;

        boolean isEmpty() {
            return !mPowerAchievement && !mMonsterHunterAchievement && !mBestTrackerAchievement &&
                    !mLevel1Achievement && !mSentinelAchievement && !mSearchRemotelyAchievement &&
                    !mLevel2Achievement && !mBonusPowerAchievement && !mGreatPowerAchievement &&
                    !mLevel3Achievement && !mDoublePowerAchievement && !mLevel4Achievement &&
                    !mExtraPowerAchievement && !mLevel5Achievement && !mLegendaryHunterAchievement;
        }

        public void saveLocal(Context ctx) {
            /* TODO: This is left as an exercise. To make it more difficult to cheat,
             * this data should be stored in an encrypted file! And remember not to
             * expose your encryption key (obfuscate it by building it from bits and
             * pieces and/or XORing with another string, for instance). */
        }

        public void loadLocal(Context ctx) {
            /* TODO: This is left as an exercise. Write code here that loads data
             * from the file you wrote in saveLocal(). */
        }
    }
    
 // Class with extends AsyncTask class
    private class LongOperation  extends AsyncTask<String, Void, Void> {
         
        private final HttpClient Client = new DefaultHttpClient();
        private String Content;
        private String Error = null;
        private ProgressDialog Dialog = new ProgressDialog(MainActivity.this);
                  
        protected void onPreExecute() {
            // NOTE: You can call UI Element here.
             
            //UI Element
            Dialog.setMessage("connecting to game server...");
            Dialog.show();
        }
 
        // Call after onPreExecute method
        protected Void doInBackground(String... urls) {
            try {
                 
                // Call long running operations here (perform background computation)
                // NOTE: Don't call UI Element here.
                 
                // Server url call by GET method
                HttpGet httpget = new HttpGet(urls[0]);
                ResponseHandler<String> responseHandler = new BasicResponseHandler();
                Content = Client.execute(httpget, responseHandler);
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
             
            // Close progress dialog
            Dialog.dismiss();
             
            if (Error != null) {
                 
//                uiUpdate.setText("Output : "+Error);
                Toast.makeText(MainActivity.this, "Game server not reachable..",
                      Toast.LENGTH_LONG).show();
                 
            } else {
            	boolean registration = mPrefs.getBoolean(AppConstants.REGISTRATION, false);  
//            	Toast.makeText(MainActivity.this, Content,
//                        Toast.LENGTH_LONG).show();
            	if(registration){
            		//Parse Response into our object
                    Type collectionType = new TypeToken<JData>() {
                    }.getType();
                    JData jData = new Gson().fromJson(Content, collectionType);
                    String userAccess = jData.getUserAccess();
                    
            		 if("Y".equalsIgnoreCase(userAccess)){
            			 mEditor.putBoolean(AppConstants.ACCESS , true);
            			 mEditor.putInt(AppConstants.COLLECTED_POWER , jData.getCollectedPowerCount());
            			 mEditor.putInt(AppConstants.TOTAL_SCORE, jData.getScore());
            			 mEditor.putInt(AppConstants.BUSTED_ALIENS, jData.getBustedAliens());
                  		 mEditor.commit();
                    	 startGameActivity();
                     } else if("N".equalsIgnoreCase(userAccess)) {
                    	 showAlert(getString(R.string.submit_user_consent));
                     }
            	} else {
            		mEditor.putBoolean(AppConstants.REGISTRATION , true);
             		mEditor.commit();
            	}             	
             }
        }
         
    }
}
