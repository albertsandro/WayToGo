package com.app.waytogo.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.InflateException;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.app.waytogo.DestinationActivity;
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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Locale;


@SuppressWarnings("deprecation")
public class MapCourierFragment extends Fragment implements ConnectionCallbacks,
		OnConnectionFailedListener, LocationListener, OnMapClickListener, Callback {

	private GoogleApiClient mGoogleApiClient;
	private LocationRequest mLocationRequest;
	private LocationManager mLocationManager;
	private GoogleMap myMap;
	private Location location;
	private LatLng myLatLng;
	private static View view;
	private TextView tv_pick_from;
	private Button btn_pick, btn_confirm_photo, btn_retake_photo;
	private EditText edt_address, edt_weight;
	private ImageView iv_search, iv_cancel;
	private Geocoder geocoder;
	private List<Address> addresses;
	private RelativeLayout rl_destination, rl_courier_photo, rl_confirm;
	private AlertDialog alertDialog;
	private double mLatitude, mLongitude;
	private SharedPreferences mySharedPreferences;
	private Editor editor;
	private String USER = "user_pref";
	private SurfaceView sv_surface;
	private SurfaceHolder surfaceHolder;
	private Camera camera;
	private Bitmap InputImages;
	private ImageView iv_takephoto;
	private PictureCallback jpegCallback;
	private String strAddress, strToken, strWeight, strCourierPicture;
	private Bitmap bm;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// return inflater.inflate(R.layout.fragment_map_ride, container,
		// false);
		if (view != null) {
			ViewGroup parent = (ViewGroup) view.getParent();
			if (parent != null)
				parent.removeView(view);
		}
		try {
			view = inflater.inflate(R.layout.fragment_map_courier, container,
					false);
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

		mySharedPreferences = getActivity().getSharedPreferences(USER, Context.MODE_PRIVATE);
		editor = mySharedPreferences.edit();
		
		myMap = ((SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map_courier)).getMap();
		rl_destination = (RelativeLayout) getView().findViewById(R.id.rl_destination);
		rl_courier_photo = (RelativeLayout) getView().findViewById(R.id.rl_courier_photo);
		rl_confirm = (RelativeLayout) getView().findViewById(R.id.rl_confirm);
		tv_pick_from = (TextView) getView().findViewById(R.id.tv_pick_from);
		btn_pick = (Button) getView().findViewById(R.id.btn_pick);
		edt_address = (EditText) getView().findViewById(R.id.edt_address);
		iv_search = (ImageView) getView().findViewById(R.id.iv_search);
		iv_cancel = (ImageView) getView().findViewById(R.id.iv_cancel);
		iv_takephoto = (ImageView) getView().findViewById(R.id.iv_takephoto);
		btn_confirm_photo = (Button) getView().findViewById(R.id.btn_confirm_photo);
		btn_retake_photo = (Button) getView().findViewById(R.id.btn_retake_photo);
		edt_weight = (EditText) getView().findViewById(R.id.edt_weight);
		sv_surface = (SurfaceView) getView().findViewById(R.id.surface);
		surfaceHolder = sv_surface.getHolder();
        surfaceHolder.addCallback(this);
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		
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
	    
	    iv_search.setOnClickListener(new OnClickListener() {
			
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
	    
	    iv_cancel.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				edt_address.setText("");
			}
		});
	    
	    btn_pick.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				String strAddress = edt_address.getText().toString();
				if(strAddress.equals("") == false) {
					rl_destination.setVisibility(View.GONE);
					rl_courier_photo.setVisibility(View.VISIBLE);
					rl_confirm.setVisibility(View.GONE);
				} else {
					Toast.makeText(getActivity(), getResources().getString(R.string.strSourceLocationEmpty), Toast.LENGTH_SHORT).show();
				}
			}
		});
	    
	    iv_takephoto.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(camera != null) {
					
					camera.takePicture(null, null, jpegCallback);
			    } else {
			    	camera = Camera.open();
			    	camera.takePicture(null, null, jpegCallback);
			    }
				
			}
		});
	    
	    btn_confirm_photo.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
				rl_destination.setVisibility(View.GONE);
				rl_courier_photo.setVisibility(View.GONE);
				rl_confirm.setVisibility(View.GONE);
				alertProductWeight();
			}
		});
	    
	    btn_retake_photo.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
				rl_destination.setVisibility(View.GONE);
				rl_courier_photo.setVisibility(View.VISIBLE);
				rl_confirm.setVisibility(View.GONE);
			}
		});
	    
	    jpegCallback = new PictureCallback() {
	        public void onPictureTaken(byte[] data, Camera camera) {
	        	
	            FileOutputStream outStream = null;
	            try {
	            	
	            	File folderPath = new File(Environment.getExternalStorageDirectory() + "/Rider/Courier");
					if (!folderPath.exists()) {
						folderPath.mkdirs();
					}
					File photofile = new File(folderPath, (System.currentTimeMillis())+ ".jpg");
					strCourierPicture = photofile.getAbsolutePath();
					outStream = new FileOutputStream(photofile);
					
					int angleToRotate = getRoatationAngle(getActivity(), Camera.CameraInfo.CAMERA_FACING_BACK);
			        // Solve image inverting problem
			        angleToRotate = angleToRotate + 0;
			        
					BitmapFactory.Options opts = new BitmapFactory.Options();               
					opts.inPreferredConfig = Bitmap.Config.ARGB_8888;
					Bitmap pict = BitmapFactory.decodeByteArray(data, 0, data.length, opts);
					Bitmap bitmapImage = rotate(pict, angleToRotate);
					
					ByteArrayOutputStream stream = new ByteArrayOutputStream();
					bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, stream);
					byte[] byteArray = stream.toByteArray();
					
	                outStream.write(byteArray);
	                outStream.close();
	                
	                rl_destination.setVisibility(View.GONE);
	                rl_courier_photo.setVisibility(View.GONE);
	                rl_confirm.setVisibility(View.VISIBLE);
	                
	            } catch (FileNotFoundException e) {
	                e.printStackTrace();
	            } catch (IOException e) {
	                e.printStackTrace();
	            } finally {
	            }
	            	            
		    	refreshCamera();
	        }
	    };
	}
	
	public static int getRoatationAngle(Activity mContext, int cameraId) {
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, info);
        int rotation = mContext.getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;
        switch (rotation) {
        case Surface.ROTATION_0:
            degrees = 0;
            break;
        case Surface.ROTATION_90:
            degrees = 90;
            break;
        case Surface.ROTATION_180:
            degrees = 180;
            break;
        case Surface.ROTATION_270:
            degrees = 270;
            break;
        }
        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360; // compensate the mirror
        } else { // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        return result;
    }
		
	public static Bitmap rotate(Bitmap bitmap, int degree) {
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();

        Matrix mtx = new Matrix();
        mtx.postRotate(degree);

        return Bitmap.createBitmap(bitmap, 0, 0, w, h, mtx, true);
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
	
	private void alertProductWeight()
	{
		AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
		LayoutInflater inflater = getActivity().getLayoutInflater();
		View dialogView = inflater.inflate(R.layout.alert_product_weight, null);
		dialogBuilder.setView(dialogView);
		
		final EditText edt_weight = (EditText) dialogView.findViewById(R.id.edt_weight);
		ImageButton btnOk = (ImageButton) dialogView.findViewById(R.id.btnOk);
		ImageButton btnCancel = (ImageButton) dialogView.findViewById(R.id.btnCancel);
		
		alertDialog = dialogBuilder.create();
		alertDialog.show();
		
		btnOk.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				strToken = mySharedPreferences.getString("strToken", "");
				strWeight = edt_weight.getText().toString();
				strAddress = edt_address.getText().toString();
				if(strToken.equals("") == true)
				{
					//Redirect to register with credit card
					registerCrediCard();
				}
				else
				{
					if(strWeight.equals("") == false)
					{
						try {
							Intent i_destination = new Intent(getActivity(), DestinationActivity.class);
							i_destination.putExtra("strToken", strToken);
							i_destination.putExtra("strWeight", strWeight);
							i_destination.putExtra("strCourierPicture", strCourierPicture);
							i_destination.putExtra("strCourierSelected", "true");
							i_destination.putExtra("strAddress", strAddress);
							i_destination.putExtra("strLatitude", mLatitude);
							i_destination.putExtra("strLongitude", mLongitude);
							
							getActivity().startActivity(i_destination);
							alertDialog.dismiss();
						} catch(Exception e) {e.printStackTrace();}
					}
					else
					{
						Toast.makeText(getActivity(), "Please enter the weight of your item before proceeding", Toast.LENGTH_SHORT).show();
					}
				}
			}
		});
		
		btnCancel.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				alertDialog.dismiss();
				rl_destination.setVisibility(View.GONE);
				rl_courier_photo.setVisibility(View.VISIBLE);
				rl_confirm.setVisibility(View.GONE);
			}
		});
	}

	private void registerCrediCard() {
		// TODO Auto-generated method stub
		new AlertDialog.Builder(getActivity())
		.setMessage(getResources().getString(R.string.strcredicard))
		.setCancelable(false)
		.setPositiveButton(getResources().getString(R.string.strgotit),
			new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
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
		// getLocation();
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
				LatLng newLatLng = new LatLng(Double.valueOf(lat), Double.valueOf(lng)); 
				MarkerOptions options = new MarkerOptions();
			 		options.position(newLatLng);
			        options.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_user_marker));
			        myMap.addMarker(options);
			        //myMap.animateCamera(CameraUpdateFactory.newLatLngZoom(newLatLng, 15.0f));
				 
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
	
	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		// TODO Auto-generated method stub
		if (camera != null) {
	        Camera.Parameters params = camera.getParameters();
	        List<Camera.Size> sizes = params.getSupportedPreviewSizes();
	        Camera.Size selected = sizes.get(0);
	        //params.setPreviewSize(selected.width, selected.height);
	        params.set("jpeg-quality", 70);
	        params.setPictureFormat(PixelFormat.JPEG);
	        params.setPictureSize(640, 480);
	        camera.setParameters(params);
	        camera.setDisplayOrientation(90);


	        try {
	            camera.setPreviewDisplay(holder);
	        } catch (IOException e) {
	            e.printStackTrace();
	        }

	        camera.startPreview();
	    }
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		// TODO Auto-generated method stub
		camera = Camera.open();
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		// TODO Auto-generated method stub
		camera.stopPreview();
	    camera.release();
	    camera = null;
	}
	
	public void refreshCamera() {
		if (surfaceHolder.getSurface() == null) {
			return;
		}
		try {
			camera.stopPreview();
		} catch (Exception e) {
			
		}
		try {
			camera.setPreviewDisplay(surfaceHolder);
			camera.startPreview();
		} catch (Exception e) {

		}
	}

	
	@Override
	public void onDestroy() {
	    super.onDestroy();
	    if(camera != null) {
	    	camera.release();
	    }
	}

	@Override
	public void onPause() {
	    super.onPause();
	    if(camera != null) {
	    	camera.stopPreview();
	    }
	}
}