package com.app.waytogo;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.app.waytogo.support.UrlGenerator;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONObject;

import java.text.DecimalFormat;


public class CompleteRideActivity extends AppCompatActivity {

	private ConnectivityManager cm;
	private TextView tv_earnings, tv_earnings_total, tv_complete_tap_continue;
	private String strTripCost, strTotalEarnings, strFeedID;
	private DecimalFormat df = new DecimalFormat("#.##");
		
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_complete_ride);
		
		cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		
		try {
			Bundle b = getIntent().getExtras();
			strTripCost = b.getString("strTripCost");
			strTotalEarnings = b.getString("strTotalEarnings");
			strFeedID = b.getString("strFeedID");
			
		} catch(Exception e) {e.printStackTrace();}
		
		tv_earnings = (TextView) findViewById(R.id.tv_earnings);
		tv_earnings_total = (TextView) findViewById(R.id.tv_earnings_total);
		tv_complete_tap_continue = (TextView) findViewById(R.id.tv_complete_tap_continue);
		
		double dTripCost = Double.valueOf(strTripCost);
		double dTotalEarnings = Double.valueOf(strTotalEarnings);
		
		tv_earnings.setText("Earnings: $"+ df.format(dTripCost));
		tv_earnings_total.setText("Total : $"+ df.format(dTotalEarnings));
				
		tv_complete_tap_continue.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isAvailable() && cm.getActiveNetworkInfo().isConnected()) {
					completeRide(strFeedID);
				} else {
					networkConnectivity();
				}
			}
		});
	}
	
	
	//To update payment confirmation
	public void completeRide(final String strFeedID)
	{
		// TODO Auto-generated method stub
		AsyncHttpClient client = new AsyncHttpClient();
		// Http Request Params Object
		RequestParams params = new RequestParams();
		
		String url = null;
		url = UrlGenerator.completedRide();
		
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
						Intent i_complete = new Intent(CompleteRideActivity.this, DriverActivity.class);
						startActivity(i_complete);
						CompleteRideActivity.this.finish();
					}
												
				} catch (Exception e) {e.printStackTrace();}
			}

			// When error occured
			@Override
			public void onFailure(int statusCode, Throwable error, String content) {
				// TODO Auto-generated method stub
				if (statusCode == 404) {
					//Toast.makeText(CompleteRideActivity.this, "Requested resource not found", Toast.LENGTH_LONG).show();
				} else if (statusCode == 500) {
					//Toast.makeText(CompleteRideActivity.this, "Something went wrong at server end", Toast.LENGTH_LONG).show();
				} else {
					networkConnectivity();
				}
			}
		});
	}
	
	private void networkConnectivity()
	{
        new AlertDialog.Builder(CompleteRideActivity.this)
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