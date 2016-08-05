package com.app.waytogo;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.app.waytogo.route.Route;
import com.app.waytogo.route.Routing;
import com.app.waytogo.route.RoutingListener;
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
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class DriverDropOffActivity extends AppCompatActivity implements ConnectionCallbacks, OnConnectionFailedListener, LocationListener, RoutingListener {

	private Toolbar mToolbar;
	private ConnectivityManager cm;
	private GoogleApiClient mGoogleApiClient;
	private LocationRequest mLocationRequest;
	private LocationManager mLocationManager;
	private GoogleMap myMap;
	private Location location;
	private LatLng myLatLng;
	private SharedPreferences mySharedPreferences;
	private Editor editor;
	private String USER = "user_pref";
	private double mDestinationLatitude, mDestinationLongitude;
	private Geocoder geocoder;
	private List<Address> addresses;
	private Timer timer, timerCancel;
	private String strFeedID, strFeedUserID, strLat, strLng;
	private ImageView iv_call_image;
	private CircularImageView iv_user_image;
	private TextView tv_user_name, tv_user_address;
	private EditText edt_driver_address;
	private Button btn_complete;
	private RelativeLayout rl_user_info;
	private Routing routing;
    private String routeDistance, routeDuration;
    private String strPickLat, strPickLong, strDropLat, strDropLong, strPaymentType, strFirst, strLast, strImage, strCost, strUserPhone;
    protected LatLng start;
    protected LatLng end;
    private int mTripCancelStatus = 0;
    private int mTripStatus = 1;
    	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_driver_dropoff);
		
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		
		mToolbar = (Toolbar) findViewById(R.id.toolbar);

		cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		
		setSupportActionBar(mToolbar);
		getSupportActionBar().setDisplayShowHomeEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		
		try {
			Bundle b = getIntent().getExtras();
			strFeedID = b.getString("iFeedID");
			strPickLat = b.getString("strPickLat");
			strPickLong = b.getString("strPickLong");
			strDropLat = b.getString("strDropLat");
			strDropLong = b.getString("strDropLong");
			strFirst = b.getString("strFirst");
			strLast = b.getString("strLast");
			strImage = b.getString("strImage");
			strPaymentType = b.getString("strPaymentType");
			strCost = b.getString("strCost");
			strUserPhone = b.getString("strUserPhone");
			
		} catch(NullPointerException e) {}
		
		mySharedPreferences = getSharedPreferences(USER, MODE_PRIVATE);
		editor = mySharedPreferences.edit();
		
		mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

		if (!mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
			buildAlertMessageNoGps();
		}

		mGoogleApiClient = new GoogleApiClient.Builder(this)
				.addApi(LocationServices.API).addConnectionCallbacks(this)
				.addOnConnectionFailedListener(this).build();

		myMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map_driver_dropoff)).getMap();
		
		iv_user_image = (CircularImageView) findViewById(R.id.iv_user_image);
		iv_call_image = (ImageView) findViewById(R.id.iv_call_image);
		tv_user_name = (TextView) findViewById(R.id.tv_user_name);
		tv_user_address = (TextView) findViewById(R.id.tv_user_address);
		edt_driver_address = (EditText) findViewById(R.id.edt_driver_address);
		btn_complete = (Button) findViewById(R.id.btn_complete);
		rl_user_info = (RelativeLayout) findViewById(R.id.rl_user_info);
			
		String strToAddress = getAddress(strDropLat, strDropLong);
		Picasso.with(DriverDropOffActivity.this).load(strImage).resize(60, 60).into(iv_user_image);
		tv_user_name.setText(strFirst+" "+strLast);
		edt_driver_address.setText(strToAddress);
		iv_call_image.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + strUserPhone));
				startActivity(intent);
			}
		});
		
		showStaticMapDialog(strFirst, strImage, strDropLat, strDropLong, strToAddress);
		
		myMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
		myMap.getUiSettings().setZoomControlsEnabled(false);
		myMap.setMyLocationEnabled(true);
		
		location = myMap.getMyLocation();

	    if (location != null) {
	    	myLatLng = new LatLng(location.getLatitude(), location.getLongitude());
	    	//myMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myLatLng, 15));
	    	MarkerOptions options = new MarkerOptions();
		 	    options.position(myLatLng);
		        options.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_driver_marker));
		        options.title("Current Location");
		   	    myMap.addMarker(options);
		   	    
		   	mDestinationLatitude = location.getLatitude();
		   	mDestinationLongitude = location.getLongitude();
	    }
	    
	    if (cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isAvailable() && cm.getActiveNetworkInfo().isConnected()) {
	    	updateDriverTripLocation(strFeedID, strPickLat, strPickLong);
	    } else {
	    	networkConnectivity();
	    }

		timerCancel = new Timer();
	    timerCancel.schedule(new TimerTask() {

			@Override
			public void run() {
				
				DriverDropOffActivity.this.runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						// TODO Auto-generated method stub
						if (cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isAvailable() && cm.getActiveNetworkInfo().isConnected()) {
							chkTripCancelled(strFeedID, mySharedPreferences.getString("strUserId", ""));
						} else {
							networkConnectivity();
						}
					}
				});
				
			}
	    	
	    },0,3000);
	    
		routing = new Routing(Routing.TravelMode.DRIVING);
        routing.registerListener(this);
        
        btn_complete.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isAvailable() && cm.getActiveNetworkInfo().isConnected()) {
					driverCompleteConfirmation();
				} else {
					networkConnectivity();
				}
			}
		});
        
	}

	private void buildAlertMessageNoGps() {
		// TODO Auto-generated method stub
		new AlertDialog.Builder(DriverDropOffActivity.this)
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
						DriverDropOffActivity.this.finish();
					}
				}).show();
	}
	
	private void driverCompleteConfirmation() {
		// TODO Auto-generated method stub
		new AlertDialog.Builder(DriverDropOffActivity.this)
		.setMessage(getResources().getString(R.string.strdriverCompleteConfirmation))
		.setCancelable(false)
		.setPositiveButton(getResources().getString(R.string.stryes),
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						if (cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isAvailable() && cm.getActiveNetworkInfo().isConnected()) {
							getDriverEarnings();
						} else {
							networkConnectivity();
						}
					}
				})
		.setNegativeButton(getResources().getString(R.string.strno),
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog,
							int which) {
						dialog.dismiss();
					}
				}).show();
	}

	@Override
	public void onConnectionFailed(ConnectionResult arg0) {
		// TODO Auto-generated method stub
		
	}

	private void animateCameraTo(double latitude, double longitude) {
		// TODO Auto-generated method stub
		mDestinationLatitude = latitude;
		mDestinationLongitude = longitude;
		
		myMap.clear();
		LatLng markerLatLng = new LatLng(latitude, longitude);
		
		myMap.animateCamera(CameraUpdateFactory.newLatLngZoom(markerLatLng, 15));
    	MarkerOptions options = new MarkerOptions();
	 	    options.position(markerLatLng);
	        options.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_driver_marker));
	        options.title("Current Location");
	   	    myMap.addMarker(options);

	}

	@Override
	public void onLocationChanged(Location location) {
		// TODO Auto-generated method stub
		double lat = location.getLatitude();
		double lng = location.getLongitude();

		//animateCameraTo(lat, lng);
		
		strLat = String.valueOf(lat);
		strLng = String.valueOf(lng);
		
		timer = new Timer();
		timer.schedule(new TimerTask() {

			@Override
			public void run() {
				if (cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isAvailable() && cm.getActiveNetworkInfo().isConnected()) {
					updateDriverTripLocation(strFeedID, strLat, strLng);
				} else {
					networkConnectivity();
				}
			}
		},0,5000);
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
			// mLatitudeText.setText(String.valueOf(mLastLocation.getLatitude()));
			// mLongitudeText.setText(String.valueOf(mLastLocation.getLongitude()));
			animateCameraTo(mLastLocation.getLatitude(),mLastLocation.getLongitude());
			
		   	mDestinationLatitude = mLastLocation.getLatitude();
		   	mDestinationLongitude = mLastLocation.getLongitude();
		}
	}

	@Override
	public void onConnectionSuspended(int arg0) {
		// TODO Auto-generated method stub

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

	private class GeocoderHandler extends Handler {
		@Override
        public void handleMessage(Message message) {
            String locationAddress;
            switch (message.what) {
                case 1:
                    Bundle bundle = message.getData();
                    locationAddress = bundle.getString("address");
                    break;
                default:
                    locationAddress = null;
            }
            
            if(locationAddress != null)
            {
	            String[] latlng = locationAddress.split("\n");
		        getAddress(latlng[0], latlng[1]);
            }
	    }
	}

	private String getAddress(String lat, String lng) {
		// TODO Auto-generated method stub
		String strLocationAddress = null;
		try 
		{
			geocoder = new Geocoder(DriverDropOffActivity.this, Locale.getDefault());
			addresses = geocoder.getFromLocation(Double.valueOf(lat), Double.valueOf(lng), 1);
			 
			if(addresses.size() > 0)
			{
				LatLng newLatLng = new LatLng(Double.valueOf(lat), Double.valueOf(lng)); 
				MarkerOptions options = new MarkerOptions();
			 		options.position(newLatLng);
			        options.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_driver_marker));
			        myMap.addMarker(options);
			        //myMap.animateCamera(CameraUpdateFactory.newLatLngZoom(newLatLng, 15.0f));
				 
				String address = addresses.get(0).getAddressLine(0);
		        String city = addresses.get(0).getLocality();
		        String state = addresses.get(0).getAdminArea();
		        String country = addresses.get(0).getCountryName();
		        String postalCode = addresses.get(0).getPostalCode();
			         
		        strLocationAddress = address + ", " + city + ", " + state + ", " + country + ", " + postalCode;
		     }
		} catch (NumberFormatException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return strLocationAddress; 
	}
	
	protected void updateDriverTripLocation(String strFeedid, final String strLat, final String strLng) {
		// TODO Auto-generated method stub
		// TODO Auto-generated method stub
		AsyncHttpClient client = new AsyncHttpClient();
		// Http Request Params Object
		RequestParams params = new RequestParams();
		
		String url = null;
		url = UrlGenerator.updateDriverTripLocation();
		
		params.add("iFeedID", strFeedid);
		params.add("currentLat", strLat);
		params.add("currentLong", strLng);
				
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
						if ( (strDropLat.equals("") == false) && (strDropLong.equals("") == false) )
						{
							start = new LatLng(Double.valueOf(strLat), Double.valueOf(strLng));
					        end = new LatLng(Double.valueOf(strDropLat), Double.valueOf(strDropLong));
						}
						else
						{
							myMap.clear();
							
							LatLng markerLatLng = new LatLng(Double.valueOf(strLat), Double.valueOf(strLng));
							
							MarkerOptions options = new MarkerOptions();
						 	    options.position(markerLatLng);
						        options.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_driver_marker));
						        options.title("Current Location");
						        myMap.addMarker(options);
						   	    myMap.addMarker(options);
						}
					}
												
				} catch (Exception e) {e.printStackTrace();}
			}

			// When error occured
			@Override
			public void onFailure(int statusCode, Throwable error, String content) {
				// TODO Auto-generated method stub
				if (statusCode == 404) {
					//Toast.makeText(DriverCourierActivity.this, "Requested resource not found", Toast.LENGTH_LONG).show();
				} else if (statusCode == 500) {
					//Toast.makeText(DriverCourierActivity.this, "Something went wrong at server end", Toast.LENGTH_LONG).show();
				} else {
					networkConnectivity();
				}
			}
		});
	}
	
	//To update payment confirmation
	public void getDriverEarnings()
	{
		// TODO Auto-generated method stub
		AsyncHttpClient client = new AsyncHttpClient();
		// Http Request Params Object
		
		// Show ProgressBar
		final ProgressDialog pdialog = new ProgressDialog(DriverDropOffActivity.this);
		pdialog.setMessage("Calculating your earnings...");
		pdialog.setCancelable(false);
		pdialog.show();
				
		RequestParams params = new RequestParams();
		
		String url = null;
		url = UrlGenerator.getDriverEarnings();
		
		params.add("iDriverID", mySharedPreferences.getString("strUserId", ""));
		
		client.post(url, params, new AsyncHttpResponseHandler() {

			@Override
			public void onSuccess(String response) {
				// Hide ProgressBar
				pdialog.hide();
				JSONObject jObject;
				try {
					jObject = new JSONObject(response);
					
					String strStatus = jObject.optString("status");
					
					if(strStatus.equals("0") == true) 
					{
						String strData = jObject.optString("data");
						Double dEarnings  = Double.valueOf(strData);
						Double dCost = Double.valueOf(strCost);
						Double dTotal = dEarnings + dCost;
						
						if (cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isAvailable() && cm.getActiveNetworkInfo().isConnected()) {
							updateDriverEarnings(dTotal);
						} else {
							networkConnectivity();
						}
					}
												
				} catch (Exception e) {e.printStackTrace();}
			}

			// When error occured
			@Override
			public void onFailure(int statusCode, Throwable error, String content) {
				// TODO Auto-generated method stub
				pdialog.hide();
				if (statusCode == 404) {
					//Toast.makeText(DriverCourierActivity.this, "Requested resource not found", Toast.LENGTH_LONG).show();
				} else if (statusCode == 500) {
					//Toast.makeText(DriverCourierActivity.this, "Something went wrong at server end", Toast.LENGTH_LONG).show();
				} else {
					networkConnectivity();
				}
			}
		});
	}
	
	//To update payment confirmation
	public void updateDriverEarnings(final Double dTotal)
	{
		// TODO Auto-generated method stub
		AsyncHttpClient client = new AsyncHttpClient();
		
		// Show ProgressBar
		final ProgressDialog pdialog = new ProgressDialog(DriverDropOffActivity.this);
		pdialog.setMessage("Updating your earnings...");
		pdialog.setCancelable(false);
		pdialog.show();
		
		// Http Request Params Object
		RequestParams params = new RequestParams();
		
		String url = null;
		url = UrlGenerator.updateDriverEarnings();
		
		params.add("iDriverID", mySharedPreferences.getString("strUserId", ""));
		params.add("earnings", String.valueOf(dTotal));
		
		client.post(url, params, new AsyncHttpResponseHandler() {

			@Override
			public void onSuccess(String response) {
				// Hide ProgressBar
				pdialog.hide();
				
				Intent i_confirmation = new Intent(DriverDropOffActivity.this, CompleteRideActivity.class);
				i_confirmation.putExtra("strTripCost", strCost);
				i_confirmation.putExtra("strTotalEarnings", String.valueOf(dTotal));
				i_confirmation.putExtra("strFeedID", strFeedID);
				startActivity(i_confirmation);
				
				if (cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isAvailable() && cm.getActiveNetworkInfo().isConnected()) {
					DriverDropOffActivity.this.finish();
				} else {
					networkConnectivity();
				}
			}

			// When error occured
			@Override
			public void onFailure(int statusCode, Throwable error, String content) {
				// TODO Auto-generated method stub
				pdialog.hide();
				if (statusCode == 404) {
					//Toast.makeText(DriverCourierActivity.this, "Requested resource not found", Toast.LENGTH_LONG).show();
				} else if (statusCode == 500) {
					//Toast.makeText(DriverCourierActivity.this, "Something went wrong at server end", Toast.LENGTH_LONG).show();
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
				//Log.e("get sms verification","@@@"+response);
				
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
					//Toast.makeText(DriverCourierActivity.this, "Requested resource not found", Toast.LENGTH_LONG).show();
				} else if (statusCode == 500) {
					//Toast.makeText(DriverCourierActivity.this, "Something went wrong at server end", Toast.LENGTH_LONG).show();
				} else {
					networkConnectivity();
				}
			}
		});
	}
	
	private void showTripCancelAlert() {
		// TODO Auto-generated method stub
		mTripCancelStatus = 1;
		new AlertDialog.Builder(DriverDropOffActivity.this)
		.setMessage(getResources().getString(R.string.strTripCancel))
		.setCancelable(false)
		.setPositiveButton(getResources().getString(R.string.stryes),
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						Intent i_driver = new Intent(DriverDropOffActivity.this, DriverActivity.class);
						startActivity(i_driver);
						DriverDropOffActivity.this.finish();
						
						timer.cancel();
						timerCancel.cancel();
						mTripCancelStatus = 0;
					}
				})
		.show();
	}
	
	@Override
	public void onRoutingFailure() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onRoutingStart() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onRoutingSuccess(PolylineOptions mPolyOptions, Route route) {
		// TODO Auto-generated method stub
		routeDistance = route.getDistanceText(); 
		routeDuration = route.getDurationText();
		
		PolylineOptions polyOptions = new PolylineOptions();
        polyOptions.color(Color.BLUE);
        polyOptions.width(10);
        polyOptions.addAll(mPolyOptions.getPoints());
        myMap.addPolyline(polyOptions);

        // Start marker
        MarkerOptions options = new MarkerOptions();
        options.position(start);
        options.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_driver_marker));
        options.title("Your location");
  		myMap.addMarker(options);
        
        tv_user_address.setText(routeDuration);
        
        // End marker
        options = new MarkerOptions();
        options.position(end);
        options.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_user_marker));
        myMap.addMarker(options);
	}
	
	private void networkConnectivity()
	{
        new AlertDialog.Builder(DriverDropOffActivity.this)
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
	
	private void userCancelTrip(String strFeedId) {
		// TODO Auto-generated method stub
		AsyncHttpClient client = new AsyncHttpClient();
		RequestParams params = new RequestParams();
		
		// Show ProgressBar
		final ProgressDialog pdialog = new ProgressDialog(DriverDropOffActivity.this);
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
					jObject.optString("message");
					if(strStatus.equals("0") == true) {
						Intent i_destination = new Intent(DriverDropOffActivity.this, DriverActivity.class);
						startActivity(i_destination);
						DriverDropOffActivity.this.finish();
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
					//Toast.makeText(DestinationActivity.this, "Requested resource not found", Toast.LENGTH_LONG).show();
				} else if (statusCode == 500) {
					//Toast.makeText(DestinationActivity.this, "Something went wrong at server end", Toast.LENGTH_LONG).show();
				} else {
					networkConnectivity();
				}
			}
		});
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

		if (id == R.id.action_cancel) {
			deleteTripConfirm(strFeedID);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		if(mTripStatus == 1){
			menu.findItem(R.id.action_steeringwheel_active).setVisible(false);
			menu.findItem(R.id.action_overflow).setVisible(true);
		}
		return super.onPrepareOptionsMenu(menu);
	}
	
	private void deleteTripConfirm(final String strFeedID)
	{
        new AlertDialog.Builder(DriverDropOffActivity.this)
        .setMessage(R.string.strdeletetripmessage)
        .setCancelable(false)
        .setPositiveButton(R.string.stryes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            	userCancelTrip(strFeedID);
            }
        })
        .setNegativeButton(R.string.strcancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            	dialog.dismiss();
            }
        }).show();
	}
	
	private void showStaticMapDialog(String strFName, final String strImage, final String strDropLat, final String strDropLong, String strToAddress)
	{
		AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(DriverDropOffActivity.this);
		LayoutInflater inflater = getLayoutInflater();
		View dialogView = inflater.inflate(R.layout.alert_static_map_navigate, null);
		dialogBuilder.setView(dialogView);
		
		ImageView iv_user_image_alert = (ImageView) dialogView.findViewById(R.id.iv_user_image);
		ImageView iv_static_map_alert = (ImageView) dialogView.findViewById(R.id.iv_static_map);
		TextView tv_user_name_alert = (TextView) dialogView.findViewById(R.id.tv_user_name);
		TextView tv_address_alert = (TextView) dialogView.findViewById(R.id.tv_address);
		ImageButton btnNavigate = (ImageButton) dialogView.findViewById(R.id.btnNavigate);
		ImageButton btnCancel = (ImageButton) dialogView.findViewById(R.id.btnCancel);
		
		String strStaticMap = "http://maps.googleapis.com/maps/api/staticmap?center="+strDropLat+","+strDropLong+"&zoom=15&scale=2&size=250x150&maptype=roadmap&format=png&visual_refresh=true&markers=size:mid|color:0xff0000|label:|"+strDropLat+","+strDropLong;
		
		Picasso.with(DriverDropOffActivity.this).load(strImage).resize(60, 60).into(iv_user_image_alert);
		Picasso.with(DriverDropOffActivity.this).load(strStaticMap).into(iv_static_map_alert);
		
		tv_user_name_alert.setText("Drop off "+strFName);
		tv_address_alert.setText(strToAddress);
		
		final AlertDialog alertDialog = dialogBuilder.create();
		alertDialog.show();
		
		btnNavigate.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				alertDialog.dismiss();
				
				String uri = String.format(Locale.ENGLISH, "http://maps.google.com/maps?saddr=%f,%f&daddr=%f,%f", Double.parseDouble(strPickLat), 
						Double.parseDouble(strPickLong), Double.parseDouble(strDropLat), Double.parseDouble(strDropLong));
				Intent intent_navigate = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
				intent_navigate.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
				if (intent_navigate.resolveActivity(getPackageManager()) != null) {
					startActivity(intent_navigate);
				} else {
					Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
					startActivity(intent);
				}
			}
		});
		
		btnCancel.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				alertDialog.dismiss();
				try {
					LatLngBounds bounds = new LatLngBounds.Builder()
	                .include(start)
	                .include(end)
	                .build();
					
					myMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100));
					
			        routing.execute(start, end);
				} catch (Exception e) {e.printStackTrace();}
			}
		});
	}
}
