package com.app.waytogo.model;

import java.util.List;

public class DestinationLocationDetails {

	public List<DestinationLocationDetail> results;

	public class DestinationLocationDetail {
		public List<address_components> address_components;
		public String formatted_address;
		public GeoMetry geometry;
	}
	
	public class address_components {
		public String short_name;
	}
		
	public class GeoMetry {
		public LocationLatLng location;
	}
	
	public class LocationLatLng {
		public String lat;
		public String lng;
	}
}
