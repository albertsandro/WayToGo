package com.app.waytogo;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.SearchView.OnQueryTextListener;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import com.app.waytogo.adapter.DBAdapter;
import com.app.waytogo.adapter.NearestRestaurantAdapter;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;

public class ListNearestRestauratActivity extends AppCompatActivity {

	private Toolbar mToolbar;
	private ConnectivityManager cm;
	private ListView lv_restaurant;
	private String strLat, strLng, dt;
	private ArrayList<HashMap <String, String>> arrRestaurant = new ArrayList<HashMap<String, String>>();
	private DBAdapter dbadapter;
				
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_list_nearest_restaurant);
		
		mToolbar = (Toolbar) findViewById(R.id.toolbar);

		cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		
		setSupportActionBar(mToolbar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setDisplayUseLogoEnabled(true);
		getSupportActionBar().setDisplayShowHomeEnabled(true);
		getSupportActionBar().setTitle("Choose");
		
		Bundle b = getIntent().getExtras();
		strLat = b.getString("currentLat");
		strLng = b.getString("currentLng");
		
		lv_restaurant = (ListView) findViewById(R.id.lv_restaurant);
		
		try {
			dbadapter = new DBAdapter(ListNearestRestauratActivity.this);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//To delete the cart items
		dbadapter.clearCartItems();		
		
		DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		dt = dateFormatter.format(new java.util.Date());
		
		/*strLat = "37.707596";
		strLng = "-121.910701";*/
		
		if (cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isAvailable() && cm.getActiveNetworkInfo().isConnected()) {
			getNearestRestaurant(strLat, strLng, dt, "");
		} else {
			networkConnectivity();
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.restaurant_menu, menu);
		
		//Search Menu
        MenuItem searchItem = menu.findItem(R.id.action_search);
		SearchView mSearchView = (SearchView) MenuItemCompat.getActionView(searchItem);
		mSearchView.setQueryHint(getString(R.string.action_search));
	    mSearchView.setOnQueryTextListener(new OnQueryTextListener() {
			
			@Override
			public boolean onQueryTextSubmit(String strSearch) {
				// TODO Auto-generated method stub
				if (cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isAvailable() && cm.getActiveNetworkInfo().isConnected()) {
					getNearestRestaurant(strLat, strLng, dt, strSearch);
				} else {
					networkConnectivity();
				}
				return false;
			}
			
			@Override
			public boolean onQueryTextChange(String arg0) {
				// TODO Auto-generated method stub
				return false;
			}
		});
		
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		supportInvalidateOptionsMenu();
		int id = item.getItemId();

		if (id == R.id.action_search) {
			return true;
		}
		
		if (id == android.R.id.home) {
			ListNearestRestauratActivity.this.finish();
			return true;
		}
		
		return super.onOptionsItemSelected(item);
	}
	
	protected void getNearestRestaurant(final String str_Lat, final String str_Lng, String dt, String str_Search) {
		// TODO Auto-generated method stub
		final ProgressDialog pdialog = new ProgressDialog(ListNearestRestauratActivity.this);
		pdialog.setMessage("Loading restaurants near you...");
		pdialog.setCancelable(false);
		pdialog.show();
		
		AsyncHttpClient client = new AsyncHttpClient();
		// Http Request Params Object
		RequestParams params = new RequestParams();
		dt = dt.replace(" ", "%20");
		
		String url = null;
		
		if(str_Search.equals("") == true) {
			url = "https://api.locu.com/v1_0/venue/search/?location="+ str_Lat +","+ str_Lng + "&has_menu=TRUE&radius=10000&category=restaurant&open_at="+ dt +"&api_key=fede0683136f93a2a3b9a74b55a2f7be9fec5c15";
		} else {
			url = "https://api.locu.com/v1_0/venue/search/?location="+ str_Lat +","+ str_Lng + "&has_menu=TRUE&name="+ str_Search +"&radius=10000&category=restaurant&open_at="+ dt +"&api_key=fede0683136f93a2a3b9a74b55a2f7be9fec5c15";
		}
		
		client.get(url, params, new AsyncHttpResponseHandler() {

			@Override
			public void onSuccess(String response) {
				// Hide ProgressBar
				JSONObject jObject;
				JSONArray jArray;
				try {
					jObject = new JSONObject(response);
					String strObject = jObject.getString("objects"); 
					
					jArray = new JSONArray(strObject); 
					
					for(int i = 0; i < jArray.length(); i++)
					{
						HashMap<String, String> hMap = new HashMap<String, String>();
						JSONObject jObj = jArray.getJSONObject(i);
						
						String strName = jObj.optString("name");
						String strLocality = jObj.optString("locality");
						String strStreetAddress = jObj.optString("street_address");
						String strLat = jObj.optString("lat");
						String strLong = jObj.optString("long");
						String strID = jObj.optString("id");
						String strResourceUri = jObj.optString("resource_uri");
												
						hMap.put("strName",strName);
						hMap.put("strLocality",strLocality);
						hMap.put("strStreetAddress",strStreetAddress);
						hMap.put("strLat", strLat);
						hMap.put("strLong",strLong);
						hMap.put("strID", strID);
						hMap.put("strResourceUri", strResourceUri);
						
						arrRestaurant.add(hMap);
						
						if(i == jArray.length() - 1) {
							pdialog.hide();
							lv_restaurant.setAdapter(new NearestRestaurantAdapter(ListNearestRestauratActivity.this, arrRestaurant, str_Lat, str_Lng));
						}
					}
					
				} catch (Exception e) {e.printStackTrace();}
			}

			// When error occured
			@Override
			public void onFailure(int statusCode, Throwable error, String content) {
				// TODO Auto-generated method stub
				pdialog.hide();
				if (statusCode == 404) {
					//Toast.makeText(ListNearestRestauratActivity.this, "Requested resource not found", Toast.LENGTH_LONG).show();
				} else if (statusCode == 500) {
					//Toast.makeText(ListNearestRestauratActivity.this, "Something went wrong at server end", Toast.LENGTH_LONG).show();
				} else {
					networkConnectivity();
				}
			}
		});
	}
	
	
	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		ListNearestRestauratActivity.this.finish();
	}
	
	private void networkConnectivity()
	{
        new AlertDialog.Builder(ListNearestRestauratActivity.this)
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
