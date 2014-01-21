package com.cha122977.android.filecontroller;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBarActivity;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActionBarActivity extends ActionBarActivity {
	
	private LinearLayout ll_rootWindow;
	
	private FileManagerWindowFragment topWindow, bottomWindow;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_action_bar_activity_layout);
		
		setViews();
		setFragment();
	}
	
	private void setViews() {
		ll_rootWindow = (LinearLayout) findViewById(R.id.rootView_MainActionBarActivity_LinearLayout);
		
		FragmentManager fm = getSupportFragmentManager();
		
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
		case R.id.action_createNewTopDir:
			// TODO create new top dir.
			return true;
		case R.id.action_createNewBottomDir:
			// TODO create new bottom dir.
			return true;
		case R.id.action_aboutApp:
			showAboutDialog();
			return true;
		case R.id.action_help:
			showHelpDialog();
			return true;
		case R.id.action_search:
			Intent intent = new Intent(this, SearchActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivityForResult(intent, AppConstant.REQUEST_CODE_SEARCH);
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
}
