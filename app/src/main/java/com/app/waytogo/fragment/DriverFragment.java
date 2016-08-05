package com.app.waytogo.fragment;


import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.app.waytogo.R;

public class DriverFragment extends Fragment {

	private TextView tv_profit, tv_profit_label;
	private EditText edt_referral;
	private Spinner spin_city;
	private ImageButton btn_next;
	private SharedPreferences myPreferences;
	private Editor editor;
	private String DRIVER = "driver_pref";
	private String strReferralCode, strCity;
		
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_driver, container, false);
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
		
		myPreferences = getActivity().getSharedPreferences(DRIVER, Context.MODE_PRIVATE);
		editor = myPreferences.edit();
		
		tv_profit = (TextView) getView().findViewById(R.id.tv_profit);
		tv_profit_label = (TextView) getView().findViewById(R.id.tv_profit_label);
		edt_referral = (EditText) getView().findViewById(R.id.edt_referral);
		spin_city = (Spinner) getView().findViewById(R.id.spin_city);
		btn_next = (ImageButton) getView().findViewById(R.id.btn_next);
		
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),R.array.driver_cities, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		
		spin_city.setAdapter(adapter);
		spin_city.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,int position, long id) {
				// TODO Auto-generated method stub
				strCity = parent.getItemAtPosition(position).toString();
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
				
			}
		});
		
		btn_next.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(strCity.equals("More cities coming soon!") == true) {
					Toast.makeText(getActivity(), "More cities coming soon!", Toast.LENGTH_SHORT).show();
				} else if(strCity.equals("Select your City") == true) {
					Toast.makeText(getActivity(), "Whoops! Did you forget to select in which city you'd like to drive in?", Toast.LENGTH_SHORT).show();
				} else {
					
					//Intent i_driver = new Intent(getActivity(), DriverRegisterActivity.class);
					//startActivity(i_driver);
					Fragment fragment = new ZipFragment();
					FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
					FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
					fragmentTransaction.add(R.id.container_body, fragment);
					fragmentTransaction.addToBackStack("FragmentA");
					fragmentTransaction.commit();
					
					strReferralCode = edt_referral.getText().toString();
					editor.putString("strReferralCode", strReferralCode);
					editor.putString("strCity", strCity);
					editor.commit();
				}
			}
		});
	}
	
}
