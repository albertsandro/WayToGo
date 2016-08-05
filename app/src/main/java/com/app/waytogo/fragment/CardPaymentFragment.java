package com.app.waytogo.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.app.waytogo.CreditCardRegisterActivity;
import com.app.waytogo.R;

public class CardPaymentFragment extends Fragment {
	
	private Typeface tf;
	private SharedPreferences myPreferences, mySharedPreferences;
	private Editor editor, userEditor;
	private String CARD = "card_pref";
	private String USER = "user_pref";
	private TextView tv_last_four_digits, tv_card_register;
	private String strLastFourDigits;
		
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_card_payment, container, false);
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);

		tf = Typeface.createFromAsset(getActivity().getAssets(),"fonts/Roboto-Medium.ttf");
	
		myPreferences = getActivity().getSharedPreferences(CARD, Context.MODE_PRIVATE);
		editor = myPreferences.edit();	
		mySharedPreferences = getActivity().getSharedPreferences(USER, Context.MODE_PRIVATE);
		userEditor = mySharedPreferences.edit();
				
		tv_last_four_digits = (TextView) getView().findViewById(R.id.tv_last_four_digits);
		tv_card_register = (TextView) getView().findViewById(R.id.tv_card_register);
			
		strLastFourDigits = mySharedPreferences.getString("strLastFour", "");
		if(strLastFourDigits.equals("") == false) {
			tv_last_four_digits.setText("* "+strLastFourDigits);
		}
		
		tv_card_register.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent i_card = new Intent(getActivity(), CreditCardRegisterActivity.class);
				startActivity(i_card);
				getActivity().finish();
			}
		});
	}
}
