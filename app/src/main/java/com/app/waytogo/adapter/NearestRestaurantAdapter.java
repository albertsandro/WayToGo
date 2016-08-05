package com.app.waytogo.adapter;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.app.waytogo.R;
import com.app.waytogo.RestaurantFoodMenuActivity;
import com.app.waytogo.route.Routing;
import com.google.android.gms.maps.model.LatLng;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;

public class NearestRestaurantAdapter extends BaseAdapter {

	private Context mContext;
	private ArrayList<HashMap<String, String>> arrData;
	private LayoutInflater inflater;
	protected LatLng start;
    protected LatLng end;
    private Routing routing;
    private String strCurrentLat, strCurrentLong; 
	
	public NearestRestaurantAdapter(Context c, ArrayList<HashMap<String, String>> arrRestaurant, String strLat, String strLng) {
		// TODO Auto-generated constructor stub
		mContext = c;
		arrData = arrRestaurant;
		inflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		strCurrentLat = strLat;
		strCurrentLong = strLng;
    }

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return arrData.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return arrData.get(position);
	}
	
	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return arrData.size();
	}

    public class ViewHolder {
    	public TextView tv_restaurant_name, tv_restaurant_address, tv_miles;
		public ImageView ic_restaurant_icon;
	}
	
	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		final ViewHolder holder;
		
		if (convertView == null) {
            convertView = inflater.inflate(R.layout.restaurant_list_item, null);
            holder = new ViewHolder();
 
		    holder.tv_restaurant_name = (TextView) convertView.findViewById(R.id.tv_restaurant_name);
		    holder.tv_restaurant_address = (TextView) convertView.findViewById(R.id.tv_restaurant_address);
		    holder.tv_miles = (TextView) convertView.findViewById(R.id.tv_miles);
		    holder.ic_restaurant_icon = (ImageView) convertView.findViewById(R.id.ic_restaurant_icon);
		    		    
	        convertView.setTag(holder);
	        
	        
	    } else {
	        holder = (ViewHolder) convertView.getTag();
	    }
		
		holder.tv_restaurant_name.setText(arrData.get(position).get("strName"));
		holder.tv_restaurant_address.setText(arrData.get(position).get("strStreetAddress"));
		
		end = new LatLng(Double.valueOf(arrData.get(position).get("strLat")), Double.valueOf(arrData.get(position).get("strLong")));

		Location locationA = new Location("point A");

		locationA.setLatitude(Double.valueOf(strCurrentLat));
		locationA.setLongitude(Double.valueOf(strCurrentLong));

		Location locationB = new Location("point B");

		locationB.setLatitude(Double.valueOf(arrData.get(position).get("strLat")));
		locationB.setLongitude(Double.valueOf(arrData.get(position).get("strLong")));

		float distance = locationA.distanceTo(locationB);
		
		double dist = distance * 0.000621371192;
		
		DecimalFormat df = new DecimalFormat("#.#");
		String strmiles = df.format(dist);
		
		holder.tv_miles.setText(strmiles+" Miles away");
				
		convertView.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent i_food = new Intent(mContext, RestaurantFoodMenuActivity.class);
				i_food.putExtra("strName",arrData.get(position).get("strName"));
				i_food.putExtra("strStreetAddress",arrData.get(position).get("strStreetAddress"));
				i_food.putExtra("strID",arrData.get(position).get("strID"));
				i_food.putExtra("strCurrentLat",strCurrentLat);
				i_food.putExtra("strCurrentLong",strCurrentLong);
				
				mContext.startActivity(i_food);
				//((Activity) mContext).finish();
			}
		});
		
		return convertView;
	}
}
