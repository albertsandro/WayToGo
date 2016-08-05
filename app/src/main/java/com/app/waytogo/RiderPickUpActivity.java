package com.app.waytogo;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
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
import android.text.Html;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

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

import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class RiderPickUpActivity extends AppCompatActivity implements ConnectionCallbacks, OnConnectionFailedListener, LocationListener, RoutingListener {

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
	private TextView tv_address_onway, tv_address_onroute, tv_driver_name, tv_car_number, tv_car_type, tv_time;
	private ImageView iv_address_onway, iv_address_onroute, iv_car;
	private CircularImageView iv_driver;
	private LinearLayout ll_slide;
	private TextView tv_drivers_eta, tv_call_driver, tv_text_driver, tv_cancel_trip;
	private ImageButton btn_arrow_up, btn_arrow_down;
	private String feedID, fLat, fLong, toLat, toLong, currentLat, currentLong, toAddress, fromAddress;
	private Routing routing;
    protected LatLng start;
    protected LatLng end;
    private String str_eta_name, str_eta_car_type, str_eta_car_plate, str_eta_dist, str_eta_driver_phone;
    private Timer timer;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_rider_pickup);
		
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		
		mToolbar = (Toolbar) findViewById(R.id.toolbar);

		cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		
		setSupportActionBar(mToolbar);
		getSupportActionBar().setDisplayShowHomeEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		//getSupportActionBar().setTitle(strSourceAddress);
		
		mySharedPreferences = getSharedPreferences(USER, MODE_PRIVATE);
		editor = mySharedPreferences.edit();
		
		try {
			Bundle b = getIntent().getExtras();
			feedID = b.getString("strFeedId");
			fLat = b.getString("fLat");
			fLong = b.getString("fLong");
			toLat = b.getString("toLat");
			toLong = b.getString("toLong");
			currentLat = b.getString("currentLat");
			currentLong = b.getString("currentLong");
			
			start = new LatLng(Double.valueOf(fLat), Double.valueOf(fLong));
	        end = new LatLng(Double.valueOf(toLat), Double.valueOf(toLong));
			
		} catch(NullPointerException e) {e.printStackTrace();}
		
		tv_address_onway = (TextView) findViewById(R.id.tv_address_onway);
		tv_address_onroute = (TextView) findViewById(R.id.tv_address_onroute);
		tv_driver_name = (TextView) findViewById(R.id.tv_driver_name);
		tv_car_number = (TextView) findViewById(R.id.tv_car_number);
		tv_car_type = (TextView) findViewById(R.id.tv_car_type);
		tv_time = (TextView) findViewById(R.id.tv_time);
		iv_address_onway = (ImageView) findViewById(R.id.iv_address_onway);
		iv_address_onroute = (ImageView) findViewById(R.id.iv_address_onroute);
		iv_driver = (CircularImageView) findViewById(R.id.iv_driver);
		iv_car = (ImageView) findViewById(R.id.iv_car);
		btn_arrow_up = (ImageButton) findViewById(R.id.btn_arrow_up);
		btn_arrow_down = (ImageButton) findViewById(R.id.btn_arrow_down);
		ll_slide = (LinearLayout) findViewById(R.id.ll_slide);
		tv_drivers_eta = (TextView) findViewById(R.id.tv_drivers_eta);
		tv_call_driver = (TextView) findViewById(R.id.tv_call_driver);
		tv_text_driver = (TextView) findViewById(R.id.tv_text_driver);
		tv_cancel_trip = (TextView) findViewById(R.id.tv_cancel_trip);
		
		mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

		if (!mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
			buildAlertMessageNoGps();
		}

		mGoogleApiClient = new GoogleApiClient.Builder(this)
				.addApi(LocationServices.API).addConnectionCallbacks(this)
				.addOnConnectionFailedListener(this).build();

		myMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map_rider_pickup)).getMap();
				
		myMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
		myMap.getUiSettings().setZoomControlsEnabled(false);
		myMap.setMyLocationEnabled(true);
		
		location = myMap.getMyLocation();

	    if (location != null) {
	    	myLatLng = new LatLng(location.getLatitude(), location.getLongitude());
	    	myMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myLatLng, 15));
	    	MarkerOptions options = new MarkerOptions();
		 	    options.position(myLatLng);
		        options.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_user_marker));
		        options.title("Current Location");
		   	    myMap.addMarker(options);
		   	    
		   	mDestinationLatitude = location.getLatitude();
		   	mDestinationLongitude = location.getLongitude();
	    }
	    fromAddress = getAddress(fLat, fLong);
	    toAddress = getAddress(toLat, toLong);
	    
	    tv_address_onway.setText(fromAddress);
	    tv_address_onroute.setText(toAddress);
	    
	    timer = new Timer();
	    timer.schedule(new TimerTask() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				RiderPickUpActivity.this.runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						// TODO Auto-generated method stub
						if (cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isAvailable() && cm.getActiveNetworkInfo().isConnected()) {
							getFeed(feedID);
						} else {
							networkConnectivity();
						}
					}
				});
			}
	    	
	    },0,5000);
	    	    
	    routing = new Routing(Routing.TravelMode.DRIVING);
        routing.registerListener(this);
            
        btn_arrow_up.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				btn_arrow_up.setVisibility(View.GONE);
				btn_arrow_down.setVisibility(View.VISIBLE);
				ll_slide.setVisibility(View.VISIBLE);
				/*ll_slide.animate()
					.alpha(1f)
	                .translationYBy(120)
	                .translationY(0)     
	                .setDuration(getResources().getInteger(android.R.integer.config_mediumAnimTime));*/
			}
		});
        
        
        btn_arrow_down.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				btn_arrow_up.setVisibility(View.VISIBLE);
				btn_arrow_down.setVisibility(View.GONE);
				ll_slide.setVisibility(View.GONE);
				/*ll_slide.animate()
					.alpha(0f)
	                .translationYBy(0)
	                .translationY(120)
	                .setDuration(getResources().getInteger(android.R.integer.config_mediumAnimTime));*/
			}
		});
        
        tv_drivers_eta.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				showDriverInfo(str_eta_name, str_eta_car_type, str_eta_car_plate, str_eta_dist);
			}
		});
        
        tv_call_driver.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + str_eta_driver_phone));
				startActivity(intent);
			}
		});
        
        tv_text_driver.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
			    startActivity(new Intent(Intent.ACTION_VIEW, Uri.fromParts("sms", str_eta_driver_phone, null)));  
			}
		});
        
        tv_cancel_trip.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isAvailable() && cm.getActiveNetworkInfo().isConnected()) {
					cancelTrip(feedID);
					updateBalance();
				} else {
					networkConnectivity();
				}
			}
		});
       /* 
        RiderPickUpActivity.this.runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
		        try {
		            LatLngBounds bounds = new LatLngBounds.Builder()
			        .include(start)
			        .include(end)
			        .build();
			
		            myMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100));
					
					routing.execute(start, end);
				}catch(Exception e) {e.printStackTrace();}
			}
		});*/
        
        
	}

	private void cancelTrip(String strFeedId) {
		// TODO Auto-generated method stub
		AsyncHttpClient client = new AsyncHttpClient();
		RequestParams params = new RequestParams();
		
		// Show ProgressBar
		final ProgressDialog pdialog = new ProgressDialog(RiderPickUpActivity.this);
		pdialog.setMessage("Cancel Trip...");
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
			}

			// When error occured
			@Override
			public void onFailure(int statusCode, Throwable error, String content) {
				// TODO Auto-generated method stub
				// Hide ProgressBar
				pdialog.hide();
				if (statusCode == 404) {
					//Toast.makeText(RiderPickUpActivity.this, "Requested resource not found", Toast.LENGTH_LONG).show();
				} else if (statusCode == 500) {
					//Toast.makeText(RiderPickUpActivity.this, "Something went wrong at server end", Toast.LENGTH_LONG).show();
				} else {
					networkConnectivity();
				}
			}
		});
	}
	
	private void updateBalance() {
		// TODO Auto-generated method stub
		AsyncHttpClient client = new AsyncHttpClient();
		RequestParams params = new RequestParams();
		
		String url = null;
		url = UrlGenerator.updateBalance();
		
		params.add("iUserID", mySharedPreferences.getString("strUserId", ""));
		params.add("balance", "-5.00");
						
		client.get(url, params, new AsyncHttpResponseHandler() {

			@Override
			public void onSuccess(String response) {
				// Hide ProgressBar
				JSONObject jObject;
				try {
					jObject = new JSONObject(response);
					
					String strStatus = jObject.optString("status");
					 
					if(strStatus.equals("0") == true) {
						try {
							
							Intent i_cancel = new Intent(RiderPickUpActivity.this, MainActivity.class);
							startActivity(i_cancel);
							RiderPickUpActivity.this.finish();
						} catch(Exception e) {}
					}
				} catch (Exception e) {e.printStackTrace();}
			}

			// When error occured
			@Override
			public void onFailure(int statusCode, Throwable error, String content) {
				// TODO Auto-generated method stub
				// Hide ProgressBar
				if (statusCode == 404) {
					//Toast.makeText(RiderPickUpActivity.this, "Requested resource not found", Toast.LENGTH_LONG).show();
				} else if (statusCode == 500) {
					//Toast.makeText(RiderPickUpActivity.this, "Something went wrong at server end", Toast.LENGTH_LONG).show();
				} else {
					networkConnectivity();
				}
			}
		});
	}
	
	private void getFeed(final String strFeedId) {
		// TODO Auto-generated method stub
		AsyncHttpClient client = new AsyncHttpClient();
		RequestParams params = new RequestParams();
		
		String url = null;
		url = UrlGenerator.getFeed();
		
		params.add("iFeedID", strFeedId);
						
		client.get(url, params, new AsyncHttpResponseHandler() {

			@Override
			public void onSuccess(String response) {
				// Hide ProgressBar
				JSONObject jObject;
				try {
					jObject = new JSONObject(response);
					String strUserView = jObject.optString("vUserView");
					String strDriverID = jObject.optString("iDriverID");
					String strCost = jObject.optString("vCost");
					String strPayment = jObject.optString("vPayment");
					
					getDriverInfo(strFeedId, strDriverID, strUserView, strCost, strPayment);
					
				} catch (Exception e) {e.printStackTrace();}
			}

			// When error occured
			@Override
			public void onFailure(int statusCode, Throwable error, String content) {
				// TODO Auto-generated method stub
				// Hide ProgressBar
				if (statusCode == 404) {
					//Toast.makeText(RiderPickUpActivity.this, "Requested resource not found", Toast.LENGTH_LONG).show();
				} else if (statusCode == 500) {
					//Toast.makeText(RiderPickUpActivity.this, "Something went wrong at server end", Toast.LENGTH_LONG).show();
				} else {
					networkConnectivity();
				}
			}
		});
	}
	
	private void getDriverInfo(final String strFeedID, String strDriverID, final String strUserView, final String strCost, final String strPayment) {
		// TODO Auto-generated method stub
		AsyncHttpClient client = new AsyncHttpClient();
		RequestParams params = new RequestParams();
		
		String url = null;
		url = UrlGenerator.getDriver();
		
		params.add("iDriverID", strDriverID);
						
		client.get(url, params, new AsyncHttpResponseHandler() {

			@Override
			public void onSuccess(String response) {
				// Hide ProgressBar
				JSONObject jObject;
				try {
					jObject = new JSONObject(response);
					
					String strFirst = jObject.optString("vFirst");
					String strLast = jObject.optString("vLast");
					String strDriverLicensePlate = jObject.optString("driverLicensePlate");
					String strCarType = jObject.optString("vCarType");
					String strImage = jObject.optString("vImage");
					String strDriverPhone = jObject.optString("iDriverPhone");
										
					tv_driver_name.setText(strFirst+" "+strLast);
					tv_driver_name.toString().toUpperCase();
					tv_car_number.setText(strDriverLicensePlate);
					tv_car_type.setText(strCarType);
					
					str_eta_name = strFirst+" "+strLast;
					str_eta_car_type = strCarType;
					str_eta_car_plate = strDriverLicensePlate;
					str_eta_driver_phone = strDriverPhone;
					
					if(strUserView.equals("friends") == true) {
						getSupportActionBar().setTitle(strFirst+" "+strLast+" is on the way");
						iv_address_onway.setBackgroundResource(R.drawable.ic_address_active);
						iv_address_onroute.setBackgroundResource(R.drawable.ic_address_normal);
					}
					if(strUserView.equals("pickedup") == true) {
						getSupportActionBar().setTitle("On Route");
						iv_address_onway.setBackgroundResource(R.drawable.ic_address_normal);
						iv_address_onroute.setBackgroundResource(R.drawable.ic_address_inactive);
					}
					if(strUserView.equals("complete") == true) 	{
						Intent i_receipt = new Intent(RiderPickUpActivity.this, ReceiptActivity.class);
						i_receipt.putExtra("strFeedID", strFeedID);
						i_receipt.putExtra("strCost", strCost);
						i_receipt.putExtra("strImage", strImage);
						i_receipt.putExtra("strPayment", strPayment);
						startActivity(i_receipt);
						timer.cancel();
						RiderPickUpActivity.this.finish();
					} 
					if(strUserView.equals("delete") == true) 	{
						showTripCancelAlert();
					}
					Picasso.with(RiderPickUpActivity.this).load(getResources().getString(R.string.strDriverThumbImageUrl)+""+strImage).error(R.drawable.ic_profile_pic).centerCrop().resize(60, 60).into(iv_driver);
					
					RiderPickUpActivity.this.runOnUiThread(new Runnable() {
						
						@Override
						public void run() {
							// TODO Auto-generated method stub
					        try {
					            LatLngBounds bounds = new LatLngBounds.Builder()
						        .include(start)
						        .include(end)
						        .build();
						
					            myMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100));
								
								routing.execute(start, end);
							}catch(Exception e) {e.printStackTrace();}
						}
					});
					
				} catch (Exception e) {e.printStackTrace();}
			}

			// When error occured
			@Override
			public void onFailure(int statusCode, Throwable error, String content) {
				// TODO Auto-generated method stub
				// Hide ProgressBar
				if (statusCode == 404) {
					Toast.makeText(RiderPickUpActivity.this, "Requested resource not found", Toast.LENGTH_LONG).show();
				} else if (statusCode == 500) {
					Toast.makeText(RiderPickUpActivity.this, "Something went wrong at server end", Toast.LENGTH_LONG).show();
				} else {
					//networkConnectivity();
				}
			}
		});
	}
	
	private void showTripCancelAlert() {
		// TODO Auto-generated method stub
		new AlertDialog.Builder(RiderPickUpActivity.this)
		.setMessage(getResources().getString(R.string.strTripCancel))
		.setCancelable(false)
		.setPositiveButton(getResources().getString(R.string.strok),
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						Intent i_main = new Intent(RiderPickUpActivity.this, MainActivity.class);
						i_main.putExtra("page", 0);
						startActivity(i_main);
						timer.cancel();
						RiderPickUpActivity.this.finish();
					}
				})
		.show();
	}
	
	private void buildAlertMessageNoGps() {
		// TODO Auto-generated method stub
		new AlertDialog.Builder(RiderPickUpActivity.this)
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
						RiderPickUpActivity.this.finish();
					}
				}).show();
	}

	@Override
	public void onConnectionFailed(ConnectionResult arg0) {
		// TODO Auto-generated method stub
		
	}

	private void animateCameraTo(double latitude, double longitude) {
		// TODO Auto-generated method stub
		myMap.clear();
		LatLng markerLatLng = new LatLng(latitude, longitude);
				
		myMap.animateCamera(CameraUpdateFactory.newLatLngZoom(markerLatLng, 15));
    	MarkerOptions options = new MarkerOptions();
	 	    options.position(markerLatLng);
	        options.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_user_marker));
	        options.title("Current Location");
	   	    myMap.addMarker(options);
	   	    
		   	mDestinationLatitude = latitude;
		   	mDestinationLongitude = longitude;
		   	
		   	try {
	            LatLngBounds bounds = new LatLngBounds.Builder()
		        .include(start)
		        .include(end)
		        .build();
		
	            myMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100));
				
				routing.execute(start, end);
			}catch(Exception e) {e.printStackTrace();}
	}

	@Override
	public void onLocationChanged(Location location) {
		// TODO Auto-generated method stub
		double lat = location.getLatitude();
		double lng = location.getLongitude();

		animateCameraTo(lat, lng);
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
			geocoder = new Geocoder(RiderPickUpActivity.this, Locale.getDefault());
			addresses = geocoder.getFromLocation(Double.valueOf(lat), Double.valueOf(lng), 1);
			 
			if(addresses.size() > 0)
			{
				LatLng newLatLng = new LatLng(Double.valueOf(lat), Double.valueOf(lng)); 
				MarkerOptions options = new MarkerOptions();
			 		options.position(newLatLng);
			        options.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_user_marker));
			        myMap.addMarker(options);
			        myMap.animateCamera(CameraUpdateFactory.newLatLngZoom(newLatLng, 15.0f));
				 
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
		tv_time.setText(route.getDurationText());
		str_eta_dist = route.getDistanceText();
		
		PolylineOptions polyOptions = new PolylineOptions();
        polyOptions.color(Color.BLUE);
        polyOptions.width(10);
        polyOptions.addAll(mPolyOptions.getPoints());
        myMap.addPolyline(polyOptions);

        // Start marker
        MarkerOptions options = new MarkerOptions();
        options.position(start);
        options.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_driver_marker));
        myMap.addMarker(options);
        
        // End marker
        options = new MarkerOptions();
        options.position(end);
        options.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_user_marker));
        myMap.addMarker(options);
	}
	
	private void showDriverInfo(String strName, String strCarType, String strCarPlate, String strMiles) {
		// TODO Auto-generated method stub
		
		Resources res = getResources();
		String textTitle = String.format(res.getString(R.string.strdriveretatitle), strMiles);
		String textMessage = String.format(res.getString(R.string.strdriveretamessage), strName, strCarType, strCarPlate);
		CharSequence str_driver_title = Html.fromHtml(textTitle);
		CharSequence str_driver_message = Html.fromHtml(textMessage);
		
		new AlertDialog.Builder(RiderPickUpActivity.this)
		.setTitle(str_driver_title)
		.setMessage(str_driver_message)
		.setCancelable(false)
		.setPositiveButton(getResources().getString(R.string.strok),
			new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			})
		.show();
	}

	private void networkConnectivity()
	{
        new AlertDialog.Builder(RiderPickUpActivity.this)
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
	
	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
	}
	
}
