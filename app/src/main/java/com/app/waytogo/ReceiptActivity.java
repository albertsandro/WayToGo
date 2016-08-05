package com.app.waytogo;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RatingBar;
import android.widget.RatingBar.OnRatingBarChangeListener;
import android.widget.TextView;

import com.app.waytogo.support.UrlGenerator;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.squareup.picasso.Picasso;

public class ReceiptActivity extends AppCompatActivity {

	private ConnectivityManager cm;
	private TextView tv_report_driver, tv_cost;
	private CircularImageView iv_driver_pics;
	private RatingBar rb_rating;
	private ImageButton btn_submit;
	private String strFeedID, strCost, strImage, strUserID, strPayment, strToken;
	private SharedPreferences mySharedPreferences;
	private Editor editor;
	private String USER = "user_pref";
	private int ratings;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_receipt);
		
		mySharedPreferences = getSharedPreferences(USER, MODE_PRIVATE);
		editor = mySharedPreferences.edit();
		
		cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		
		try {
			Bundle b = getIntent().getExtras();
			strFeedID = b.getString("strFeedID");
			strCost = b.getString("strCost");
			strImage = b.getString("strImage");
			strPayment = b.getString("strPayment");
			strUserID = mySharedPreferences.getString("strUserId", "");
			strToken = mySharedPreferences.getString("strToken", "");
		} catch (Exception e) {}
		
		tv_report_driver = (TextView) findViewById(R.id.tv_report_driver);
		tv_cost = (TextView) findViewById(R.id.tv_cost);
		iv_driver_pics = (CircularImageView) findViewById(R.id.iv_driver_pics);
		rb_rating = (RatingBar) findViewById(R.id.rb_rating);
		btn_submit = (ImageButton) findViewById(R.id.btn_submit);
	
		tv_cost.setText("$ "+strCost);
		Picasso.with(ReceiptActivity.this).load(getResources().getString(R.string.strDriverThumbImageUrl)+""+strImage).error(R.drawable.ic_profile_pic).centerCrop().resize(100, 100).into(iv_driver_pics);
		
		if(strPayment.equals("Cash") == true) {
		} else {
			String currentCountry = mySharedPreferences.getString("country_code", "");
			
			makePayment(strUserID, strToken, strFeedID, strCost, strPayment, currentCountry);
		}
		
		tv_report_driver.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				alertReportDriver();
			}
		});
		
		rb_rating.setOnRatingBarChangeListener(new OnRatingBarChangeListener() {
			
			@Override
			public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
				// TODO Auto-generated method stub
				rb_rating.setRating(rating); 
				ratings = (int)rb_rating.getRating();
			}
		});
		
		btn_submit.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isAvailable() && cm.getActiveNetworkInfo().isConnected()) {
					feedback(strFeedID, ratings, "S");
				} else {
					networkConnectivity();
				}
			}
		});
		
	}
	
	private void makePayment(String strUserID, String strToken, String strFeedID, String strCost, String strPayment, String currentCountry) {
		// TODO Auto-generated method stub
		 if (currentCountry.equals("SE") == true) {
			 updateTokenSweden(strUserID, strToken, strFeedID, strCost, strPayment);
		 } else {
			 updateToken(strUserID, strToken, strFeedID, strCost, strPayment);
		 }
	}

	private void updateTokenSweden(final String strUserID, String strToken, final String strFeedID, final String strCost, String strPayment) {
		// TODO Auto-generated method stub
		AsyncHttpClient client = new AsyncHttpClient();
		RequestParams params = new RequestParams();
		
		// Show ProgressBar
		final ProgressDialog pdialog = new ProgressDialog(ReceiptActivity.this);
		pdialog.setMessage("Payment...");
		pdialog.setCancelable(false);
		pdialog.show();
		
		String url = null;
		url = UrlGenerator.updateTokenSweden();
				
		params.add("iUserID", strUserID);
		params.add("token", strToken);
		params.add("feedId", strFeedID);
		params.add("cost", strCost);
		params.add("vPayment", strPayment);
						
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
				userCancelTrip(strFeedID);
				updateBalance(strUserID, strCost);
			}
		});
	}
	
	private void updateToken(final String strUserID, String strToken, final String strFeedID, final String strCost, String strPayment) {
		// TODO Auto-generated method stub
		AsyncHttpClient client = new AsyncHttpClient();
		RequestParams params = new RequestParams();
		
		// Show ProgressBar
		final ProgressDialog pdialog = new ProgressDialog(ReceiptActivity.this);
		pdialog.setMessage("Payment...");
		pdialog.setCancelable(false);
		pdialog.show();
		
		String url = null;
		url = UrlGenerator.updateToken();
				
		params.add("iUserID", strUserID);
		params.add("token", strToken);
		params.add("feedId", strFeedID);
		params.add("cost", strCost);
		params.add("vPayment", strPayment);
			
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
				userCancelTrip(strFeedID);
				updateBalance(strUserID, strCost);
			}
		});
	}

	private void alertReportDriver() {
		// TODO Auto-generated method stub
		new AlertDialog.Builder(ReceiptActivity.this)
		.setTitle(getResources().getString(R.string.strreporttitle))
		.setMessage(getResources().getString(R.string.strreportmessage))
		.setCancelable(false)
		.setPositiveButton(getResources().getString(R.string.strreport),
			new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					if (cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isAvailable() && cm.getActiveNetworkInfo().isConnected()) {
						report(strFeedID, strUserID);
						feedback(strFeedID, 2, "R");
					} else {
						networkConnectivity();
					}
				}
			})
		.setNegativeButton(getResources().getString(R.string.strcancel),
			new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			}).show();
	}

	protected void report(String strFeedID, String strUserID) {
		// TODO Auto-generated method stub
		AsyncHttpClient client = new AsyncHttpClient();
		RequestParams params = new RequestParams();
		
		// Show ProgressBar
		final ProgressDialog pdialog = new ProgressDialog(ReceiptActivity.this);
		pdialog.setMessage("Loading...");
		pdialog.setCancelable(false);
		pdialog.show();
		
		String url = null;
		url = UrlGenerator.report();
		
		params.add("iFeedID", strFeedID);
		params.add("strUserID", strUserID);
						
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
					//Toast.makeText(ReceiptActivity.this, "Requested resource not found", Toast.LENGTH_LONG).show();
				} else if (statusCode == 500) {
					//Toast.makeText(ReceiptActivity.this, "Something went wrong at server end", Toast.LENGTH_LONG).show();
				} else {
					networkConnectivity();
				}
			}
		});
	}
	
	
	protected void feedback(String strFeedID, int feedback_rating, String strType) {
		// TODO Auto-generated method stub
		String strFeedRating;
		AsyncHttpClient client = new AsyncHttpClient();
		RequestParams params = new RequestParams();
		
		// Show ProgressBar
		final ProgressDialog pdialog = new ProgressDialog(ReceiptActivity.this);
		pdialog.setMessage("Loading...");
		pdialog.setCancelable(false);
		pdialog.show();
		
		String url = null;
		url = UrlGenerator.feedback();
		
		if(strType.equals("S") == true)
		{
			if(feedback_rating > 0) {
				feedback_rating = feedback_rating + 1;
				strFeedRating = String.valueOf(feedback_rating);
			} else {
				strFeedRating = "unrated";
			}
		}
		else
		{
			strFeedRating = String.valueOf(feedback_rating);
		}
		
		params.add("iFeedID", strFeedID);
		params.add("feedback", strFeedRating);
		
		client.get(url, params, new AsyncHttpResponseHandler() {

			@Override
			public void onSuccess(String response) {
				// Hide ProgressBar
				pdialog.hide();
				Intent i_receipe = new Intent(ReceiptActivity.this, MainActivity.class);
				startActivity(i_receipe);
				ReceiptActivity.this.finish();
			}

			// When error occured
			@Override
			public void onFailure(int statusCode, Throwable error, String content) {
				// TODO Auto-generated method stub
				// Hide ProgressBar
				pdialog.hide();
				if (statusCode == 404) {
					//Toast.makeText(ReceiptActivity.this, "Requested resource not found", Toast.LENGTH_LONG).show();
				} else if (statusCode == 500) {
					//Toast.makeText(ReceiptActivity.this, "Something went wrong at server end", Toast.LENGTH_LONG).show();
				} else {
					networkConnectivity();
				}
			}
		});
	}
	
	private void userCancelTrip(String strFeedId) {
		// TODO Auto-generated method stub
		AsyncHttpClient client = new AsyncHttpClient();
		RequestParams params = new RequestParams();
		
		String url = null;
		url = UrlGenerator.UserDeleteRide();
		
		params.add("iFeedID", strFeedId);
						
		client.get(url, params, new AsyncHttpResponseHandler() {

			@Override
			public void onSuccess(String response) {
				// Hide ProgressBar
			}

			// When error occured
			@Override
			public void onFailure(int statusCode, Throwable error, String content) {
				// TODO Auto-generated method stub
			}
		});
	}
	
	private void updateBalance(String strUserID, String strCost) {
		// TODO Auto-generated method stub
		AsyncHttpClient client = new AsyncHttpClient();
		RequestParams params = new RequestParams();
		
		String url = null;
		url = UrlGenerator.UserDeleteRide();
		
		params.add("iUserID", strUserID);
		params.add("balance", strCost);
						
		client.get(url, params, new AsyncHttpResponseHandler() {

			@Override
			public void onSuccess(String response) {
				// Hide ProgressBar
			}

			// When error occured
			@Override
			public void onFailure(int statusCode, Throwable error, String content) {
				// TODO Auto-generated method stub
			}
		});
	}
	
	private void networkConnectivity()
	{
		new AlertDialog.Builder(ReceiptActivity.this)
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