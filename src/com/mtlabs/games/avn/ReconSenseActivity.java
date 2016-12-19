package com.mtlabs.games.avn;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Point;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ShareActionProvider;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.CancelableCallback;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class ReconSenseActivity extends FragmentActivity
		implements
		// LocationListener,
//		LocationSource, 
		OnMarkerClickListener,
		OnMapClickListener,
		GooglePlayServicesClient.ConnectionCallbacks,
		GooglePlayServicesClient.OnConnectionFailedListener, LocationListener {

	/*
	 * Define a request code to send to Google Play services This code is
	 * returned in Activity.onActivityResult
	 */
	private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

	private static final LatLng HOME = new LatLng(40.531115, -74.567288);
	private static final LatLng ALIEN11 = new LatLng(40.531115, -74.567288);// home
	private static final LatLng ALIEN12 = new LatLng(40.846553, -74.45176);// office
	private static final LatLng ALIEN13 = new LatLng(40.744382, -74.180492);// njit

	private static final LatLng ALIEN21 = new LatLng(40.530086, -74.567218);// home
	private static final LatLng ALIEN22 = new LatLng(40.846265, -74.451841);// office
	private static final LatLng ALIEN23 = new LatLng(40.743727, -74.179028);// njit

	private static final LatLng ALIEN31 = new LatLng(40.532059, -74.567041);// home
	private static final LatLng ALIEN32 = new LatLng(40.846068,-74.452396);// office
	private static final LatLng ALIEN33 = new LatLng(40.744337, -74.179787);// njit

	private static final double DEFAULT_REGION_HINT_RADIUS = 30;
	private static final float ZERO = 0;
	private static final float REGION_HUE = 270;
	private static final int REGION_ALPHA = 60;
	private static final float MAP_HUE = 240;
	private static final int MAP_ALPHA = 60;
	private static final double EARTH_RADIUS = 6371000;
	private static final float ALIEN_SEARCH_REGION = 30;

	/**
	 * NEW API changes
	 */
	// Milliseconds per second
	private static final int MILLISECONDS_PER_SECOND = 1000;
	// Update frequency in seconds
	public static final int UPDATE_INTERVAL_IN_SECONDS = 5;
	// Update frequency in milliseconds
	private static final long UPDATE_INTERVAL = MILLISECONDS_PER_SECOND
			* UPDATE_INTERVAL_IN_SECONDS;
	// The fastest update frequency, in seconds
	private static final int FASTEST_INTERVAL_IN_SECONDS = 1;
	// A fast frequency ceiling in milliseconds
	private static final long FASTEST_INTERVAL = MILLISECONDS_PER_SECOND
			* FASTEST_INTERVAL_IN_SECONDS;

	private static final int SCORE_100 = 100;

	private static final int SCORE_200 = 200;

	private static final String ALIEN_RUN_COUNTER = "ALIEN_RUN_COUNTER";

	private static final String BLOOD_MARKER_LAT = "BLOOD_MARKER_LAT";

	private static final String BLOOD_MARKER_LNG = "BLOOD_MARKER_LNG";

	private static final String HINT = "HINT";

	private static final float FIRE_RANGE = 10;

	private static final int MIN_POWER_LIMIT = 5;
	private static final int MAX_POWER_LIMIT = 10;

	private GoogleMap mMap;
//	private OnLocationChangedListener mListener;
	// private LocationManager locationManager;
	private LatLng alienLatLng1;
	private LatLng alienLatLng2;
	private Marker alienMarker;
	private Marker playerMarker;
	private Marker bulletMarker;
	private Marker alienBloodMarker;
	private ArrayList<LatLng> aliensList;

	private int alienRunCounter = 0;
	private boolean hint;

	private LocationClient mLocationClient;

	private Location mCurrentLocation;
	
	private Location mLastSensedLocation;

	private LocationRequest mLocationRequest;

	private SharedPreferences mPrefs;

	private Editor mEditor;

	private boolean mUpdatesRequested;

	private LatLng alienNextLoc;

	private TextView mScoreTextView;

	private int score;

	private TextView mAliensBustedTextView;

	private int bustedAliens;

	private LatLng alienBloodMarkerLatLng;

	private ShareActionProvider myShareActionProvider;

	private SensorManager mSensorManager;

	protected float mOrientation;

	private int alienPosition;

	private LatLng alienLatLng;

	private Marker targetMarker;

	private Marker sentinelMarker;
	
	private Circle scannerCircle;
	
	private Button mSentinelButton;
	private Button mGenieButton;
	
//	HardwareData hardwareData;
	
	private boolean showSignIn = false;

	private int playerIcon;

	private ArrayList<Marker> powerList;

	private float POWER_COLLECT_REGION = 10;

	private int collectedPowerCount;

	private float ACCURACY;
	
	public float xPosition, xAcceleration,xVelocity = 0.0f;
	public float yPosition, yAcceleration,yVelocity = 0.0f;
	public float xmax,ymax;
	public float frameTime = 0.666f;

	protected long mSensorTimeStamp;

	private boolean mPracticeHunt;

	private boolean bloodTrail;

	private boolean regionHint;

	private LatLng alienHintLoc;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		

		// SupportMapFragment fragment = new SupportMapFragment();
		// getSupportFragmentManager().beginTransaction()
		// .add(android.R.id.content, fragment).commit();
		SharedPreferences settings = getSharedPreferences(AppConstants.PREFS_NAME, 0);
		showSignIn = settings.getBoolean("showSignIn", false);
		showSignIn= true;
		if(showSignIn){
			setContentView(R.layout.activity_show_map);
			setTitle(R.string.app_name);
			setUpMapIfNeeded();

			loadGameDetails();

			if (servicesConnected()) {
				getCurrentLocation();
			}
		} 
		
	}

	private void loadGameDetails() {

		//load game details
		// Open the shared preferences
		mPrefs = getSharedPreferences(AppConstants.PREFS_NAME, Context.MODE_PRIVATE);
		// Get a SharedPreferences editor
		mEditor = mPrefs.edit();
		
		
		
		mScoreTextView = (TextView) findViewById(R.id.score);
//		mBustedAliensTextView = (TextView) findViewById(R.id.bustedAliens);
		
		double alienBloodMarkerLat = Double.parseDouble(mPrefs.getString(BLOOD_MARKER_LAT, "0"));
		double alienBloodMarkerLng = Double.parseDouble(mPrefs.getString(BLOOD_MARKER_LNG, "0"));
		
		if(alienBloodMarkerLat != 0 && alienBloodMarkerLng != 0)
			alienBloodMarkerLatLng = new LatLng(alienBloodMarkerLat, alienBloodMarkerLng);
		
		hint = true;
		alienRunCounter = mPrefs.getInt(ALIEN_RUN_COUNTER, 0);
		score = mPrefs.getInt(AppConstants.TOTAL_SCORE, 0);
		bustedAliens = mPrefs.getInt(AppConstants.BUSTED_ALIENS, 0);		
		collectedPowerCount = mPrefs.getInt(AppConstants.COLLECTED_POWER, AppConstants.DEFAULT_POWER);
		updateUI();	
		 
		aliensList = new ArrayList<LatLng>();
		// aliensList.add(ALIEN11);
		// aliensList.add(ALIEN21);
		// aliensList.add(ALIEN31);
		aliensList.add(ALIEN12);
		aliensList.add(ALIEN22);
		aliensList.add(ALIEN32);
		// aliensList.add(ALIEN13);
		// aliensList.add(ALIEN23);
		// aliensList.add(ALIEN33);
		// load from server
		
		playerIcon = mPrefs.getInt(AppConstants.PLAYER_ICON, R.drawable.ic_pl_girl_knife);

		boolean showWinScore = mPrefs.getBoolean(AppConstants.SHOW_WIN_SCORE, false);
		boolean showGenie = mPrefs.getBoolean(AppConstants.SHOW_GENIE, false);
		
		if(showGenie){			
			loadGenie();			

			mEditor.putBoolean(AppConstants.SHOW_WIN_SCORE , false);
			mEditor.commit();
		} else {
			mGenieButton = (Button) findViewById(R.id.hints);
			mGenieButton.setVisibility(View.GONE);//TODO make this GONE after testing, remove load
//			loadGenie();
		}
		
		boolean showSentinel = mPrefs.getBoolean(AppConstants.SHOW_SENTINEL, false);
		if(showSentinel)
			loadSentinel();
		else{
			mSentinelButton = (Button) findViewById(R.id.sentinel);
			mSentinelButton.setVisibility(View.GONE);
		}
	}

	private void loadGenie() {
		
		boolean mHelpGestures = mPrefs.getBoolean(AppConstants.HELP_GESTURES, true);
		
		if(mHelpGestures) {
			showImageToast(R.drawable.ic_genie, getString(R.string.touch_genie));
		}
		
		mGenieButton = (Button) findViewById(R.id.hints);
		mGenieButton.setVisibility(View.VISIBLE);
		mGenieButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				markAlienHints();
			}
		});
	}

	private void loadSentinel() {
		mSentinelButton = (Button) findViewById(R.id.sentinel);
		
		mSentinelButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				markSentinel();
			}
		});
		
		loadSentinelMarker();
	}

	private void loadSentinelMarker() {
		double sentinelLat = Double.longBitsToDouble(mPrefs.getLong(AppConstants.SENTINEL_LAT, Double.doubleToLongBits(0)));
		double sentinelLng = Double.longBitsToDouble(mPrefs.getLong(AppConstants.SENTINEL_LNG, Double.doubleToLongBits(0)));
		double ZERO = 0.0;
		if(sentinelLat != ZERO || sentinelLng != ZERO){
			sentinelMarker = placeMarker(sentinelLat, sentinelLng,
					"Sentinel", R.drawable.ic_sentinel_button);
		}
	}

	private void getCurrentLocation() {

		/**
		 * New API
		 */
		/*
		 * Create a new location client, using the enclosing class to handle
		 * callbacks.
		 */
		mLocationClient = new LocationClient(this, this, this);
		// Start with updates turned on as default
		mUpdatesRequested = true;

		// Create the LocationRequest object
		mLocationRequest = LocationRequest.create();
		// Use high accuracy
		mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
		// Set the update interval to 5 seconds
		mLocationRequest.setInterval(UPDATE_INTERVAL);
		// Set the fastest update interval to 1 second
		mLocationRequest.setFastestInterval(FASTEST_INTERVAL);

//		mCurrentLocation = mLocationClient.getLastLocation();

		
		/**
		 * OLD CODE
		 */
		// locationManager = (LocationManager)
		// getSystemService(LOCATION_SERVICE);
		//
		// if (locationManager != null) {
		// boolean gpsIsEnabled = locationManager
		// .isProviderEnabled(LocationManager.GPS_PROVIDER);
		// boolean networkIsEnabled = locationManager
		// .isProviderEnabled(LocationManager.NETWORK_PROVIDER);
		//
		// if (gpsIsEnabled) {
		// locationManager.requestLocationUpdates(
		// LocationManager.GPS_PROVIDER, 5000L, 10F, this);
		// } else if (networkIsEnabled) {
		// locationManager.requestLocationUpdates(
		// LocationManager.NETWORK_PROVIDER, 5000L, 10F, this);
		// } else {
		// // Show an error dialog that GPS is disabled...
		// }
		// } else {
		// // Show some generic error dialog because something must have gone
		// // wrong with location manager.
		// }

//		setUpMapIfNeeded();
	}

//	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.show_map, menu);
//		MenuItem item = menu.findItem(R.id.menu_item_share);
//	     myShareActionProvider = (ShareActionProvider)item.getActionProvider();
//	     myShareActionProvider.setShareHistoryFileName(
//	       ShareActionProvider.DEFAULT_SHARE_HISTORY_FILE_NAME);
//	     myShareActionProvider.setShareIntent(createShareIntent());
		return true;
	}
	
	private Intent createShareIntent() {
		Intent shareIntent = new Intent(Intent.ACTION_SEND);
		shareIntent.setType("text/plain");
		String currentLatLng = "";
		if (mCurrentLocation != null) {
			currentLatLng = "Monster found at Lat/Lng: "
					+ mCurrentLocation.getLatitude() + ", "
					+ mCurrentLocation.getLongitude();
		}
		shareIntent.putExtra(Intent.EXTRA_TEXT, currentLatLng);

		return shareIntent;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.practiceHunt:
			practiceHunt();			
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void practiceHunt() {
		mPracticeHunt = true;
		mEditor.putBoolean(AppConstants.HELP_GESTURES , true);
		mEditor.commit();	
		showPracticeAlien();//show alien
	}

	private void showPracticeAlien() {
		if(mCurrentLocation!=null){
			double lat = mCurrentLocation.getLatitude();
			double lon = mCurrentLocation.getLongitude() + (Math.random() / 100000);

			LatLng alienPracticeLoc = new LatLng(lat, lon);
			startingGame(mCurrentLocation, alienPracticeLoc);
		}
		
		
	}

	private void loadSettings() {
		Intent i = new Intent(getApplicationContext(),
				SettingsActivity.class);
		startActivity(i);
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();

		saveGameState();
		finish();
	}

	@Override
	public void onPause() {
		mUpdatesRequested = false;
		
		boolean alienAlertValue = mPrefs.getBoolean(AppConstants.ALIEN_ALERT, true);
		
		if(alienAlertValue){
			// Save the current setting for monster alerts
			mEditor.putBoolean(AppConstants.ALIEN_UPDATES_ON, true);
			mEditor.commit();
		} else {
			mEditor.putBoolean(AppConstants.ALIEN_UPDATES_ON, false);
			mEditor.commit();
		}

		// if (locationManager != null) {
		// locationManager.removeUpdates(this);
		// }
		saveGameState();
		super.onPause();
		// Toast.makeText(this, "onPause!!", Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onResume() {
		super.onResume();

		playerIcon = mPrefs.getInt(AppConstants.PLAYER_ICON, R.drawable.ic_pl_girl_knife);
		
		// Save the current setting for monster alerts
		mEditor.putBoolean(AppConstants.ALIEN_UPDATES_ON, false);
		mEditor.commit();
//		setUpMapIfNeeded();
//
//		getCurrentLocation();
		// Toast.makeText(this, "onResume!!", Toast.LENGTH_SHORT).show();
	}

	@Override
	protected void onStart() {
		super.onStart();
		// Connect the client.
		mLocationClient.connect();
		if(alienMarker!=null){
			setupSensorManager();
		}
//		hardwareData.prepareWifiRadio();
	}

	@Override
	protected void onStop() {
		// If the client is connected
		if (mLocationClient.isConnected()) {
			stopPeriodicUpdates();
		}
		// Disconnecting the client invalidates it.
		mLocationClient.disconnect();
		if(mSensorManager!=null && mSensorListener!= null){
//			Toast.makeText(this, "Orientation sensing stopped!!", Toast.LENGTH_SHORT).show();
			mSensorManager.unregisterListener(mSensorListener, mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION));
		}
//		hardwareData.setWifiInitialState();
		super.onStop();
	}

	private void stopPeriodicUpdates() {
		mLocationClient.removeLocationUpdates(this);
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {

		super.onConfigurationChanged(newConfig);
		// Toast.makeText(this, "onConfigurationChanged!!",
		// Toast.LENGTH_SHORT).show();
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {

		super.onSaveInstanceState(outState);

	}

	/**
	 * Sets up the map if it is possible to do so (i.e., the Google Play
	 * services APK is correctly installed) and the map has not already been
	 * instantiated.. This will ensure that we only ever call
	 * {@link #setUpMap()} once when {@link #mMap} is not null.
	 * <p>
	 * If it isn't installed {@link SupportMapFragment} (and
	 * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt
	 * for the user to install/update the Google Play services APK on their
	 * device.
	 * <p>
	 * A user can return to this Activity after following the prompt and
	 * correctly installing/updating/enabling the Google Play services. Since
	 * the Activity may not have been completely destroyed during this process
	 * (it is likely that it would only be stopped or paused),
	 * {@link #onCreate(Bundle)} may not be called again so we should call this
	 * method in {@link #onResume()} to guarantee that it will be called.
	 */
	private void setUpMapIfNeeded() {
		// Do a null check to confirm that we have not already instantiated the
		// map.
//		Toast.makeText(this, "Loading Map!!",
//				Toast.LENGTH_SHORT).show();
		if (mMap == null) {
			SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
					.findFragmentById(R.id.map);
			mapFragment.setRetainInstance(true);
			// Try to obtain the map from the SupportMapFragment.
			mMap = ((SupportMapFragment) getSupportFragmentManager()
					.findFragmentById(R.id.map)).getMap();
			// Check if we were successful in obtaining the map.

			if (mMap != null) {
				setUpMap();
			}

			// This is how you register the LocationSource
//			mMap.setLocationSource(this);

			// set map color
			// setMapColor();
		}
//		Toast.makeText(this, "Setup Map complete!!.",
//				Toast.LENGTH_SHORT).show();
	}

	private void setMapColor() {
		int mFillColor = Color.HSVToColor(MAP_ALPHA, new float[] { MAP_HUE, 1,
				1 });
		// Create a rectangle with two rectangular holes.
		mMap.addPolygon(new PolygonOptions()
				.addAll(createCWRectangle(HOME, EARTH_RADIUS, EARTH_RADIUS))
				.fillColor(mFillColor).strokeColor(Color.BLUE).strokeWidth(0));
	}

	/**
	 * Creates a List of LatLngs that form a rectangle with the given
	 * dimensions.
	 */
	private List<LatLng> createRectangle(LatLng center, double halfWidth,
			double halfHeight) {
		// Note that the ordering of the points is counterclockwise (as long as
		// the halfWidth and
		// halfHeight are less than 90).
		return Arrays.asList(new LatLng(center.latitude - halfHeight,
				center.longitude - halfWidth), new LatLng(center.latitude
				- halfHeight, center.longitude + halfWidth), new LatLng(
				center.latitude + halfHeight, center.longitude + halfWidth),
				new LatLng(center.latitude + halfHeight, center.longitude
						- halfWidth), new LatLng(center.latitude - halfHeight,
						center.longitude - halfWidth));
	}

	private List<LatLng> createCWRectangle(LatLng center, double halfWidth,
			double halfHeight) {
		List<LatLng> rect = createRectangle(center, halfWidth, halfHeight);
		Collections.reverse(rect);
		return rect;
	}

	/**
	 * This is where we can add markers or lines, add listeners or move the
	 * camera.
	 * <p>
	 * This should only be called once and when we are sure that {@link #mMap}
	 * is not null.
	 */
	private void setUpMap() {
		mMap.setMyLocationEnabled(true);
		mMap.setOnMarkerClickListener(this);
	}

//	@Override
//	public void activate(OnLocationChangedListener listener) {
//		mListener = listener;
//	}
//
//	@Override
//	public void deactivate() {
//		mListener = null;
//	}

	@Override
	public void onLocationChanged(Location location) {
//		if (mListener != null) {
//			mListener.onLocationChanged(location);
//		Toast.makeText(this, "Location Changed", Toast.LENGTH_SHORT).show();
		if(mLastSensedLocation==null)
			mLastSensedLocation = location;
		
		float distMoved = getDistance(mLastSensedLocation, location);
		if(distMoved >= AppConstants.MOVEMENT_LIMIT){
			mLastSensedLocation = location;
			//perform WiFi scan and send to server
			iniHardwareMonitorService(location);
		}
		
			mCurrentLocation = location;
			double lat = location.getLatitude();
			double lng = location.getLongitude();
			
			ACCURACY = location.getAccuracy();

			mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(
					lat, lng), 18), new CancelableCallback() {

						@Override
						public void onFinish() {
							if (alienMarker == null) {
								if(alienBloodMarker == null)
									searchForAlien(mCurrentLocation);
//								searchForDemoAlien(location);
//								searchNJITAlien(location);
							} else {
								float alienLocDist = getDistance(alienMarker.getPosition(), mCurrentLocation);
								if (alienLocDist != 0.0	&& alienLocDist > ALIEN_SEARCH_REGION + 100) {
									
									resetGameMarkers();
									Toast.makeText(getApplicationContext(), "Moved out of Monster range!!",
											Toast.LENGTH_SHORT).show();
									if(powerList.size()==0){
										loadPower(mCurrentLocation);
									}
								}
							}
						}

						@Override
						public void onCancel() {
						}
					});

			
			
			
			
			if(playerMarker == null){
				playerMarker = placeMarker(location.getLatitude(),
						location.getLongitude(), "You", playerIcon);
			} else {
				LatLng currLocation = new LatLng(location.getLatitude(),
						location.getLongitude());
				playerMarker.setPosition(currLocation);	
			}
			
			if(powerList == null){
				loadPower(location);
			} else {
				checkForPowerMarker(location);
			}
			
//		}
	}
	
	protected void resetGameMarkers() {

		mEditor.putBoolean(AppConstants.ALIEN_FOUND , false);

		mEditor.commit();		
		
		// remove markers

		hint = true;
		bloodTrail = false;
		regionHint = false;
		
		alienMarker.remove();
		alienMarker = null;
		targetMarker.remove();
		targetMarker=null;
		scannerCircle.remove();
		scannerCircle=null;
		mMap.getUiSettings().setZoomGesturesEnabled(true);
		mMap.getUiSettings().setAllGesturesEnabled(true);
		mMap.clear();
		powerList.clear();
		if(mSensorManager!=null && mSensorListener!= null){
//			Toast.makeText(this, "Orientation sensing stopped!!", Toast.LENGTH_SHORT).show();
			mSensorManager.unregisterListener(mSensorListener, mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION));
		}
	}

	private void searchNJITAlien(Location location) {
		if(mCurrentLocation!=null){
			String serverURL = AppConstants.ip+"/PlaysWEB/AlienServlet?requestType=alienSearch";
	      // Create Object and call AsyncTask execute Method
			new LongOperation().execute(serverURL);
		}
	}
	
	private void alienShot(LatLng latLng) {
		if(mCurrentLocation!=null){
			String serverURL = AppConstants.ip+"/PlaysWEB/AlienServlet?requestType=alienShot";
		      // Create Object and call AsyncTask execute Method
				new LongOperation().execute(serverURL);
		}	
	}
	
	private void alienHints(Location location) {
		if(mCurrentLocation!=null){
			String serverURL = AppConstants.ip+"/PlaysWEB/AlienServlet?requestType=alienHints";
	      // Create Object and call AsyncTask execute Method
			new LongOperation().execute(serverURL);
		}
	}

	protected void iniHardwareMonitorService(Location location) {
//		if(location!=null)
		HardwareData hardwareData = new HardwareData(getApplicationContext(), location);
			hardwareData.startWifiScan();
	}
	
	private void checkForPowerMarker(Location location) {
		boolean needMorePower = false;
		for(Iterator<Marker> powerIterator = powerList.iterator(); powerIterator.hasNext();){
			Marker powerMarker = powerIterator.next();
			float dist = getDistance(powerMarker.getPosition(), location);
//			Toast.makeText(this, "Distance: "+dist, Toast.LENGTH_SHORT).show();
			if (dist < POWER_COLLECT_REGION || (ACCURACY < 50 && dist < ACCURACY) ) {
				powerMarker.remove();
				powerIterator.remove();
				powerMarker = null;
				collectedPowerCount++;
				updatePoweronUI();	
				showImageToast(R.drawable.ic_power, "Woo Hoo!! More power!! You power count: "+collectedPowerCount);
				
				if(powerList.size()<=MIN_POWER_LIMIT){
					needMorePower = true;					
				}
			}
		}
		
		if(needMorePower){
			addMorePower(location);
		}
	}

	private void addMorePower(Location location) {
		//placing power randomly. improve to place them mostly near the aliens.
		int markerPosition = 1;
		for (int i = powerList.size(); i < MAX_POWER_LIMIT; i++) {
			// Toast.makeText(this, "Power: "+i, Toast.LENGTH_SHORT).show();
			LatLng powerLatLng = null;
			
			if(markerPosition == 1){		                    	
				powerLatLng = new LatLng(location.getLatitude() - (Math.random() / 1000),location.getLongitude() - (Math.random() / 1000));
				markerPosition = 2;
            } else if(markerPosition == 2){
            	powerLatLng = new LatLng(location.getLatitude() + (Math.random() / 1000),location.getLongitude() + (Math.random() / 1000));
            	markerPosition = 3;
            } else if(markerPosition == 3){
            	powerLatLng = new LatLng(location.getLatitude() + (Math.random() / 1000),location.getLongitude() - (Math.random() / 1000));
            	markerPosition = 4;
            } else if(markerPosition == 4){
            	powerLatLng = new LatLng(location.getLatitude() - (Math.random() / 1000),location.getLongitude() + (Math.random() / 1000));
            	markerPosition = 1;
            }
			
			float distToPower = getDistance(powerLatLng, location);
			if( (ACCURACY > POWER_COLLECT_REGION && ACCURACY < 50 && distToPower > ACCURACY) || distToPower > POWER_COLLECT_REGION){
				Marker powerMarker = placeMarker(powerLatLng.latitude, powerLatLng.longitude, "Fire power",
						R.drawable.ic_m_power);
				
				powerList.add(powerMarker);
			} 
			
		}
		
	}

	private void loadPower(Location location) {
		//placing power randomly. improve to place them mostly near the aliens.
		powerList = new ArrayList<Marker>();
		int markerPosition = 1;
		for (int i = 0; i < MAX_POWER_LIMIT; i++) {
			// Toast.makeText(this, "Power: "+i, Toast.LENGTH_SHORT).show();
			LatLng powerLatLng = null;
			
			if(markerPosition == 1){		                    	
				powerLatLng = new LatLng(location.getLatitude() - (Math.random() / 1000),location.getLongitude() - (Math.random() / 1000));
				markerPosition = 2;
            } else if(markerPosition == 2){
            	powerLatLng = new LatLng(location.getLatitude() + (Math.random() / 1000),location.getLongitude() + (Math.random() / 1000));
            	markerPosition = 3;
            } else if(markerPosition == 3){
            	powerLatLng = new LatLng(location.getLatitude() + (Math.random() / 1000),location.getLongitude() - (Math.random() / 1000));
            	markerPosition = 4;
            } else if(markerPosition == 4){
            	powerLatLng = new LatLng(location.getLatitude() - (Math.random() / 1000),location.getLongitude() + (Math.random() / 1000));
            	markerPosition = 1;
            }
						
			float distToPower = getDistance(powerLatLng, location);
			if( (ACCURACY > POWER_COLLECT_REGION && ACCURACY < 50 && distToPower > ACCURACY) || distToPower > POWER_COLLECT_REGION){
				Marker powerMarker = placeMarker(powerLatLng.latitude, powerLatLng.longitude, "Fire power",
						R.drawable.ic_m_power);
				
				powerList.add(powerMarker);
			} 
		}
		
		boolean mHelpGestures = mPrefs.getBoolean(AppConstants.HELP_GESTURES, true);
		
		if(mHelpGestures)
			showImageToast(R.drawable.ic_power, "Walk around to collect more power!!");
	}
	
	private void searchForDemoAlien(Location location) {
//		Toast.makeText(this, "Searching Monster!!", Toast.LENGTH_SHORT).show();
		drawScanner(new LatLng(location.getLatitude(), location.getLongitude()));
		
		if(alienBloodMarkerLatLng != null){
			alienBloodMarker = placeMarker(alienBloodMarkerLatLng.latitude,
					alienBloodMarkerLatLng.longitude, "Monster Blood", R.drawable.ic_alien_blood);
			alienBloodMarker.remove();
			alienBloodMarkerLatLng = null;
		}
		
		if(alienBloodMarker == null) {
			if(alienNextLoc == null){
				// place region randomly
				double lat = location.getLatitude();
				double lon = location.getLongitude() + (Math.random() / 100000);

				alienNextLoc = new LatLng(lat, lon);
			}
			
			
			float dist = getDistance(alienNextLoc, location);
//			Toast.makeText(this, "Distance: "+dist, Toast.LENGTH_SHORT).show();
			if (dist != 0.0 && (dist < ALIEN_SEARCH_REGION || (ACCURACY < 100 && dist < ACCURACY) )) {
				Toast.makeText(this, "Found Monster", Toast.LENGTH_SHORT).show();
				mMap.clear();
				//This check is important, in early version it caused crashing of app on map load
				if(powerList!=null)
					powerList.clear();
				alienMarker = mMap.addMarker(new MarkerOptions().position(alienNextLoc)
				// .title("Monster")
				// .snippet("Monster is dangerous")
						.icon(BitmapDescriptorFactory
								.fromResource(R.drawable.ic_alien)));
				startingGame(location, alienNextLoc);
			} else if (alienMarker != null && dist != 0.0
					&& dist > ALIEN_SEARCH_REGION + 10) {
				alienMarker.remove();
				alienMarker = null;
				Toast.makeText(this, "Moved out of Monster range!!",
						Toast.LENGTH_SHORT).show();
			}
		}
		
	}

	private void searchForAlien(Location location) {
		// Toast.makeText(this, "Searching Monster!!",
		// Toast.LENGTH_SHORT).show();
		drawScanner(new LatLng(location.getLatitude(), location.getLongitude()));

		if (alienNextLoc == null) {
			// place region randomly
			// double lat = location.getLatitude();
			// double lon = location.getLongitude() + (Math.random() / 100000);
			//
			// alienNextLoc = new LatLng(lat, lon);

			alienNextLoc = getNextAlienLoc();
		}
		if (alienHintLoc == null) {
			alienHintLoc = getAlienHintLoc();
		}

		double alienLat = Double.longBitsToDouble(mPrefs.getLong(
				AppConstants.ALIEN_LAT, Double.doubleToLongBits(0)));
		double alienLng = Double.longBitsToDouble(mPrefs.getLong(
				AppConstants.ALIEN_LNG, Double.doubleToLongBits(0)));
		boolean alienFound = mPrefs.getBoolean(AppConstants.ALIEN_FOUND, false);
		double ZERO = 0.0;
		LatLng alienLoc = null;
		if (alienLat != ZERO || alienLng != ZERO) {
			alienLoc = new LatLng(alienLat, alienLng);
		}

		boolean alienNext = mPrefs.getBoolean(AppConstants.SHOT_ALIEN, false);
		float alienNextLocDist = 0;
		if (alienNextLoc != null) {
			alienNextLocDist = getDistance(alienNextLoc, location);
		}

		boolean alienHint = mPrefs.getBoolean(AppConstants.HINT_ALIEN, false);
		float alienHintLocDist = 0;
		if (alienHintLoc != null) {
			alienHintLocDist = getDistance(alienHintLoc, location);
		}

		// Toast.makeText(this, "Distance: "+dist, Toast.LENGTH_SHORT).show();
		if (alienFound && alienLoc != null) {
			startingGame(location, alienLoc);
		} else if (alienNext && alienNextLoc != null
				&& alienNextLocDist != 0.0
				&& (alienNextLocDist < ALIEN_SEARCH_REGION || (ACCURACY < 100 && alienNextLocDist < ACCURACY))) {
			mEditor.putBoolean(AppConstants.SHOT_ALIEN , false);
			mEditor.commit();
			startingGame(location, alienNextLoc);
		} else if (alienHint && alienHintLoc != null
				&& alienHintLocDist != 0.0
				&& (alienHintLocDist < ALIEN_SEARCH_REGION || (ACCURACY < 100 && alienHintLocDist < ACCURACY))) {
			mEditor.putBoolean(AppConstants.HINT_ALIEN , false);
			mEditor.commit();
			startingGame(location, alienHintLoc);
		}

	}

	private void startingGame(Location location, LatLng alien12) {
		Toast.makeText(this, "Found Monster", Toast.LENGTH_SHORT).show();
		mMap.clear();
		//This check is important, in early version it caused crashing of app on map load
		if(powerList!=null)
			powerList.clear();
		alienLatLng2 = new LatLng(location.getLatitude() + (Math.random() / 1000),location.getLongitude() + (Math.random() / 1000));
		alienMarker = mMap.addMarker(new MarkerOptions().position(alienLatLng2)
		// .title("Monster")
		// .snippet("Monster is dangerous")
				.icon(BitmapDescriptorFactory
						.fromResource(R.drawable.ic_alien)));
		// move alienMarker in random position
		// moveAlienRandomly(alien12);
		alienLatLng1 = new LatLng(location.getLatitude() + (Math.random() / 1000),location.getLongitude() + (Math.random() / 1000));
		
		alienLatLng = alien12;
		alienPosition = 1;
		//move it in straight line
		animateAlienMarker(alienMarker, alienLatLng1, false);
		// draw firing target
		drawFiringTarget(location);
		// listen to touch event to fire the bullets
		mMap.setOnMapClickListener(this);
		mMap.getUiSettings().setZoomGesturesEnabled(false);
		mMap.getUiSettings().setAllGesturesEnabled(false);
	}
	
	 public void animateAlienMarker(final Marker marker, final LatLng toPosition,
		        final boolean hideMarker) {
		    final Handler handler = new Handler();
		    final long start = SystemClock.uptimeMillis();
		    Projection proj = mMap.getProjection();
		    Point startPoint = proj.toScreenLocation(marker.getPosition());
		    final LatLng startLatLng = proj.fromScreenLocation(startPoint);
		    final long duration = 10000;
		    final Interpolator interpolator = new LinearInterpolator();
		    handler.post(new Runnable() {
		        @Override
		        public void run() {
		            long elapsed = SystemClock.uptimeMillis() - start;
		            float t = interpolator.getInterpolation((float) elapsed
		                    / duration);
		            double lng = t * toPosition.longitude + (1 - t)
		                    * startLatLng.longitude;
		            double lat = t * toPosition.latitude + (1 - t)
		                    * startLatLng.latitude;
		            if(alienMarker!=null)
		            	marker.setPosition(new LatLng(lat, lng));
		            if (t < 1.0) {
		                // Post again 16ms later.
		                handler.postDelayed(this, 16);
		            } else {
		                if (hideMarker) {
		                    marker.setVisible(false);
		                } else {
		                    marker.setVisible(true);
		                    if(alienMarker!=null && alienPosition == 1){		                    	
		                		alienLatLng2 = new LatLng(alienLatLng.latitude - (Math.random() / 1000),alienLatLng.longitude - (Math.random() / 1000));
		                    	animateAlienMarker(alienMarker, alienLatLng2, false);
		                    	alienPosition = 2;
		                    } else if(alienMarker!=null && alienPosition == 2){
		                    	alienLatLng1 = new LatLng(alienLatLng.latitude + (Math.random() / 1000),alienLatLng.longitude + (Math.random() / 1000));
		                    	animateAlienMarker(alienMarker, alienLatLng1, false);
		                    	alienPosition = 3;
		                    } else if(alienMarker!=null && alienPosition == 3){
		                    	alienLatLng1 = new LatLng(alienLatLng.latitude - (Math.random() / 1000),alienLatLng.longitude + (Math.random() / 1000));
		                    	animateAlienMarker(alienMarker, alienLatLng1, false);
		                    	alienPosition = 4;
		                    } else if(alienMarker!=null && alienPosition == 4){
		                    	alienLatLng1 = new LatLng(alienLatLng.latitude + (Math.random() / 1000),alienLatLng.longitude - (Math.random() / 1000));
		                    	animateAlienMarker(alienMarker, alienLatLng1, false);
		                    	alienPosition = 1;
		                    }
		                }
		            }
		        }
		    });
		}

	 private void showImageToast(int toastImage, String toastText){
		 LayoutInflater inflater = getLayoutInflater();
			View layout = inflater.inflate(R.layout.tilt_toast,
			                               (ViewGroup) findViewById(R.id.toast_layout_root), false);
			
			ImageView image = (ImageView) layout.findViewById(R.id.tilt);
			image.setImageResource(toastImage);
			TextView text = (TextView) layout.findViewById(R.id.toasttext);
			text.setText(toastText);
			
			Toast toast = new Toast(this);
			toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
			toast.setDuration(Toast.LENGTH_LONG);
			toast.setView(layout);
			toast.show();
			
	 }
	 
	private void drawFiringTarget(Location currLoc) {

		boolean mHelpGestures = mPrefs.getBoolean(AppConstants.HELP_GESTURES, true);
		
		if(mHelpGestures){
			showImageToast(R.drawable.tilt, getString(R.string.tilt_phone));		
	
			showImageToast(R.drawable.touch, getString(R.string.touch_phone));

			mEditor.putBoolean(AppConstants.HELP_GESTURES , false);
			mEditor.commit();
		}
		setupSensorManager();
		
		targetMarker = placeMarker(currLoc.getLatitude(),
				currLoc.getLongitude(), getString(R.string.target_marker_text), R.drawable.ic_target);
		playerMarker = placeMarker(currLoc.getLatitude(),
				currLoc.getLongitude(), getString(R.string.player_marker_text), playerIcon);
	}

	private float getDistance(LatLng alien, Location currLoc) {
		LatLng currLocation = new LatLng(currLoc.getLatitude(),
				currLoc.getLongitude());
		return getDistance(alien, currLocation);
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
	
	private float getDistance(Location loc1, Location loc2) {
		// Location l = new Location(l);
		// l.setLatitude(alienMarker.latitude);
		// l.setLongitude(alienMarker.longitude);
		float[] results = new float[1];
		if(loc1!=null && loc2!=null) {
			Location.distanceBetween(loc1.getLatitude(),
					loc1.getLongitude(),
					loc2.getLatitude(),
					loc2.getLongitude(), results);
		}
		return results[0];
	}

	private Marker placeMarker(double lat, double lng, String title,
			int resourceId) {
		Marker marker = mMap.addMarker(new MarkerOptions()
				.position(new LatLng(lat, lng)).title(title)
				.icon(BitmapDescriptorFactory.fromResource(resourceId)));
		return marker;
	}

	protected void markAlienHints(){

		Location currLoc = mMap.getMyLocation();
		if (currLoc != null) {
			if (!hint) {
				if (bloodTrail) {
					showImageToast(R.drawable.hiking,
							getString(R.string.praticeWalk));
				} 
				if(regionHint) {
					Toast.makeText(this, "Monster possibly hiding at highlighted circles!!",
							Toast.LENGTH_SHORT).show();
				}
				
			} else if(alienMarker == null){

				alienHints(currLoc);
				boolean showGenie2 = mPrefs.getBoolean(
						AppConstants.SHOW_GENIE_2, false);
				if (alienBloodMarkerLatLng != null) {
					alienNextLoc = getNextAlienLoc();

					if (alienNextLoc != null) {
						// get busted latlng

						drawTrailHint(alienBloodMarkerLatLng, alienNextLoc);
						
						//add floor num
						int floorNum = mPrefs.getInt(AppConstants.SHOT_ALIEN_FLOOR, 0);
						if(floorNum!=0){
							addFloorNum(alienNextLoc.latitude, alienNextLoc.longitude, floorNum);						
						}

						//if (showGenie2)
							drawRegionHint(alienNextLoc);
							
						showImageToast(R.drawable.hiking,
								getString(R.string.praticeWalk));
						float dist = getDistance(playerMarker.getPosition(), alienBloodMarkerLatLng);
						if(dist>200){
							Toast.makeText(this, "Zoom out to view the blood trail.",
									Toast.LENGTH_SHORT).show();
						}
					} else {// TODO remove this toast
//						Toast.makeText(this, "Escaped monster location not found.",
//								Toast.LENGTH_SHORT).show();
					}
				} 
				// Hints for near by monsters
					alienHintLoc = getAlienHintLoc();

					if (alienHintLoc != null) {
						if (showGenie2){
							//add floor num
							int floorNum = mPrefs.getInt(AppConstants.HINT_ALIEN_FLOOR, 0);
							if(floorNum!=0){
								addFloorNum(alienNextLoc.latitude, alienNextLoc.longitude, floorNum);
							}
							drawRegionHint(alienHintLoc);
							Toast.makeText(this, "Monster possibly hiding at highlighted circles!!",
									Toast.LENGTH_SHORT).show();
						}
					} else {// TODO remove this toast
//						Toast.makeText(this, "No near by monsters.",
//								Toast.LENGTH_SHORT).show();
					}
					
					if (!bloodTrail && !regionHint) {
						Toast.makeText(this, "Find new Monster to get tracking hints!!",
								Toast.LENGTH_SHORT).show();
					}
					
			} else {
				Toast.makeText(this, "Hunt the Monster on screen!!",
						Toast.LENGTH_SHORT).show();
			}
				
			
		} else {
			Toast.makeText(this, "Wait for current location!!",
					Toast.LENGTH_SHORT).show();
		}
	
	}

	private void addFloorNum(double lat, double lng, int floorNum) {
		Marker marker = mMap.addMarker(new MarkerOptions()
		.position(new LatLng(lat, lng))
		.title("Floor#"+floorNum)
		.icon(BitmapDescriptorFactory.fromResource(R.drawable.blank)));
		marker.showInfoWindow();
	}

	protected void markAlienBusted() {
		Location currLoc = mMap.getMyLocation();
		if (currLoc != null) {
			if (!hint) {
				Toast.makeText(this,
						R.string.follow_hint,
						Toast.LENGTH_SHORT).show();
			} else if (alienBloodMarker != null) {
				if(alienMarker == null){
					// get busted latlng
					LatLng alienLoc1 = new LatLng(
							alienBloodMarker.getPosition().latitude,
							alienBloodMarker.getPosition().longitude);
					// remove blood
					alienBloodMarker.remove();
					alienBloodMarker = null;
					alienHints(currLoc);
					
						alienNextLoc = getNextAlienLoc();
						if(alienNextLoc!=null){
							drawTrailHint(alienLoc1, alienNextLoc);
							boolean showGenie2 = mPrefs.getBoolean(AppConstants.SHOW_GENIE_2, false);
							if(showGenie2)
								drawRegionHint(alienNextLoc);

							
							showImageToast(R.drawable.hiking, getString(R.string.praticeWalk));	
						}
						
					
//					alienNextLoc = getNextDemoAlien(currLoc);
					
					// draw trail
//					drawTrailHint(alienLoc1, aliensList.get(alienRunCounter));
//					drawRegionHint(aliensList.get(alienRunCounter));
					
				} else {
					Toast.makeText(this, "Hunt the Monster already found!!",
							Toast.LENGTH_SHORT).show();
				}
				
			} else {
				Toast.makeText(this, "Follow the blood trail!!",
						Toast.LENGTH_SHORT).show();
			}
		} else {
			Toast.makeText(this, "Wait for current location!!",
					Toast.LENGTH_SHORT).show();
		}
	}

	private LatLng getNextAlienLoc() {
		double alienNextLat = Double.longBitsToDouble(mPrefs.getLong(AppConstants.SHOT_ALIEN_LAT, Double.doubleToLongBits(0)));
		double alienNextLng = Double.longBitsToDouble(mPrefs.getLong(AppConstants.SHOT_ALIEN_LNG, Double.doubleToLongBits(0)));
//		Toast.makeText(this, "SHOT_ALIEN: "+alienNextLat+", "+alienNextLng,
//				Toast.LENGTH_SHORT).show();
		double ZERO = 0.0;
		if(alienNextLat != ZERO || alienNextLng != ZERO){
			alienNextLoc = new LatLng(alienNextLat, alienNextLng);
		} 	
		
		return alienNextLoc;
	}
	
	private LatLng getAlienHintLoc() {
			double alienHintLat = Double.longBitsToDouble(mPrefs.getLong(AppConstants.HINT_ALIEN_LAT, Double.doubleToLongBits(0)));
			double alienHintLng = Double.longBitsToDouble(mPrefs.getLong(AppConstants.HINT_ALIEN_LNG, Double.doubleToLongBits(0)));
			
			if(alienHintLat != ZERO || alienHintLng != ZERO){
				alienHintLoc = new LatLng(alienHintLat, alienHintLng);
			}
//			Toast.makeText(this, "HINT_ALIEN: "+alienNextLat+", "+alienNextLng,
//					Toast.LENGTH_SHORT).show();		
		
		return alienHintLoc;
	}

	private LatLng getNextDemoAlien(Location currLoc) {
		// place region randomly
		double lat = currLoc.getLatitude();
		double lon = currLoc.getLongitude() + (Math.random() / 1000);

		LatLng alienLoc = new LatLng(lat, lon);
		return alienLoc;
	}

	private void drawRegionHint(LatLng alien1) {
		regionHint = true;
		hint = false;
		int mFillColor = Color.HSVToColor(REGION_ALPHA, new float[] {
				REGION_HUE, 1, 1 });

		// place region randomly
		double lat = alien1.latitude;
		double lon = alien1.longitude + (Math.random() / 10000);

		LatLng regionLatLng = new LatLng(lat, lon);

		Circle circle = mMap.addCircle(new CircleOptions().center(regionLatLng)
				.radius(DEFAULT_REGION_HINT_RADIUS).strokeWidth(ZERO)
				.strokeColor(Color.BLACK).fillColor(mFillColor));
	}
	
	private void drawScanner(LatLng currentLoc) {
		if(scannerCircle==null){
			int mFillColor = Color.HSVToColor(REGION_ALPHA, new float[] {
					REGION_HUE, 1, 1 });
			int color = Color.argb(30, 255, 0, 0);
			scannerCircle = mMap.addCircle(new CircleOptions().center(currentLoc)
					.radius(DEFAULT_REGION_HINT_RADIUS).strokeWidth(1)
					.strokeColor(Color.MAGENTA).fillColor(0));

			
			animateScannerCircle(scannerCircle, scannerCircle.getRadius());
//			animateCircle();
		} else {
			scannerCircle.setCenter(currentLoc);
		}
	}

	
	 public void animateScannerCircle(final Circle circle, final double orgRadius) {
		    final Handler handler = new Handler();
		    final long start = SystemClock.uptimeMillis();
		    final long duration = 1000;
		    final Interpolator interpolator = new LinearInterpolator();
		    handler.post(new Runnable() {
		        @Override
		        public void run() {
		            long elapsed = SystemClock.uptimeMillis() - start;
		            float t = interpolator.getInterpolation((float) elapsed
		                    / duration);
		            double radius = t * 100 + (1 - t)
		                    * orgRadius;
		            if(scannerCircle!=null)
		            	circle.setRadius(radius);
		            if (t < 1.0) {
		                // Post again 16ms later.
		                handler.postDelayed(this, 16);
		            } else {
		            	if(scannerCircle!=null){
		            		scannerCircle.setRadius(DEFAULT_REGION_HINT_RADIUS);
		            		animateScannerCircle(scannerCircle, scannerCircle.getRadius());
		            	}
		            }
		        }
		    });
		}

	private void drawTrailHint(LatLng alien1, LatLng alien2) {
		bloodTrail = true;
		hint = false;
		float totalDist = getDistance(alien1, alien2);
		float dist = 0;
		LatLng nextMarkerLocation = alien1;
		int i = 0;
		while (dist < totalDist) {
			// Toast.makeText(this, "Dist: "+dist, Toast.LENGTH_SHORT).show();
			placeMarker(nextMarkerLocation.latitude,
					nextMarkerLocation.longitude, "Monster Blood",
					R.drawable.ic_alien_blood);
			nextMarkerLocation = getNextMarker(alien1, alien2, dist, i++);
			dist = dist + 20;
		}
	}

	private LatLng getNextMarker(LatLng currLocation, LatLng alien,
			double distance, int i) {
		double PI = Math.PI;
		double lat1 = currLocation.latitude * PI / 180;
		double lon1 = currLocation.longitude * PI / 180;
		double lat2 = alien.latitude * PI / 180;
		double lon2 = alien.longitude * PI / 180;
		double dLon = (alien.longitude - currLocation.longitude) * PI / 180;

		// Find the bearing from this point to the next.
		double brng = Math.atan2(
				Math.sin(dLon) * Math.cos(lat2),
				Math.cos(lat1) * Math.sin(lat2) - Math.sin(lat1)
						* Math.cos(lat2) * Math.cos(dLon));

		double angDist = distance / EARTH_RADIUS; // Earth's radius.

		// Calculate the destination point, given the source and bearing.
		lat2 = Math.asin(Math.sin(lat1) * Math.cos(angDist) + Math.cos(lat1)
				* Math.sin(angDist) * Math.cos(brng));

		lon2 = lon1
				+ Math.atan2(
						Math.sin(brng) * Math.sin(angDist) * Math.cos(lat1),
						Math.cos(angDist) - Math.sin(lat1) * Math.sin(lat2));

		if (Double.isNaN(lat2) || Double.isNaN(lon2))
			return null;

		lat2 = lat2 * 180 / PI;
		lon2 = lon2 * 180 / PI;

		if (i % 2 == 0) {
			lon2 = lon2 + (Math.random() / 10000);
		}

		return new LatLng(lat2, lon2);

	}

	protected void markSentinel() {
		Location currLoc = mMap.getMyLocation();

		if (currLoc != null) {
			if(sentinelMarker == null) {
				sentinelMarker = placeMarker(currLoc.getLatitude(), currLoc.getLongitude(),
					getString(R.string.sentinel), R.drawable.ic_sentinel_button);
				
			} else {
				sentinelMarker.setPosition(new LatLng(currLoc.getLatitude(), currLoc.getLongitude()));
			}
			
			mEditor.putLong(AppConstants.SENTINEL_LAT, Double.doubleToRawLongBits(sentinelMarker.getPosition().latitude));
			mEditor.putLong(AppConstants.SENTINEL_LNG, Double.doubleToRawLongBits(sentinelMarker.getPosition().longitude));
			
			mEditor.putBoolean(AppConstants.SENTINEL_PLACED , true);//this is for sentinel marker
			mEditor.putBoolean(AppConstants.SENTINEL_FOUND_ALIEN , false);//this is for sentinel alerts
			mEditor.commit();
			
			showImageToast(R.drawable.ic_sentinel, getString(R.string.sentinel_toast));	
		} else {
			Toast.makeText(this, "Wait for current location!!",
					Toast.LENGTH_SHORT).show();
		}

	}
	
	@Override
	public void onMapClick(LatLng touchLatLng) {
//		if (playerMarker!=null && playerMarker.isVisible()) {
//			LatLng latLng = playerMarker.getPosition();
//			playerMarker.remove();
//			playerMarker = placeMarker(latLng.latitude,
//					latLng.longitude, "Target", playerIcon);
//			
			// Move target to touchLatLng
//			Toast.makeText(this, "Bullet fired!!", Toast.LENGTH_SHORT).show();
			fireBullet();
			// match the alienMarker's and touch event's position and update the
			// score
			
//		}
	}

	private void fireBullet() {
		if(!mPracticeHunt && collectedPowerCount < 1){
			showImageToast(R.drawable.ic_power, getString(R.string.no_power));
			addMorePower(mCurrentLocation);
		} else {
			LatLng currLatLng = playerMarker.getPosition();
			if(bulletMarker!=null)
				bulletMarker.remove();
			
			//move it in straight line
			if(targetMarker!=null){
				//create new marker for bullet
				bulletMarker = placeMarker(currLatLng.latitude,
						currLatLng.longitude, getString(R.string.fire_marker_text), R.drawable.fire);
				animateFireMarker(bulletMarker, targetMarker.getPosition(), true);
				if(!mPracticeHunt){
					collectedPowerCount--;
					//update power on UI and save it
					updatePoweronUI();	
				}
			}
		}
	}

	public void animateFireMarker(final Marker marker, final LatLng toPosition,
		        final boolean hideMarker) {
		    final Handler handler = new Handler();
		    final long start = SystemClock.uptimeMillis();
		    Projection proj = mMap.getProjection();
		    Point startPoint = proj.toScreenLocation(marker.getPosition());
		    final LatLng startLatLng = proj.fromScreenLocation(startPoint);
		    final long duration = 2500;
		    final Interpolator interpolator = new LinearInterpolator();
		    handler.post(new Runnable() {
		        @Override
		        public void run() {
		            long elapsed = SystemClock.uptimeMillis() - start;
		            float t = interpolator.getInterpolation((float) elapsed
		                    / duration);
		            double lng = t * toPosition.longitude + (1 - t)
		                    * startLatLng.longitude;
		            double lat = t * toPosition.latitude + (1 - t)
		                    * startLatLng.latitude;
		            marker.setPosition(new LatLng(lat, lng));
		            if(alienMarker!=null && targetMarker!=null)
		            	hitAlien(new LatLng(lat, lng));
		            if (t < 1.0) {
		                // Post again 16ms later.
		                handler.postDelayed(this, 16);
		            } else {
		                if (hideMarker) {
		                    marker.setVisible(false);
		                } else {
		                    marker.setVisible(true);
		                }
		            }
		        }
		    });
		}
	
	private void hitAlien(LatLng bulletLatLng) {
		// match the alienMarker's and fire event's position and update
		// the score
		float totalDist = getDistance(alienMarker.getPosition(),
				bulletLatLng);
		if (totalDist < FIRE_RANGE) {
			if(mPracticeHunt){
				showImageToast(R.drawable.ic_alien_hurt, getString(R.string.monster_hit));
				removePracticeAlien(bulletLatLng);
			} else {

				String toastText = getString(R.string.monster_hit);
				int toastImage = R.drawable.ic_alien_hurt;
				
				if(alienRunCounter==2){
					updateKilledAliens(alienRunCounter);
					toastText = getString(R.string.monster_destroyed);
					toastImage = R.drawable.ic_alien_killed;
//					Toast.makeText(this, "Monster Killed!!", Toast.LENGTH_SHORT).show();
				} else {
					updateScore(SCORE_100);		

					int shotsRemaining = 2 - alienRunCounter;
					toastText = toastText + " Track it " +shotsRemaining+" more ";
					
					if(shotsRemaining == 1)
						toastText = toastText + "time to completely destroy!!";
					else
						toastText = toastText + "times to completely destroy!!";
//					Toast.makeText(this, "Monster is Hurt and escaped!!", Toast.LENGTH_SHORT).show();
				}
				
				
				showImageToast(toastImage, toastText);
				
				removeGameMarkers(bulletLatLng);
//				Toast.makeText(this,
//						"Mark it and claim hints to track the alienMarker!!",
//						Toast.LENGTH_SHORT).show();
			}
		}
	}


	 private void removePracticeAlien(LatLng bulletLatLng) {
		 alienMarker.remove();
			alienMarker = null;
			targetMarker.remove();
			targetMarker=null;
			scannerCircle.remove();
			scannerCircle=null;
			mMap.getUiSettings().setZoomGesturesEnabled(true);
			mMap.getUiSettings().setAllGesturesEnabled(true);
			mMap.clear();
			powerList.clear();
			Marker bloodMarker = placeMarker(bulletLatLng.latitude,
					bulletLatLng.longitude, "Monster Blood", R.drawable.ic_alien_blood);
			Thread thread = new Thread(){
	            @Override
	           public void run() {
	                try {
	                   Thread.sleep(3500); // As I am using LENGTH_LONG in Toast
	                   ReconSenseActivity.this.finish();
	                   ReconSenseActivity.this.onBackPressed();
	               } catch (Exception e) {
	                   e.printStackTrace();
	               }
	           }  
	        };
	        thread.start();
		
	}

	private void updatePoweronUI() {
		score = score + 1;
		updateUI();
		mEditor.putInt(AppConstants.TOTAL_SCORE, score);
		mEditor.putInt(AppConstants.COLLECTED_POWER , collectedPowerCount);
		mEditor.commit();
	}
	
	private void updateKilledAliens(int shotCount) {
		score = (score + SCORE_100 
						+ (SCORE_100 * (shotCount +1)));//double the points for tracking shots		
		bustedAliens++;
		updateUI();
		// Save the current setting for updates
		mEditor.putInt(AppConstants.TOTAL_SCORE, score);
		mEditor.putInt(AppConstants.BUSTED_ALIENS, bustedAliens);
		mEditor.commit();
	}

	private void updateScore(int addScore) {
		score = score + addScore;
		updateUI();
		// Save the current setting for updates
		mEditor.putInt(AppConstants.TOTAL_SCORE, score);
		mEditor.commit();
	}
	
	private void updateUI() {
		mScoreTextView.setText("Score: "+score);
		mScoreTextView.append("    Slayed Monsters: "+bustedAliens);
		mScoreTextView.append("    Fire Power: "+collectedPowerCount);
	}
	
	private void saveGameState() {
		// Save the current setting for updates
		if(alienBloodMarker!=null){
			mEditor.putString(BLOOD_MARKER_LAT, alienBloodMarker.getPosition().latitude+"");
			mEditor.putString(BLOOD_MARKER_LNG, alienBloodMarker.getPosition().longitude+"");
		}
		mEditor.putBoolean(HINT, hint);
		mEditor.putInt(ALIEN_RUN_COUNTER, alienRunCounter);
		mEditor.putInt(AppConstants.TOTAL_SCORE, score);
		mEditor.putInt(AppConstants.BUSTED_ALIENS, bustedAliens);
		mEditor.commit();
	}

	private void removeGameMarkers(LatLng touchLatLng) {
		LatLng playerPosition = playerMarker.getPosition();
		mEditor.putBoolean(AppConstants.ALIEN_FOUND , false);

		mEditor.commit();		
		
		// remove markers

		hint = true;
		bloodTrail = false;
		regionHint = false;
		
		alienMarker.remove();
		alienMarker = null;
		targetMarker.remove();
		targetMarker=null;
		scannerCircle.remove();
		scannerCircle=null;
		mMap.getUiSettings().setZoomGesturesEnabled(true);
		mMap.getUiSettings().setAllGesturesEnabled(true);
		mMap.clear();
		powerList.clear();
		alienBloodMarker = placeMarker(touchLatLng.latitude,
				touchLatLng.longitude, "Monster Blood", R.drawable.ic_alien_blood);
		
        alienShot(playerPosition);
		if (alienRunCounter == 2)// resetting for 
									// next alien hits
			alienRunCounter = 0;
		else
			alienRunCounter++;
		
		awardBonusPower();
//		mSensorManager.unregisterListener(mSensorListener, mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION));

		showWinScreen(playerPosition);
		
	}

	private void awardBonusPower() {
		collectedPowerCount = collectedPowerCount + 10;
		updatePoweronUI();
		showImageToast(R.drawable.ic_power, "Bonus power awarded!! You power increased to: "+collectedPowerCount);
	}

	// ////////////////////////////////////////New API
	// changes////////////////////////////////////////

	private void showWinScreen(final LatLng playerPosition) {
		saveGameState();
		mEditor.putBoolean(AppConstants.SHOW_WIN_SCORE , true);
		mEditor.commit();
		Thread thread = new Thread(){
            @Override
           public void run() {
                try {
                   Thread.sleep(3500); // As I am using LENGTH_LONG in Toast
                   ReconSenseActivity.this.finish();
                   ReconSenseActivity.this.onBackPressed();
               } catch (Exception e) {
                   e.printStackTrace();
               }
           }  
        };
        thread.start();
	}

	// Define a DialogFragment that displays the error dialog
	public static class ErrorDialogFragment extends DialogFragment {
		// Global field to contain the error dialog
		private Dialog mDialog;

		// Default constructor. Sets the dialog field to null
		public ErrorDialogFragment() {
			super();
			mDialog = null;
		}

		// Set the dialog to display
		public void setDialog(Dialog dialog) {
			mDialog = dialog;
		}

		// Return a Dialog to the DialogFragment.
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			return mDialog;
		}
	}

	/*
	 * Handle results returned to the FragmentActivity by Google Play services
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// Decide what to do based on the original request code
		switch (requestCode) {

		case CONNECTION_FAILURE_RESOLUTION_REQUEST:
			/*
			 * If the result code is Activity.RESULT_OK, try to connect again
			 */
			switch (resultCode) {
			case Activity.RESULT_OK:
				/*
				 * Try the request again
				 */

				break;
			}
		}
	}

	private boolean servicesConnected() {
		// Check that Google Play services is available
		int resultCode = GooglePlayServicesUtil
				.isGooglePlayServicesAvailable(this);
		// If Google Play services is available
		if (ConnectionResult.SUCCESS == resultCode) {
			// In debug mode, log the status
			Log.d("Location Updates", "Google Play services is available.");
			// Continue
			return true;
			// Google Play services was not available for some reason
		} else {
			// Get the error code
			// int errorCode = connectionResult.getErrorCode();
			// Get the error dialog from Google Play services
			Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(
					resultCode, this, CONNECTION_FAILURE_RESOLUTION_REQUEST);

			// If Google Play services can provide an error dialog
			if (errorDialog != null) {
				// Create a new DialogFragment for the error dialog
				ErrorDialogFragment errorFragment = new ErrorDialogFragment();
				// Set the dialog in the DialogFragment
				errorFragment.setDialog(errorDialog);
				// Show the error dialog in the DialogFragment
				errorFragment.show(getSupportFragmentManager(),
						"Location Updates");
			}
		}
		return false;
	}

	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {
		/*
		 * Google Play services can resolve some errors it detects. If the error
		 * has a resolution, try sending an Intent to start a Google Play
		 * services activity that can resolve error.
		 */
		if (connectionResult.hasResolution()) {
			try {
				// Start an Activity that tries to resolve the error
				connectionResult.startResolutionForResult(this,
						CONNECTION_FAILURE_RESOLUTION_REQUEST);
				/*
				 * Thrown if Google Play services canceled the original
				 * PendingIntent
				 */
			} catch (IntentSender.SendIntentException e) {
				// Log the error
				e.printStackTrace();
			}
		} else {
			/*
			 * If no resolution is available, display a dialog to the user with
			 * the error.
			 */
			showErrorDialog(connectionResult.getErrorCode());
		}

	}

	private void showErrorDialog(int errorCode) {
		// Get the error dialog from Google Play services
		Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(errorCode,
				this, CONNECTION_FAILURE_RESOLUTION_REQUEST);

		// If Google Play services can provide an error dialog
		if (errorDialog != null) {
			// Create a new DialogFragment for the error dialog
			ErrorDialogFragment errorFragment = new ErrorDialogFragment();
			// Set the dialog in the DialogFragment
			errorFragment.setDialog(errorDialog);
			// Show the error dialog in the DialogFragment
			errorFragment.show(getSupportFragmentManager(), "Location Updates");
		}

	}

	@Override
	public void onConnected(Bundle arg0) {
//		Toast.makeText(this, "Connected", Toast.LENGTH_SHORT).show();
		if(mUpdatesRequested)
			mLocationClient.requestLocationUpdates(mLocationRequest, this);
	}

	@Override
	public void onDisconnected() {
		Toast.makeText(this, "Disconnected. Please re-connect.",
				Toast.LENGTH_SHORT).show();
	}
	
	/**
	 * Initialize the sensor manager.
	 */
	private void setupSensorManager() {
//		Toast.makeText(this, "Orientation sensing started!!", Toast.LENGTH_SHORT).show();
	    mSensorManager = (SensorManager) getApplicationContext()
	            .getSystemService(Context.SENSOR_SERVICE);
	    mSensorManager.registerListener(mSensorListener,
	            mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
	            SensorManager.SENSOR_DELAY_GAME);

//	    Log.d("ReconSense", "SensorManager setup");
	}

	/**
	 * The sensor event listener.
	 */
	SensorEventListener mSensorListener = new SensorEventListener() {

	    @Override
	    public void onSensorChanged(SensorEvent event) {
	       	
	        Display display = getWindowManager().getDefaultDisplay(); 
            int ymax = display.getWidth() - 50;// + 300;//100 is the size of the target
            int xmax = display.getHeight() - 150;// - 300;
            int x = (int) Math.pow(event.values[1], 2); 
            int y = (int) Math.pow(event.values[2], 2);
           
            if (x > xmax) {
                x = xmax;
            } else if (x < 100) {
                x = 100;
            }
            if (y > ymax) { 
                y = ymax;
            } else if (y < 50) {
                y = 50;
            }
//            Log.d("ReconSense", "Phone Moved "+x +","+y);
	        moveTarget(x, y);
	    }

	    @Override
	    public void onAccuracyChanged(Sensor sensor, int accuracy) {
	    }
	};

	protected void moveTarget(int x, int y) {
//		Toast.makeText(this, "x,y: "+x+","+y, Toast.LENGTH_SHORT).show();
		if(targetMarker!=null){
			LatLng latLng = mMap.getProjection().fromScreenLocation(new Point(y, x));
			targetMarker.setPosition(latLng);
		}
	}

	@Override
	public boolean onMarkerClick(Marker marker) {
		if (targetMarker != null && (marker.equals(targetMarker) || marker.equals(playerMarker) || marker.equals(alienMarker))) 
        {
			fireBullet();//fire bullet even when user touch the markers
        }
		return false;
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
//            	 JSONObject json = new JSONObject();
//                 json.put("email", "tmanoop@gmail.com");
//                 json.put("wifi", "wifimac");
            	 JData jData = new JData();
            	 jData.setAlienRunCounter(mPrefs.getInt(ALIEN_RUN_COUNTER, 0));
            	 jData.setEmail(mPrefs.getString(AppConstants.ACCOUNT_NAME, "noAccountName"));
            	 jData.setMeid(AppUtils.getMeid(getApplicationContext()));
         		 jData.setCurrentLat(mCurrentLocation.getLatitude());
           	     jData.setCurrentLng(mCurrentLocation.getLongitude());
            	 
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
                 
            } else {
//Content
//            	Toast.makeText(ReconSenseActivity.this, Content,
//                        Toast.LENGTH_LONG).show();
            	//Parse Response into our object
            	 double prevShotAlienLat = Double.longBitsToDouble(mPrefs.getLong(
         				AppConstants.SHOT_ALIEN_LAT, Double.doubleToLongBits(0)));
         		double prevShotAlienLng = Double.longBitsToDouble(mPrefs.getLong(
         				AppConstants.SHOT_ALIEN_LNG, Double.doubleToLongBits(0)));
         		
         		 double prevHintAlienLat = Double.longBitsToDouble(mPrefs.getLong(
          				AppConstants.HINT_ALIEN_LAT, Double.doubleToLongBits(0)));
          		double prevHintAlienLng = Double.longBitsToDouble(mPrefs.getLong(
          				AppConstants.HINT_ALIEN_LNG, Double.doubleToLongBits(0)));
            	
                Type collectionType = new TypeToken<JData>() {
                }.getType();
                JData jData = new Gson().fromJson(Content, collectionType);	 
                
                if(jData!=null && jData.getRequestType() != null && jData.getCurrentLat()!=0 && jData.getCurrentLng() != 0){
                	String requestType = jData.getRequestType();
					if ("alienShot".equalsIgnoreCase(requestType)) {
						float distToShotAlien = getDistance(
								new LatLng(prevShotAlienLat, prevShotAlienLng),
								new LatLng(jData.getCurrentLat(), jData
										.getCurrentLng()));

						if (distToShotAlien > 5) {
							mEditor.putLong(AppConstants.SHOT_ALIEN_LAT, Double
									.doubleToRawLongBits(jData.getCurrentLat()));
							mEditor.putLong(AppConstants.SHOT_ALIEN_LNG, Double
									.doubleToRawLongBits(jData.getCurrentLng()));
							mEditor.putInt(AppConstants.SHOT_ALIEN_FLOOR, jData.getFloorNum());
							mEditor.putBoolean(AppConstants.SHOT_ALIEN, true);
						}

					} else if ("alienHints".equalsIgnoreCase(requestType)) {
						float distToHintAlien = getDistance(
								new LatLng(prevHintAlienLat, prevHintAlienLng),
								new LatLng(jData.getCurrentLat(), jData
										.getCurrentLng()));

						if (distToHintAlien > 5) {//5 meters
							mEditor.putLong(AppConstants.HINT_ALIEN_LAT, Double
									.doubleToRawLongBits(jData.getCurrentLat()));
							mEditor.putLong(AppConstants.HINT_ALIEN_LNG, Double
									.doubleToRawLongBits(jData.getCurrentLng()));
							mEditor.putInt(AppConstants.HINT_ALIEN_FLOOR, jData.getFloorNum());
							mEditor.putBoolean(AppConstants.HINT_ALIEN, true);
						}
					}
                    
//                    Toast.makeText(getApplicationContext(), "requestType: "+requestType+", ALIEN Loc: "+jData.getCurrentLat()+", "+jData.getCurrentLng(),
//        					Toast.LENGTH_SHORT).show();

        			mEditor.commit();
                }
                
                   	            	
             }
        }
         
    }
}
