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
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.Toast;

import com.app.waytogo.support.UrlGenerator;
import com.devmarvel.creditcardentry.library.CardValidCallback;
import com.devmarvel.creditcardentry.library.CreditCard;
import com.devmarvel.creditcardentry.library.CreditCardForm;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.stripe.android.Stripe;
import com.stripe.android.TokenCallback;
import com.stripe.android.model.Card;
import com.stripe.android.model.Token;
import com.stripe.exception.AuthenticationException;

import org.json.JSONObject;

public class CreditCardRegisterActivity extends AppCompatActivity {

	private Toolbar mToolbar;
	private ConnectivityManager cm;
	private Stripe stripe;
	private SharedPreferences mySharedPreferences;
	private Editor editor;
	private String USER = "user_pref";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_credit_card_register);
		
		cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		
		mToolbar = (Toolbar) findViewById(R.id.toolbar);

		setSupportActionBar(mToolbar);
		getSupportActionBar().setDisplayShowHomeEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setTitle("Card Register");
		
		mySharedPreferences = getSharedPreferences(USER, Context.MODE_PRIVATE);
		editor = mySharedPreferences.edit();
		
		CreditCardForm zipForm = (CreditCardForm) findViewById(R.id.form_with_zip);
		zipForm.setOnCardValidCallback(cardValidCallback);
		
	}
	
	CardValidCallback cardValidCallback = new CardValidCallback() {
		@Override
		public void cardValid(CreditCard card) {
			//Toast.makeText(CreditCardRegisterActivity.this, "Card valid and complete", Toast.LENGTH_SHORT).show();
			if (cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isAvailable() && cm.getActiveNetworkInfo().isConnected()) {
				getToken(card);
			} else {
				networkConnectivity();
			}
		}
		
	};
	
	protected void getToken(CreditCard cc) {
		// TODO Auto-generated method stub
		
		String strCCNumber = cc.getCardNumber();
		int mCCMonth = cc.getExpMonth();
		int mCCYear = cc.getExpYear();
		String strCCCode = cc.getSecurityCode();
		final String strCCType = cc.getCardType().toString();
		
		Card card = new Card(strCCNumber, mCCMonth, mCCYear, strCCCode);

		try {
			stripe = new Stripe("pk_test_###YOURVALUE");
		} catch (AuthenticationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		stripe.createToken(
		  card,
		  new TokenCallback() {
		      public void onSuccess(Token token) {
		          // Send token to your server
		    	  String strTokenID = token.getId();
		    	  String strLastFour = token.getCard().getLast4();
		    	  updateLast4Digits(strLastFour, strCCType, strTokenID);
		      }
		      public void onError(Exception error) {
		          // Show localized error message
		          
		      }
		  }
		);
	}

	protected void updateLast4Digits(final String strLastFourDigits, String strCardType, final String strTokenID) {
		// TODO Auto-generated method stub
		AsyncHttpClient client = new AsyncHttpClient();
		// Http Request Params Object
		RequestParams params = new RequestParams();
		
		// Show ProgressBar
		final ProgressDialog pdialog = new ProgressDialog(CreditCardRegisterActivity.this);
		pdialog.setMessage("Updating card details...");
		pdialog.setCancelable(false);
		pdialog.show();
		
		String url = null;
		url = UrlGenerator.updatelastfourdigits();
		
		params.add("iUserID", mySharedPreferences.getString("strUserId", ""));
		params.add("lastfour", strLastFourDigits);
		params.add("typeofcard", strCardType);
		
		client.post(url, params, new AsyncHttpResponseHandler() {

			@Override
			public void onSuccess(String response) {
				// Hide ProgressBar
				pdialog.hide();				
				
				JSONObject jObject;
				try {
					jObject = new JSONObject(response);
					
					String strStatus = jObject.optString("status");
					
					if(strStatus.equals("0") == true) 
					{
						editor.putString("strLastFour", strLastFourDigits);
						editor.commit();
						
						updateTokenID(strTokenID);
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
					//Toast.makeText(CreditCardRegisterActivity.this, "Requested resource not found", Toast.LENGTH_LONG).show();
				} else if (statusCode == 500) {
					//Toast.makeText(CreditCardRegisterActivity.this, "Something went wrong at server end", Toast.LENGTH_LONG).show();
				} else {
					networkConnectivity();
				}
			}
		});
	}

	protected void updateTokenID(final String strToken) {
		// TODO Auto-generated method stub
		AsyncHttpClient client = new AsyncHttpClient();
		// Http Request Params Object
		RequestParams params = new RequestParams();
		
		// Show ProgressBar
		final ProgressDialog pdialog = new ProgressDialog(CreditCardRegisterActivity.this);
		pdialog.setMessage("Loading...");
		pdialog.setCancelable(false);
		pdialog.show();
		
		String url = null;
		url = UrlGenerator.updateUserToken();
		
		params.add("iUserID", mySharedPreferences.getString("strUserId", ""));
		params.add("token", strToken);
		params.add("vEmail", mySharedPreferences.getString("strEmail", ""));
		
		client.post(url, params, new AsyncHttpResponseHandler() {

			@Override
			public void onSuccess(String response) {
				// Hide ProgressBar
				pdialog.hide();
				
				JSONObject jObject;
				try {
					jObject = new JSONObject(response);
					
					String strStatus = jObject.optString("status");
					
					if(strStatus.equals("0") == true) 
					{
						editor.putString("strToken", strToken);
						editor.commit();
						
						Intent i_main = new Intent(CreditCardRegisterActivity.this, MainActivity.class);
						i_main.putExtra("page", 1);
						startActivity(i_main);
						CreditCardRegisterActivity.this.finish();
						Toast.makeText(CreditCardRegisterActivity.this, R.string.strCardRegisteredSuccess, Toast.LENGTH_LONG).show();
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
					//Toast.makeText(CreditCardRegisterActivity.this, "Requested resource not found", Toast.LENGTH_LONG).show();
				} else if (statusCode == 500) {
					//Toast.makeText(CreditCardRegisterActivity.this, "Something went wrong at server end", Toast.LENGTH_LONG).show();
				} else {
					networkConnectivity();
				}
			}
		});
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		supportInvalidateOptionsMenu();
		int id = item.getItemId();

		if (id == android.R.id.home) {
			Intent i_main = new Intent(CreditCardRegisterActivity.this, MainActivity.class);
			i_main.putExtra("page", 1);
			startActivity(i_main);
			CreditCardRegisterActivity.this.finish();
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		super.onBackPressed();
		
		Intent i_main = new Intent(CreditCardRegisterActivity.this, MainActivity.class);
		i_main.putExtra("page", 1);
		startActivity(i_main);
		CreditCardRegisterActivity.this.finish();
	}
	
	private void networkConnectivity()
	{
        new AlertDialog.Builder(CreditCardRegisterActivity.this)
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
