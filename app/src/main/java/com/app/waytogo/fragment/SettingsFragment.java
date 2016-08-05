package com.app.waytogo.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.app.waytogo.PaymentSettingsActivity;
import com.app.waytogo.R;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.squareup.picasso.Picasso;

public class SettingsFragment extends Fragment {

	private Toolbar mToolbar;
	private ConnectivityManager cm;
	private TextView tv_fname, tv_lname, tv_email, tv_referral_code;
	private CircularImageView iv_profile;
	private Button btn_payout_settings;
	private Typeface tf;
	private SharedPreferences myPreferences, myDriverPreferences;
	private Editor editor, editorDriver;
	private String USER = "user_pref";
	private String DRIVER = "driver_pref";

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_settings, container, false);
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);

		cm = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
	
		tf = Typeface.createFromAsset(getActivity().getAssets(),"fonts/Roboto-Medium.ttf");
	
		myPreferences = getActivity().getSharedPreferences(USER, Context.MODE_PRIVATE);
		editor = myPreferences.edit();
		myDriverPreferences = getActivity().getSharedPreferences(DRIVER, Context.MODE_PRIVATE);
		editorDriver = myPreferences.edit();
		
		tv_fname = (TextView) getView().findViewById(R.id.tv_fname);
		tv_lname = (TextView) getView().findViewById(R.id.tv_lname);
		tv_email = (TextView) getView().findViewById(R.id.tv_email);
		tv_referral_code = (TextView) getView().findViewById(R.id.tv_referral_code);
		iv_profile = (CircularImageView) getView().findViewById(R.id.iv_profile);
		btn_payout_settings = (Button) getView().findViewById(R.id.btn_payout_settings);
		
		tv_fname.setText(myPreferences.getString("strFirstName", ""));
		tv_lname.setText(myPreferences.getString("strLastName", ""));
		tv_email.setText(myPreferences.getString("strEmail", ""));
		
		String strReferralCode = myDriverPreferences.getString("strReferralCode", "");
		if(strReferralCode.equals("") == false) {
			tv_referral_code.setVisibility(View.VISIBLE);
			tv_referral_code.setText("Referral code: "+strReferralCode);
		} else {
			tv_referral_code.setVisibility(View.GONE);
		}
		
		Picasso.with(getActivity()).load(myPreferences.getString("strImageUrl", "")).centerCrop().resize(75, 75).into(iv_profile);
		
		btn_payout_settings.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent i_payment = new Intent(getActivity(), PaymentSettingsActivity.class);
				startActivity(i_payment);
				getActivity().finish();
			}
		});
	}

	
}
