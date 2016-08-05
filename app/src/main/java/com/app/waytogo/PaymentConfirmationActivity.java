package com.app.waytogo;


import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

public class PaymentConfirmationActivity extends AppCompatActivity {

	private TextView tv_payment_tap_continue;
	private String strFeedID, strPickLat, strPickLong, strDropLat, strDropLong, strFirst, strLast, strImage, strPaymentType, strCost, strUserPhone;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_payment_confirmation);
		
		Bundle b = getIntent().getExtras();
		strFeedID = b.getString("iFeedID");
		strPickLat = b.getString("strPickLat");
		strPickLong = b.getString("strPickLong");
		strDropLat = b.getString("strDropLat");
		strDropLong = b.getString("strDropLong");
		strFirst = b.getString("strFirst");
		strLast = b.getString("strLast");
		strImage = b.getString("strImage");
		strPaymentType = b.getString("strPaymentType");
		strCost = b.getString("strCost");
		strUserPhone = b.getString("strUserPhone");
		
		tv_payment_tap_continue = (TextView) findViewById(R.id.tv_payment_tap_continue);
		
		tv_payment_tap_continue.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent i_drop = new Intent(PaymentConfirmationActivity.this, DriverDropOffActivity.class);
				i_drop.putExtra("iFeedID", strFeedID);
				i_drop.putExtra("strPickLat", strPickLat);
				i_drop.putExtra("strPickLong", strPickLong);
				i_drop.putExtra("strDropLat", strDropLat);
				i_drop.putExtra("strDropLong", strDropLong);
				i_drop.putExtra("strFirst", strFirst);
				i_drop.putExtra("strLast", strLast);
				i_drop.putExtra("strImage", strImage);
				i_drop.putExtra("strPaymentType", strPaymentType);
				i_drop.putExtra("strCost", strCost);
				i_drop.putExtra("strUserPhone", strUserPhone);
				startActivity(i_drop);
				PaymentConfirmationActivity.this.finish();
			}
		});
	}
	
}
