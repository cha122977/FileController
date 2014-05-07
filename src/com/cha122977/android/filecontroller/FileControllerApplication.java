package com.cha122977.android.filecontroller;

import android.app.Application;

public class FileControllerApplication extends Application {

	@Override
	public void onCreate() {
		super.onCreate();

		IconsPool.initIconPool(getApplicationContext());
	}

	@Override
	public void onTerminate() {
		// TODO Auto-generated method stub
		super.onTerminate();
	}
}
