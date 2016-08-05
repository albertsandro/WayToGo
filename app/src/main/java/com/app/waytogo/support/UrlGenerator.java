package com.app.waytogo.support;


import com.app.waytogo.R;
import com.app.waytogo.main.MainApplication;

import java.io.UnsupportedEncodingException;


public class UrlGenerator {

	public static String GOOGLE_API_KEY = "AIzaSyDzv6HL63OWMnStpWpMZX2yy7jVRxOAh5w";
	
	private static int getBaseUrl() {
		// TODO Auto-generated method stub
		return R.string.base_url;
	}
	
	private static int getGoogleAPIUrl() {
		// TODO Auto-generated method stub
		return R.string.google_api_base_url;
	}
	
	public static String userSMSVerificationCode() throws UnsupportedEncodingException {
		// TODO Auto-generated method stub
		return MainApplication.getAppString(getBaseUrl(), MainApplication.getAppString(R.string.SMSVerfication));
	}
	
	public static String getUserEmail() throws UnsupportedEncodingException {
		// TODO Auto-generated method stub
		return MainApplication.getAppString(getBaseUrl(),MainApplication.getAppString(R.string.GetUserEmail));
	}
	
	public static String getSMSVerifiedPhone() throws UnsupportedEncodingException {
		// TODO Auto-generated method stub
		return MainApplication.getAppString(getBaseUrl(),MainApplication.getAppString(R.string.SMSVerifiedPhone));
	}	
	
	public static String addAccessToken() throws UnsupportedEncodingException {
		// TODO Auto-generated method stub
		return MainApplication.getAppString(getBaseUrl(),MainApplication.getAppString(R.string.AddAccessToken));
	}
	
	public static String userLogin() throws UnsupportedEncodingException {
		// TODO Auto-generated method stub
		return MainApplication.getAppString(getBaseUrl(),MainApplication.getAppString(R.string.Login));
	}

	public static String userRegistration() throws UnsupportedEncodingException {
		// TODO Auto-generated method stub
		return MainApplication.getAppString(getBaseUrl(),MainApplication.getAppString(R.string.Registration));
	}
	
	public static String fetchDestination() throws UnsupportedEncodingException {
		// TODO Auto-generated method stub
		return MainApplication.getAppString(getGoogleAPIUrl(),MainApplication.getAppString(R.string.GoogleApiGeocode));
	}
	
	public static String fetchDestinationList() throws UnsupportedEncodingException {
		// TODO Auto-generated method stub
		return MainApplication.getAppString(getGoogleAPIUrl(),MainApplication.getAppString(R.string.GooglePlacesApi));
	}
	
	public static String userFeedAdd() throws UnsupportedEncodingException {
		// TODO Auto-generated method stub
		return MainApplication.getAppString(getBaseUrl(),MainApplication.getAppString(R.string.FeedAdd));
	}
	
	public static String driverProfileRegistration() throws UnsupportedEncodingException {
		// TODO Auto-generated method stub
		return MainApplication.getAppString(getBaseUrl(),MainApplication.getAppString(R.string.DriverProfileRegistration));
	}
	
	public static String driverVehicleRegistration() throws UnsupportedEncodingException {
		// TODO Auto-generated method stub
		return MainApplication.getAppString(getBaseUrl(),MainApplication.getAppString(R.string.DriverVehicleRegistration));
	}
	
	public static String updateDriver() throws UnsupportedEncodingException {
		// TODO Auto-generated method stub
		return MainApplication.getAppString(getBaseUrl(),MainApplication.getAppString(R.string.UpdateDriver));
	}	
	
	public static String userFeedList() throws UnsupportedEncodingException {
		// TODO Auto-generated method stub
		return MainApplication.getAppString(getBaseUrl(),MainApplication.getAppString(R.string.FeedList));
	}

	public static String UserDeleteRide() {
		// TODO Auto-generated method stub
		return MainApplication.getAppString(getBaseUrl(),MainApplication.getAppString(R.string.UserDeleteRide));
	}
	//todo
	public static String updateDriverStatus() {
		// TODO Auto-generated method stub
		return MainApplication.getAppString(getBaseUrl(),MainApplication.getAppString(R.string.UpdateDriverStatus));
	}
	
	public static String updateDriverCurrentLocation() {
		// TODO Auto-generated method stub
		return MainApplication.getAppString(getBaseUrl(),MainApplication.getAppString(R.string.DriverCurrentLocation));
	}

	public static String fetchDriverFeedList() {
		// TODO Auto-generated method stub
		return MainApplication.getAppString(getBaseUrl(),MainApplication.getAppString(R.string.DriverFeedList));
	}
	
	public static String selectNearestDriver() {
		// TODO Auto-generated method stub
		return MainApplication.getAppString(getBaseUrl(),MainApplication.getAppString(R.string.SelectNearestDriver));
	}
	
	public static String getLatestInfo() {
		// TODO Auto-generated method stub
		return MainApplication.getAppString(getBaseUrl(),MainApplication.getAppString(R.string.GetLatestInfo));
	}
	
	public static String updateBookingStatus() {
		// TODO Auto-generated method stub
		return MainApplication.getAppString(getBaseUrl(),MainApplication.getAppString(R.string.UpdateBookingStatus));
	}
	
	public static String getUsernamefromiUserID() {
		// TODO Auto-generated method stub
		return MainApplication.getAppString(getBaseUrl(),MainApplication.getAppString(R.string.GetUsernamefromiUserID));
	}	
	
	public static String getFromAddress() {
		// TODO Auto-generated method stub
		return MainApplication.getAppString(getBaseUrl(),MainApplication.getAppString(R.string.GetFromAddress));
	}
	
	public static String acceptedRide() {
		// TODO Auto-generated method stub
		return MainApplication.getAppString(getBaseUrl(),MainApplication.getAppString(R.string.AcceptedRide));
	}
	
	public static String getFeed() {
		// TODO Auto-generated method stub
		return MainApplication.getAppString(getBaseUrl(),MainApplication.getAppString(R.string.GetFeed));
	}
	
	public static String updateDriverTripLocation() {
		// TODO Auto-generated method stub
		return MainApplication.getAppString(getBaseUrl(),MainApplication.getAppString(R.string.DriverTripLocation));
	}
	
	public static String pickedupRide() {
		// TODO Auto-generated method stub
		return MainApplication.getAppString(getBaseUrl(),MainApplication.getAppString(R.string.PickedUpRide));
	}

	public static String getDriverEarnings() {
		// TODO Auto-generated method stub
		return MainApplication.getAppString(getBaseUrl(),MainApplication.getAppString(R.string.GetDriverEarnings));
	}
	
	public static String updateDriverEarnings() {
		// TODO Auto-generated method stub
		return MainApplication.getAppString(getBaseUrl(),MainApplication.getAppString(R.string.UpdateDriverEarnings));
	}
	
	public static String completedRide() {
		// TODO Auto-generated method stub
		return MainApplication.getAppString(getBaseUrl(),MainApplication.getAppString(R.string.CompletedRide));
	}
	
	public static String getDriver() {
		// TODO Auto-generated method stub
		return MainApplication.getAppString(getBaseUrl(),MainApplication.getAppString(R.string.GetDriver));
	}
	
	public static String updateBalance() {
		// TODO Auto-generated method stub
		return MainApplication.getAppString(getBaseUrl(),MainApplication.getAppString(R.string.UpdateBalance));
	}
	
	public static String report() {
		// TODO Auto-generated method stub
		return MainApplication.getAppString(getBaseUrl(),MainApplication.getAppString(R.string.Report));
	}
	
	public static String feedback() {
		// TODO Auto-generated method stub
		return MainApplication.getAppString(getBaseUrl(),MainApplication.getAppString(R.string.FeedBack));
	}
	
	public static String checkBalance() {
		// TODO Auto-generated method stub
		return MainApplication.getAppString(getBaseUrl(),MainApplication.getAppString(R.string.CheckBalance));
	}
	
	public static String uploadCourierImage() {
		// TODO Auto-generated method stub
		return MainApplication.getAppString(getBaseUrl(),MainApplication.getAppString(R.string.UploadCourierImage));
	}
	
	public static String uploadCarImage() {
		// TODO Auto-generated method stub
		return MainApplication.getAppString(getBaseUrl(),MainApplication.getAppString(R.string.strCarImageUrl));
	}
	
	public static String uploadCarRegImage() {
		// TODO Auto-generated method stub
		return MainApplication.getAppString(getBaseUrl(),MainApplication.getAppString(R.string.strCarRegUrl));
	}
	
	public static String uploadCarInsImage() {
		// TODO Auto-generated method stub
		return MainApplication.getAppString(getBaseUrl(),MainApplication.getAppString(R.string.strCarInsUrl));
	}
	
	public static String chkTripCancelled() {
		// TODO Auto-generated method stub
		return MainApplication.getAppString(getBaseUrl(),MainApplication.getAppString(R.string.ChkTripCancelled));
	}
	
	public static String updateUserToken() {
		// TODO Auto-generated method stub
		return MainApplication.getAppString(getBaseUrl(),MainApplication.getAppString(R.string.UpdateUserToken));
	}
	
	public static String updatelastfourdigits() {
		// TODO Auto-generated method stub
		return MainApplication.getAppString(getBaseUrl(),MainApplication.getAppString(R.string.UpdateLastFourDigits));
	}
	
	public static String getCourierUrl() {
		// TODO Auto-generated method stub
		return MainApplication.getAppString(getBaseUrl(),MainApplication.getAppString(R.string.GetCourierUrl));
	}
	
	public static String updateTokenSweden() {
		// TODO Auto-generated method stub
		return MainApplication.getAppString(getBaseUrl(),MainApplication.getAppString(R.string.UpdateTokenSweden));
	}
	
	public static String updateToken() {
		// TODO Auto-generated method stub
		return MainApplication.getAppString(getBaseUrl(),MainApplication.getAppString(R.string.UpdateToken));
	}
	
	public static String updatePaypalAddress() {
		// TODO Auto-generated method stub
		return MainApplication.getAppString(getBaseUrl(),MainApplication.getAppString(R.string.UpdatePaypalAddress));
	}
	
	public static String updateDriverBank() {
		// TODO Auto-generated method stub
		return MainApplication.getAppString(getBaseUrl(),MainApplication.getAppString(R.string.UpdateBankInfo));
	}
	
	public static String getFBUser() {
		// TODO Auto-generated method stub
		return MainApplication.getAppString(getBaseUrl(),MainApplication.getAppString(R.string.GetFBUser));
	}
		
}
