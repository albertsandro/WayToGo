package com.app.waytogo.model;

import java.util.List;

public class ListNearestRestaurants {

	public List<objects> obj;

	public class objects {
		public String name;
		public String locality;
		public String street_address;
		public List<cuisines> cuisines;
		public String region;
		//public String long;
		public String phone;
		public String postal_code;
		public List<categories> categories;
		public String has_menu;
		public String country;
		public String lat;
		public String id;
		public String website_url;
		public String resource_uri;
	}
	
	public class cuisines {
		
	}
	
	public class categories {
		
	}
	
}
