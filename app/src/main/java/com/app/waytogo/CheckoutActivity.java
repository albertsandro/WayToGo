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
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.app.waytogo.adapter.DBAdapter;
import com.app.waytogo.model.TripDetails;
import com.app.waytogo.support.UrlGenerator;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;


public class CheckoutActivity extends AppCompatActivity {

	private Toolbar mToolbar;
	private ConnectivityManager cm;
	private ListView lv_checkout;
	private TextView tv_address, tv_subtotal, tv_tax, tv_tip, tv_delivery_charge, tv_total;
	private Button btn_pay;
	private LinearLayout rl_bottom;
	private DBAdapter dbadapter;
	private ArrayList<HashMap<String, String>> arrCart = new ArrayList<HashMap<String, String>>();
	private String strRestaurantName, strStreetAddress, strID, strCurrentLat, strCurrentLong;
	private CartAdapter mAdapter;
	private DecimalFormat df_coordinates = new DecimalFormat("#.######");
	private DecimalFormat df_cost = new DecimalFormat("#.##");
	private SharedPreferences myPreferences;
	private Editor editor;
	private String USER = "user_pref";
	private String str_RestaurantName, str_RestaurantAddress, str_RestaurantLat, str_RestaurantLng;
	private Timer timer, timerWaitForDriver, timerGrabbingDriverLocation;
	private String strFeedId, strUserView, strTotal;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_order_checkout);
		
		cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		
		mToolbar = (Toolbar) findViewById(R.id.toolbar);

		setSupportActionBar(mToolbar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setDisplayUseLogoEnabled(true);
		getSupportActionBar().setDisplayShowHomeEnabled(true);
		getSupportActionBar().setTitle("Checkout");
		
		Bundle b = getIntent().getExtras();
		
		strRestaurantName = b.getString("strRestaurantName");
		strStreetAddress = b.getString("strStreetAddress");
		strID = b.getString("strID");
		strCurrentLat = b.getString("strCurrentLat");
		strCurrentLong = b.getString("strCurrentLong");
		
		myPreferences = getSharedPreferences(USER, MODE_PRIVATE);
		editor = myPreferences.edit();
		
		try {
			dbadapter = new DBAdapter(CheckoutActivity.this);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		rl_bottom = (LinearLayout) findViewById(R.id.rl_bottom);
		lv_checkout = (ListView) findViewById(R.id.lv_checkout);
		tv_subtotal = (TextView) findViewById(R.id.tv_subtotal);
		tv_tax = (TextView) findViewById(R.id.tv_tax);
		tv_tip = (TextView) findViewById(R.id.tv_tip);
		tv_delivery_charge = (TextView) findViewById(R.id.tv_delivery_charge);
		tv_total = (TextView) findViewById(R.id.tv_total);
		tv_address = (TextView) findViewById(R.id.tv_address);
		btn_pay = (Button) findViewById(R.id.btn_pay);
				
		tv_address.setText(strStreetAddress);
		
		mAdapter = new CartAdapter(CheckoutActivity.this);
		lv_checkout.setAdapter(mAdapter);
		
		btn_pay.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isAvailable() && cm.getActiveNetworkInfo().isConnected()) {
					
					if(strTotal.equals("5") == true) {
						Toast.makeText(CheckoutActivity.this, R.string.strAddCart, Toast.LENGTH_SHORT).show();
					} else {
						//To check whether CC registered or not
						String strToken = myPreferences.getString("strToken", "");
						if(strToken.equals("") == false) {
							//Redirect to register CC
							NoCrediCardRegistered();
						} else {
							//Send request to driver
							arrCart = dbadapter.fetchCartItems();
							
							ArrayList<String> arrItemName = new ArrayList<String>();
							ArrayList<String> arrItemPrice = new ArrayList<String>();
							ArrayList<String> arrItemQty = new ArrayList<String>();
							ArrayList<String> arrSpl = new ArrayList<String>();
							
							for(int i = 0 ; i < arrCart.size(); i++)
							{
								arrItemName.add(arrCart.get(i).get("str_item_name"));
								arrItemPrice.add(arrCart.get(i).get("str_item_price"));
								arrItemQty.add(arrCart.get(i).get("str_item_qty"));
								arrSpl.add(arrCart.get(i).get("str_rest_spl_instructions"));
								str_RestaurantName = arrCart.get(i).get("str_rest_locality");
								str_RestaurantAddress = arrCart.get(i).get("str_rest_address");
								str_RestaurantLat = arrCart.get(i).get("str_rest_lat");
								str_RestaurantLng = arrCart.get(i).get("str_rest_lng");
							}
							
							RequestTrip(arrItemName, arrItemPrice, arrItemQty, arrSpl, str_RestaurantName, str_RestaurantAddress, str_RestaurantLat, str_RestaurantLng, strTotal);
						}
					}
				}
				else
				{
					networkConnectivity();
				}
			}
		});
	}
	
	protected void NoCrediCardRegistered() {
		// TODO Auto-generated method stub
		new AlertDialog.Builder(CheckoutActivity.this)
		.setMessage(getResources().getString(R.string.strcredicard))
		.setCancelable(false)
		.setPositiveButton(getResources().getString(R.string.strgotit),
				new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			}).show();
	}

	//Request Rider Trip
	private void RequestTrip(ArrayList<String> arrItemName, ArrayList<String> arrItemPrice, ArrayList<String> arrItemQty, ArrayList<String> arrSpl,
			String strRestName, String strRestAddress, String strRestLat, String strRestLng, String strTotalCost) {
		// TODO Auto-generated method stub
		AsyncHttpClient client = new AsyncHttpClient();
		RequestParams params = new RequestParams();
		
		// Show ProgressBar
		final ProgressDialog pdialog = new ProgressDialog(CheckoutActivity.this);
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
		
		JSONArray jArrayItemName = new JSONArray(arrItemName);
		JSONArray jArrayItemQty = new JSONArray(arrItemQty);
		JSONArray jArrayItemPrice = new JSONArray(arrItemPrice);
		JSONArray jArraySpl = new JSONArray(arrSpl);
				
		params.add("iUserID", myPreferences.getString("strUserId", ""));
		params.add("vFeedTitle", myPreferences.getString("strUserPhone", ""));
		params.add("fLat", String.valueOf(strRestLat));
		params.add("fLong", String.valueOf(strRestLng));
		params.add("toLat", String.valueOf(strCurrentLat));
		params.add("toLong", String.valueOf(strCurrentLong));
		params.add("tFeedDescription", String.valueOf(jArrayItemName));
		params.add("foodQuantity", String.valueOf(jArrayItemQty));
		params.add("vCost", String.valueOf(strTotalCost));
		params.add("specialInst", String.valueOf(jArraySpl));
		params.add("nameofRest", String.valueOf(strRestName));
		params.add("addressofRest", String.valueOf(strRestAddress));
		params.add("foodPrices", String.valueOf(jArrayItemPrice));
		params.add("vPayment", "Paypal");
		params.add("vUserView", "food");
		
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
						if(strMessage.equals("No current route") == true) {
							showAletNoDriver(); 
						} else {
							showWaitingForDriver();
						}
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
					//Toast.makeText(CheckoutActivity.this, "Requested resource not found", Toast.LENGTH_LONG).show();
				} else if (statusCode == 500) {
					//Toast.makeText(CheckoutActivity.this, "Something went wrong at server end", Toast.LENGTH_LONG).show();
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
		final ProgressDialog pdialogaccept = new ProgressDialog(CheckoutActivity.this);
		pdialogaccept.setMessage("Waiting for a driver...");
		pdialogaccept.setCancelable(false);
		pdialogaccept.show();
		
		timerWaitForDriver = new Timer();
		timerWaitForDriver.schedule(new TimerTask() {

			@Override
			public void run() {
				
				 CheckoutActivity.this.runOnUiThread(new Runnable() {
					    public void run() {
		
						String url = null;
						try {
							url = UrlGenerator.userFeedList();
						} catch (UnsupportedEncodingException e) {
							e.printStackTrace();
						}
						
						params.add("iUserID", myPreferences.getString("strUserId", ""));
										
						client.get(url, params, new AsyncHttpResponseHandler() {
				
							@Override
							public void onSuccess(String response) {
								// Hide ProgressBar
								
								JSONObject jObject;
								try {
									jObject = new JSONObject(response);
									
									String strStatus = jObject.optString("status");
									String strMessage = jObject.optString("message");
									 
									if(strStatus.equals("0") == true) {
										
										try {
											GsonBuilder gsonBuilder = new GsonBuilder();
											Gson gson = gsonBuilder.create();
							
											TripDetails trip = gson.fromJson(response, TripDetails.class);
											
											strFeedId = trip.data.get(0).iFeedID;
											strUserView = trip.data.get(0).vUserView;
											
											if(strUserView.equals("friends") == true)
											{
												pdialogaccept.hide();
												timerWaitForDriver.cancel();
												timer = new Timer();
											    timer.schedule(new TimerTask() {
				
													@Override
													public void run() {
														CheckoutActivity.this.runOnUiThread(new Runnable() {
															public void run() {
																// TODO Auto-generated method stub
															    driverAccepted(strFeedId);
															}
														});
													}
											    	
											    },0,5000);
												
											}
											
										} catch(NullPointerException e) { e.printStackTrace(); }
									}
									
								} catch (Exception e) {e.printStackTrace();}
							}
				
							// When error occured
							@Override
							public void onFailure(int statusCode, Throwable error, String content) {
								// TODO Auto-generated method stub
								// Hide ProgressBar
								if (statusCode == 404) {
									//Toast.makeText(CheckoutActivity.this, "Requested resource not found", Toast.LENGTH_LONG).show();
								} else if (statusCode == 500) {
									//Toast.makeText(CheckoutActivity.this, "Something went wrong at server end", Toast.LENGTH_LONG).show();
								} else {
									networkConnectivity();
								}
							}
						});
					}
				 });		    
			}
	    },0,10000);
	}
	
	private void driverAccepted(final String strFeedId) {
		// TODO Auto-generated method stub
		timer.cancel();//To cancel the timer task
		
		final AsyncHttpClient client = new AsyncHttpClient();
		final RequestParams params = new RequestParams();
		
		// Show ProgressBar
		final ProgressDialog pdialogGrabbing = new ProgressDialog(CheckoutActivity.this);
		pdialogGrabbing.setMessage("Driver accepted...");
		pdialogGrabbing.setCancelable(false);
		pdialogGrabbing.show();
		
		timerGrabbingDriverLocation = new Timer();
		timerGrabbingDriverLocation.schedule(new TimerTask() {

			@Override
			public void run() {
				
				CheckoutActivity.this.runOnUiThread(new Runnable() {
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
									
									String strMyCurrentLat = jObject.optString("currentLat");
									String strMyCurrentLong = jObject.optString("currentLong");
									
									if(strCurrentLat.equals(strCurrentLong) == true)
									{
										//Grabbing the driver location
										pdialogGrabbing.setMessage("Grabbing the drivers location");
									}
									else
									{
										pdialogGrabbing.hide();
										timerGrabbingDriverLocation.cancel();
										Intent i_rider = new Intent(CheckoutActivity.this, RiderPickUpActivity.class);
										try {
						
											i_rider.putExtra("strFeedId", strFeedId);
											i_rider.putExtra("fLat", str_RestaurantLat);
											i_rider.putExtra("fLong", str_RestaurantLng);
											i_rider.putExtra("toLat", strCurrentLat);
											i_rider.putExtra("toLong", strCurrentLong);
											i_rider.putExtra("currentLat", strMyCurrentLat);
											i_rider.putExtra("currentLong", strMyCurrentLong);
											
										} catch(Exception e) {e.printStackTrace();}
										
										startActivity(i_rider);
									}
														
								} catch (Exception e) {e.printStackTrace();}
							}
				
							// When error occured
							@Override
							public void onFailure(int statusCode, Throwable error, String content) {
								// TODO Auto-generated method stub
								// Hide ProgressBar
								pdialogGrabbing.hide();
								if (statusCode == 404) {
									//Toast.makeText(CheckoutActivity.this, "Requested resource not found", Toast.LENGTH_LONG).show();
								} else if (statusCode == 500) {
									//Toast.makeText(CheckoutActivity.this, "Something went wrong at server end", Toast.LENGTH_LONG).show();
								} else {
									networkConnectivity();
								}
							}
						});
					}
				});
			}
		},0,5000);
	}
	
	
	protected void showAletNoDriver() {
		// TODO Auto-generated method stub
		new AlertDialog.Builder(CheckoutActivity.this)
		.setMessage(getResources().getString(R.string.strnofooddriver))
		.setCancelable(false)
		.setPositiveButton(getResources().getString(R.string.strgotit),
				new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			}).show();
	}


	public class CartAdapter extends BaseAdapter {

		private Context mContext;
		private ArrayList<HashMap<String, String>> arrCart;
		private LayoutInflater inflater;
		private DBAdapter dbAdapter;
		private double mSubTotal = 0;
		private int isDeleted = 0; 
		
		public CartAdapter(Context c) {
			// TODO Auto-generated constructor stub
			mContext = c;
			arrCart = dbadapter.fetchCartItems();
			//mSubTotal = dbadapter.getCartSubTotal();
			
			inflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			try {
				dbAdapter = new DBAdapter(mContext);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return arrCart.size();
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

	    public class ViewHolder {
	    	public TextView tv_item_name, tv_item_qty, tv_item_price;
			public ImageView iv_delete;
		}
		
		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			final ViewHolder holder;
			
			mSubTotal = dbadapter.getCartSubTotal();
			
			if (convertView == null) {
	            convertView = inflater.inflate(R.layout.order_checkout_item, null);
	            holder = new ViewHolder();
	 
			    holder.tv_item_name = (TextView) convertView.findViewById(R.id.tv_item_name);
			    holder.tv_item_qty = (TextView) convertView.findViewById(R.id.tv_item_qty);
			    holder.tv_item_price = (TextView) convertView.findViewById(R.id.tv_item_price);
			    holder.iv_delete = (ImageView) convertView.findViewById(R.id.iv_delete);
			    		    
		        convertView.setTag(holder);
		    } else {
		        holder = (ViewHolder) convertView.getTag();
		    }
			
			holder.tv_item_name.setText(arrCart.get(position).get("str_item_name"));
			holder.tv_item_qty.setText(arrCart.get(position).get("str_item_qty"));
			holder.tv_item_price.setText(arrCart.get(position).get("str_item_price"));
			
			if(isDeleted == 0)
			{
				double taxNumber = mSubTotal*0.10;
				double tipNumber = mSubTotal*0.15;
				
				String strTax = df_cost.format(taxNumber);
				String strTip = df_cost.format(tipNumber);
				
				tv_subtotal.setText("$"+mSubTotal);
				tv_tax.setText("$"+strTax);
				tv_tip.setText("$"+strTip);
				tv_delivery_charge.setText("$5.00");
				
				double mTotal = mSubTotal + taxNumber + tipNumber + 5.00;
				strTotal = df_cost.format(mTotal);
				
				tv_total.setText("$"+strTotal);
			}
			
			holder.iv_delete.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					
					isDeleted = 1;
					
					double mSubTotalDel = dbAdapter.removeCartItems(arrCart.get(position).get("str_id"));
					
					arrCart.remove(position);
					mAdapter.notifyDataSetChanged();
					lv_checkout.invalidateViews();
					rl_bottom.invalidate();
					
					double taxNumberDel = mSubTotalDel*0.10;
					double tipNumberDel = mSubTotalDel*0.15;
					
					String strTaxDel = df_cost.format(taxNumberDel);
					String strTipDel = df_cost.format(tipNumberDel);
					
					tv_subtotal.setText("$"+mSubTotalDel);
					tv_tax.setText("$"+strTaxDel);
					tv_tip.setText("$"+strTipDel);
					tv_delivery_charge.setText("$5.00");
					
					double mTotalDel = mSubTotalDel + taxNumberDel + tipNumberDel + 5.00;
					strTotal = df_cost.format(mTotalDel);
					
					tv_total.setText("$"+strTotal);
				}
			});
			
			return convertView;
		}
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();

		if (id == android.R.id.home) {
			CheckoutActivity.this.finish();
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		Intent i_details = new Intent(CheckoutActivity.this, RestaurantFoodMenuActivity.class);
		i_details.putExtra("strName", strRestaurantName);
		i_details.putExtra("strStreetAddress", strStreetAddress);
		i_details.putExtra("strID", strID);
		i_details.putExtra("strCurrentLat", strCurrentLat);
		i_details.putExtra("strCurrentLong", strCurrentLong);
		startActivity(i_details);
		CheckoutActivity.this.finish();
	}
	
	private void networkConnectivity()
	{
        new AlertDialog.Builder(CheckoutActivity.this)
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


