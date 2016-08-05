package com.app.waytogo;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
import android.widget.TextView;

import com.app.waytogo.adapter.DBAdapter;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;
import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

public class RestaurantFoodMenuActivity extends AppCompatActivity {

	private Toolbar mToolbar;
	private ConnectivityManager cm;
	private StickyListHeadersListView lv_restaurant_menu;
	private Button btn_checkout;
	private TextView tv_address;
	private String strRestaurantName, strStreetAddress, strID, strRestLocality, strRestAddress, strRestLat, strRestLong, strCurrentLat, strCurrentLong;
	private ArrayList<String> arrHeader = new ArrayList<String>();
	private ArrayList<HashMap <String, String>> arrItems = new ArrayList<HashMap<String, String>>();
	private DBAdapter dbadapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_restaurant_food_menu);
		
		mToolbar = (Toolbar) findViewById(R.id.toolbar);

		cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		
		setSupportActionBar(mToolbar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setDisplayUseLogoEnabled(true);
		getSupportActionBar().setDisplayShowHomeEnabled(true);
		
		try {
			dbadapter = new DBAdapter(getApplicationContext());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Bundle b = getIntent().getExtras();
		strRestaurantName = b.getString("strName");
		strStreetAddress = b.getString("strStreetAddress");
		strID = b.getString("strID");
		strCurrentLat = b.getString("strCurrentLat");
		strCurrentLong = b.getString("strCurrentLong");
				
		getSupportActionBar().setTitle(strRestaurantName);
		
		lv_restaurant_menu = (StickyListHeadersListView) findViewById(R.id.lv_restaurant_menu);
		btn_checkout = (Button) findViewById(R.id.btn_checkout);
		tv_address = (TextView) findViewById(R.id.tv_address);
		
		tv_address.setText(strStreetAddress);
		
		if (cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isAvailable() && cm.getActiveNetworkInfo().isConnected()) {
			getRestaurantMenu(strID);
		} else {
			networkConnectivity();
		}
		
		String retval = dbadapter.countCartItems();
		
		if(retval.equals("1") == true) {
			btn_checkout.setVisibility(View.VISIBLE);
		} else {
			btn_checkout.setVisibility(View.GONE);
		}
		
		btn_checkout.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent i_details = new Intent(RestaurantFoodMenuActivity.this, CheckoutActivity.class);
				i_details.putExtra("strRestaurantName", strRestaurantName);
				i_details.putExtra("strStreetAddress", strStreetAddress);
				i_details.putExtra("strID", strID);
				i_details.putExtra("strCurrentLat", strCurrentLat);
				i_details.putExtra("strCurrentLong", strCurrentLong);
				startActivity(i_details);
				RestaurantFoodMenuActivity.this.finish();
			}
		});
		
	}
	

	protected void getRestaurantMenu(final String strID) {
		// TODO Auto-generated method stub
		final ProgressDialog pdialog = new ProgressDialog(RestaurantFoodMenuActivity.this);
		pdialog.setMessage("Loading menu...");
		pdialog.setCancelable(false);
		pdialog.show();
		
		AsyncHttpClient client = new AsyncHttpClient();
		// Http Request Params Object
		RequestParams params = new RequestParams();
		
		String url = null;
		
		url = "https://api.locu.com/v1_0/venue/"+ strID +"/?api_key=fede0683136f93a2a3b9a74b55a2f7be9fec5c15";
		
		client.get(url, params, new AsyncHttpResponseHandler() {

			@Override
			public void onSuccess(String response) {
				// Hide ProgressBar
				JSONObject jObject, jMenus, jSections;
				JSONArray jArray, jArrayMenus, jArraySubSections, jArraySubsectionName;
				try {
					jObject = new JSONObject(response);
					String strObject = jObject.getString("objects");
					
					jArray = new JSONArray(strObject); 
					
					for(int i = 0; i < jArray.length(); i++)
					{
						JSONObject jObj = jArray.getJSONObject(i);
						String strMenus = jObj.optString("menus");
						strRestLocality = jObj.optString("locality");
						strRestAddress = jObj.optString("street_address");
						strRestLat = jObj.optString("lat");
						strRestLong = jObj.optString("long");
						
						jArrayMenus = new JSONArray(strMenus); 
						
						for(int j = 0; j < jArrayMenus.length(); j++)
						{
							JSONObject jObjMenu = jArrayMenus.getJSONObject(j);
							String strSections = jObjMenu.optString("sections");
							
							jArraySubSections = new JSONArray(strSections); 
							
							for(int k = 0; k < jArraySubSections.length(); k++)
							{
								JSONObject jObjSubSections = jArraySubSections.getJSONObject(k);
								String strSubSections = jObjSubSections.optString("subsections");
								String strSectionName = jObjSubSections.optString("section_name");
								
								jArraySubsectionName = new JSONArray(strSubSections); 
								
								for(int l = 0; l < jArraySubsectionName.length(); l++)
								{
									JSONObject jObjSubSectionsName = jArraySubsectionName.getJSONObject(l);
									String strSubSectionsName = jObjSubSectionsName.optString("subsection_name");
									String strContents = jObjSubSectionsName.optString("contents");
									
									arrHeader.add(strSubSectionsName);
									
									JSONArray jArrayContents = new JSONArray(strContents); 
									
									for(int m = 0; m < jArrayContents.length(); m++)
									{
										HashMap<String, String> hMap = new HashMap<String, String>();
										
										JSONObject jObjContents = jArrayContents.getJSONObject(m);
										String strName = jObjContents.optString("name");
										String strPrice = jObjContents.optString("price");
										String strType = jObjContents.optString("type");
										String strDescription = jObjContents.optString("description");
										
										if(strType.equals("ITEM") == true) {
											
											hMap.put("strName", strName);
											hMap.put("strPrice", strPrice);
											hMap.put("strDescription", strDescription);
											hMap.put("strSection", strSubSectionsName);
											
											arrItems.add(hMap);
										}
										
										if(l == jArraySubsectionName.length() - 1)
										{
											pdialog.hide();
											try {
												lv_restaurant_menu.setAdapter(new MyAdapter(RestaurantFoodMenuActivity.this, arrHeader, arrItems));
											} catch(ArrayIndexOutOfBoundsException e) {e.printStackTrace();}
										}
									}	
								}
							}
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
					//Toast.makeText(RestaurantFoodMenuActivity.this, "Requested resource not found", Toast.LENGTH_LONG).show();
				} else if (statusCode == 500) {
					//Toast.makeText(RestaurantFoodMenuActivity.this, "Something went wrong at server end", Toast.LENGTH_LONG).show();
				} else {
					networkConnectivity();
				}
			}
		});
	}
	
	public class MyAdapter extends BaseAdapter implements StickyListHeadersAdapter {

	    private LayoutInflater inflater;
	    ArrayList<String> arrHeader;
	    ArrayList<HashMap<String, String>> arrItems;

	    public MyAdapter(Context context, ArrayList<String> arr_header, ArrayList<HashMap<String, String>> arr_items) {
	        inflater = LayoutInflater.from(context);
	        arrHeader = arr_header;
	        arrItems = arr_items;
	    }

	    @Override
	    public int getCount() {
	        return arrHeader.size();
	    }

	    @Override
	    public Object getItem(int position) {
	        return position;
	    }

	    @Override
	    public long getItemId(int position) {
	        return position;
	    }

	    @Override 
	    public View getView(final int position, View convertView, ViewGroup parent) {
	        ViewHolder holder;

	        try {
	        
		        if (convertView == null) {
		            holder = new ViewHolder();
		            convertView = inflater.inflate(R.layout.restaurant_food_menu_item, parent, false);
		            holder.tv_food_name = (TextView) convertView.findViewById(R.id.tv_food_name);
		            holder.tv_food_price = (TextView) convertView.findViewById(R.id.tv_food_price);
		            holder.tv_food_ingredients = (TextView) convertView.findViewById(R.id.tv_food_ingredients);
		            convertView.setTag(holder);
		        } else {
		            holder = (ViewHolder) convertView.getTag();
		        }
	
		        holder.tv_food_name.setText(arrItems.get(position).get("strName"));
		        
		        String desc = arrItems.get(position).get("strDescription");
		        String price = arrItems.get(position).get("strPrice");
		        
		        if(desc.equals("") == false)
		        	holder.tv_food_ingredients.setText(arrItems.get(position).get("strDescription"));
		        else
		        	holder.tv_food_ingredients.setText("No Description");
	
		        if(price.equals("") == false)
		        	holder.tv_food_price.setText("$ "+arrItems.get(position).get("strPrice"));
		        		        
		        convertView.setOnClickListener(new View.OnClickListener() {
					
					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						Intent i_details = new Intent(RestaurantFoodMenuActivity.this, OrderDetailsActivity.class);
						i_details.putExtra("strItemName", arrItems.get(position).get("strName"));
						i_details.putExtra("strItemPrice", arrItems.get(position).get("strPrice"));
						i_details.putExtra("strName", strRestaurantName);
						i_details.putExtra("strStreetAddress", strStreetAddress);
						i_details.putExtra("strID", strID);
						i_details.putExtra("strRestLocality", strRestLocality);
						i_details.putExtra("strRestAddress", strRestAddress);
						i_details.putExtra("strRestLat", strRestLat);
						i_details.putExtra("strRestLong", strRestLong);
						i_details.putExtra("strCurrentLat", strCurrentLat);
						i_details.putExtra("strCurrentLong", strCurrentLong);
						
						startActivity(i_details);
						
						RestaurantFoodMenuActivity.this.finish();
					}
				});
		        
	        } catch(ArrayIndexOutOfBoundsException e) {e.printStackTrace();}
	        
	        return convertView;
	    }

	    @Override 
	    public View getHeaderView(int position, View convertView, ViewGroup parent) {
	        HeaderViewHolder holder;
	        
	        try {
	        	
		        if (convertView == null) {
		            holder = new HeaderViewHolder();
		            convertView = inflater.inflate(R.layout.restaurant_food_header, parent, false);
		            holder.tv_header = (TextView) convertView.findViewById(R.id.tv_header);
		            convertView.setTag(holder);
		        } else {
		            holder = (HeaderViewHolder) convertView.getTag();
		        }
		        //set header text as first char in name
		        //String headerText = "" + countries[position].subSequence(0, 1).charAt(0);
		        
		        String strHeader = arrHeader.get(position);
		        if(strHeader.equals("") == false)
		        	holder.tv_header.setText(arrHeader.get(position));
		        else
		        	holder.tv_header.setText("Others");
		        
	        } catch(ArrayIndexOutOfBoundsException e) {e.printStackTrace();}
	        
	        return convertView;
	    }

	    @Override
	    public long getHeaderId(int position) {
	        //return the first character of the country as ID because this is what headers are based upon
	        //return countries[position].subSequence(0, 1).charAt(0);
	    	//return arrHeader.get(position).subSequence(0, 1).charAt(0);
	    	return position;
	    }

	    class HeaderViewHolder {
	        TextView tv_header;
	    }

	    class ViewHolder {
	        TextView tv_food_name, tv_food_price, tv_food_ingredients;
	    }

	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();

		if (id == android.R.id.home) {
			RestaurantFoodMenuActivity.this.finish();
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		RestaurantFoodMenuActivity.this.finish();
	}
	
	private void networkConnectivity()
	{
        new AlertDialog.Builder(RestaurantFoodMenuActivity.this)
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
