package com.app.waytogo.main;

import android.app.Application;
import android.content.Context;
import android.content.res.Resources;
import android.support.multidex.MultiDex;

public class MainApplication  extends Application {

	private static Context context;

	@Override
	public void onCreate() {
		super.onCreate();
		MultiDex.install(this);
		context = null;
		context = getApplicationContext();
		
	}

	public static Resources getAppResources() {
		return context.getResources();
	}

	public static String getAppString(int resourceId, Object... formatArgs) {
		return getAppResources().getString(resourceId, formatArgs);
	}

}
