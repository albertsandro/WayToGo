package com.app.waytogo.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.app.waytogo.R;

import org.json.JSONArray;
import org.json.JSONException;

public class FoodListConfirmationAdapter extends BaseAdapter {

    private Context mContext;
    private JSONArray jArrayDesc, jArrayQty, jArrayPrice;
    private LayoutInflater inflater;

    public FoodListConfirmationAdapter(Context c, String strFoodDesc, String strFoodQty, String strFoodPrice) {
        // TODO Auto-generated constructor stub
        mContext = c;
        inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        try {
            jArrayDesc = new JSONArray(strFoodDesc);
            jArrayQty = new JSONArray(strFoodQty);
            jArrayPrice = new JSONArray(strFoodPrice);

        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return jArrayQty.length();
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
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        final ViewHolder holder;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.driver_food_list_confirmation, null);
            holder = new ViewHolder();

            holder.tv_item_name = (TextView) convertView.findViewById(R.id.tv_item_name);
            holder.tv_item_qty = (TextView) convertView.findViewById(R.id.tv_item_qty);
            holder.tv_item_price = (TextView) convertView.findViewById(R.id.tv_item_price);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        try {
            holder.tv_item_name.setText("" + jArrayDesc.get(position));
            holder.tv_item_qty.setText("" + jArrayQty.get(position));
            holder.tv_item_price.setText("" + jArrayPrice.get(position));
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return convertView;
    }

}
