package com.app.waytogo;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Paint;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.app.waytogo.adapter.DestinationListAdapter;
import com.app.waytogo.helper.GeocodingLocation;
import com.app.waytogo.helper.RiderDestinationHelper;
import com.app.waytogo.model.DestinationLocationDetails;
import com.app.waytogo.model.DestinationPlaceDetails;
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

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.math.RoundingMode;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;


public class DestinationActivity extends AppCompatActivity implements ConnectionCallbacks, OnConnectionFailedListener, OnMapClickListener, LocationListener, RiderDestinationHelper.RiderDestinationInterface {

	private Toolbar mToolbar;
	private ConnectivityManager cm;
	private GoogleApiClient mGoogleApiClient;
	private LocationRequest mLocationRequest;
	private LocationManager mLocationManager;
	private GoogleMap myMap;
	private Location location;
	private TextView tv_to_from, tv_trip_cost, tv_uber_cost, tv_uber_label;
	private Button btn_destination;
	private ImageButton btn_paypal, btn_cash;
	private EditText edt_address;
	private ImageView iv_search, iv_cancel;
	private Geocoder geocoder;
	private List<Address> addresses;
	private RelativeLayout rl_destination, rl_trip, rl_destination_list, rl_payment;
	private String strSourceAddress;
	private double mSourceLatitude, mSourceLongitude, mDestinationLatitude, mDestinationLongitude;
	private RiderDestinationHelper mRiderDestinationHelper;
	private ListView lv_destination_list;
	private DestinationListAdapter dla;
	private Location mSourceLocation, mDestinationLocation;
	private double price, ubersprice;
	private String currentCountry, isShowPrice = "0";
	private static String isMapTouched = "0";
	private SharedPreferences mySharedPreferences;
	private Editor editor;
	private String USER = "user_pref";
	private String strFeedId, strUserView, strCurrentLat, strCurrentLong;
	private Timer timer, timerWaitForDriver, timerGrabbingDriverLocation;
	private DecimalFormat df = new DecimalFormat("#.######");
	private DecimalFormat df_cost = new DecimalFormat("#.##");
	private String strToken, strWeight, strCourierSelected = "false", strCourierPicture;
	private ProgressDialog pdialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_destination);

		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

		try {
			Bundle b = getIntent().getExtras();

			mSourceLocation = new Location("");
			//Rider module
			strSourceAddress = b.getString("strAddress");
			mSourceLatitude = b.getDouble("strLatitude");
			mSourceLongitude = b.getDouble("strLongitude");
			//Courier module
			strToken = b.getString("strToken");
			strWeight = b.getString("strWeight");
			strCourierPicture = b.getString("strCourierPicture");
			strCourierSelected = b.getString("strCourierSelected");

			mSourceLocation.setLatitude(mSourceLatitude);
			mSourceLocation.setLongitude(mSourceLongitude);

		} catch (NullPointerException e) {
			e.printStackTrace();
		}

		mToolbar = (Toolbar) findViewById(R.id.toolbar);

		setSupportActionBar(mToolbar);
		getSupportActionBar().setDisplayShowHomeEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setTitle(strSourceAddress);

		mySharedPreferences = getSharedPreferences(USER, MODE_PRIVATE);
		editor = mySharedPreferences.edit();

		mRiderDestinationHelper = new RiderDestinationHelper();
		mRiderDestinationHelper.setRiderDestinationHelper(DestinationActivity.this);

		mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

		if (!mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
			buildAlertMessageNoGps();
		}

		mGoogleApiClient = new GoogleApiClient.Builder(this)
				.addApi(LocationServices.API).addConnectionCallbacks(this)
				.addOnConnectionFailedListener(this).build();

		myMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map_destination)).getMap();
		rl_destination = (RelativeLayout) findViewById(R.id.rl_destination);
		rl_destination_list = (RelativeLayout) findViewById(R.id.rl_destination_list);
		rl_payment = (RelativeLayout) findViewById(R.id.rl_payment);
		rl_trip = (RelativeLayout) findViewById(R.id.rl_trip);
		lv_destination_list = (ListView) findViewById(R.id.lv_destination_list);
		tv_to_from = (TextView) findViewById(R.id.tv_to_from);
		tv_trip_cost = (TextView) findViewById(R.id.tv_trip_cost);
		tv_uber_cost = (TextView) findViewById(R.id.tv_uber_cost);
		tv_uber_label = (TextView) findViewById(R.id.tv_uber_label);
		btn_destination = (Button) findViewById(R.id.btn_destination);
		btn_paypal = (ImageButton) findViewById(R.id.btn_paypal);
		btn_cash = (ImageButton) findViewById(R.id.btn_cash);
		edt_address = (EditText) findViewById(R.id.edt_address);
		iv_search = (ImageView) findViewById(R.id.iv_search);
		iv_cancel = (ImageView) findViewById(R.id.iv_cancel);

		rl_destination.setClickable(false);

		myMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
		myMap.getUiSettings().setZoomControlsEnabled(false);
		if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
			// TODO: Consider calling
			//    ActivityCompat#requestPermissions
			// here to request the missing permissions, and then overriding
			//   public void onRequestPermissionsResult(int requestCode, String[] permissions,
			//                                          int[] grantResults)
			// to handle the case where the user grants the permission. See the documentation
			// for ActivityCompat#requestPermissions for more details.
			return;
		}
		if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
			// TODO: Consider calling
			//    ActivityCompat#requestPermissions
			// here to request the missing permissions, and then overriding
			//   public void onRequestPermissionsResult(int requestCode, String[] permissions,
			//                                          int[] grantResults)
			// to handle the case where the user grants the permission. See the documentation
			// for ActivityCompat#requestPermissions for more details.
			return;
		}
		myMap.setMyLocationEnabled(true);
		myMap.setOnMapClickListener(this);

		location = myMap.getMyLocation();

		edt_address.setOnEditorActionListener(new EditText.OnEditorActionListener() {

			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				// TODO Auto-generated method stub
				if (actionId == EditorInfo.IME_ACTION_GO ||
						event.getAction() == KeyEvent.ACTION_DOWN &&
								event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {

					String strAddress = edt_address.getText().toString();

					if (strAddress.equals("") == false) {
							/*GeocodingLocation locationAddress = new GeocodingLocation();
							locationAddress.getAddressFromLocation(strAddress, DestinationActivity.this, new GeocoderHandler());*/

						mRiderDestinationHelper.fetchDestination(DestinationActivity.this, strAddress);
					}

					InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
					imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);

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

				if (strAddress.equals("") == false) {
					/*GeocodingLocation locationAddress = new GeocodingLocation();
					locationAddress.getAddressFromLocation(strAddress, DestinationActivity.this, new GeocoderHandler());*/

					mRiderDestinationHelper.fetchDestination(DestinationActivity.this, strAddress);
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

		btn_destination.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (isMapTouched.trim().equals("1") == true) {

					if (isShowPrice == "1") {
						rl_payment.setVisibility(View.VISIBLE);
					} else {
						showSameLocationAlert();
					}

				} else {
					showSameLocationAlert();
				}
			}
		});

		btn_paypal.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				rl_payment.setVisibility(View.VISIBLE);

				if (cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isAvailable() && cm.getActiveNetworkInfo().isConnected()) {

					if (strCourierSelected.equals("true") == true) {
						//Courier trip
						String strToken = mySharedPreferences.getString("strToken", "");
						if (strToken.equals("") == true) {
							//Redirect to register CC
							rl_payment.setVisibility(View.GONE);
							NoCrediCardRegistered();
						} else {
							rl_payment.setVisibility(View.GONE);
							new UploadCourierImage(strCourierPicture).execute();
						}
					} else {
						rl_payment.setVisibility(View.GONE);
						RequestTrip(price, "Paypal", "ride", "");
					}
				} else {
					networkConnectivity();
				}
			}
		});

		btn_cash.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				rl_payment.setVisibility(View.VISIBLE);
				if (cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isAvailable() && cm.getActiveNetworkInfo().isConnected()) {
					if (strCourierSelected.equals("true") == true) {
						//Courier trip
						showCourierCashAlert();
					} else {
						rl_payment.setVisibility(View.GONE);
						RequestTrip(price, "Cash", "ride", "");
					}
				} else {
					networkConnectivity();
				}
			}
		});
	}

	protected void NoCrediCardRegistered() {
		// TODO Auto-generated method stub
		new AlertDialog.Builder(DestinationActivity.this)
				.setMessage(getResources().getString(R.string.strcredicard))
				.setCancelable(false)
				.setPositiveButton(getResources().getString(R.string.strgotit),
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								dialog.dismiss();
								Intent i_register = new Intent(DestinationActivity.this, MainActivity.class);
								i_register.putExtra("page", 1);
								startActivity(i_register);
								DestinationActivity.this.finish();
							}
						}).show();
	}

	private class UploadCourierImage extends AsyncTask<String, Integer, String> {

		private Bitmap bMap = null;
		private String strPicture;

		public UploadCourierImage(String strCourierPicture) {
			// TODO Auto-generated constructor stub
			strPicture = strCourierPicture;
		}

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
			pdialog = new ProgressDialog(DestinationActivity.this);
			pdialog.setMessage("Sending your picture...");
			pdialog.setCancelable(false);
			pdialog.show();
		}

		@SuppressWarnings("deprecation")
		@Override
		protected String doInBackground(String... params) {
			// TODO Auto-generated method stub
			String struserid = mySharedPreferences.getString("strUserId", "");

			String fileName = "courier_" + struserid + ".jpg";

			bMap = BitmapFactory.decodeFile(strPicture);
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			bMap.compress(CompressFormat.JPEG, 100, bos);

			return uploadImage(bMap, 100, fileName);
		}

		@Override
		protected void onPostExecute(String result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			pdialog.dismiss();
			RequestTrip(price, "Paypal", "courier", strWeight);
		}

	}

	private String uploadImage(Bitmap file, int compressorQuality, String fileName) {

		String url = null;
		url = UrlGenerator.uploadCourierImage();
		String response = null;
		HttpURLConnection conn = null;
		try {
			String lineEnd = "\r\n";
			String twoHyphens = "--";
			String boundary = "---------------------------14737809831466499882746641449";
			URL url_courier = new URL(url);
			conn = (HttpURLConnection) url_courier.openConnection();
			conn.setDoInput(true);
			conn.setDoOutput(true);
			conn.setUseCaches(false);
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Connection", "Keep-Alive");
			conn.setRequestProperty("ENCTYPE", "multipart/form-data");
			conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
			conn.setRequestProperty("uploaded_file", fileName);
			DataOutputStream dos = new DataOutputStream(conn.getOutputStream());
			dos.writeBytes(lineEnd + twoHyphens + boundary + lineEnd);
			dos.writeBytes("Content-Disposition: form-data; name=\"userfile\"; filename=\"" + fileName + "\"" + lineEnd);
			dos.writeBytes("Content-Type: application/octet-stream" + lineEnd);
			dos.writeBytes(lineEnd);
			file.compress(CompressFormat.PNG, compressorQuality, dos);
			dos.writeBytes(lineEnd);
			dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
			dos.flush();
			dos.close();
			InputStream is = conn.getInputStream();
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			int bytesRead;
			byte[] bytes = new byte[1024];
			while ((bytesRead = is.read(bytes)) != -1) {
				baos.write(bytes, 0, bytesRead);
			}
			byte[] bytesReceived = baos.toByteArray();
			baos.close();
			is.close();
			response = new String(bytesReceived);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (conn != null) {
				conn.disconnect();
			}
		}

		return response;
	}


	//Request Rider Trip
	private void RequestTrip(double price, final String paymentType, String strTripType, String strWeightItem) {
		// TODO Auto-generated method stub
		AsyncHttpClient client = new AsyncHttpClient();
		RequestParams params = new RequestParams();

		// Show ProgressBar
		final ProgressDialog pdialog = new ProgressDialog(DestinationActivity.this);
		pdialog.setMessage("Sending your request...");
		pdialog.setCancelable(false);
		pdialog.show();

		String url = null;
		try {
			url = UrlGenerator.userFeedAdd();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		df_cost.setRoundingMode(RoundingMode.CEILING);

		url = url + "iUserID=" + mySharedPreferences.getString("strUserId", "") + "&vFeedTitle=" + mySharedPreferences.getString("strUserPhone", "") +
				"&fLat=" + String.valueOf(df.format(mSourceLatitude)) + "&fLong=" + String.valueOf(df.format(mSourceLongitude)) +
				"&toLat=" + String.valueOf(df.format(mDestinationLatitude)) + "&toLong=" + String.valueOf(df.format(mDestinationLongitude)) +
				"&vUserView=public&vCost=" + String.valueOf(df_cost.format(price)) + "&vPayment=" + paymentType;

		params.add("iUserID", mySharedPreferences.getString("strUserId", ""));
		params.add("vFeedTitle", mySharedPreferences.getString("strUserPhone", ""));
		params.add("fLat", String.valueOf(df.format(mSourceLatitude)));
		params.add("fLong", String.valueOf(df.format(mSourceLongitude)));
		params.add("toLat", String.valueOf(df.format(mDestinationLatitude)));
		params.add("toLong", String.valueOf(df.format(mDestinationLongitude)));
		params.add("vCost", String.valueOf(df_cost.format(price)));
		params.add("vPayment", paymentType);

		if (strTripType.equals("ride") == true) {
			params.add("vUserView", "public");
		}
		if (strTripType.equals("courier") == true) {
			params.add("vUserView", "courier");
			params.add("courierWeight", strWeightItem);
		}

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

					if (strStatus.equals("0") == true) {
						if (strMessage.equals("No current route") == true) {
							showAletNoDriver();
						} else if (paymentType.equals("Paypal") == true) {
							showWaitingForDriver();
						} else if (paymentType.equals("Cash") == true) {
							showWaitingForDriver();
						}
					}

				} catch (Exception e) {
					e.printStackTrace();
				}
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

	private void showWaitingForDriver() {
		// TODO Auto-generated method stub
		final AsyncHttpClient client = new AsyncHttpClient();
		final RequestParams params = new RequestParams();

		// Show ProgressBar
		final ProgressDialog pdialogaccept = new ProgressDialog(DestinationActivity.this);
		pdialogaccept.setMessage("Waiting for a driver...");
		pdialogaccept.setCancelable(false);
		pdialogaccept.show();

		timerWaitForDriver = new Timer();
		timerWaitForDriver.schedule(new TimerTask() {

			@Override
			public void run() {

				DestinationActivity.this.runOnUiThread(new Runnable() {
					public void run() {

						String url = null;
						try {
							url = UrlGenerator.userFeedList();
						} catch (UnsupportedEncodingException e) {
							e.printStackTrace();
						}

						params.add("iUserID", mySharedPreferences.getString("strUserId", ""));

						client.get(url, params, new AsyncHttpResponseHandler() {

							@Override
							public void onSuccess(String response) {
								// Hide ProgressBar

								JSONObject jObject;
								try {
									jObject = new JSONObject(response);

									String strStatus = jObject.optString("status");
									jObject.optString("message");

									if (strStatus.equals("0") == true) {

										try {
											GsonBuilder gsonBuilder = new GsonBuilder();
											Gson gson = gsonBuilder.create();

											TripDetails trip = gson.fromJson(response, TripDetails.class);

											strFeedId = trip.data.get(0).iFeedID;
											strUserView = trip.data.get(0).vUserView;

											if (strUserView.equals("friends") == true) {
												pdialogaccept.hide();
												timerWaitForDriver.cancel();
												timer = new Timer();
												timer.schedule(new TimerTask() {

													@Override
													public void run() {
														DestinationActivity.this.runOnUiThread(new Runnable() {
															public void run() {
																// TODO Auto-generated method stub
																driverAccepted(strFeedId);
															}
														});
													}

												}, 0, 5000);

											}

										} catch (NullPointerException e) {
											e.printStackTrace();
										}
									}

								} catch (Exception e) {
									e.printStackTrace();
								}
							}

							// When error occured
							@Override
							public void onFailure(int statusCode, Throwable error, String content) {
								// TODO Auto-generated method stub
								// Hide ProgressBar
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
				});
			}
		}, 0, 10000);
	}

	private void userCancelTrip(String strFeedId) {
		// TODO Auto-generated method stub
		AsyncHttpClient client = new AsyncHttpClient();
		RequestParams params = new RequestParams();

		// Show ProgressBar
		final ProgressDialog pdialog = new ProgressDialog(DestinationActivity.this);
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
					if (strStatus.equals("0") == true) {
						Intent i_destination = new Intent(DestinationActivity.this, MainActivity.class);
						i_destination.putExtra("page", 0);
						startActivity(i_destination);
						DestinationActivity.this.finish();
					}

				} catch (Exception e) {
					e.printStackTrace();
				}
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

	private void driverAccepted(final String strFeedId) {
		// TODO Auto-generated method stub
		timer.cancel();//To cancel the timer task

		final AsyncHttpClient client = new AsyncHttpClient();
		final RequestParams params = new RequestParams();

		// Show ProgressBar
		final ProgressDialog pdialogGrabbing = new ProgressDialog(DestinationActivity.this);
		pdialogGrabbing.setMessage("Driver accepted...");
		pdialogGrabbing.setCancelable(false);
		pdialogGrabbing.show();

		timerGrabbingDriverLocation = new Timer();
		timerGrabbingDriverLocation.schedule(new TimerTask() {

			@Override
			public void run() {

				DestinationActivity.this.runOnUiThread(new Runnable() {
					public void run() {
						String url = null;
						url = UrlGenerator.getFeed();

						params.add("iFeedID", strFeedId);

						client.get(url, params, new AsyncHttpResponseHandler() {

							@Override
							public void onSuccess(String response) {
								// Hide ProgressBar
								//pdialog.hide();
								JSONObject jObject;
								try {
									jObject = new JSONObject(response);

									strCurrentLat = jObject.optString("currentLat");
									strCurrentLong = jObject.optString("currentLong");

									if (strCurrentLat.equals(strCurrentLong) == true) {
										//Grabbing the driver location
										pdialogGrabbing.setMessage("Grabbing the drivers location");
									} else {
										pdialogGrabbing.hide();
										timerGrabbingDriverLocation.cancel();
										Intent i_rider = new Intent(DestinationActivity.this, RiderPickUpActivity.class);
										try {
											i_rider.putExtra("strFeedId", strFeedId);
											i_rider.putExtra("fLat", String.valueOf(df.format(mSourceLatitude)));
											i_rider.putExtra("fLong", String.valueOf(df.format(mSourceLongitude)));
											i_rider.putExtra("toLat", String.valueOf(df.format(mDestinationLatitude)));
											i_rider.putExtra("toLong", String.valueOf(df.format(mDestinationLongitude)));
											i_rider.putExtra("currentLat", strCurrentLat);
											i_rider.putExtra("currentLong", strCurrentLong);

										} catch (Exception e) {
											e.printStackTrace();
										}

										startActivity(i_rider);
										DestinationActivity.this.finish();
									}

								} catch (Exception e) {
									e.printStackTrace();
								}
							}

							// When error occured
							@Override
							public void onFailure(int statusCode, Throwable error, String content) {
								// TODO Auto-generated method stub
								// Hide ProgressBar
								//pdialog.hide();
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
				});
			}
		}, 0, 5000);
	}

	private void showCourierCashAlert() {
		// TODO Auto-generated method stub
		new AlertDialog.Builder(DestinationActivity.this)
				.setMessage(getResources().getString(R.string.strcouriercashalert))
				.setCancelable(false)
				.setPositiveButton(getResources().getString(R.string.strgotit),
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								dialog.dismiss();
							}
						}).show();
	}

	private void showSameLocationAlert() {
		// TODO Auto-generated method stub
		new AlertDialog.Builder(DestinationActivity.this)
				.setMessage(getResources().getString(R.string.strsamelocation))
				.setCancelable(false)
				.setPositiveButton(getResources().getString(R.string.strgotit),
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								dialog.dismiss();
							}
						}).show();
	}

	private void showAletNoDriver() {
		// TODO Auto-generated method stub
		new AlertDialog.Builder(DestinationActivity.this)
				.setMessage(getResources().getString(R.string.strnodriver))
				.setCancelable(false)
				.setPositiveButton(getResources().getString(R.string.strgotit),
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								dialog.dismiss();
							}
						}).show();
	}

	private void buildAlertMessageNoGps() {
		// TODO Auto-generated method stub
		new AlertDialog.Builder(DestinationActivity.this)
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
								DestinationActivity.this.finish();
							}
						}).show();
	}

	private void networkConnectivity() {
		new AlertDialog.Builder(DestinationActivity.this)
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

		mDestinationLatitude = latitude;
		mDestinationLongitude = longitude;
	}

	@Override
	public void onLocationChanged(Location location) {
		location.getLatitude();
		location.getLongitude();
	}

	@Override
	public void onConnected(Bundle arg0) {
		// TODO Auto-generated method stub
		mLocationRequest = LocationRequest.create();
		mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
		mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
		mLocationRequest.setInterval(1000); // Update location every second

		if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
			// TODO: Consider calling
			//    ActivityCompat#requestPermissions
			// here to request the missing permissions, and then overriding
			//   public void onRequestPermissionsResult(int requestCode, String[] permissions,
			//                                          int[] grantResults)
			// to handle the case where the user grants the permission. See the documentation
			// for ActivityCompat#requestPermissions for more details.
			return;
		}else {
			LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);

			Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
			if (mLastLocation != null) {
				animateCameraTo(mLastLocation.getLatitude(), mLastLocation.getLongitude());

				mDestinationLatitude = mLastLocation.getLatitude();
				mDestinationLongitude = mLastLocation.getLongitude();
			}
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
		isMapTouched = "1";
		myMap.clear();
		myMap.animateCamera(CameraUpdateFactory.newLatLng(point));
		MarkerOptions options = new MarkerOptions();
		options.position(point);
		options.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_user_marker));
		//options.title("Current Location");
		myMap.addMarker(options);
		
		mDestinationLatitude = point.latitude;
		mDestinationLongitude = point.longitude;
		
		//getAddress(String.valueOf(point.latitude), String.valueOf(point.longitude));
		
		mDestinationLocation = new Location("");
	   	
	   	mDestinationLocation.setLatitude(mDestinationLatitude);
	   	mDestinationLocation.setLongitude(mDestinationLongitude);

	   	float distanceInMeters = mSourceLocation.distanceTo(mDestinationLocation);
	   	
	   	getCost(distanceInMeters);
	   	
	}
	
	private void getCost(float distanceInMeters) {
		// TODO Auto-generated method stub
		
		currentCountry = mySharedPreferences.getString("country_code", "");
		
		if (currentCountry == "SE") {
	   		double prices = distanceInMeters *0.01;
	        price = (prices * 1.15) + 10;
	    }else{
	    	double prices = distanceInMeters *0.001;
	    	price = (prices * 1.20) + 5;
	    }
	   	
	   	if(price > 5000) {
	   		isShowPrice = "0";
	   		showSameLocationAlert();
	   		rl_trip.setVisibility(View.GONE);
	   		tv_trip_cost.setVisibility(View.GONE);
			tv_uber_cost.setVisibility(View.GONE);
			tv_uber_label.setVisibility(View.GONE);
	   	} else {
	   		isShowPrice = "1";
	   		ubersprice = (price * 1.25);
	   		rl_trip.setVisibility(View.VISIBLE);
	   		tv_trip_cost.setVisibility(View.VISIBLE);
			tv_uber_cost.setVisibility(View.VISIBLE);
			tv_uber_label.setVisibility(View.VISIBLE);
			
			double roundOffPrice = (double) Math.round(price * 100) / 100;
			double roundOffUberPrice = (double) Math.round(ubersprice * 100) / 100;
			
			tv_trip_cost.setText(String.valueOf("$ "+roundOffPrice));
			tv_uber_cost.setText(String.valueOf("$ "+roundOffUberPrice));
			
			tv_uber_cost.setPaintFlags( Paint.STRIKE_THRU_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG);
	   	}
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
		        
		        mDestinationLocation = new Location("");
			   	
			   	mDestinationLocation.setLatitude(Double.parseDouble(latlng[0]));
			   	mDestinationLocation.setLongitude(Double.parseDouble(latlng[1]));

			   	float distanceInMeters = mSourceLocation.distanceTo(mDestinationLocation);
			   	
			   	getCost(distanceInMeters);
		        
            }
	    }
	}

	private void getAddress(String lat, String lng) {
		// TODO Auto-generated method stub
		try 
		{
			geocoder = new Geocoder(DestinationActivity.this, Locale.getDefault());
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
		        isMapTouched = "1";
		        
		        mDestinationLatitude = newLatLng.latitude;
				mDestinationLongitude = newLatLng.longitude;
			}
		} catch (NumberFormatException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}

	//@Override
	public void onSucess(List<DestinationLocationDetails.DestinationLocationDetail> location_lists, String strSearchlocation) {
		// TODO Auto-generated method stub
		
		int address_index = location_lists.get(0).address_components.size();
		
		String searchLat = location_lists.get(0).geometry.location.lat;
		String searchLng = location_lists.get(0).geometry.location.lng;
		String searchCountryCode = location_lists.get(0).address_components.get(address_index - 1).short_name;
		
		mRiderDestinationHelper.fetchDestinationList(DestinationActivity.this, strSearchlocation, searchLat, searchLng, searchCountryCode);
	}

	//@Override
	public void onSucessDestinationList(final List<DestinationPlaceDetails.DestinationPlaceDetail> place_lists) {
		// TODO Auto-generated method stub
		/*for(int i = 0; i < place_lists.size(); i++) {
			Log.e("place_lists","#@" + place_lists.get(i).description);
		}*/
		
		if(place_lists.size() > 0)
		{
			rl_destination_list.setVisibility(View.VISIBLE);
			dla = new DestinationListAdapter(getBaseContext(), place_lists);
			lv_destination_list.setAdapter(dla);
			
			lv_destination_list.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					// TODO Auto-generated method stub
					isMapTouched = "1";
					String strAddress = place_lists.get(position).description.toString();
					edt_address.setText(strAddress);
					rl_destination_list.setVisibility(View.GONE);
					GeocodingLocation locationAddress = new GeocodingLocation();
					locationAddress.getAddressFromLocation(strAddress, DestinationActivity.this, new GeocoderHandler());
				}
			});
		}
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();

		if (id == android.R.id.home) {
			DestinationActivity.this.finish();
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		DestinationActivity.this.finish();
	}
	
}
