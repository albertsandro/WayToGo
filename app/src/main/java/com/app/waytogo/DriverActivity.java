package com.app.waytogo;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.app.waytogo.model.DriverInfo;
import com.app.waytogo.model.TripDetails;
import com.app.waytogo.support.UrlGenerator;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class DriverActivity extends AppCompatActivity implements ConnectionCallbacks, OnConnectionFailedListener, LocationListener, OnMapClickListener {

	private Toolbar mToolbar;
	private ConnectivityManager cm;
	private GoogleApiClient mGoogleApiClient;
	private LocationRequest mLocationRequest;
	private LocationManager mLocationManager;
	private GoogleMap myMap;
	private Location location;
	private LatLng myLatLng;
	private static double mLatitude, mLongitude;
	private SharedPreferences mySharedPreferences;
	private Editor editor, editorDriver;
	private String USER = "user_pref";
	private int isDriverMode = 1;
	private Timer timer, timerCancel;
	private String strFeedId, strStatusID, strDriverFeedId, strDriverUserID, strDriverDistance, strDriverCost, 
			strFLat, strFLong, strTLat, strTLong, strFeedTitle, strFeedDesc, strUserView, strUserID, strPayment, strCourierWeight,
			strSpecial, strFoodQuantity, strFoodPrices, strNameOfRest, strAddressOfRest, strUserPhone;
	private RelativeLayout rl_user_info, rl_bottom, rl_courier_user_info, rl_courier_bottom, rl_food_user_info, rl_food_bottom;
	private CircularImageView iv_user_image, iv_courier_user_image;
	private ImageView iv_courier_image;
	private TextView tv_user_name, tv_user_address, tv_user_miles, tv_counter, tv_tap_accept, tv_price;
	private TextView tv_courier_user_name, tv_courier_miles, tv_courier_cost, tv_courier_weight;
	private TextView tv_food_user_name, tv_food_user_address, tv_food_user_miles, tv_food_counter, tv_food_tap_accept, tv_food_price;
	private CountDownTimer cdt;
	private Geocoder geocoder;
	private List<Address> addresses;
	private int mTripStatus = 0, mTripCancelStatus = 0;
	private DecimalFormat df;
	private double dist;
	private MediaPlayer mPlayer;	
	private boolean isRunning = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_driver);
		
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		
		cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		
		mToolbar = (Toolbar) findViewById(R.id.toolbar);

		setSupportActionBar(mToolbar);
		getSupportActionBar().setDisplayShowHomeEnabled(true);
		mySharedPreferences = getSharedPreferences(USER, MODE_PRIVATE);
		editor = mySharedPreferences.edit();
		
		//To initialize music on new request
		mPlayer = new MediaPlayer();
		mPlayer = MediaPlayer.create(this, R.raw.newrequest);
        mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mPlayer.setLooping(false);
				
		mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

		if (!mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
			buildAlertMessageNoGps();
		}

		mGoogleApiClient = new GoogleApiClient.Builder(DriverActivity.this)
				.addApi(LocationServices.API).addConnectionCallbacks(this)
				.addOnConnectionFailedListener(this).build();

		myMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map_driver)).getMap();
		rl_user_info = (RelativeLayout) findViewById(R.id.rl_user_info);
		rl_bottom = (RelativeLayout) findViewById(R.id.rl_bottom);
		
		iv_user_image = (CircularImageView) findViewById(R.id.iv_user_image);
		tv_user_name = (TextView) findViewById(R.id.tv_user_name);
		tv_user_address = (TextView) findViewById(R.id.tv_user_address);
		tv_user_miles = (TextView) findViewById(R.id.tv_user_miles);
		tv_counter = (TextView) findViewById(R.id.tv_counter);
		tv_tap_accept = (TextView) findViewById(R.id.tv_tap_accept);
		tv_price = (TextView) findViewById(R.id.tv_price);
		
		//Courier
		rl_courier_user_info = (RelativeLayout) findViewById(R.id.rl_courier_user_info);
		rl_courier_bottom = (RelativeLayout) findViewById(R.id.rl_courier_bottom);
		iv_courier_user_image = (CircularImageView) findViewById(R.id.iv_courier_user_image);
		iv_courier_image = (ImageView) findViewById(R.id.iv_courier_image);
		tv_courier_user_name = (TextView) findViewById(R.id.tv_courier_user_name);
		tv_courier_miles = (TextView) findViewById(R.id.tv_courier_miles);
		tv_courier_cost = (TextView) findViewById(R.id.tv_courier_cost);
		tv_courier_weight = (TextView) findViewById(R.id.tv_courier_weight);
		
		//food
		rl_food_user_info = (RelativeLayout) findViewById(R.id.rl_food_user_info);
		rl_food_bottom = (RelativeLayout) findViewById(R.id.rl_food_bottom);
		tv_food_user_name = (TextView) findViewById(R.id.tv_food_user_name);
		tv_food_user_address = (TextView) findViewById(R.id.tv_food_user_address);
		tv_food_user_miles = (TextView) findViewById(R.id.tv_food_user_miles);
		tv_food_counter = (TextView) findViewById(R.id.tv_food_counter);
		tv_food_price = (TextView) findViewById(R.id.tv_food_price);
		
		myMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
		myMap.getUiSettings().setZoomControlsEnabled(false);
		myMap.setMyLocationEnabled(true);
		myMap.setOnMapClickListener(this);
		
		location = myMap.getMyLocation();

	    if (location != null) {
	    	myLatLng = new LatLng(location.getLatitude(), location.getLongitude());
	    	//myMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myLatLng, 15));
	    	MarkerOptions options = new MarkerOptions();
		 	    options.position(myLatLng);
		        options.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_driver_marker));
		        options.title("Current Location");
		   	    myMap.addMarker(options);
		   	    
		   	 mLatitude = myLatLng.latitude;
		   	 mLongitude = myLatLng.longitude;
		   	 
		   	 Toast.makeText(DriverActivity.this, "lat : "+mLatitude+" lng "+mLongitude, 10).show();
	    }
	    
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.driver_menu, menu);
		
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		supportInvalidateOptionsMenu();
		int id = item.getItemId();

		if (id == R.id.action_steeringwheel_active) {
			isDriverMode = 0;
			if (cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isAvailable() && cm.getActiveNetworkInfo().isConnected()) {
				switchToUserMode();
			} else {
				networkConnectivity();
			}
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		if(mTripStatus == 0) {
			menu.findItem(R.id.action_steeringwheel_active).setVisible(true);
			menu.findItem(R.id.action_overflow).setVisible(false);
		}
		if(mTripStatus == 1) {
			menu.findItem(R.id.action_steeringwheel_active).setVisible(false);
			menu.findItem(R.id.action_overflow).setVisible(true);
		}
		return super.onPrepareOptionsMenu(menu);
	}

	private void buildAlertMessageNoGps() {
		// TODO Auto-generated method stub
		new AlertDialog.Builder(DriverActivity.this)
		.setMessage(getResources().getString(R.string.strgpsmessage))
		.setCancelable(false)
		.setPositiveButton(getResources().getString(R.string.stryes),
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog,
							int which) {
						startActivity(new Intent(
								android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
					}
				})
		.setNegativeButton(getResources().getString(R.string.strno),
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog,
							int which) {
						dialog.dismiss();
						DriverActivity.this.finish();
					}
				}).show();
	}


	@Override
	public void onConnectionFailed(ConnectionResult arg0) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void onConnected(Bundle arg0) {
		// TODO Auto-generated method stub
		mLocationRequest = LocationRequest.create();
		mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
		mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
		mLocationRequest.setInterval(1000); // Update location every second

		LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
		
		Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
		if (mLastLocation != null) {
		
			mLatitude = mLastLocation.getLatitude();
			mLongitude = mLastLocation.getLongitude();
			
			animateCameraTo(mLastLocation.getLatitude(), mLastLocation.getLongitude());
		}
		
		if( (mLatitude != 0.0) && (mLongitude != 0.0) )
		{
			timer = new Timer();
		    timer.schedule(new TimerTask() {

				@Override
				public void run() {
					// TODO Auto-generated method stub
					DriverActivity.this.runOnUiThread(new Runnable() {
						
						@Override
						public void run() {
							// TODO Auto-generated method stub
							if (cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isAvailable() && cm.getActiveNetworkInfo().isConnected()) {
								if(isRunning) {
									cdt.cancel();
									rl_user_info.setVisibility(View.GONE);
									rl_courier_user_info.setVisibility(View.GONE);
									rl_food_user_info.setVisibility(View.GONE);
								}
								updateDriverCurrentLocation(mLatitude, mLongitude);
								getDriverFeedList();
							} else {
								networkConnectivity();
							}
						}
					});
				}
		    	
		    },0,6000);//},0,30000);
		}
	}

	@Override
	public void onConnectionSuspended(int arg0) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void onLocationChanged(Location location) {
		// TODO Auto-generated method stub
		double lat = location.getLatitude();
		double lng = location.getLongitude();
	}


	@Override
	public void onMapClick(LatLng arg0) {
		// TODO Auto-generated method stub
		
	}
	
	private void animateCameraTo(double latitude, double longitude) {
		// TODO Auto-generated method stub
		myMap.clear();
		LatLng markerLatLng = new LatLng(latitude, longitude);
		
		myMap.animateCamera(CameraUpdateFactory.newLatLngZoom(markerLatLng, 15));
    	MarkerOptions options = new MarkerOptions();
	 	    options.position(markerLatLng);
	        options.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_driver_marker));
	        options.title("Current Location");
	   	    myMap.addMarker(options);

		mLatitude = latitude;
	   	mLongitude = longitude;
	}
	
	@Override
	public void onStart() {
		super.onStart();
		// Connect the client.
		mGoogleApiClient.connect();
		
	}

	@Override
	public void onStop() {
		// Disconnecting the client invalidates it.
		mGoogleApiClient.disconnect();
		super.onStop();
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		timer.cancel();
		super.onDestroy();
	}
	
	public void switchToUserMode()
	{
		// TODO Auto-generated method stub
		AsyncHttpClient client = new AsyncHttpClient();
		// Http Request Params Object
		RequestParams params = new RequestParams();
		
		// Show ProgressBar
		final ProgressDialog pdialog = new ProgressDialog(DriverActivity.this);
		pdialog.setMessage("Switching to User mode...");
		pdialog.setCancelable(false);
		pdialog.show();
		// Make Http call to getusers.php
				
		String url = null;
		url = UrlGenerator.updateDriverStatus();
		
		params.add("iUserID", mySharedPreferences.getString("strUserId", ""));
		params.add("status", "offline");
				
		client.post(url, params, new AsyncHttpResponseHandler() {

			@Override
			public void onSuccess(String response) {
				// Hide ProgressBar
				pdialog.hide();
				
				JSONObject jObject;
				try {
					jObject = new JSONObject(response);
					
					String strStatus = jObject.optString("status");
					String strMessage = jObject.optString("message");
					
					if(strStatus.equals("0") == true) 
					{
						//editor.putString("strEmail", strMessage);
						Intent i_driver = new Intent(DriverActivity.this, MainActivity.class);
						startActivity(i_driver);
						DriverActivity.this.finish();
					}
					else
					{
						Toast.makeText(DriverActivity.this, strMessage, Toast.LENGTH_LONG).show();
					}
							
				} catch (Exception e) {e.printStackTrace();}
			}

			// When error occured
			@Override
			public void onFailure(int statusCode, Throwable error, String content) {
				// TODO Auto-generated method stub
				// Hide ProgressBar
				pdialog.hide();
				if (statusCode == 404) {
					//Toast.makeText(DriverActivity.this, "Requested resource not found", Toast.LENGTH_LONG).show();
				} else if (statusCode == 500) {
					//Toast.makeText(DriverActivity.this, "Something went wrong at server end", Toast.LENGTH_LONG).show();
				} else {
					networkConnectivity();
				}
			}
		});
	}
	
	//To update driver current location
	public void refreshTableViewData()
	{
		// TODO Auto-generated method stub
		AsyncHttpClient client = new AsyncHttpClient();
		// Http Request Params Object
		RequestParams params = new RequestParams();
		
		String url = null;
		try {
			url = UrlGenerator.userFeedList();
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		params.add("iUserID", mySharedPreferences.getString("strUserId", ""));
		
		client.post(url, params, new AsyncHttpResponseHandler() {

			@Override
			public void onSuccess(String response) {
				// Hide ProgressBar
				JSONObject jObject;
				try {
					jObject = new JSONObject(response);
					
					String strStatus = jObject.optString("status");
					String strMessage = jObject.optString("message");
					
					if(strStatus.equals("0") == true) 
					{
						try {
							GsonBuilder gsonBuilder = new GsonBuilder();
							Gson gson = gsonBuilder.create();
			
							TripDetails trip = gson.fromJson(response, TripDetails.class);
							strUserID = trip.data.get(0).iUserID;
							strFeedId = trip.data.get(0).iFeedID;
							strFLat = trip.data.get(0).fLat;
							strFLong = trip.data.get(0).fLong;
							strTLat = trip.data.get(0).toLat;
							strTLong = trip.data.get(0).toLong;
							strFeedTitle = trip.data.get(0).vFeedTitle;
							strFeedDesc = trip.data.get(0).tFeedDescription;
							strUserView = trip.data.get(0).vUserView;
							strCourierWeight = trip.data.get(0).courierWeight;
							strSpecial = trip.data.get(0).specialInst;
							strFoodQuantity = trip.data.get(0).foodQuantity;
							strFoodPrices = trip.data.get(0).foodPrices;
							strNameOfRest = trip.data.get(0).nameofRest;
							strAddressOfRest = trip.data.get(0).addressofRest;
							
							if(trip.data.size() > 0) {
								editor.putString("strFeedUserID", strUserID);
								editor.putString("strFeedId", strFeedId);
								editor.putBoolean("feedList", true);
								editor.commit();
								pushtodeserve(true, strUserID, strUserView, strFeedId);
							} else {
								editor.putBoolean("feedList", false);
								editor.commit();
							}
							
						    timerCancel = new Timer();
						    timerCancel.schedule(new TimerTask() {

								@Override
								public void run() {
									
									DriverActivity.this.runOnUiThread(new Runnable() {
										
										@Override
										public void run() {
											// TODO Auto-generated method stub
											String mUserID = mySharedPreferences.getString("strUserId", "");
											String mFeedID = mySharedPreferences.getString("strFeedId", "");
											
											chkTripCancelled(mFeedID, mUserID);
										}
									});
									
								}
						    	
						    },0,3000);
														
						} catch(NullPointerException e) { e.printStackTrace(); }
					}
												
				} catch (Exception e) {e.printStackTrace();}
			}

			// When error occured
			@Override
			public void onFailure(int statusCode, Throwable error, String content) {
				// TODO Auto-generated method stub
				if (statusCode == 404) {
					//Toast.makeText(DriverActivity.this, "Requested resource not found", Toast.LENGTH_LONG).show();
				} else if (statusCode == 500) {
					//Toast.makeText(DriverActivity.this, "Something went wrong at server end", Toast.LENGTH_LONG).show();
				} else {
					networkConnectivity();
				}
			}
		});
	}
	
	
	protected void pushtodeserve(boolean b, String strUserID, String strUserView, String strFeedID) {
		// TODO Auto-generated method stub
		if(strUserView.equals("") == false) {
			if(strUserID.endsWith(mySharedPreferences.getString("strUserId", "")))
			{
				if(strUserView.equals("friends") == true)
				{
					//friends
				}
				
				if(strUserView.equals("pickedup") == true)
				{
					//pickedup
				}
				
				if(strUserView.equals("public") == true)
				{
					deleteRide(strFeedID);
				}
			}
		}
	}

	//To update driver current location
	public void updateDriverCurrentLocation(double mLatitude, double mLongitude)
	{
		// TODO Auto-generated method stub
		AsyncHttpClient client = new AsyncHttpClient();
		// Http Request Params Object
		RequestParams params = new RequestParams();
		
		String url = null;
		url = UrlGenerator.updateDriverCurrentLocation();
		
		params.add("iUserID", mySharedPreferences.getString("strUserId", ""));
		params.add("fLat", String.valueOf(mLatitude));
		params.add("fLong", String.valueOf(mLongitude)); 
		client.post(url, params, new AsyncHttpResponseHandler() {

			@Override
			public void onSuccess(String response) {
				// Hide ProgressBar
				JSONObject jObject;
				try {
					jObject = new JSONObject(response);
					
					String strStatus = jObject.optString("status");
					String strMessage = jObject.optString("message");
					
					if(strStatus.equals("0") == true) 
					{
						String feeduserid = mySharedPreferences.getString("strFeedUserID", "");
						if(feeduserid.equals("") == false)
						{
							if(strUserView.equals("friends") == true) {
								
							}
							
							else if(strUserView.equals("pickedup") == true) {
								
							}
							
							else { 
								if (cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isAvailable() && cm.getActiveNetworkInfo().isConnected()) {
									selectNearestDriver(mySharedPreferences.getString("strFeedId", ""));
								} else {
									networkConnectivity();
								}
							}
						}
					}
												
				} catch (Exception e) {e.printStackTrace();}
			}

			// When error occured
			@Override
			public void onFailure(int statusCode, Throwable error, String content) {
				// TODO Auto-generated method stub
				if (statusCode == 404) {
					//Toast.makeText(DriverActivity.this, "Requested resource not found", Toast.LENGTH_LONG).show();
				} else if (statusCode == 500) {
					//Toast.makeText(DriverActivity.this, "Something went wrong at server end", Toast.LENGTH_LONG).show();
				} else {
					networkConnectivity();
				}
			}
		});
	}
	
	//To update driver feed list
	public void getDriverFeedList()
	{
		// TODO Auto-generated method stub
		AsyncHttpClient client = new AsyncHttpClient();
		// Http Request Params Object
		RequestParams params = new RequestParams();
		
		String url = null;
		url = UrlGenerator.fetchDriverFeedList();
				
		params.add("iUserID", mySharedPreferences.getString("strUserId", ""));
				
		client.post(url, params, new AsyncHttpResponseHandler() {

			@Override
			public void onSuccess(String response) {
				// Hide ProgressBar
				JSONObject jObject;
				try {
					
					jObject = new JSONObject(response);
					
					String strStatus = jObject.optString("status");
					
					if(strStatus.equals("0") == true) 
					{
						try {
							GsonBuilder gsonBuilder = new GsonBuilder();
							Gson gson = gsonBuilder.create();
			
							TripDetails trip = gson.fromJson(response, TripDetails.class);
							strUserID = trip.data.get(0).iUserID;
							strFeedId = trip.data.get(0).iFeedID;
							strFLat = trip.data.get(0).fLat;
							strFLong = trip.data.get(0).fLong;
							strTLat = trip.data.get(0).toLat;
							strTLong = trip.data.get(0).toLong;
							strFeedTitle = trip.data.get(0).vFeedTitle;
							strFeedDesc = trip.data.get(0).tFeedDescription;
							strUserView = trip.data.get(0).vUserView;
							strPayment = trip.data.get(0).vPayment;
							strCourierWeight = trip.data.get(0).courierWeight;
							strSpecial = trip.data.get(0).specialInst;
							strFoodQuantity = trip.data.get(0).foodQuantity;
							strFoodPrices = trip.data.get(0).foodPrices;
							strNameOfRest = trip.data.get(0).nameofRest;
							strAddressOfRest = trip.data.get(0).addressofRest;
							
							editor.putString("strFeedId", strFeedId);
							editor.putString("strFeedUserID", trip.data.get(0).iUserID);
							editor.commit();
							
						} catch(NullPointerException e) { e.printStackTrace(); }
					}
												
				} catch (Exception e) {e.printStackTrace();}
			}
			
			// When error occured
			@Override
			public void onFailure(int statusCode, Throwable error, String content) {
				// TODO Auto-generated method stub
				if (statusCode == 404) {
					//Toast.makeText(DriverActivity.this, "Requested resource not found", Toast.LENGTH_LONG).show();
				} else if (statusCode == 500) {
					//Toast.makeText(DriverActivity.this, "Something went wrong at server end", Toast.LENGTH_LONG).show();
				} else {
					networkConnectivity();
				}
			}
		});
	}
	
	//To select nearest driver
	public void selectNearestDriver(String strFeedId)
	{
		// TODO Auto-generated method stub
		AsyncHttpClient client = new AsyncHttpClient();
		// Http Request Params Object
		RequestParams params = new RequestParams();
		
		String url = null;
		url = UrlGenerator.selectNearestDriver();
		//Log.e("url ##", "!! "+url);
		
		params.add("iFeedID", strFeedId);
				
		client.post(url, params, new AsyncHttpResponseHandler() {

			@Override
			public void onSuccess(String response) {
				// Hide ProgressBar
				JSONObject jObject;
				try {
					jObject = new JSONObject(response);
					
					String strStatus = jObject.optString("status");
					String strMessage = jObject.optString("message");
					
					if(strMessage.equals("No current route") == true) {
						//do nothing
					} else {
						String userid = mySharedPreferences.getString("strUserId", "");
						if(strMessage.equals(userid) == true)
						{
							//you are the closest driver
							if (cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isAvailable() && cm.getActiveNetworkInfo().isConnected()) {
								getLatestInfo();
							} else {
								networkConnectivity();
							}
						}
					}
												
				} catch (Exception e) {e.printStackTrace();}
			}

			// When error occured
			@Override
			public void onFailure(int statusCode, Throwable error, String content) {
				// TODO Auto-generated method stub
				if (statusCode == 404) {
					//Toast.makeText(DriverActivity.this, "Requested resource not found", Toast.LENGTH_LONG).show();
				} else if (statusCode == 500) {
					//Toast.makeText(DriverActivity.this, "Something went wrong at server end", Toast.LENGTH_LONG).show();
				} else {
					networkConnectivity();
				}
			}
		});
	}
	
	//To get LatestInfo
	public void getLatestInfo()
	{
		// TODO Auto-generated method stub
		AsyncHttpClient client = new AsyncHttpClient();
		// Http Request Params Object
		RequestParams params = new RequestParams();
				
		String url = null;
		url = UrlGenerator.getLatestInfo();
		
		params.add("iUserID", mySharedPreferences.getString("strUserId", ""));
				
		client.post(url, params, new AsyncHttpResponseHandler() {

			@Override
			public void onSuccess(String response) 
			{
				// Hide ProgressBar
				JSONObject jObject;
				try {
					jObject = new JSONObject(response);
					
					String strStatus = jObject.optString("status");
					String strMessage = jObject.optString("message");

					if(strMessage.equals("Nearest Driver Info") == true)
					{
						try {
							GsonBuilder gsonBuilder = new GsonBuilder();
							Gson gson = gsonBuilder.create();
			
							final DriverInfo driverInfo = gson.fromJson(response, DriverInfo.class);
							
							strStatusID = driverInfo.data.ID;
							strDriverFeedId = driverInfo.data.iFeedID;
							strDriverUserID = driverInfo.data.iUserID;
							strDriverCost = driverInfo.data.vCost;
														
							df = new DecimalFormat("#.#");
							dist = Double.valueOf(driverInfo.data.distance);
							
							if (dist > 15) {
                                //Driver seems to be far away 
								if (cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isAvailable() && cm.getActiveNetworkInfo().isConnected()) {
									acceptRide(strStatusID, strDriverFeedId, strDriverUserID, "rejected");
								} else { 
									networkConnectivity();
								}
							} else {
								//View for ride
								if(strUserView.equals("public") == true)
								{
									DriverActivity.this.runOnUiThread(new Runnable() {
										
										@Override
										public void run() {
											// TODO Auto-generated method stub
											//Rider
											rl_user_info.setVisibility(View.VISIBLE);
											rl_courier_user_info.setVisibility(View.GONE);
											rl_food_user_info.setVisibility(View.GONE);
											
											//to play music
											mPlayer.start();
																						
											tv_user_miles.setText(String.valueOf(df.format(dist))+" miles");
											tv_price.setText("$ "+strDriverCost);
											
											if (cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isAvailable() && cm.getActiveNetworkInfo().isConnected()) {
												getUsernamefromiUserID(strDriverFeedId);
											} else {
												networkConnectivity();
											}
											
											rl_bottom.setOnClickListener(new View.OnClickListener() {
												
												@Override
												public void onClick(View v) {
													//TODO Auto-generated method stub
													timer.cancel();
													mTripStatus = 1;
													rl_user_info.setVisibility(View.GONE);
													if (cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isAvailable() && cm.getActiveNetworkInfo().isConnected()) {
														acceptRide(strStatusID, strDriverFeedId, strDriverUserID, "accepted");
													} else {
														networkConnectivity();
													}
												}
											});
											
											cdt = new CountDownTimer(31000,1000) 
											{
												@Override
												public void onTick(long millisUntilFinished) {
													//TODO Auto-generated method stub
													isRunning = true;
													int seconds = (int) ((millisUntilFinished / 1000) % 60);
													tv_counter.setText(""+seconds);
												}
												
												@Override
												public void onFinish() {
													//TODO Auto-generated method stub
													isRunning = false;
													rl_user_info.setVisibility(View.GONE);
													timer.cancel();
													if(mTripStatus == 0) {
														editor.putString("strFeedId", "");
														editor.putString("strFeedUserID", "");
														editor.commit();
														if (cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isAvailable() && cm.getActiveNetworkInfo().isConnected()) {
															acceptRide(strStatusID, strDriverFeedId, strDriverUserID, "rejected");
														} else {
															networkConnectivity();
														}
													}
												}
												
											}.start();
										}
									});
									
								}
								
								//View for courier
								if(strUserView.equals("courier") == true)
								{
									DriverActivity.this.runOnUiThread(new Runnable() {
										
										@Override
										public void run() {
											// TODO Auto-generated method stub
											tv_courier_weight = (TextView) findViewById(R.id.tv_courier_weight);
											rl_user_info.setVisibility(View.GONE);
											rl_food_user_info.setVisibility(View.GONE);
											
											//to play music
											mPlayer.start();
											
									        tv_courier_miles.setText(String.valueOf(df.format(dist))+" miles away");
											tv_courier_cost.setText("$ "+strDriverCost);
											tv_courier_weight.setText("Item weights: "+strCourierWeight+ "lbs");
											
											if (cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isAvailable() && cm.getActiveNetworkInfo().isConnected()) {
												getUsernamefromiUserID(strDriverFeedId);
											} else {
												networkConnectivity();
											}
											
											rl_courier_bottom.setOnClickListener(new View.OnClickListener() {
												
												@Override
												public void onClick(View v) {
													//TODO Auto-generated method stub
													timer.cancel();
													mTripStatus = 1;
													rl_courier_user_info.setVisibility(View.GONE);
													
													if (cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isAvailable() && cm.getActiveNetworkInfo().isConnected()) {
														acceptRide(strStatusID, strDriverFeedId, strDriverUserID, "accepted");
													} else {
														networkConnectivity();
													}
												}
											});
										}
									});
								
								}
								
								//View for food
								if(strUserView.equals("food") == true)
								{
									DriverActivity.this.runOnUiThread(new Runnable() {
										
										@Override
										public void run() {
											// TODO Auto-generated method stub
											//Rider
											rl_user_info.setVisibility(View.GONE);
											rl_courier_user_info.setVisibility(View.GONE);
											rl_food_user_info.setVisibility(View.VISIBLE);
											
											//to play music
											mPlayer.start();
											
		 							        tv_food_user_miles.setText(String.valueOf(df.format(dist))+" miles");
											tv_food_price.setText("$ "+strDriverCost);
											
											if (cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isAvailable() && cm.getActiveNetworkInfo().isConnected()) {
												getUsernamefromiUserID(strDriverFeedId);
											} else {
												networkConnectivity();
											}
											
											rl_food_bottom.setOnClickListener(new View.OnClickListener() {
												
												@Override
												public void onClick(View v) {
													//TODO Auto-generated method stub
													timer.cancel();
													mTripStatus = 1;
													rl_food_user_info.setVisibility(View.GONE);
													
													if (cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isAvailable() && cm.getActiveNetworkInfo().isConnected()) {
														acceptRide(strStatusID, strDriverFeedId, strDriverUserID, "accepted");
													} else {
														networkConnectivity();
													}
												}
											});
											
											cdt = new CountDownTimer(31000,1000) 
											{
												@Override
												public void onTick(long millisUntilFinished) {
													//TODO Auto-generated method stub
													isRunning = true;
													int seconds = (int) ((millisUntilFinished / 1000) % 60);
													tv_food_counter.setText(""+seconds);
												}
												
												@Override
												public void onFinish() {
													//TODO Auto-generated method stub
													isRunning = false;
													rl_food_bottom.setVisibility(View.GONE);
													timer.cancel();
													if(mTripStatus == 0) {
														editor.putString("strFeedId", "");
														editor.putString("strFeedUserID", "");
														editor.commit();
														
														if (cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isAvailable() && cm.getActiveNetworkInfo().isConnected()) {
															acceptRide(strStatusID, strDriverFeedId, strDriverUserID, "rejected");
														} else {
															networkConnectivity();
														}
													}
												}
												
											}.start();
										}
									});
									
								}
							}
							
						}catch(Exception e) {e.printStackTrace();}
					}
				} catch (Exception e) {e.printStackTrace();}
			}
	
			// When error occured
			@Override
			public void onFailure(int statusCode, Throwable error, String content) {
				//TODO Auto-generated method stub
				if (statusCode == 404) {
					//Toast.makeText(DriverActivity.this, "Requested resource not found", Toast.LENGTH_LONG).show();
				} else if (statusCode == 500) {
					//Toast.makeText(DriverActivity.this, "Something went wrong at server end", Toast.LENGTH_LONG).show();
				} else {
					networkConnectivity();
				}
			}
		});
		
	}
	
	//To update Booking Status
	public void updateBookingStatus(String strStatusID, String strStatus, final String strFeedID)
	{
		// TODO Auto-generated method stub
		AsyncHttpClient client = new AsyncHttpClient();
		// Http Request Params Object
		RequestParams params = new RequestParams();
		
		String url = null;
		url = UrlGenerator.updateBookingStatus();
		
		params.add("ID", strStatusID);
		params.add("status", strStatus);
		params.add("iFeedID", strFeedID);
				
		client.post(url, params, new AsyncHttpResponseHandler() {

			@Override
			public void onSuccess(String response) {
				// Hide ProgressBar
				JSONObject jObject;
				try {
					jObject = new JSONObject(response);
					
					String strStatus = jObject.optString("status");
					String strMessage = jObject.optString("message");
					
					if( (strStatus.equals("0")) || (strStatus == "0") ) 
					{
						Intent i = new Intent(DriverActivity.this, DriverPickUpActivity.class);
						i.putExtra("iFeedID", strFeedID);
						i.putExtra("iFeedUserID", mySharedPreferences.getString("strFeedUserID", ""));
						i.putExtra("currentLat", String.valueOf(mLatitude));
						i.putExtra("currentLong", String.valueOf(mLongitude));
						i.putExtra("pickLat", strFLat);
						i.putExtra("pickLong", strFLong);
						i.putExtra("dropLat", strTLat);
						i.putExtra("dropLong", strTLong);
						i.putExtra("strPayment", strPayment);
						i.putExtra("strUserView", strUserView);
						i.putExtra("strUserPhone", strUserPhone);
						startActivity(i);
					}
												
				} catch (Exception e) {e.printStackTrace();}
			}

			// When error occured
			@Override
			public void onFailure(int statusCode, Throwable error, String content) {
				// TODO Auto-generated method stub
				if (statusCode == 404) {
					//Toast.makeText(DriverActivity.this, "Requested resource not found", Toast.LENGTH_LONG).show();
				} else if (statusCode == 500) {
					//Toast.makeText(DriverActivity.this, "Something went wrong at server end", Toast.LENGTH_LONG).show();
				} else {
					networkConnectivity();
				}
			}
		});
	}
	
	//To get UsernamefromiUserID
	public void getUsernamefromiUserID(String strFeedID)
	{
		// TODO Auto-generated method stub
		AsyncHttpClient client = new AsyncHttpClient();
		// Http Request Params Object
		RequestParams params = new RequestParams();
		
		String url = null;
		url = UrlGenerator.getUsernamefromiUserID();
		
		params.add("iFeedID", strFeedId);
				
		client.post(url, params, new AsyncHttpResponseHandler() {

			@Override
			public void onSuccess(String response) {
				// Hide ProgressBar
				JSONArray jArray;
				try {
					jArray = new JSONArray(response);
					
					JSONObject jObj = jArray.getJSONObject(0);
					final String strFirstName = jObj.getString("vFirst");
					final String strLastName = jObj.getString("vLast");
					final String strImageName = jObj.optString("vImage");
					strUserPhone = jObj.optString("userPhone");
					
					DriverActivity.this.runOnUiThread(new Runnable() {
						public void run() {
							
							if(strUserView.equals("courier") == true)
							{
								String str_courier_url;
								
								str_courier_url = "http://ec2-52-24-193-7.us-west-2.compute.amazonaws.com/ws/images/courierInfo/courier_"+ strUserID +".jpg";
								
								rl_courier_user_info.setVisibility(View.VISIBLE);
								rl_user_info.setVisibility(View.GONE);
								rl_food_user_info.setVisibility(View.GONE);
								tv_courier_user_name.setText(strFirstName+" "+strLastName);
								String imgpath = getResources().getString(R.string.strDriverThumbImageUrl)+strImageName;
								Picasso.with(DriverActivity.this).load(imgpath).resize(60, 60).into(iv_courier_user_image);
								Picasso.with(DriverActivity.this).load(str_courier_url).into(iv_courier_image);
								
							}
							else if(strUserView.equals("food") == true)
							{
								rl_food_user_info.setVisibility(View.VISIBLE);
								rl_user_info.setVisibility(View.GONE);
								rl_courier_user_info.setVisibility(View.GONE);
								tv_food_user_name.setText("Food delivery: "+strNameOfRest);
							}
							else
							{
								rl_user_info.setVisibility(View.VISIBLE);
								rl_courier_user_info.setVisibility(View.GONE);
								rl_food_user_info.setVisibility(View.GONE);
								tv_user_name.setText(strFirstName+" "+strLastName);
								String imgpath = getResources().getString(R.string.strDriverThumbImageUrl)+strImageName;
								Picasso.with(DriverActivity.this).load(imgpath).resize(60, 60).into(iv_user_image);
							}
						}
					});
					
					getFromAddress(strFeedId, strUserView);					
												
				} catch (Exception e) {e.printStackTrace();}
			}

			// When error occured
			@Override
			public void onFailure(int statusCode, Throwable error, String content) {
				// TODO Auto-generated method stub
				if (statusCode == 404) {
					//Toast.makeText(DriverActivity.this, "Requested resource not found", Toast.LENGTH_LONG).show();
				} else if (statusCode == 500) {
					//Toast.makeText(DriverActivity.this, "Something went wrong at server end", Toast.LENGTH_LONG).show();
				} else {
					networkConnectivity();
				}
			}
		});
	}
	
	//To get UsernamefromiUserID
	public void getFromAddress(String strFeedID, final String strType)
	{
		// TODO Auto-generated method stub
		AsyncHttpClient client = new AsyncHttpClient();
		// Http Request Params Object
		RequestParams params = new RequestParams();
		
		String url = null;
		url = UrlGenerator.getFromAddress();
		//Log.e("url ##", "!! "+url);
		
		params.add("iFeedID", strFeedId);
				
		client.post(url, params, new AsyncHttpResponseHandler() {

			@Override
			public void onSuccess(String response) {
				// Hide ProgressBar
				
				JSONArray jArray;
				try {
					jArray = new JSONArray(response);
					
					JSONObject jObj = jArray.getJSONObject(0);
					final String strLat = jObj.getString("fLat");
					final String strLng = jObj.getString("fLong");
					
					DriverActivity.this.runOnUiThread(new Runnable() {
						public void run() {
							
							String strDriverAddress = getAddress(String.valueOf(strLat), String.valueOf(strLng));
							if(strType.equals("courier") == true) { } 
							else if(strType.equals("food") == true) {
								tv_food_user_address.setText(strDriverAddress);
							}
							else {
								tv_user_address.setText(strDriverAddress);
							}
						}
					});
					
					//getFromAddress(strFeedId);					
												
				} catch (Exception e) {e.printStackTrace();}
			}

			// When error occured
			@Override
			public void onFailure(int statusCode, Throwable error, String content) {
				// TODO Auto-generated method stub
				if (statusCode == 404) {
					//Toast.makeText(DriverActivity.this, "Requested resource not found", Toast.LENGTH_LONG).show();
				} else if (statusCode == 500) {
					//Toast.makeText(DriverActivity.this, "Something went wrong at server end", Toast.LENGTH_LONG).show();
				} else {
					networkConnectivity();
				}
			}
		});
	}
	
	//To accept or reject ride
	public void acceptRide(final String strStatusID,final String strFeedID, String strDriverUserID,final String strStatus)
	{
		// TODO Auto-generated method stub
		AsyncHttpClient client = new AsyncHttpClient();
		// Http Request Params Object
		RequestParams params = new RequestParams();
		
		String url = null;
		url = UrlGenerator.acceptedRide();
		
		params.add("iFeedID", strFeedID);
		params.add("iDriverID", strDriverUserID);
		
		client.post(url, params, new AsyncHttpResponseHandler() {

			@Override
			public void onSuccess(String response) {
				// Hide ProgressBar
				if (cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isAvailable() && cm.getActiveNetworkInfo().isConnected()) {
					updateBookingStatus(strStatusID, strStatus, strFeedID);
				} else {
					networkConnectivity();
				}
			}

			// When error occured
			@Override
			public void onFailure(int statusCode, Throwable error, String content) {
				// TODO Auto-generated method stub
				
				if (statusCode == 404) {
					//Toast.makeText(DriverActivity.this, "Requested resource not found", Toast.LENGTH_LONG).show();
				} else if (statusCode == 500) {
					//Toast.makeText(DriverActivity.this, "Something went wrong at server end", Toast.LENGTH_LONG).show();
				} else {
					networkConnectivity();
				}
			}
		});
	}
	
	private void deleteRide(String strFeedId) {
		// TODO Auto-generated method stub
		AsyncHttpClient client = new AsyncHttpClient();
		RequestParams params = new RequestParams();
		
		// Show ProgressBar
		final ProgressDialog pdialog = new ProgressDialog(DriverActivity.this);
		pdialog.setMessage("Loading...");
		pdialog.setCancelable(false);
		pdialog.show();
		
		String url = null;
		url = UrlGenerator.UserDeleteRide();
		
		params.add("iFeedID", strFeedId);
						
		client.get(url, params, new AsyncHttpResponseHandler() {

			@Override
			public void onSuccess(String response) {
				// Hide ProgressBar
				pdialog.hide();
				
				JSONObject jObject;
				try {
					jObject = new JSONObject(response);
					
					String strStatus = jObject.optString("status");
					String strMessage = jObject.optString("message");
					final String strUserId; 
					if(strStatus.equals("0") == true) {
						/*Intent i_destination = new Intent(DriverActivity.this, MainActivity.class);
						i_destination.putExtra("page", 0);
						startActivity(i_destination);
						DriverActivity.this.finish();*/
					}
					
				} catch (Exception e) {e.printStackTrace();}
			}

			// When error occured
			@Override
			public void onFailure(int statusCode, Throwable error, String content) {
				// TODO Auto-generated method stub
				// Hide ProgressBar
				pdialog.hide();
				if (statusCode == 404) {
					//Toast.makeText(DriverActivity.this, "Requested resource not found", Toast.LENGTH_LONG).show();
				} else if (statusCode == 500) {
					//Toast.makeText(DriverActivity.this, "Something went wrong at server end", Toast.LENGTH_LONG).show();
				} else {
					networkConnectivity();
				}
			}
		});
	}
	
	
	public void chkTripCancelled(String mFeedID, String mUserID)
	{
		// TODO Auto-generated method stub
		AsyncHttpClient client = new AsyncHttpClient();
		// Http Request Params Object
		RequestParams params = new RequestParams();
				
		String url = null;
		url = UrlGenerator.chkTripCancelled();
		
		params.add("iFeedID", mFeedID);
		params.add("iDriverID", mUserID);
				
		client.post(url, params, new AsyncHttpResponseHandler() {

			@Override
			public void onSuccess(String response) {
				// Hide ProgressBar
				JSONArray jArray;
				try {
					jArray = new JSONArray(response);
					
					JSONObject jObject = jArray.getJSONObject(0);
					String strTripStatus = jObject.optString("vUserView");
					
					if(strTripStatus.equals("delete") == true) {
						if(mTripCancelStatus == 0) {
							showTripCancelAlert();
						}
					}
						
				} catch (Exception e) {e.printStackTrace();}
			}

			// When error occured
			@Override
			public void onFailure(int statusCode, Throwable error, String content) {
				// TODO Auto-generated method stub
				if (statusCode == 404) {
					//Toast.makeText(DriverActivity.this, "Requested resource not found", Toast.LENGTH_LONG).show();
				} else if (statusCode == 500) {
					//Toast.makeText(DriverActivity.this, "Something went wrong at server end", Toast.LENGTH_LONG).show();
				} else {
					networkConnectivity();
				}
			}
		});
	}
	
	private void showTripCancelAlert() {
		// TODO Auto-generated method stub
		mTripCancelStatus = 1;
		new AlertDialog.Builder(DriverActivity.this)
		.setMessage(getResources().getString(R.string.strTripCancel))
		.setCancelable(false)
		.setPositiveButton(getResources().getString(R.string.stryes),
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						Intent i_driver = new Intent(DriverActivity.this, DriverActivity.class);
						overridePendingTransition(0, 0);
						startActivity(i_driver);
						DriverActivity.this.finish();
						timer.cancel();
						timerCancel.cancel();
						cdt.cancel();
						mTripCancelStatus = 0;
					}
				})
		.show();
	}
	
	private String getAddress(String lat, String lng) {
		// TODO Auto-generated method stub
		String strLocationAddress = null;
		try 
		{
			geocoder = new Geocoder(this, Locale.getDefault());
			addresses = geocoder.getFromLocation(Double.valueOf(lat), Double.valueOf(lng), 1);
			 
			if(addresses.size() > 0)
			{
				LatLng newLatLng = new LatLng(Double.valueOf(lat), Double.valueOf(lng)); 
				MarkerOptions options = new MarkerOptions();
			 		options.position(newLatLng);
			 		options.snippet("User Location");
			        options.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_user_marker));
			        myMap.addMarker(options);
			        //myMap.animateCamera(CameraUpdateFactory.newLatLngZoom(newLatLng, 15.0f));
				 
				String address = addresses.get(0).getAddressLine(0);
		        String city = addresses.get(0).getLocality();
		        String state = addresses.get(0).getAdminArea();
		        String country = addresses.get(0).getCountryName();
		        String postalCode = addresses.get(0).getPostalCode();
			         
		        strLocationAddress = address + ", " + city + ", " + state + ", " + country + ", " + postalCode;
		        //Log.e("strLocationAddress",""+strLocationAddress);
		   }
		} catch (NumberFormatException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return strLocationAddress;
	}
	
	private void networkConnectivity()
	{
        new AlertDialog.Builder(DriverActivity.this)
        .setTitle(R.string.title)
        .setMessage(R.string.message)
        .setCancelable(false)
        .setPositiveButton(R.string.strok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            	dialog.dismiss();
            }
        }).show();
	}
	
}