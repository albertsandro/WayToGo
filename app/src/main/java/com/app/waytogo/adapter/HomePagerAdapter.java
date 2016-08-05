package com.app.waytogo.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.app.waytogo.fragment.MapCourierFragment;
import com.app.waytogo.fragment.MapFoodFragment;
import com.app.waytogo.fragment.MapRideFragment;

public class HomePagerAdapter extends FragmentPagerAdapter {

	private String[] TITLES = {"Ride", "Courier", "Food"};
	
	public HomePagerAdapter(FragmentManager supportFragmentManager) {
		// TODO Auto-generated constructor stub
		super(supportFragmentManager);
	}

	@Override
	public int getCount() {
		return 3;
	}
	
	@Override
    public CharSequence getPageTitle(int position) {
        return TITLES[position];
	}

	@Override
	public Fragment getItem(int position) {
		// TODO Auto-generated method stub
		switch (position) {
			case 0:
				return new MapRideFragment();
			case 1:
				return new MapCourierFragment();
			case 2:
				return new MapFoodFragment();
		}

		return null;
	}
}
