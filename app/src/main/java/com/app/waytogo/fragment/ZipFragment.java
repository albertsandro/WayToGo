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
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.app.waytogo.R;

public class ZipFragment extends Fragment {

	private EditText edt_zipcode;
	private CheckedTextView cb_agree;
	private ImageButton btn_next;
	private boolean isChecked = false;
	private SharedPreferences myPreferences;
	private Editor editor;
	private String DRIVER = "driver_pref";
	private String strZipcode;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_page_driver_zipcode_info, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);

		myPreferences = getActivity().getSharedPreferences(DRIVER, Context.MODE_PRIVATE);
		editor = myPreferences.edit();
		edt_zipcode = (EditText) getView().findViewById(R.id.edt_zipcode);
		cb_agree = (CheckedTextView) getView().findViewById(R.id.cb_agree);
		btn_next = (ImageButton) getView().findViewById(R.id.btn_next);
		
		cb_agree.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (cb_agree.isChecked()) {
					cb_agree.setChecked(false);
					isChecked = false;
				}
	    		else {
	    			cb_agree.setChecked(true);
	    			isChecked = true;
	    		}
			}
		});
		
		btn_next.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				strZipcode = edt_zipcode.getText().toString();
				isChecked = cb_agree.isChecked();
				
				if (strZipcode.length() == 0 || strZipcode.toString().equals("")) {
					Toast.makeText(getActivity(), getResources().getString(R.string.strZipcodeEmpty), Toast.LENGTH_SHORT).show();
				} else if(!isChecked) { 
					Toast.makeText(getActivity(), getResources().getString(R.string.strAcceptDiscolsure), Toast.LENGTH_SHORT).show();
				} else {
					
					editor.putString("strZipcode", strZipcode);
					editor.commit();
					
					Fragment fragment = new ProfileFragment();
					FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
					FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
					fragmentTransaction.add(R.id.container_body, fragment);
					fragmentTransaction.addToBackStack("FragmentB");
					fragmentTransaction.commit();
				}
			}
		});
	}		
}
