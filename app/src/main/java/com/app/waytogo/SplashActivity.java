package com.app.waytogo;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;
import android.view.WindowManager;

import java.util.Locale;

public class SplashActivity extends AppCompatActivity {

	protected boolean active = true;
	protected int splashTime = 5000;
	private SharedPreferences mySharedPreferences;
	private Editor editor;
	private String USER = "user_pref";
	private String isLoggedin;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {

		// To remove title bar
		getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash);
		
		mySharedPreferences = getSharedPreferences(USER, MODE_PRIVATE);
		editor = mySharedPreferences.edit();
		
		editor.putString("country_code", Locale.getDefault().getCountry());
		editor.commit();
		
		Thread splashThread = new Thread() {
			public void run() {
				try {
					int waited = 0;
					while (active && waited < splashTime) {
						sleep(100);
						if (active)
							waited += 100;
					}
				} catch (Exception e) {
				}

				finally {
					finish();
					isLoggedin = mySharedPreferences.getString("isLogin", "");
					if(isLoggedin.equals("1") == true)
					{
						Intent i_main = new Intent(SplashActivity.this, MainActivity.class);
						startActivity(i_main);
						SplashActivity.this.finish();
					}
					else
					{
						Intent i_login = new Intent(SplashActivity.this, LoginActivity.class);
						startActivity(i_login);
						SplashActivity.this.finish();
					}
				}
			}
		};
		splashThread.start();
	}

}