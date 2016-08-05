package com.app.waytogo.adapter;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.app.waytogo.helper.DatabaseHelper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class DBAdapter {
    private DatabaseHelper DBHelper;
    private String table_cart = "tbl_cart";
    private String DBPath;
    private Cursor curSelectItems;
    private String flag = "0";

    public DBAdapter(Context context) throws IOException {
        DBHelper = new DatabaseHelper(context);
        DBHelper.CreateDatabase();
        DBPath = DBHelper.DB_NEWPATH;
    }

    /*
     * To insert cart items
     */
    public String insertCartItems(String strItemName, String strItemPrice, String strItemQty, String strRestID, String strRestLocality, String strRestAddress,
                                  String strRestLat, String strRestLng, String strRestSplInstructions) {
        // TODO Auto-generated method stub
        boolean retval;
        SQLiteDatabase helperdb = DBHelper.openDB();

        //New Item
        ContentValues input = new ContentValues();
        input.put("item_name", strItemName);
        input.put("item_price", strItemPrice);
        input.put("item_qty", strItemQty);
        input.put("rest_id", strRestID);
        input.put("rest_locality", strRestLocality);
        input.put("rest_address", strRestAddress);
        input.put("rest_lat", strRestLat);
        input.put("rest_lng", strRestLng);
        input.put("rest_spl_instructions", strRestSplInstructions);

        helperdb.insert(table_cart, null, input);

        flag = "1";

        return flag;
    }

    /*
     * To fetch cart items
     */
    public ArrayList<HashMap<String, String>> fetchCartItems() {
        // TODO Auto-generated method stub
        String query;
        ArrayList<HashMap<String, String>> aItem = new ArrayList<HashMap<String, String>>();
        SQLiteDatabase helperdb = DBHelper.openDB();

        query = "SELECT * FROM " + table_cart;

        curSelectItems = helperdb.rawQuery(query, null);
        if (curSelectItems.moveToFirst()) {
            do {

                HashMap<String, String> hmap = new HashMap<String, String>();

                String str_id = curSelectItems.getString(curSelectItems.getColumnIndex("id"));
                String str_item_name = curSelectItems.getString(curSelectItems.getColumnIndex("item_name"));
                String str_item_price = curSelectItems.getString(curSelectItems.getColumnIndex("item_price"));
                String str_item_qty = curSelectItems.getString(curSelectItems.getColumnIndex("item_qty"));
                String str_rest_id = curSelectItems.getString(curSelectItems.getColumnIndex("rest_id"));
                String str_rest_locality = curSelectItems.getString(curSelectItems.getColumnIndex("rest_locality"));
                String str_rest_address = curSelectItems.getString(curSelectItems.getColumnIndex("rest_address"));
                String str_rest_lat = curSelectItems.getString(curSelectItems.getColumnIndex("rest_lat"));
                String str_rest_lng = curSelectItems.getString(curSelectItems.getColumnIndex("rest_lng"));
                String str_rest_spl_instructions = curSelectItems.getString(curSelectItems.getColumnIndex("rest_spl_instructions"));

                hmap.put("str_id", str_id);
                hmap.put("str_item_name", str_item_name);
                hmap.put("str_item_price", str_item_price);
                hmap.put("str_item_qty", str_item_qty);
                hmap.put("str_rest_id", str_rest_id);
                hmap.put("str_rest_locality", str_rest_locality);
                hmap.put("str_rest_address", str_rest_address);
                hmap.put("str_rest_lat", str_rest_lat);
                hmap.put("str_rest_lng", str_rest_lng);
                hmap.put("str_rest_spl_instructions", str_rest_spl_instructions);

                aItem.add(hmap);
            } while (curSelectItems.moveToNext());
        }

        curSelectItems.close();
        helperdb.close();
        return aItem;
    }

    /*
     * To remove cart items
     */
    public double removeCartItems(String strID) {
        // TODO Auto-generated method stub
        boolean retval;
        double mSubTotal = 0;

        SQLiteDatabase helperdb = DBHelper.openDB();

        retval = helperdb.delete(table_cart, "id" + " = '" + strID + "'", null) > 0;

        //flag = "1";
        if (retval)
            mSubTotal = getCartSubTotal();

        helperdb.close();
        return mSubTotal;
    }

    /*
     * To get the count of cart items
     */
    public String countCartItems() {
        // TODO Auto-generated method stub
        String strSize = "0";
        SQLiteDatabase helperdb = DBHelper.openDB();
        String itemFavQuery = "SELECT * FROM " + table_cart;
        curSelectItems = helperdb.rawQuery(itemFavQuery, null);
        if (curSelectItems.getCount() > 0) {
            strSize = "1";
        } else {
            strSize = "0";
        }
        curSelectItems.close();
        helperdb.close();
        return strSize;
    }

    /*
     * To get the subtotal
     */
    public double getCartSubTotal() {
        // TODO Auto-generated method stub
        String query;
        SQLiteDatabase helperdb = DBHelper.openDB();
        double mSubTotal = 0;

        query = "SELECT * FROM " + table_cart;

        curSelectItems = helperdb.rawQuery(query, null);
        if (curSelectItems.moveToFirst()) {
            do {

                HashMap<String, String> hmap = new HashMap<String, String>();

                String str_item_price = curSelectItems.getString(curSelectItems.getColumnIndex("item_price"));
                String str_item_qty = curSelectItems.getString(curSelectItems.getColumnIndex("item_qty"));

                double price = Double.valueOf(str_item_price);
                double qty = Double.valueOf(str_item_qty);

                double mPrice = (price) * (qty);

                mSubTotal = mPrice + mSubTotal;


            } while (curSelectItems.moveToNext());
        }

        curSelectItems.close();
        helperdb.close();
        return mSubTotal;
    }

    public void clearCartItems() {
        // TODO Auto-generated method stub
        boolean retval;

        SQLiteDatabase helperdb = DBHelper.openDB();

        retval = helperdb.delete(table_cart, null, null) > 0;

    }


}
