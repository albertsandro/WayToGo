package com.app.waytogo.model;

public class ProfileDetails {

	public String status;
	public String message;
	public Data data;
	
	public class Data {
		public String iUserID;
		public String vUsername;
		public String vEmail;
		public String vFirst;
		public String vLast;
		public String vImage;
		public String eLogin;
		public String dLoginDate;
		public String vFbID;
		public String vDeviceToken;
		public String userPhone;
		public String verifiedPhone;
		public String codefordiscount;
		public String version;
		public String vDriverorNot;
		public String questcount;
		public String followingcount;
		public String followercount;
		public String vToken;
		public ProfileImage profileImage;
	}
	
	public class ProfileImage {
		public String original;
		public String thumb;
	}
}
