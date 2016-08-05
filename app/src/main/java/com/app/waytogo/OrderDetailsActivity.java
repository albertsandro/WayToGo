package com.app.waytogo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.app.waytogo.adapter.DBAdapter;

import java.io.IOException;

public class OrderDetailsActivity extends AppCompatActivity implements OnClickListener {

	private Toolbar mToolbar;
	private String strItemName, strItemPrice, strItemQty, strName, strStreetAddress, strID, 
			strRestLocality, strRestAddress, strRestLat, strRestLong, strCurrentLat, strCurrentLong;
	private TextView tv_item, tv_cost, tv_quantity;
	private Button btn_minus, btn_plus;
	private ImageButton btn_add_to_cart, btn_cancel;
	private EditText edt_spl_instructions;
	private DBAdapter dbadapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_order_details);
		
		mToolbar = (Toolbar) findViewById(R.id.toolbar);

		setSupportActionBar(mToolbar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setDisplayUseLogoEnabled(true);
		getSupportActionBar().setDisplayShowHomeEnabled(true);
		getSupportActionBar().setTitle("Details");
		
		try {
			dbadapter = new DBAdapter(getApplicationContext());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Bundle b = getIntent().getExtras();
	
		strItemName = b.getString("strItemName");
		strItemPrice = b.getString("strItemPrice");
		strName = b.getString("strName");
		strStreetAddress = b.getString("strStreetAddress");
		strID = b.getString("strID");
		strRestLocality = b.getString("strRestLocality");
		strRestAddress = b.getString("strRestAddress");
		strRestLat = b.getString("strRestLat");
		strRestLong = b.getString("strRestLong");
		strCurrentLat = b.getString("strCurrentLat");
		strCurrentLong = b.getString("strCurrentLong");
		
		tv_item = (TextView) findViewById(R.id.tv_item);
		tv_cost = (TextView) findViewById(R.id.tv_cost);
		tv_quantity = (TextView) findViewById(R.id.tv_quantity);
		btn_minus = (Button) findViewById(R.id.btn_minus);
		btn_plus = (Button) findViewById(R.id.btn_plus);
		btn_add_to_cart = (ImageButton) findViewById(R.id.btn_add_to_cart);
		btn_cancel = (ImageButton) findViewById(R.id.btn_cancel);
		edt_spl_instructions = (EditText) findViewById(R.id.edt_spl_instructions);
		
		tv_item.setText(strItemName);
		tv_cost.setText("$ "+strItemPrice);
		
		btn_minus.setOnClickListener(OrderDetailsActivity.this);
		btn_plus.setOnClickListener(OrderDetailsActivity.this);
		btn_add_to_cart.setOnClickListener(OrderDetailsActivity.this);
		btn_cancel.setOnClickListener(OrderDetailsActivity.this);
		
	}
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch(v.getId())
		{
			case R.id.btn_minus:
				strItemQty = tv_quantity.getText().toString();
				if(strItemQty.equals("1") == true) {
					btn_minus.setEnabled(false);
					btn_minus.setClickable(false);
				} else {
					btn_minus.setEnabled(true);
					btn_minus.setClickable(true);
					
					int qty = Integer.valueOf(strItemQty) - 1;
					tv_quantity.setText(String.valueOf(qty));
				}
				break;
				
			case R.id.btn_plus:
				btn_minus.setEnabled(true);
				btn_minus.setClickable(true);
				
				strItemQty = tv_quantity.getText().toString();
				int qty = Integer.valueOf(strItemQty) + 1;
				tv_quantity.setText(String.valueOf(qty));
				break;
				
			case R.id.btn_add_to_cart:
				
				String edt_spl = edt_spl_instructions.getText().toString();
				strItemQty = tv_quantity.getText().toString();
				
				String retval = dbadapter.insertCartItems(strItemName, strItemPrice, strItemQty, strID, strRestLocality, 
							strRestAddress, strRestLat, strRestLong, edt_spl); 				
				
				if(retval.equals("1") == true)
				{
					Intent i_add_cart = new Intent(OrderDetailsActivity.this, RestaurantFoodMenuActivity.class);
					i_add_cart.putExtra("strItemName", strItemName);
					i_add_cart.putExtra("strName", strName);
					i_add_cart.putExtra("strStreetAddress", strStreetAddress);
					i_add_cart.putExtra("strID", strID);
					i_add_cart.putExtra("strCurrentLat", strCurrentLat);
					i_add_cart.putExtra("strCurrentLong", strCurrentLong);
					startActivity(i_add_cart);
					OrderDetailsActivity.this.finish();
					
					Toast.makeText(getApplicationContext(), "Yumm.. Added successfully!", Toast.LENGTH_SHORT).show();
				}
				break;
				
			case R.id.btn_cancel:
				Intent i_cancel = new Intent(OrderDetailsActivity.this, RestaurantFoodMenuActivity.class);
				i_cancel.putExtra("strItemName", strItemName);
				i_cancel.putExtra("strName", strName);
				i_cancel.putExtra("strStreetAddress", strStreetAddress);
				i_cancel.putExtra("strID", strID);
				i_cancel.putExtra("strCurrentLat", strCurrentLat);
				i_cancel.putExtra("strCurrentLong", strCurrentLong);
				startActivity(i_cancel);
				OrderDetailsActivity.this.finish();
				break;
		}
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();

		if (id == android.R.id.home) {
			OrderDetailsActivity.this.finish();
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		Intent i_back = new Intent(OrderDetailsActivity.this, RestaurantFoodMenuActivity.class);
		i_back.putExtra("strItemName", strItemName);
		i_back.putExtra("strName", strName);
		i_back.putExtra("strStreetAddress", strStreetAddress);
		i_back.putExtra("strID", strID);
		i_back.putExtra("strCurrentLat", strCurrentLat);
		i_back.putExtra("strCurrentLong", strCurrentLong);
		startActivity(i_back);
		OrderDetailsActivity.this.finish();
	}

}
