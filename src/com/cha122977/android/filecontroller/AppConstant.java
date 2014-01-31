package com.cha122977.android.filecontroller;

import android.os.Environment;

public class AppConstant {

	public final static String PRIMARY_STORAGE_ROOT = Environment.getExternalStorageDirectory().getAbsolutePath();
	public final static String ROOT = Environment.getRootDirectory().getAbsolutePath();

	public static final String PREFS_NAME = "UserPrefs";
	public static final String OPEN_FIRST = "OpenAppFirst";

	public static final int REQUEST_CODE_SEARCH = 11; //use to start searchActivity.
	public static final int RESULT_CODE_OPEN_TOP = 12;
	public static final int RESULT_CODE_OPEN_BOTTOM = 13;
	public static final int REQUEST_CODE_SETTING = 14;

	public static final String KEY_FILE_PATH = "path"; 
}
