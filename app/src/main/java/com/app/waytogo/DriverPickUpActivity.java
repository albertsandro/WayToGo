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
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.app.waytogo.adapter.FoodListConfirmationAdapter;
import com.app.waytogo.model.TripDetails;
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
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class DriverPickUpActivity extends AppCompatActivity implements ConnectionCallbacks, OnConnectionFailedListener, LocationListener, RoutingListener {

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
	private ImageView iv_user_image, iv_call_image, iv_food_user_image, iv_food_call_image;
	private TextView tv_user_name, tv_user_address, tv_food_user_name;
	private EditText edt_driver_address;
	private Button btn_arrived, btn_drop_off;
	private ListView lv_items;
	private RelativeLayout rl_user_info, rl_food_item_info;
	private Routing routing;
    private String routeDistance, routeDuration;
    private String strPayment, strPaymentType, strFirst, strLast, strImage, strCost, strPickLat, strPickLong, strDropLat, strDropLong, strUserView, strUserPhone;
    private String strFeedDesc, strFoodQty, strFoodPrice, strRestaurantName;
    protected LatLng start;
    protected LatLng end;
    private int mTripCancelStatus = 0;
    private int mTripStatus = 1;
        	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_driver_pickup);
		
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
				
		cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		
		mToolbar = (Toolbar) findViewById(R.id.toolbar);

		setSupportActionBar(mToolbar);
		getSupportActionBar().setDisplayShowHomeEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		//getSupportActionBar().setTitle(strSourceAddress);
		
		try {
			Bundle b = getIntent().getExtras();
			strFeedID = b.getString("iFeedID");
			strFeedUserID = b.getString("iFeedUserID");
			strLat = b.getString("currentLat");
			strLng = b.getString("currentLong");
            strPickLat = b.getString("pickLat");
            strPickLong = b.getString("pickLong");
            strDropLat = b.getString("dropLat");
            strDropLong = b.getString("dropLong");
            strPayment = b.getString("strPayment");
            strUserView = b.getString("strUserView");
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

		myMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map_driver_pickup)).getMap();
		
		iv_user_image = (ImageView) findViewById(R.id.iv_user_image);
		iv_call_image = (ImageView) findViewById(R.id.iv_call_image);
		tv_user_name = (TextView) findViewById(R.id.tv_user_name);
		tv_user_address = (TextView) findViewById(R.id.tv_user_address);
		edt_driver_address = (EditText) findViewById(R.id.edt_driver_address);
		btn_arrived = (Button) findViewById(R.id.btn_arrived);
		rl_user_info = (RelativeLayout) findViewById(R.id.rl_user_info);
		rl_food_item_info = (RelativeLayout) findViewById(R.id.rl_food_item_info);
		iv_food_user_image = (ImageView) findViewById(R.id.iv_food_user_image);
		iv_food_call_image = (ImageView) findViewById(R.id.iv_food_call_image);
		tv_food_user_name = (TextView) findViewById(R.id.tv_food_user_name);
		btn_drop_off = (Button) findViewById(R.id.btn_drop_off);
		lv_items = (ListView) findViewById(R.id.lv_items);
			
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
	    
	    updateDriverTripLocation(strFeedID, strLat, strLng);
		
		getFeedList(strFeedUserID);
		
		timerCancel = new Timer();
	    timerCancel.schedule(new TimerTask() {

			@Override
			public void run() {
				
				DriverPickUpActivity.this.runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						// TODO Auto-generated method stub
						if (cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isAvailable() && cm.getActiveNetworkInfo().isConnected()) {
							chkTripCancelled(strFeedID, strFeedUserID);
						} else {
							networkConnectivity();
						}
					}
				});
				
			}
	    	
	    },0,3000);
		
		routing = new Routing(Routing.TravelMode.DRIVING);
        routing.registerListener(this);
        
        btn_arrived.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(strUserView.equals("food") == true) {
					rl_food_item_info.setVisibility(View.VISIBLE);
					rl_user_info.setVisibility(View.GONE);
				} else {
					rl_food_item_info.setVisibility(View.GONE);
					rl_user_info.setVisibility(View.VISIBLE);
					driverArrivedConfirmation();
				}
			}
		});
        
        btn_drop_off.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				driverFoodConfirmation();
			}
		});
        
	}

	private void buildAlertMessageNoGps() {
		// TODO Auto-generated method stub
		new AlertDialog.Builder(DriverPickUpActivity.this)
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
						DriverPickUpActivity.this.finish();
					}
				}).show();
	}
	
	private void driverArrivedConfirmation() {
		// TODO Auto-generated method stub
		new AlertDialog.Builder(DriverPickUpActivity.this)
		.setMessage(getResources().getString(R.string.strdriverArrivedConfirmation))
		.setCancelable(false)
		.setPositiveButton(getResources().getString(R.string.stryes),
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						if(strPayment.equals("Cash") == true)
						{
							if (cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isAvailable() && cm.getActiveNetworkInfo().isConnected()) {
								pickedupRide(strFeedID);
							} else {
								networkConnectivity();
							}
						}
						else
						{
							//card
							if (cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isAvailable() && cm.getActiveNetworkInfo().isConnected()) {
								pickedupRide(strFeedID);
							} else {
								networkConnectivity();
							}
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
	
	private void driverFoodConfirmation() {
		// TODO Auto-generated method stub
		new AlertDialog.Builder(DriverPickUpActivity.this)
		.setMessage(getResources().getString(R.string.strdriverFoodConfirmation))
		.setCancelable(false)
		.setPositiveButton(getResources().getString(R.string.stryes),
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						if (cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isAvailable() && cm.getActiveNetworkInfo().isConnected()) {
							pickedupRide(strFeedID);
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
				
				DriverPickUpActivity.this.runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						// TODO Auto-generated method stub
						if (cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isAvailable() && cm.getActiveNetworkInfo().isConnected()) {
							updateDriverTripLocation(strFeedID, strLat, strLng);
						} else {
							networkConnectivity();
						}
					}
				});
				
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
			geocoder = new Geocoder(DriverPickUpActivity.this, Locale.getDefault());
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
	
	protected void updateDriverTripLocation(String strFeedID, final String strLat, final String strLng) {
		// TODO Auto-generated method stub
		// TODO Auto-generated method stub
		AsyncHttpClient client = new AsyncHttpClient();
		// Http Request Params Object
		RequestParams params = new RequestParams();
		
		String url = null;
		url = UrlGenerator.updateDriverTripLocation();
		params.add("iFeedID", strFeedID);
		params.add("currentLat", strLat);
		params.add("currentLong", strLng);
				
		client.post(url, params, new AsyncHttpResponseHandler() {

			@Override
			public void onSuccess(final String response) {
				// Hide ProgressBar
				JSONObject jObject;
				try {
					jObject = new JSONObject(response);
					
					String strStatus = jObject.optString("status");
					
					if(strStatus.equals("0") == true) 
					{
						if ( (strPickLat.equals("") == false) && (strPickLong.equals("") == false) )
						{
							DriverPickUpActivity.this.runOnUiThread(new Runnable() {
								
								@Override
								public void run() {
									// TODO Auto-generated method stub
							
									start = new LatLng(Double.valueOf(strLat), Double.valueOf(strLng));
							        end = new LatLng(Double.valueOf(strPickLat), Double.valueOf(strPickLong));
									
									LatLngBounds bounds = new LatLngBounds.Builder()
					                 .include(start)
					                 .include(end)
					                 .build();
									
									myMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100));
									
							        routing.execute(start, end);
					        	}
							});
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
						        //myMap.animateCamera(CameraUpdateFactory.newLatLngZoom(markerLatLng, 15.0f));
						   	    myMap.addMarker(options);
						}
					}
												
				} catch (Exception e) {}
					
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
	
	//To get UsernamefromiUserID
	public void getFeedList(String strFeedUserID)
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
		params.add("iUserID", strFeedUserID);
				
		client.post(url, params, new AsyncHttpResponseHandler() {

			@Override
			public void onSuccess(String response) {
				// Hide ProgressBar
				try {
					
					GsonBuilder gsonBuilder = new GsonBuilder();
					Gson gson = gsonBuilder.create();
	
					TripDetails trip = gson.fromJson(response, TripDetails.class);
					
					strFirst = trip.data.get(0).vFirst;
					strLast = trip.data.get(0).vLast;
					strImage = trip.data.get(0).vImage;
					strPaymentType = trip.data.get(0).vPayment;
					strCost = trip.data.get(0).vCost;
					strFeedDesc = trip.data.get(0).tFeedDescription;
					strFoodQty = trip.data.get(0).foodQuantity;
					strFoodPrice = trip.data.get(0).foodPrices;
					strRestaurantName = trip.data.get(0).nameofRest;
					
					String strToAddress = getAddress(strPickLat, strPickLong);
					Picasso.with(DriverPickUpActivity.this).load(strImage).resize(60, 60).into(iv_user_image);
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
					
					
					Picasso.with(DriverPickUpActivity.this).load(strImage).resize(60, 60).into(iv_food_user_image);
					tv_food_user_name.setText(strFirst+" "+strLast);
					iv_food_call_image.setOnClickListener(new View.OnClickListener() {
						
						@Override
						public void onClick(View v) {
							// TODO Auto-generated method stub
							Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + strUserPhone));
							startActivity(intent);
						}
					});
					
					if(strUserView.equals("food") == true) {
						lv_items.setAdapter(new FoodListConfirmationAdapter(DriverPickUpActivity.this, strFeedDesc, strFoodQty, strFoodPrice));
					}		
					
					showStaticMapDialog(strUserView, strFirst, strImage, strPickLat, strPickLong, strToAddress, strRestaurantName);
												
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
	public void pickedupRide(final String strFeedID)
	{
		// TODO Auto-generated method stub
		AsyncHttpClient client = new AsyncHttpClient();
		// Http Request Params Object
		RequestParams params = new RequestParams();
		
		String url = null;
		url = UrlGenerator.pickedupRide();
		params.add("iFeedID", strFeedID);
		
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
						if(strUserView.equals("public") == true)
						{
							Intent i_payment = new Intent(DriverPickUpActivity.this, PaymentConfirmationActivity.class);
							i_payment.putExtra("iFeedID", strFeedID);
							i_payment.putExtra("strPickLat", strPickLat);
							i_payment.putExtra("strPickLong", strPickLong);
							i_payment.putExtra("strDropLat", strDropLat);
							i_payment.putExtra("strDropLong", strDropLong);
							i_payment.putExtra("strFirst", strFirst);
							i_payment.putExtra("strLast", strLast);
							i_payment.putExtra("strImage", strImage);
							i_payment.putExtra("strPaymentType", strPaymentType);
							i_payment.putExtra("strCost", strCost);
							i_payment.putExtra("strUserPhone", strUserPhone);
							startActivity(i_payment);
							DriverPickUpActivity.this.finish();
						}
						if(strUserView.equals("courier") == true)
						{
							Intent i_payment = new Intent(DriverPickUpActivity.this, DriverDropOffActivity.class);
							i_payment.putExtra("iFeedID", strFeedID);
							i_payment.putExtra("strPickLat", strPickLat);
							i_payment.putExtra("strPickLong", strPickLong);
							i_payment.putExtra("strDropLat", strDropLat);
							i_payment.putExtra("strDropLong", strDropLong);
							i_payment.putExtra("strFirst", strFirst);
							i_payment.putExtra("strLast", strLast);
							i_payment.putExtra("strImage", strImage);
							i_payment.putExtra("strPaymentType", strPaymentType);
							i_payment.putExtra("strCost", strCost);
							i_payment.putExtra("strUserPhone", strUserPhone);
							startActivity(i_payment);
							DriverPickUpActivity.this.finish();
						}
						if(strUserView.equals("food") == true)
						{
							Intent i_payment = new Intent(DriverPickUpActivity.this, DriverDropOffActivity.class);
							i_payment.putExtra("iFeedID", strFeedID);
							i_payment.putExtra("strPickLat", strPickLat);
							i_payment.putExtra("strPickLong", strPickLong);
							i_payment.putExtra("strDropLat", strDropLat);
							i_payment.putExtra("strDropLong", strDropLong);
							i_payment.putExtra("strFirst", strFirst);
							i_payment.putExtra("strLast", strLast);
							i_payment.putExtra("strImage", strImage);
							i_payment.putExtra("strPaymentType", strPaymentType);
							i_payment.putExtra("strCost", strCost);
							i_payment.putExtra("strUserPhone", strUserPhone);
							startActivity(i_payment);
							DriverPickUpActivity.this.finish();
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
					//Toast.makeText(DriverCourierActivity.this, "Requested resource not found", Toast.LENGTH_LONG).show();
				} else if (statusCode == 500) {
					//Toast.makeText(DriverCourierActivity.this, "Something went wrong at server end", Toast.LENGTH_LONG).show();
				} else {
					networkConnectivity();
				}
			}
		});
	}
	
	private void userCancelTrip(String strFeedId) {
		// TODO Auto-generated method stub
		AsyncHttpClient client = new AsyncHttpClient();
		RequestParams params = new RequestParams();
		
		// Show ProgressBar
		final ProgressDialog pdialog = new ProgressDialog(DriverPickUpActivity.this);
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
						Intent i_destination = new Intent(DriverPickUpActivity.this, DriverActivity.class);
						startActivity(i_destination);
						DriverPickUpActivity.this.finish();
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
	
	private void showTripCancelAlert() {
		// TODO Auto-generated method stub
		mTripCancelStatus = 1;
		new AlertDialog.Builder(DriverPickUpActivity.this)
		.setMessage(getResources().getString(R.string.strTripCancel))
		.setCancelable(false)
		.setPositiveButton(getResources().getString(R.string.stryes),
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						Intent i_driver = new Intent(DriverPickUpActivity.this, DriverActivity.class);
						startActivity(i_driver);
						DriverPickUpActivity.this.finish();
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
        options.title("Distance from your location");
  		options.snippet("Approximate Distance: "+routeDistance+" time: "+routeDuration);
        myMap.addMarker(options);
        
        tv_user_address.setText(routeDuration);
        
        // End marker
        options = new MarkerOptions();
        options.position(end);
        options.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_user_marker));
        myMap.addMarker(options);
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
        new AlertDialog.Builder(DriverPickUpActivity.this)
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
	
	private void networkConnectivity()
	{
        new AlertDialog.Builder(DriverPickUpActivity.this)
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
	
	private void showStaticMapDialog(String strUserView, String strFName, final String strImage, final String strPickLat, final String strPickLong, String strToAddress, String strRestaurantName)
	{
		AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(DriverPickUpActivity.this);
		LayoutInflater inflater = getLayoutInflater();
		View dialogView = inflater.inflate(R.layout.alert_static_map_navigate, null);
		dialogBuilder.setView(dialogView);
		
		ImageView iv_user_image_alert = (ImageView) dialogView.findViewById(R.id.iv_user_image);
		ImageView iv_static_map_alert = (ImageView) dialogView.findViewById(R.id.iv_static_map);
		TextView tv_user_name_alert = (TextView) dialogView.findViewById(R.id.tv_user_name);
		TextView tv_address_alert = (TextView) dialogView.findViewById(R.id.tv_address);
		ImageButton btnNavigate = (ImageButton) dialogView.findViewById(R.id.btnNavigate);
		ImageButton btnCancel = (ImageButton) dialogView.findViewById(R.id.btnCancel);
		
		String strStaticMap = "http://maps.googleapis.com/maps/api/staticmap?center="+strPickLat+","+strPickLong+"&zoom=15&scale=2&size=250x150&maptype=roadmap&format=png&visual_refresh=true&markers=size:mid|color:0xff0000|label:|"+strPickLat+","+strPickLong;
		
		Picasso.with(DriverPickUpActivity.this).load(strImage).resize(60, 60).into(iv_user_image_alert);
		Picasso.with(DriverPickUpActivity.this).load(strStaticMap).into(iv_static_map_alert);
		
		if(strUserView.equals("food") == true) {
			tv_user_name_alert.setText("Food delivery "+strRestaurantName);
		} else {
			tv_user_name_alert.setText("Pick up "+strFName);
		}
		
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
			}
		});
	}
}
