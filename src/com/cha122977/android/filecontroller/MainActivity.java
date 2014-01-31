package com.cha122977.android.filecontroller;

import java.io.File;
import java.util.Stack;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Surface;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends Activity implements IFMWindowFragmentOwner {
	
	@SuppressWarnings("unused")
	private static final String LOG_TAG = "MainActionBarActivity";
	
	private LinearLayout ll_rootWindow;
	
	private FileManagerWindowFragment topWindow, bottomWindow;
	
	/**
	 * Used to save back stack history.
	 */
	private Stack<HistoryObject> actionHistory;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// request window feature.
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);  
		requestWindowFeature(Window.FEATURE_ACTION_BAR);
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_activity_layout);
		
		init();
		setViews();
		setFragment();
	}
	
	private static final String OPEN_FIRST = "OpenAppFirst";
	
	@Override
	protected void onResume() {
		super.onResume();
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
		boolean firstUseApp = sp.getBoolean(OPEN_FIRST, true);
		if (firstUseApp) {
			showHelpDialog(); // open help dialog if use app first time.
			sp.edit().putBoolean(OPEN_FIRST, false).commit();
		}
	}
	
	private void init() {
		actionHistory = new Stack<HistoryObject>();
	}
	
	private void setViews() {
		ll_rootWindow = (LinearLayout) findViewById(R.id.rootView_MainActionBarActivity_LinearLayout);
		
		FragmentManager fm = getFragmentManager();
		
		topWindow = (FileManagerWindowFragment) fm.findFragmentById(R.id.topWindow_MainActionBarActivity_Fragment);
		bottomWindow = (FileManagerWindowFragment) fm.findFragmentById(R.id.bottomWindow_MainActionBarActivity_Fragment);
		
		// revise UI while first load.
		int rotation = ((WindowManager) getSystemService(WINDOW_SERVICE)).getDefaultDisplay().getRotation();
        if (rotation == Surface.ROTATION_90 || rotation == Surface.ROTATION_270) { //if phone rotation
        	ll_rootWindow.setOrientation(LinearLayout.HORIZONTAL);
        }
	}
	
	private void setFragment() {
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) { //use to change the orientation of view.
    	super.onConfigurationChanged(newConfig);
    	// revise UI according new orientation.
    	if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
    		ll_rootWindow.setOrientation(LinearLayout.HORIZONTAL);
    	} else {
    		ll_rootWindow.setOrientation(LinearLayout.VERTICAL);
    	}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main_activity_menu, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_search:
			Intent intent = new Intent(this, SearchActivity.class);
			/** NOTE: Don't use FLAG_ACTIVITY_NEW_TASK since it doesn't work as expected **/
			intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
			startActivityForResult(intent, AppConstant.REQUEST_CODE_SEARCH);
			return true;
		case R.id.action_aboutApp:
			showAboutDialog();
			return true;
		case R.id.action_help:
			showHelpDialog();
			return true;
		case R.id.action_exist:
			finish();
			return true;
		case R.id.action_searchSetting:
			startActivityForResult(new Intent(this, SettingActivity.class), AppConstant.REQUEST_CODE_SETTING);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	private void showAboutDialog() {
		// Linkify the message
	    SpannableString s = new SpannableString(getResources().getString(R.string.about_msg));
	    Linkify.addLinks(s, Linkify.WEB_URLS);
		
		LayoutInflater inflater = LayoutInflater.from(this);
		View view = inflater.inflate(R.layout.alertdialog_about, null);
		TextView msgView = (TextView)view.findViewById(R.id.msg);
		msgView.setText(s);
		
		AlertDialog d = new AlertDialog.Builder(this)
			.setView(view)
			.setPositiveButton(R.string.alertButton_ok, null)
			.create();
		d.show();
		
		msgView.setMovementMethod(LinkMovementMethod.getInstance());
	}
	
	private void showHelpDialog() {		
		LayoutInflater inflater = LayoutInflater.from(this);
		View view = inflater.inflate(R.layout.alertdialog_help, null);
		
		AlertDialog d = new AlertDialog.Builder(this)
			.setView(view)
			.setPositiveButton(R.string.alertButton_ok, null)
			.create();
		d.show();
	}
	
	private void openExitCheckDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.exitDialog_title);
		builder.setPositiveButton(R.string.exitDialog_exitButtonText, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				finish();
			}
		});
		builder.setNegativeButton(R.string.exitDialog_unexitButtonText, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// do nothing.
			}
		});
		builder.show();
	}
	
	/** implementation of IFMWindowFragmentOwner **/
	
	@Override
	public void refreshAllWindow() {
		topWindow.refresh();
		bottomWindow.refresh();
	}
	
	@Override
	public void syncLists(FileManagerWindowFragment requester) {
		String requestDirPath = requester.getDirectory().getAbsolutePath();
		String anotherDirPath = getAnotherWindowDir(requester).getAbsolutePath();
		if (requestDirPath.equals(anotherDirPath)) {
			getAnotherWindow(requester).refresh();
		}
		requester.refresh();
	}
	
	@Override
	public FileManagerWindowFragment getAnotherWindow(FileManagerWindowFragment requester) {
		return requester == topWindow? bottomWindow: topWindow;
	}

	@Override
	public File getAnotherWindowDir(FileManagerWindowFragment requester) {
		return getAnotherWindow(requester).getDirectory();
	}

	@Override
	public void refreshOtherWindows(FileManagerWindowFragment requester) {
		getAnotherWindow(requester).refresh();
	}

	@Override
	public void pushDirHistory(FileManagerWindowFragment requester, File directory) {
		actionHistory.push(new HistoryObject(requester, directory));
	}
	
	/** End of implementation of IFMWindowFragmentOwner **/

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == AppConstant.REQUEST_CODE_SEARCH) {
			FileManagerWindowFragment opener = null;
			File openedFile = null;
			switch (resultCode) {
			case AppConstant.RESULT_CODE_OPEN_TOP:
				opener = topWindow;
				openedFile = new File(data.getStringExtra(AppConstant.KEY_FILE_PATH));
				break;
			case AppConstant.RESULT_CODE_OPEN_BOTTOM:
				opener = bottomWindow;
				openedFile = new File(data.getStringExtra(AppConstant.KEY_FILE_PATH));
				break;
			default:
				super.onActivityResult(requestCode, resultCode, data);
				return;
			}
			
			File currentDir = opener.getDirectory();
			if (openedFile.isDirectory()) {
				if (opener.openData(openedFile)) { // open directory succeed.
					pushDirHistory(opener, currentDir); // push previous to history.
				}
			} else {
				if (opener.openData(openedFile.getParentFile())) {
					opener.scrollToFile(openedFile); // opened data is file, thus scroll to it.
					pushDirHistory(opener, currentDir); // push previous to history.
				}
			}
		}
	}
	
	/** AREA implementation of back stack **/
	
	@Override
	public void onBackPressed() {
		if (actionHistory.size() == 0) {
			openExitCheckDialog();
		} else {
			HistoryObject ho = actionHistory.pop();
			(ho.fileManager).openDirectory(ho.dir);
		}
	}
	
	/** History Object **/
	private static class HistoryObject {
		public FileManagerWindowFragment fileManager;
		public File dir;
		public HistoryObject(FileManagerWindowFragment requester, File directory) {
			fileManager = requester;
			dir = directory;
		}
	}
	
	/** End of History Object **/
	/** End of implementation of back stack **/
}
