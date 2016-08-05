package com.app.waytogo;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.TextView;

public class TermsConditionsActivity extends AppCompatActivity {

	private Toolbar mToolbar;
	private Typeface tf;
	private TextView tv_terms_text;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_terms);
		
		mToolbar = (Toolbar) findViewById(R.id.toolbar);
		
		setSupportActionBar(mToolbar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setDisplayUseLogoEnabled(true);
		getSupportActionBar().setDisplayShowHomeEnabled(true);
		getSupportActionBar().setTitle("Terms");
		
		tf = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Regular.ttf");
		
		tv_terms_text = (TextView) findViewById(R.id.tv_terms_text);
		
		tv_terms_text.setTypeface(tf);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();

		if (id == android.R.id.home) {
			TermsConditionsActivity.this.finish();
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		TermsConditionsActivity.this.finish();
	}
	
}
