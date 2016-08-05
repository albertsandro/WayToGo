package com.app.waytogo.support;

import java.io.IOException;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.util.Log;

public class GCMUtil {

	private static String registrationId = "";
	private static SharedPreferences myPreferences;
	private static Editor editor;
	private String GCM = "gcm_pref";
	
	public GCMUtil(Context context)	{
		myPreferences = (SharedPreferences) context.getSharedPreferences(GCM, Context.MODE_PRIVATE);
		editor = myPreferences.edit();
	}
	
	public static String getGCMDeviceId(final Context context)
	{
		new AsyncTask<String, String, String>(){
	
			@Override
			protected String doInBackground(String... params) {
					
				String SENDER_ID = "651484448569";//"271579525703";
				GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(context);
				try {
					registrationId = gcm.register(SENDER_ID); 
					/*editor.putString("strDeviceToken", registrationId);
					editor.commit();*/
				} catch (IOException e) {
					e.printStackTrace();
				}
				return registrationId;
			}
			
		}.execute();
		Log.e("registrationId","$$ "+registrationId);
		return registrationId;
	}
	
}
