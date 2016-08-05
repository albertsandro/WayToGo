package com.app.waytogo;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.Signature;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.TextView;

import com.app.waytogo.model.ProfileDetails;
import com.app.waytogo.support.UrlGenerator;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;

public class LoginActivity extends AppCompatActivity implements OnClickListener {

	private ConnectivityManager cm;
	private ImageButton btn_FB, btn_PhoneNumber;
	private TextView  tv_terms, tv_help;
	private Typeface tf;
	private UiLifecycleHelper uiHelper;
	private boolean isFBClicked = false;
	private SharedPreferences mySharedPreferences, myGCMPreferences;
	private Editor editor, editorGCM;
	private String USER = "user_pref";
	private String GCM = "gcm_pref";
	private String strFBID, strFBFirstName, strFBLastName, strFBEmail, strFBImageURL;
		
	private Session.StatusCallback statusCallback = new SessionStatusCallback();

	private class SessionStatusCallback implements Session.StatusCallback {
		@Override
		public void call(Session session, SessionState state,
				Exception exception) {
			// Respond to session state changes, ex: updating the view
			onSessionStateChange(session, state, exception);
		}
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
	
		try {
			PackageInfo info = getPackageManager().getPackageInfo("com.rider", PackageManager.GET_SIGNATURES);
			for (Signature signature : info.signatures) {
				MessageDigest md = MessageDigest.getInstance("SHA");
				md.update(signature.toByteArray());
				Log.e("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
			}
		} catch (NameNotFoundException e) {

		} catch (NoSuchAlgorithmException e) {

		}
		
		cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
		mySharedPreferences = getSharedPreferences(USER, MODE_PRIVATE);
		editor = mySharedPreferences.edit();
		myGCMPreferences = getSharedPreferences(GCM, MODE_PRIVATE);
		editorGCM = myGCMPreferences.edit();
		tf = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Regular.ttf");
		
		btn_FB = (ImageButton) findViewById(R.id.btnFB);
		btn_PhoneNumber = (ImageButton) findViewById(R.id.btnPhoneNumber);
		tv_terms = (TextView) findViewById(R.id.tv_terms);
		tv_help = (TextView) findViewById(R.id.tv_help);
			
		btn_FB.setOnClickListener(LoginActivity.this);
		btn_PhoneNumber.setOnClickListener(LoginActivity.this);
		tv_terms.setOnClickListener(LoginActivity.this);
		tv_help.setOnClickListener(LoginActivity.this);
		
		tv_terms.setTypeface(tf);
		tv_help.setTypeface(tf);
		
		// For Facebook
		uiHelper = new UiLifecycleHelper(LoginActivity.this, statusCallback);
		uiHelper.onCreate(savedInstanceState);
		
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch(v.getId())
		{
			case R.id.btnFB:
				if(cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isAvailable() && cm.getActiveNetworkInfo().isConnected()) {
					fbLogin();
				} else {
					networkConnectivity();
				}
				break;
			case R.id.btnPhoneNumber:
				Intent i_phone = new Intent(LoginActivity.this, PhoneNumberLoginActivity.class);
				startActivity(i_phone);
				LoginActivity.this.finish();
				break;
			case R.id.tv_terms:
				Intent i_terms = new Intent(LoginActivity.this, TermsConditionsActivity.class);
				startActivity(i_terms);
				break;
			case R.id.tv_help:
				alertHelp();
				break;
		}
	}

	private void fbLogin() {
		// TODO Auto-generated method stub
		Session session = Session.getActiveSession();
		
		if (!session.isOpened() && !session.isClosed()) {
			Session.OpenRequest openRequest = new Session.OpenRequest(this);
			List<String> permissions = Arrays.asList("email");
			openRequest.setPermissions(permissions);
			openRequest.setCallback(statusCallback);
			session.openForRead(openRequest);
			isFBClicked = true;
		} else {
			Session.openActiveSession(this, true, statusCallback);
			isFBClicked = true;
		}
	}

	private void networkConnectivity() {
		// TODO Auto-generated method stub
		new AlertDialog.Builder(LoginActivity.this)
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
	
	private void alertHelp() {
		// TODO Auto-generated method stub
		new AlertDialog.Builder(LoginActivity.this)
		.setMessage(R.string.strhelpmessage)
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
	public void onResume() {
		super.onResume();
		// For scenarios where the main activity is launched and user
		// session is not null, the session state change notification
		// may not be triggered. Trigger it if it's open/closed.
		Session session = Session.getActiveSession();
		if (session != null && (session.isOpened() || session.isClosed())) {
			onSessionStateChange(session, session.getState(), null);
		}
		uiHelper.onResume();
	}

	private void onSessionStateChange(Session session, SessionState state,
			Object object) {
		// TODO Auto-generated method stub
		if (state.isOpened()) {
			if(isFBClicked)
				fetchDataFromFaceBook();
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		uiHelper.onPause();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		uiHelper.onDestroy();
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		uiHelper.onSaveInstanceState(outState);
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		Session.getActiveSession().onActivityResult(LoginActivity.this, requestCode, resultCode, data);
	}
	
	private void fetchDataFromFaceBook() {
		// TODO Auto-generated method stub
		// Create AsycHttpClient object
		AsyncHttpClient client = new AsyncHttpClient();
		// Http Request Params Object
		RequestParams params = new RequestParams();
		
		// Show ProgressBar
		final ProgressDialog pdialog = new ProgressDialog(LoginActivity.this);
		pdialog.setMessage("Loading...");
		pdialog.setCancelable(false);
		pdialog.show();
		// Make Http call to getusers.php
				
		String fb_url = "https://graph.facebook.com/me?fields=id,email,first_name,last_name&access_token="+Session.getActiveSession().getAccessToken();
		
		client.get(fb_url, params, new AsyncHttpResponseHandler() {

			@Override
			public void onSuccess(String response) {
				// Hide ProgressBar
				pdialog.hide();
				JSONObject jObject;
				try {
					jObject = new JSONObject(response);
					
					strFBID = jObject.optString("id");
					strFBFirstName = jObject.optString("first_name");
					strFBLastName = jObject.optString("last_name");
					strFBEmail = jObject.optString("email");
					strFBImageURL = "https://graph.facebook.com/"+strFBID+"/picture?type=large";
					
					//editor.putString("strFBID", strFBID);
					//editor.putString("strEmail", strFBEmail);
					//editor.putString("strFirstName", strFBName);
					//editor.putString("isLogin", "1");
					//editor.commit();
					
					userFBLogin(strFBID);
					
				} catch (Exception e) {e.printStackTrace();}
			}

			// When error occured
			@Override
			public void onFailure(int statusCode, Throwable error, String content) {
				// TODO Auto-generated method stub
				// Hide ProgressBar
				pdialog.hide();
				if (statusCode == 404) {
					//Toast.makeText(LoginActivity.this, "Requested resource not found", Toast.LENGTH_LONG).show();
				} else if (statusCode == 500) {
					//Toast.makeText(LoginActivity.this, "Something went wrong at server end", Toast.LENGTH_LONG).show();
				} else {
					//networkConnectivity();
				}
			}
		});
	}
	
	private void userFBLogin(String strFBID) {
		// TODO Auto-generated method stub
		
		AsyncHttpClient client = new AsyncHttpClient();
		// Http Request Params Object
		RequestParams params = new RequestParams();
		
		// Show ProgressBar
		final ProgressDialog pdialog = new ProgressDialog(LoginActivity.this);
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
		
		params.add("vFbID", strFBID);
		
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
						
					}
					else
					{
						Intent i_profile = new Intent(LoginActivity.this, ProfileActivity.class);
						i_profile.putExtra("strFirstName", strFBFirstName);
						i_profile.putExtra("strLastName", strFBLastName);
						i_profile.putExtra("strEmail", strFBEmail);
						i_profile.putExtra("strLoginType", "FB");
						i_profile.putExtra("strNumber", "0");
						i_profile.putExtra("strCode", "0000");
						startActivity(i_profile);
						
						//editor.putString("strFBID", strFBID);
						//editor.putString("strEmail", strFBEmail);
						//editor.putString("strFirstName", strFBName);
						//editor.putString("isLogin", "1");
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

}
