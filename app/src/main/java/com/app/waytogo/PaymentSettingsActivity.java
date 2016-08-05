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
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.app.waytogo.support.UrlGenerator;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PaymentSettingsActivity extends AppCompatActivity {

	private Toolbar mToolbar;
	private ConnectivityManager cm;
	private RelativeLayout rl_main, rl_paypal, rl_bank;
	private TextView tv_current_earnings, tv_paypal_pay, tv_direct_pay;
	private EditText edt_paypal, edt_routing, edt_account;
	private Spinner spin_bank;
	private String strBank, strRouting, strAccount;
	private SharedPreferences myPreferences;
	private Editor editor;
	private String USER = "user_pref";
	private int flag = 0;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_payment_settings);
		
		cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		
		mToolbar = (Toolbar) findViewById(R.id.toolbar);

		setSupportActionBar(mToolbar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setDisplayUseLogoEnabled(true);
		getSupportActionBar().setDisplayShowHomeEnabled(true);
		getSupportActionBar().setTitle("Payout Settings");
		
		myPreferences = getSharedPreferences(USER, MODE_PRIVATE);
		editor = myPreferences.edit();
		
		rl_main = (RelativeLayout) findViewById(R.id.rl_main);
		rl_paypal = (RelativeLayout) findViewById(R.id.rl_paypal);
		rl_bank = (RelativeLayout) findViewById(R.id.rl_bank);
		tv_current_earnings = (TextView) findViewById(R.id.tv_current_earnings);
		tv_paypal_pay = (TextView) findViewById(R.id.tv_paypal_pay);
		tv_direct_pay = (TextView) findViewById(R.id.tv_direct_pay);
		edt_paypal = (EditText) findViewById(R.id.edt_paypal);
		edt_routing = (EditText) findViewById(R.id.edt_routing);
		edt_account = (EditText) findViewById(R.id.edt_account);
		spin_bank = (Spinner) findViewById(R.id.spin_bank); 
		
		//To update the current earnings
		getDriverEarnings();
		
		tv_paypal_pay.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				flag = 1;
				rl_main.setVisibility(View.GONE);
				rl_paypal.setVisibility(View.VISIBLE);
				rl_bank.setVisibility(View.GONE);
			}
		});
		
		tv_direct_pay.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				flag = 1;
				rl_main.setVisibility(View.GONE);
				rl_paypal.setVisibility(View.GONE);
				rl_bank.setVisibility(View.VISIBLE);
			}
		});
		
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(PaymentSettingsActivity.this,R.array.bank_name, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		
		spin_bank.setAdapter(adapter);
		spin_bank.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,int position, long id) {
				// TODO Auto-generated method stub
				strBank = parent.getItemAtPosition(position).toString();
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
				
			}
		});
		
		edt_paypal.setOnEditorActionListener(new OnEditorActionListener() {
			
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				// TODO Auto-generated method stub
				if(actionId == EditorInfo.IME_ACTION_DONE) {
					
					String strEmail = v.getText().toString();
					
					if(emailValidator(strEmail) != true) {
						Toast.makeText(PaymentSettingsActivity.this, getResources().getString(R.string.strinvalidemail), Toast.LENGTH_LONG).show();
					} else {
						updatePaypalAddress(strEmail);
					}
					
				}
				return false;
			}
		});
		
		edt_routing.setOnEditorActionListener(new OnEditorActionListener() {
			
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				// TODO Auto-generated method stub
				if(actionId == EditorInfo.IME_ACTION_NEXT) {
					strRouting = v.getText().toString();
				}
				return false;
			}
		});
		
		edt_account.setOnEditorActionListener(new OnEditorActionListener() {
			
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				// TODO Auto-generated method stub
				if(actionId == EditorInfo.IME_ACTION_DONE) {
					strAccount = v.getText().toString();
					if(strRouting.equals("") == true) {
						Toast.makeText(PaymentSettingsActivity.this, getResources().getString(R.string.strroutingempty), Toast.LENGTH_LONG).show();
					}
					else if(strAccount.equals("") == true) {
						Toast.makeText(PaymentSettingsActivity.this, getResources().getString(R.string.straccountempty), Toast.LENGTH_LONG).show();
					}
					else if(strBank.equals("Select your Bank") == true) {
						Toast.makeText(PaymentSettingsActivity.this, getResources().getString(R.string.strbankempty), Toast.LENGTH_LONG).show();
					}
					else {
						updateDriverBank(strRouting, strAccount, strBank);
					}
				}
				return false;
			}
		});
		
	}
	
	private void updatePaypalAddress(String email) {
		// TODO Auto-generated method stub
		// Create AsycHttpClient object
		AsyncHttpClient client = new AsyncHttpClient();
		// Http Request Params Object
		RequestParams params = new RequestParams();
		
		// Show ProgressBar
		final ProgressDialog pdialog = new ProgressDialog(PaymentSettingsActivity.this);
		pdialog.setMessage("Updating your PayPal address...");
		pdialog.setCancelable(false);
		pdialog.show();
		// Make Http call to getusers.php
				
		String url;
		
		url = UrlGenerator.updatePaypalAddress();
		
		params.add("paypal", email);
		params.add("iDriverID", myPreferences.getString("strUserId", ""));
		
		client.get(url, params, new AsyncHttpResponseHandler() {

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
						
						rl_main.setVisibility(View.VISIBLE);
						rl_paypal.setVisibility(View.GONE);
						rl_bank.setVisibility(View.GONE);
						
						Toast.makeText(PaymentSettingsActivity.this, getResources().getString(R.string.strpaypalsuccess), Toast.LENGTH_LONG).show();
					} else {
						Toast.makeText(PaymentSettingsActivity.this, strMessage, Toast.LENGTH_LONG).show();
					}
					
				} catch (JSONException e) {
					// TODO Auto-generated catch block
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
					//Toast.makeText(LoginActivity.this, "Requested resource not found", Toast.LENGTH_LONG).show();
				} else if (statusCode == 500) {
					//Toast.makeText(LoginActivity.this, "Something went wrong at server end", Toast.LENGTH_LONG).show();
				} else {
					networkConnectivity();
				}
			}
		});
	}
	
	private void updateDriverBank(String strRouting, String strAccount, String strBank) {
		// TODO Auto-generated method stub
		// Create AsycHttpClient object
		AsyncHttpClient client = new AsyncHttpClient();
		// Http Request Params Object
		RequestParams params = new RequestParams();
		
		// Show ProgressBar
		final ProgressDialog pdialog = new ProgressDialog(PaymentSettingsActivity.this);
		pdialog.setMessage("Updating bank details...");
		pdialog.setCancelable(false);
		pdialog.show();
		// Make Http call to getusers.php
				
		String url;
		
		url = UrlGenerator.updateDriverBank();
		
		params.add("routingnr", strRouting);
		params.add("accountnr", strAccount);
		params.add("nameofbank", strBank);
		params.add("iDriverID", myPreferences.getString("strUserId", ""));
		
		client.get(url, params, new AsyncHttpResponseHandler() {

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
						
						rl_main.setVisibility(View.VISIBLE);
						rl_paypal.setVisibility(View.GONE);
						rl_bank.setVisibility(View.GONE);
						
						Toast.makeText(PaymentSettingsActivity.this, getResources().getString(R.string.strbanksuccess), Toast.LENGTH_LONG).show();
					} else {
						Toast.makeText(PaymentSettingsActivity.this, strMessage, Toast.LENGTH_LONG).show();
					}
					
				} catch (JSONException e) {
					// TODO Auto-generated catch block
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
					//Toast.makeText(LoginActivity.this, "Requested resource not found", Toast.LENGTH_LONG).show();
				} else if (statusCode == 500) {
					//Toast.makeText(LoginActivity.this, "Something went wrong at server end", Toast.LENGTH_LONG).show();
				} else {
					networkConnectivity();
				}
			}
		});
	}
	
	//To update payment confirmation
	public void getDriverEarnings()
	{
		// TODO Auto-generated method stub
		AsyncHttpClient client = new AsyncHttpClient();
		// Http Request Params Object
		
		// Show ProgressBar
		final ProgressDialog pdialog = new ProgressDialog(PaymentSettingsActivity.this);
		pdialog.setMessage("Calculating your earnings...");
		pdialog.setCancelable(false);
		pdialog.show();
				
		RequestParams params = new RequestParams();
		
		String url = null;
		url = UrlGenerator.getDriverEarnings();
		
		params.add("iDriverID", myPreferences.getString("strUserId", ""));
		
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
						DecimalFormat df = new DecimalFormat("#.##");
						String strData = jObject.optString("data");
						Double dEarnings  = Double.valueOf(strData);
						String strCurrentEarnings = df.format(dEarnings);
						tv_current_earnings.setText("$ "+strCurrentEarnings);
					}
												
				} catch (Exception e) {e.printStackTrace();}
			}

			// When error occured
			@Override
			public void onFailure(int statusCode, Throwable error, String content) {
				// TODO Auto-generated method stub
				pdialog.hide();
				if (statusCode == 404) {
					//Toast.makeText(DriverCourierActivity.this, "Requested resource not found", Toast.LENGTH_LONG).show();
				} else if (statusCode == 500) {
					//Toast.makeText(DriverCourierActivity.this, "Something went wrong at server end", Toast.LENGTH_LONG).show();
				} else {
					networkConnectivity();
				}
			}
		});
	}
	
	private void networkConnectivity() {
		// TODO Auto-generated method stub
		new AlertDialog.Builder(PaymentSettingsActivity.this)
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
	
	public static boolean emailValidator(final String mailAddress) {
			
	    Pattern pattern;
	    Matcher matcher;
	
	    final String EMAIL_PATTERN = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@" + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
	
	    pattern = Pattern.compile(EMAIL_PATTERN);
	    matcher = pattern.matcher(mailAddress);
	    return matcher.matches();
	
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();

		if (id == android.R.id.home) {
			if(flag == 1) {
				flag = 0;
				rl_main.setVisibility(View.VISIBLE);
				rl_paypal.setVisibility(View.GONE);
				rl_bank.setVisibility(View.GONE);
			} else {
				Intent i_pay = new Intent(PaymentSettingsActivity.this, MainActivity.class);
				i_pay.putExtra("page", 4);
				startActivity(i_pay);
				PaymentSettingsActivity.this.finish();
			}
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		if(flag == 1) {
			flag = 0;
			rl_main.setVisibility(View.VISIBLE);
			rl_paypal.setVisibility(View.GONE);
			rl_bank.setVisibility(View.GONE);
		} else {
			Intent i_pay = new Intent(PaymentSettingsActivity.this, MainActivity.class);
			i_pay.putExtra("page", 4);
			startActivity(i_pay);
			PaymentSettingsActivity.this.finish();
		}
	}
}
