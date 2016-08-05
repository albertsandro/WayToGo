package com.app.waytogo.model;

import java.util.ArrayList;

public class TripDetails {

	public String status;
	public String message;
	public ArrayList<Data> data;
		
	public class Data {

		public String iFeedID;
		public String iUserID;
		public String iDriverID;
		public String vFeedTitle;
		public String vUserView;
		public String type;
		public String tFeedDescription;
		public String specialInst;
		public String foodQuantity;
		public String foodPrices;
		public String nameofRest;
		public String addressofRest;
		public String courierWeight;
		public String vFeedMapImage;
		public String vFeedItemImage;
		public String vStartDate;
		public String vEndDate;
		public String vCost;
		public String vPayment;
		public String videofile;
		public String fLat;
		public String fLong;
		public String fAddress;
		public String toLat;
		public String toLong;
		public String tAddress;
		public String currentLat;
		public String currentLong;
		public String cAddress;
		public String dCreatedDate;
		public String tModifyDate;
		public String vPers;
		public String validAddress;
		public String vETA;
		public String payment_status;
		public String feedback;
		public String LikeStatus;
		public String LikeCount;
		public String DislikeCount;
		public String CommentArray;
		public String vFirst;
		public String vLast;
		public String vToken;
		public String last4;
		public String vDriverorNot;
		public String typeofcard;
		public String vImage;
		public ArrayList<feedTagFriends> feedTagFriends;
	}
	
	public class feedTagFriends {
		
	}

}
