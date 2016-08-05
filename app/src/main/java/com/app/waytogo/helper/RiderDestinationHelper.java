package com.app.waytogo.helper;

import android.app.Activity;
import android.util.Log;
import android.widget.Toast;

import com.app.waytogo.DestinationActivity;
import com.app.waytogo.model.DestinationLocationDetails;
import com.app.waytogo.model.DestinationPlaceDetails;
import com.app.waytogo.support.UrlGenerator;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import java.io.UnsupportedEncodingException;
import java.util.List;

public class RiderDestinationHelper {

	RiderDestinationInterface sRiderDestinationInterface;
	
	public interface RiderDestinationInterface {
		public void onSucess(List<DestinationLocationDetails.DestinationLocationDetail> location_lists, String strDestination);
		public void onSucessDestinationList(List<DestinationPlaceDetails.DestinationPlaceDetail> location_lists);
	}

	public void setRiderDestinationHelper(DestinationActivity RiderDestinationInterface) {
		this.sRiderDestinationInterface = RiderDestinationInterface;
	}

	public void fetchDestination(final Activity activity, final String strDestination) {
		// TODO Auto-generated method stub
		// Create AsycHttpClient object
		AsyncHttpClient client = new AsyncHttpClient();
		// Http Request Params Object
		RequestParams params = new RequestParams();

		// Make Http call to getusers.php
		String url = null;

		try {
			url = UrlGenerator.fetchDestination();
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		params.add("address", strDestination);
		params.add("key", UrlGenerator.GOOGLE_API_KEY);
		
		Log.e("url", "" + url);
		
		//url = "https://maps.googleapis.com/maps/api/geocode/json?address=Chennai&key=AIzaSyDzv6HL63OWMnStpWpMZX2yy7jVRxOAh5w";
		
		client.get(url, params, new AsyncHttpResponseHandler() {

			@Override
			public void onSuccess(String response) {
				// Hide ProgressBar

				Log.i("MY INFO", "Json Parser started..");

				try {
					GsonBuilder gsonBuilder = new GsonBuilder();
					Gson gson = gsonBuilder.create();

					DestinationLocationDetails destination = gson.fromJson(response,DestinationLocationDetails.class);
					List<DestinationLocationDetails.DestinationLocationDetail> location_lists = destination.results; Log.e("location_lists","@"+location_lists);

					if (response != null)
						sRiderDestinationInterface.onSucess(location_lists, strDestination);

				} catch (NullPointerException e) {
					e.printStackTrace();
				}
			}

			// When error occured
			@Override
			public void onFailure(int statusCode, Throwable error,
					String content) {
				// TODO Auto-generated method stub
				// Hide ProgressBar
				// dialog.hide();
				// mSwipeRefreshLayout.setRefreshing(false);
				if (statusCode == 404) {
					Toast.makeText(activity, "Requested resource not found", Toast.LENGTH_LONG).show();
				} else if (statusCode == 500) {
					Toast.makeText(activity,"Something went wrong at server end",Toast.LENGTH_LONG).show();
				} else {
					/*Toast.makeText(
							activity,
							"Unexpected Error occcured! [Most common Error: Device might not be connected to Internet]",
							Toast.LENGTH_LONG).show();*/
				}
			}
		});
	}

	
	public void fetchDestinationList(final Activity activity, final String strDestination, String searchLat, String searchLng, String searchCountryCode) {
		// TODO Auto-generated method stub
		AsyncHttpClient client = new AsyncHttpClient();
		RequestParams params = new RequestParams();

		String url = null;

		try {
			url = UrlGenerator.fetchDestinationList();
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		params.add("input", strDestination);
		params.add("location", searchLat+","+searchLng);
		params.add("components", "country:"+searchCountryCode);
		params.add("key", UrlGenerator.GOOGLE_API_KEY);
		
		Log.e("url", "" + url);
		
		client.get(url, params, new AsyncHttpResponseHandler() {

			@Override
			public void onSuccess(String response) {
				// Hide ProgressBar

				Log.i("MY INFO", "Json Parser started..");

				try {
					GsonBuilder gsonBuilder = new GsonBuilder();
					Gson gson = gsonBuilder.create();

					DestinationPlaceDetails destination_place = gson.fromJson(response,DestinationPlaceDetails.class);
					List<DestinationPlaceDetails.DestinationPlaceDetail> destination_lists = destination_place.predictions; Log.e("destination_lists","@"+destination_lists);

					if (response != null)
						sRiderDestinationInterface.onSucessDestinationList(destination_lists);

				} catch (NullPointerException e) {
					e.printStackTrace();
				}
			}

			// When error occured
			@Override
			public void onFailure(int statusCode, Throwable error,
					String content) {
				// TODO Auto-generated method stub
				// Hide ProgressBar
				// dialog.hide();
				// mSwipeRefreshLayout.setRefreshing(false);
				if (statusCode == 404) {
					Toast.makeText(activity, "Requested resource not found", Toast.LENGTH_LONG).show();
				} else if (statusCode == 500) {
					Toast.makeText(activity,"Something went wrong at server end",Toast.LENGTH_LONG).show();
				} else {
					/*Toast.makeText(
							activity,
							"Unexpected Error occcured! [Most common Error: Device might not be connected to Internet]",
							Toast.LENGTH_LONG).show();*/
				}
			}
		});
	}
}
