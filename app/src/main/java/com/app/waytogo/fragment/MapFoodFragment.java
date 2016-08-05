package com.app.waytogo.fragment;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.InflateException;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.app.waytogo.ListNearestRestauratActivity;
import com.app.waytogo.R;
import com.app.waytogo.helper.GeocodingLocation;
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

import java.io.IOException;
import java.util.List;
import java.util.Locale;


public class MapFoodFragment extends Fragment implements ConnectionCallbacks,
		OnConnectionFailedListener, LocationListener, OnMapClickListener {

	private GoogleApiClient mGoogleApiClient;
	private LocationRequest mLocationRequest;
	private LocationManager mLocationManager;
	private GoogleMap myMap;
	private Location location;
	private LatLng myLatLng;
	private static View view;
	private RelativeLayout rl_destination;
	private TextView tv_to_from;
	private Button btn_pick;
	private EditText edt_address;
	private ImageView iv_search, iv_cancel;
	private Geocoder geocoder;
	private List<Address> addresses;
	private AlertDialog alertDialog;
	private double mLatitude, mLongitude;
	private SharedPreferences mySharedPreferences, myUserSharedPreferences;
	private Editor editor, editorUser;
	private String FOOD = "food_pref";
	private String USER = "user_pref";
	private int mflag = 0;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		
		if (view != null) {
			ViewGroup parent = (ViewGroup) view.getParent();
			if (parent != null)
				parent.removeView(view);
		}
		try {
			view = inflater.inflate(R.layout.fragment_map_food, container, false);
		} catch (InflateException e) {
			/* map is already there, just return view as it is */
		}
		return view;
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);

		mLocationManager = (LocationManager) getActivity().getSystemService(
				Context.LOCATION_SERVICE);

		if (!mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
			buildAlertMessageNoGps();
		}

		mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
				.addApi(LocationServices.API).addConnectionCallbacks(this)
				.addOnConnectionFailedListener(this).build();

		mySharedPreferences = getActivity().getSharedPreferences(FOOD, Context.MODE_PRIVATE);
		editor = mySharedPreferences.edit();
		myUserSharedPreferences = getActivity().getSharedPreferences(USER, Context.MODE_PRIVATE);
		editorUser = myUserSharedPreferences.edit();
		
		myMap = ((SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map_food)).getMap();
		rl_destination = (RelativeLayout) getView().findViewById(R.id.rl_destination);
		tv_to_from = (TextView) getView().findViewById(R.id.tv_to_from);
		btn_pick = (Button) getView().findViewById(R.id.btn_pick);
		edt_address = (EditText) getView().findViewById(R.id.edt_address);
		iv_search = (ImageView) getView().findViewById(R.id.iv_search);
		iv_cancel = (ImageView) getView().findViewById(R.id.iv_cancel);
		
		rl_destination.setClickable(false);
		
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
		        options.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_user_marker));
		        options.title("Current Location");
		   	    myMap.addMarker(options);
		   	    
		   	 mLatitude = myLatLng.latitude;
		   	 mLongitude = myLatLng.longitude;
		   	 
		   	getAddress(String.valueOf(mLatitude), String.valueOf(mLongitude));
	    }
	    
	    edt_address.setOnEditorActionListener(new EditText.OnEditorActionListener() {

			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				// TODO Auto-generated method stub
				if (actionId == EditorInfo.IME_ACTION_GO ||
		                event.getAction() == KeyEvent.ACTION_DOWN &&
		                event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
					
						String strAddress = edt_address.getText().toString();
					
						if(strAddress.equals("") == false)
						{
							GeocodingLocation locationAddress = new GeocodingLocation();
							locationAddress.getAddressFromLocation(strAddress, getActivity(), new GeocoderHandler());
						}
						
						InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
						imm.toggleSoftInput (InputMethodManager.SHOW_FORCED, 0);
												
		            return true;
		        }
				return false;
			}
	    });
	    
	    iv_search.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				String strAddress = edt_address.getText().toString();
				
				if(strAddress.equals("") == false)
				{
					GeocodingLocation locationAddress = new GeocodingLocation();
					locationAddress.getAddressFromLocation(strAddress, getActivity(), new GeocoderHandler());
				}
			}
		});
	    
	    iv_cancel.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				edt_address.setText("");
			}
		});
	    
	    btn_pick.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				String strAddress = edt_address.getText().toString();
				if(strAddress.equals("") == false) {
					showFoodLocationConfirmDialog(getActivity(), strAddress);
				} else {
					Toast.makeText(getActivity(), getResources().getString(R.string.strSourceLocationEmpty), Toast.LENGTH_SHORT).show();
				}
			}
		});

	}

	private void buildAlertMessageNoGps() {
		// TODO Auto-generated method stub
		new AlertDialog.Builder(getActivity())
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
						getActivity().finish();
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

	   	mLatitude = latitude;
	   	mLongitude = longitude;
	   	 
	 	getAddress(String.valueOf(mLatitude), String.valueOf(mLongitude));
	}

	@Override
	public void onLocationChanged(Location location) {
		// TODO Auto-generated method stub
		double lat = location.getLatitude();
		double lng = location.getLongitude();
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
			animateCameraTo(mLastLocation.getLatitude(), mLastLocation.getLongitude());
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

	@Override
	public void onMapClick(LatLng point) {
		// TODO Auto-generated method stub
		myMap.clear();
		myMap.animateCamera(CameraUpdateFactory.newLatLng(point));
		MarkerOptions options = new MarkerOptions();
		options.position(point);
		options.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_user_marker));
		//options.title("Current Location");
		myMap.addMarker(options);
		
	   	 mLatitude = point.latitude;
	   	 mLongitude = point.longitude;
		
		getAddress(String.valueOf(point.latitude), String.valueOf(point.longitude));
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

	private void getAddress(String lat, String lng) {
		// TODO Auto-generated method stub
		try 
		{
			geocoder = new Geocoder(getActivity(), Locale.getDefault());
			addresses = geocoder.getFromLocation(Double.valueOf(lat), Double.valueOf(lng), 1);
			 
			if(addresses.size() > 0)
			{
				myMap.clear();
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
			         
		        String strLocationAddress = address + ", " + city + ", " + state + ", " + country + ", " + postalCode;
		        edt_address.setText(strLocationAddress);
			}
		} catch (NumberFormatException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
	
	private void showFoodLocationConfirmDialog(Context c, final String strAddress)
	{
		AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
		LayoutInflater inflater = getActivity().getLayoutInflater();
		View dialogView = inflater.inflate(R.layout.alert_food_restaurant, null);
		dialogBuilder.setView(dialogView);
		
		final EditText edtRestaurant = (EditText) dialogView.findViewById(R.id.edt_search);
		Button btnDone = (Button) dialogView.findViewById(R.id.btn_done);
		
		edtRestaurant.setSelection(edtRestaurant.getText().length());
		String strRelevantName = mySharedPreferences.getString("relevant_name", "");
		if(strRelevantName.equals("") == false)
		{
			edtRestaurant.setText(strRelevantName);
		}
		
		alertDialog = dialogBuilder.create();
		alertDialog.show();
		
		btnDone.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
								
				String strName = edtRestaurant.getText().toString();
				editor.putString("relevant_name", strName);
				editor.commit();
				
				String strCountryCode = myUserSharedPreferences.getString("country_code", "");
				if(strCountryCode.equals("SE") == false) {
					Intent i_food = new Intent(getActivity(), ListNearestRestauratActivity.class);
					i_food.putExtra("currentLat", String.valueOf(mLatitude));
					i_food.putExtra("currentLng", String.valueOf(mLongitude));
					startActivity(i_food);
				} else {
					Toast.makeText(getActivity(), getResources().getString(R.string.strnofood), Toast.LENGTH_LONG).show();
				}
				
				alertDialog.dismiss();
			}
		});
	}
   
}
