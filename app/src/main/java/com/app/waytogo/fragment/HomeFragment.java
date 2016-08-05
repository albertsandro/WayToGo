package com.app.waytogo.fragment;

import android.content.Context;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.app.waytogo.R;
import com.app.waytogo.adapter.HomePagerAdapter;
import com.astuetz.PagerSlidingTabStrip;

public class HomeFragment extends Fragment {

	private ConnectivityManager cm;
	private ViewPager mPager;
	private HomePagerAdapter mHomePagerAdapter;
	private PagerSlidingTabStrip mPagerSlidingTabStrip;
	private Typeface tf;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_home, container, false);
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);

		cm = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
	
		tf = Typeface.createFromAsset(getActivity().getAssets(),"fonts/Roboto-Medium.ttf");
				
		mPager = (ViewPager) getView().findViewById(R.id.view_pager);
		mPagerSlidingTabStrip = (PagerSlidingTabStrip) getView().findViewById(R.id.tabs);
		mHomePagerAdapter = new HomePagerAdapter(((AppCompatActivity) getActivity()).getSupportFragmentManager());
		mPager.setAdapter(mHomePagerAdapter);
		mPagerSlidingTabStrip.setViewPager(mPager);
		final int pageMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, getResources().getDisplayMetrics());
		mPager.setPageMargin(pageMargin);

	}

}