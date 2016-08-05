package com.app.waytogo;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.app.waytogo.support.UrlGenerator;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

public class PhoneNumberLoginActivity extends AppCompatActivity implements OnClickListener {

	private ConnectivityManager cm;
	private ImageButton btn_next;
	private TextView  tv_caption;
	private EditText edt_number;
	private Typeface tf;
	private String strNumber;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_phone_login);
		
		cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
		tf = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Regular.ttf");
		
		btn_next = (ImageButton) findViewById(R.id.btn_next);
		tv_caption = (TextView) findViewById(R.id.tv_caption);
		edt_number = (EditText) findViewById(R.id.edt_number);

		tv_caption.setTypeface(tf);
		
		btn_next.setOnClickListener(PhoneNumberLoginActivity.this);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch(v.getId())
		{
			case R.id.btn_next:
				strNumber = edt_number.getText().toString();
				if(strNumber.equals("") == false) {
					if (cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isAvailable() && cm.getActiveNetworkInfo().isConnected()) {
						getVerificationCode(strNumber);
					} else {
						networkConnectivity();
					}
				}
				else {
					Toast.makeText(PhoneNumberLoginActivity.this, "Enter the mobile number", Toast.LENGTH_SHORT).show();
				}
				break;
		}
	}
	
	private void getVerificationCode(String strNumber) {
		// TODO Auto-generated method stub
		// Create AsycHttpClient object
		final String str_number = strNumber;
		
		AsyncHttpClient client = new AsyncHttpClient();
		// Http Request Params Object
		RequestParams params = new RequestParams();
		
		// Show ProgressBar
		final ProgressDialog pdialog = new ProgressDialog(PhoneNumberLoginActivity.this);
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
					
					if(strStatus.equals("0") == true) {
						Intent i_verification = new Intent(PhoneNumberLoginActivity.this, PhoneNumberVerificationActivity.class);
						i_verification.putExtra("strNumber", str_number);
						i_verification.putExtra("strCode", strMessage);
						startActivity(i_verification);
						
						PhoneNumberLoginActivity.this.finish();
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
	
	private void networkConnectivity()
	{
		new AlertDialog.Builder(PhoneNumberLoginActivity.this)
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
