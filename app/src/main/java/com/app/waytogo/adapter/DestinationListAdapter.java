package com.app.waytogo.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.app.waytogo.R;
import com.app.waytogo.model.DestinationPlaceDetails.DestinationPlaceDetail;

import java.util.List;

public class DestinationListAdapter extends BaseAdapter {

	private Context mContext;
	private LayoutInflater inflater=null;
	private List<DestinationPlaceDetail> destinationPlace;
		
	public DestinationListAdapter(Context c, List<DestinationPlaceDetail> place_lists) {
		// TODO Auto-generated constructor stub
		mContext = c;
		inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		destinationPlace = place_lists;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return destinationPlace.size();
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

	private class ViewHolder {
    	private TextView tv_destination_title, tv_destination_snipet;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		final ViewHolder holder;
		
		if (convertView == null) {
            convertView = inflater.inflate(R.layout.destination_list_item, null);
            holder = new ViewHolder();
 
            holder.tv_destination_title = (TextView) convertView.findViewById(R.id.tv_destination_title);
            holder.tv_destination_snipet = (TextView) convertView.findViewById(R.id.tv_destination_snipet);
                        
           convertView.setTag(holder);
           
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
		
		String strDestination[] = destinationPlace.get(position).description.split(",", 2);
		
		holder.tv_destination_title.setText(strDestination[0]);
		holder.tv_destination_snipet.setText(strDestination[1]);
				
		return convertView;
	}

}
