package com.app.waytogo.model;

public class DriverInfo {
	
	public String status;
	public String message;
	public Data data;
	
	public class Data {
		public String ID;
		public String iFeedID;
		public String iUserID;
		public String distance;
		public String vCost;
	}

}
