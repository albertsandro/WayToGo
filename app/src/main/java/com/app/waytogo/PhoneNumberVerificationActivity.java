package com.app.waytogo;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.app.waytogo.model.ProfileDetails;
import com.app.waytogo.support.UrlGenerator;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

public class PhoneNumberVerificationActivity extends AppCompatActivity implements OnClickListener {

	private ConnectivityManager cm;
	private ImageButton btn_verify;
	private TextView  tv_resend, tv_caption, tv_hint;
	private EditText edt_code;
	private Typeface tf;
	private String strNumber, strCode, str_code, registrationId, strLoginType, strFirstName, strEmail;
	private SharedPreferences mySharedPreferences, myGCMPreferences;
	private Editor editor, editorGCM;
	private String USER = "user_pref";
	private String GCM = "gcm_pref";
		
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_phone_verification);
		
		cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
		mySharedPreferences = getSharedPreferences(USER, MODE_PRIVATE);
		editor = mySharedPreferences.edit();
		myGCMPreferences = getSharedPreferences(GCM, Context.MODE_PRIVATE);
		editorGCM = myGCMPreferences.edit();
		getGCMDeviceId();	
		tf = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Regular.ttf");

		try {
			Bundle b = getIntent().getExtras();
			strNumber = b.getString("strNumber");
			strCode = b.getString("strCode");
			strLoginType = b.getString("strLoginType");
			strFirstName = b.getString("strFirstName");
			strEmail = b.getString("strEmail");

		} catch(NullPointerException e) {}
		
		btn_verify = (ImageButton) findViewById(R.id.btn_verify);
		tv_resend = (TextView) findViewById(R.id.tv_resend);
		tv_caption = (TextView) findViewById(R.id.tv_caption);
		tv_hint = (TextView) findViewById(R.id.tv_hint);
		edt_code = (EditText) findViewById(R.id.edt_code);

		tv_hint.setText(getResources().getString(R.string.strloginhint)+" "+strNumber);
		
		tv_caption.setTypeface(tf);
		tv_hint.setTypeface(tf);
		
		btn_verify.setOnClickListener(PhoneNumberVerificationActivity.this);
		tv_resend.setOnClickListener(PhoneNumberVerificationActivity.this);
		showVerficationCode(strCode);
	}

	private void showVerficationCode(String strCode) {
		// TODO Auto-generated method stub
		new AlertDialog.Builder(PhoneNumberVerificationActivity.this)
		.setTitle("Verification Code")
		.setMessage("This is your verification code: "+strCode)
		.setCancelable(false)
		.setPositiveButton(R.string.strok,
		new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		}).show();
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch(v.getId())
		{
			case R.id.btn_verify:
				str_code = edt_code.getText().toString();
				if(strCode.equals(str_code) == true)
				{	
					if (cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isAvailable() && cm.getActiveNetworkInfo().isConnected()) {
						getUserEmailViaPhone();
						getSMSVerifiedPhone();
					} else {
						networkConnectivity();
					}
				}
				else
				{
					Toast.makeText(PhoneNumberVerificationActivity.this, "Incorrect verification code", Toast.LENGTH_SHORT).show();
				}
				break;
				
			case R.id.tv_resend:
				getVerificationCode(strNumber);
				break;
		}
	}
	
	
	private void getUserEmailViaPhone() {
		// TODO Auto-generated method stub
		AsyncHttpClient client = new AsyncHttpClient();
		// Http Request Params Object
		RequestParams params = new RequestParams();
		
		// Show ProgressBar
		final ProgressDialog pdialog = new ProgressDialog(PhoneNumberVerificationActivity.this);
		pdialog.setMessage("Loading...");
		pdialog.setCancelable(false);
		pdialog.show();
		// Make Http call to getusers.php
				
		String url = null;
		try {
			url = UrlGenerator.getUserEmail();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
		params.add("userPhone", strNumber);
				
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
						editor.putString("strEmail", strMessage);
						editor.commit();						
						userLogin(strMessage, "phonelogin");
					}
					else
					{
						Toast.makeText(PhoneNumberVerificationActivity.this, R.string.strVerificationSuccess, Toast.LENGTH_SHORT).show();
						Intent i_verification = new Intent(PhoneNumberVerificationActivity.this, ProfileActivity.class);
						i_verification.putExtra("strNumber", strNumber);
						i_verification.putExtra("strCode", strCode);
						i_verification.putExtra("strLoginType", strLoginType);
						i_verification.putExtra("strFirstName", strFirstName);
						i_verification.putExtra("strEmail", strEmail);
						startActivity(i_verification);
						PhoneNumberVerificationActivity.this.finish();
						editor.putString("isLogin", "1");
						editor.commit();
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
					//Toast.makeText(PhoneNumberLoginActivity.this, "Requested resource not found", Toast.LENGTH_LONG).show();
				} else if (statusCode == 500) {
					//Toast.makeText(PhoneNumberLoginActivity.this, "Something went wrong at server end", Toast.LENGTH_LONG).show();
				} else {
					networkConnectivity();
				}
			}
		});
	}
	
	
	private void getSMSVerifiedPhone() {
		// TODO Auto-generated method stub
		AsyncHttpClient client = new AsyncHttpClient();
		// Http Request Params Object
		RequestParams params = new RequestParams();
		
		// Show ProgressBar
		final ProgressDialog pdialog = new ProgressDialog(PhoneNumberVerificationActivity.this);
		pdialog.setMessage("Loading...");
		pdialog.setCancelable(false);
		pdialog.show();
		// Make Http call to getusers.php
				
		String url = null;
		try {
			url = UrlGenerator.getSMSVerifiedPhone();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
		params.add("userPhone", strNumber);
				
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
					}
					else
					{
						Toast.makeText(PhoneNumberVerificationActivity.this, strMessage, Toast.LENGTH_LONG).show();
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
					//Toast.makeText(PhoneNumberLoginActivity.this, "Requested resource not found", Toast.LENGTH_LONG).show();
				} else if (statusCode == 500) {
					//Toast.makeText(PhoneNumberLoginActivity.this, "Something went wrong at server end", Toast.LENGTH_LONG).show();
				} else {
					networkConnectivity();
				}
			}
		});
	}

	private void userLogin(String strEmail, String strPassword) {
		// TODO Auto-generated method stub
		
		AsyncHttpClient client = new AsyncHttpClient();
		// Http Request Params Object
		RequestParams params = new RequestParams();
		
		// Show ProgressBar
		final ProgressDialog pdialog = new ProgressDialog(PhoneNumberVerificationActivity.this);
		pdialog.setMessage("Loading...");
		pdialog.setCancelable(false);
		pdialog.show();
		// Make Http call to getusers.php
				
		String url = null;
		try {
			url = UrlGenerator.userLogin();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
		params.add("vEmail", strEmail);
		params.add("vPassword", strPassword);
		
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
					
					if(strStatus.equals("0") == true) {
						
						String strGCM = myGCMPreferences.getString("strDeviceToken", "");
						String strUserID = mySharedPreferences.getString("strUserId", ""); 
						
						try {
							GsonBuilder gsonBuilder = new GsonBuilder();
							Gson gson = gsonBuilder.create();
			
							ProfileDetails profile = gson.fromJson(response, ProfileDetails.class);
							
							String strUserId = profile.data.iUserID;
							String strUserName = profile.data.vUsername;
							String strEmail = profile.data.vEmail;
							String strELogin = profile.data.eLogin;
							String strFirstName = profile.data.vFirst;
							String strLastName = profile.data.vLast;
							String strLoginDate = profile.data.dLoginDate;
							String strFBID = profile.data.vFbID;
							String strUserPhone = profile.data.userPhone;
							String strDiscount = profile.data.codefordiscount;
							String strVersion = profile.data.version;
							String strImageUrl = profile.data.profileImage.original;
							String strVerifiedPhone = profile.data.verifiedPhone;
							String strIsDriver = profile.data.vDriverorNot;
							String strToken = profile.data.vToken;
														
							editor.putString("strUserId", strUserId);
							editor.putString("strUserName", strUserName);
							editor.putString("strEmail", strEmail);
							editor.putString("strELogin", strELogin);
							editor.putString("strFirstName", strFirstName);
							editor.putString("strLastName", strLastName);
							editor.putString("strLoginDate", strLoginDate);
							editor.putString("strFBID", strFBID);
							editor.putString("strUserPhone", strUserPhone);
							editor.putString("strDiscount", strDiscount);
							editor.putString("strVersion", strVersion);
							editor.putString("strImageUrl", strImageUrl);
							editor.putString("strVerifiedPhone", strVerifiedPhone);
							editor.putString("strIsDriver", strIsDriver);
							editor.putString("strDeviceToken", strGCM);
							editor.putString("strToken", strToken);
							editor.putString("isLogin", "1");
							editor.commit();
								
						} catch(NullPointerException e) { e.printStackTrace(); }
						//To update Access token
						addAccessToken(strGCM, strUserID);
					}
					else
					{
						Toast.makeText(PhoneNumberVerificationActivity.this, strMessage, Toast.LENGTH_LONG).show();
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
					//Toast.makeText(PhoneNumberLoginActivity.this, "Requested resource not found", Toast.LENGTH_LONG).show();
				} else if (statusCode == 500) {
					//Toast.makeText(PhoneNumberLoginActivity.this, "Something went wrong at server end", Toast.LENGTH_LONG).show();
				} else {
					networkConnectivity();
				}
			}
		});
	}

	protected void addAccessToken(String strGCM, String strUserID) {
		// TODO Auto-generated method stub
		AsyncHttpClient client = new AsyncHttpClient();
		// Http Request Params Object
		RequestParams params = new RequestParams();
		
		// Show ProgressBar
		final ProgressDialog pdialog = new ProgressDialog(PhoneNumberVerificationActivity.this);
		pdialog.setMessage("Loading...");
		pdialog.setCancelable(false);
		pdialog.show();
		// Make Http call to getusers.php
				
		String url = null;
		try {
			url = UrlGenerator.addAccessToken();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
		params.add("iUserID", strUserID);
		params.add("vDeviceToken", strGCM);
				
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
					
					if(strStatus.equals("0") == true) {
						Intent i_verification = new Intent(PhoneNumberVerificationActivity.this, MainActivity.class);
						i_verification.putExtra("strNumber", strNumber);
						i_verification.putExtra("strCode", strCode);
						startActivity(i_verification);
						PhoneNumberVerificationActivity.this.finish();
						String strGCM = myGCMPreferences.getString("strDeviceToken", "");
					}
					else
					{
						Toast.makeText(PhoneNumberVerificationActivity.this, strMessage, Toast.LENGTH_LONG).show();
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
					//Toast.makeText(PhoneNumberLoginActivity.this, "Requested resource not found", Toast.LENGTH_LONG).show();
				} else if (statusCode == 500) {
					//Toast.makeText(PhoneNumberLoginActivity.this, "Something went wrong at server end", Toast.LENGTH_LONG).show();
				} else {
					networkConnectivity();
				}
			}
		});
	}
	
	private void getVerificationCode(String strNumber) {
		// TODO Auto-generated method stub
		// Create AsycHttpClient object
		
		final String str_number = strNumber;
		
		AsyncHttpClient client = new AsyncHttpClient();
		// Http Request Params Object
		RequestParams params = new RequestParams();
		
		// Show ProgressBar
		final ProgressDialog pdialog = new ProgressDialog(PhoneNumberVerificationActivity.this);
		pdialog.setMessage("Loading...");
		pdialog.setCancelable(false);
		pdialog.show();
		// Make Http call to getusers.php
				
		String url = null;
		try {
			url = UrlGenerator.userSMSVerificationCode();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
		params.add("userPhone", str_number);
		
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
					strCode = strMessage;
					showVerficationCode(strCode);
					
				} catch (Exception e) {e.printStackTrace();}
			}

			// When error occured
			@Override
			public void onFailure(int statusCode, Throwable error, String content) {
				// TODO Auto-generated method stub
				// Hide ProgressBar
				pdialog.hide();
				if (statusCode == 404) {
					//Toast.makeText(PhoneNumberLoginActivity.this, "Requested resource not found", Toast.LENGTH_LONG).show();
				} else if (statusCode == 500) {
					//Toast.makeText(PhoneNumberLoginActivity.this, "Something went wrong at server end", Toast.LENGTH_LONG).show();
				} else {
					networkConnectivity();
				}
			}
		});
	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		Intent i_back = new Intent(PhoneNumberVerificationActivity.this, PhoneNumberLoginActivity.class);
		startActivity(i_back);
		PhoneNumberVerificationActivity.this.finish();
	}
	
	public void getGCMDeviceId()
	{
		new AsyncTask<String, String, String>(){
			
			@Override
			protected String doInBackground(String... params) {
					
				String SENDER_ID = "651484448569";//"271579525703";
				GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(PhoneNumberVerificationActivity.this);
				try {
					registrationId = gcm.register(SENDER_ID); 
					editorGCM.putString("strDeviceToken", registrationId);
					editorGCM.commit();
				} catch (IOException e) {
					e.printStackTrace();
				}
				return registrationId;
			}
			
		}.execute();
	}
	
	private void networkConnectivity()
	{
		new AlertDialog.Builder(PhoneNumberVerificationActivity.this)
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