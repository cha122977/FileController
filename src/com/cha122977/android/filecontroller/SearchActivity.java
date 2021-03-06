package com.cha122977.android.filecontroller;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Stack;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.Toast;

public class SearchActivity extends ListActivity implements
		PopupMenu.OnMenuItemClickListener, OnSharedPreferenceChangeListener {
	
	private EditText et_searchName;
	private ImageButton ib_searchButton;
	
	private Stack<String> visitedHistory; // history of visited directories 
	
	private ArrayList<String> searchResultPathList;
	
	private static final int PROGRESS_SHOW = 0;
	private static final int PROGRESS_HIDE = 1;
	private static final int DELETE_DATA_SUCCEED = 13;
	private static final int DELETE_DATA_FAILED = 14;
	private static final int SHOW_INFO_DIALOG = 15;
	
	@SuppressLint("HandlerLeak")
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case PROGRESS_SHOW:
				setProgressBarIndeterminateVisibility(true);
				break;
			case PROGRESS_HIDE:
				if (numberOfSearchingThread == 0) { // if all searching thread are complete
					setProgressBarIndeterminateVisibility(false);
				}
				break;
			case SHOW_INFO_DIALOG:
				Bundle infoArgs = msg.getData();
				openShowInfoDialog(infoArgs.getString(KEY_INFO_TITLE), infoArgs.getInt(KEY_INFO_ICON_RES_ID), infoArgs.getString(KEY_INFO_MSG));
				break;
			case DELETE_DATA_SUCCEED:
				String deleteSucceedDataPath = msg.getData().getString(AppConstant.KEY_FILE_PATH);
				Toast.makeText(getApplicationContext(), deleteSucceedDataPath + getString(R.string.delete_deleteDataSucceed), Toast.LENGTH_LONG).show();
				sendEmptyMessage(PROGRESS_HIDE);
				break;
			case DELETE_DATA_FAILED:
				String deleteFailedDataPath = msg.getData().getString(AppConstant.KEY_FILE_PATH);
				Toast.makeText(getApplicationContext(), deleteFailedDataPath + getString(R.string.delete_deleteDataFailure), Toast.LENGTH_LONG).show();
				sendEmptyMessage(PROGRESS_HIDE);
				break;
				
			}
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
	    setContentView(R.layout.search_activity_layout);
		
		setTitle(R.string.search_activity_title);
		
		init();
		setViews();
		setListeners();
	}
	
	@Override
	protected void onDestroy() {
	    super.onPause();
	    // unregister sharedPreferecnes callback
	    PreferenceManager.getDefaultSharedPreferences(this)
	            .unregisterOnSharedPreferenceChangeListener(this);
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {//use to change the orientation of view.
    	super.onConfigurationChanged(newConfig);    	
    	if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
    		// do nothing.  
    	} else {
    		// do nothing.
    	}
	}
	
	private boolean flag_showHiddenFiles = false;
	
	private void init() {
		visitedHistory = new Stack<String>();
		// get preferecnes.
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
		flag_showHiddenFiles = sp.getBoolean(AppConstant.KEY_IF_SHOW_HIDDEN_FILES, false);
		sp.registerOnSharedPreferenceChangeListener(this); // register preference change listener.
	}
	
	private void setViews() {
		et_searchName = (EditText) findViewById(R.id.searchNameEdit);
		ib_searchButton = (ImageButton) findViewById(R.id.searchImageButton);
	}
	
	private void setListeners() {
		ib_searchButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String searchedDir = AppConstant.PRIMARY_STORAGE_ROOT;
				String esdState = Environment.getExternalStorageState();
				if (esdState.equals(Environment.MEDIA_REMOVED)) { // no external root.
					searchedDir = AppConstant.ROOT;
				}
				String keyWord = et_searchName.getText().toString();
				startSearch(searchedDir, keyWord);
			}
		});
		
		et_searchName.setOnKeyListener(new OnKeyListener() {
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN) {
					String searchedDir = AppConstant.PRIMARY_STORAGE_ROOT;
					String esdState = Environment.getExternalStorageState();
					if (esdState.equals(Environment.MEDIA_REMOVED)) { // no external root.
						searchedDir = AppConstant.ROOT;
					}
					String keyWord = et_searchName.getText().toString();
					startSearch(searchedDir, keyWord);
					return true;
				}
				return false;
			}
		});
		
		getListView().setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapter, View view, int which, long id) {
				String filePath = searchResultPathList.get(which);
				if (new File(filePath).isDirectory()) {
					visitedHistory.push(filePath);
					openDirectory(filePath);
				} else {
					FSController.openFile(new File(filePath), getApplicationContext());
				}
			}
		});
		
		getListView().setOnItemLongClickListener(new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> adapter, View view, int position, long id) {
				showListPopupMenu(view, position);
				return true;
			}
		});
	}
	
	private void openDirectory(String filePath) {
		searchResultPathList = new ArrayList<String>();
		File[] files = new File(filePath).listFiles();
		if (flag_showHiddenFiles == false) {
			files = FSController.filterHiddenFiles(files);
		}
		for (File f: files) {
			searchResultPathList.add(f.getPath());
		}
		setListAdapter(new SearchListAdapter(this, searchResultPathList));
	}
	
	/** AREA options menu **/
	
	private int selectedListFilePosition;
	private void showListPopupMenu(View v, int position) {
	    PopupMenu popup = new PopupMenu(this, v);
	    MenuInflater inflater = popup.getMenuInflater();
	    File selectedData = new File(searchResultPathList.get(position));
	    if (selectedData.isDirectory()) {
	    	inflater.inflate(R.menu.search_directory_options_menu, popup.getMenu());
	    } else {
	    	inflater.inflate(R.menu.search_file_options_menu, popup.getMenu());
	    }
	    popup.setOnMenuItemClickListener(this);
	    selectedListFilePosition = position;
	    
	    popup.show();
	}
	
	/**
	 * Implementation of PopupMenu's options.
	 */
	@Override
	public boolean onMenuItemClick(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_showInTopWindow:
			setResultAndFinish(AppConstant.RESULT_CODE_OPEN_TOP,
					searchResultPathList.get(selectedListFilePosition));
			return true;
		case R.id.menu_showInBottomWindow:
			setResultAndFinish(AppConstant.RESULT_CODE_OPEN_BOTTOM,
					searchResultPathList.get(selectedListFilePosition));
			return true;
		case R.id.menu_showDirInfo:
		case R.id.menu_showFileInfo:
			showDataInfo(new File(searchResultPathList.get(selectedListFilePosition)));
			return true;
		case R.id.menu_deleteFile:
		case R.id.menu_deleteDir:
			openDeleteDataDialog(new File(searchResultPathList.get(selectedListFilePosition)));
			return true;
		default:
			return false;
		}
	}
	
	/*** Show on Top or Bottom window ***/
	private void setResultAndFinish(int resultCode, String path) {
		Intent intent = new Intent();
		intent.putExtra(AppConstant.KEY_FILE_PATH, path);
		setResult(resultCode, intent);
		finish();;
	}
	/*** End of Show on Top or Bottom window ***/
	
	/*** Show Data information ***/
	/** 
	 * Calculate information of data need sometimes, so we need show dialog after calculation.
	 * Thus we should mHandler to call "openInfoDialog" function.
	 */
	/**
	 * Key set for Bundle.
	 */
	private static final String KEY_INFO_TITLE = "infoTitle";
	private static final String KEY_INFO_ICON_RES_ID = "infoIconResId";
	private static final String KEY_INFO_MSG = "infoMsg";
	private void showDataInfo(final File selectedData) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				mHandler.sendEmptyMessage(PROGRESS_SHOW);
				String dataInfo = getDataInfo(selectedData);
				
				Message msg = new Message();
				Bundle args = new Bundle();
				args.putString(KEY_INFO_TITLE, selectedData.getAbsolutePath());
				args.putInt(KEY_INFO_ICON_RES_ID, selectedData.isDirectory()?
														R.drawable.open : R.drawable.file);
				args.putString(KEY_INFO_MSG, dataInfo);
				msg.setData(args);
				msg.what = SHOW_INFO_DIALOG;
				
				mHandler.sendMessage(msg);
				mHandler.sendEmptyMessage(PROGRESS_HIDE);
			}
		}).start();
	}
	
	private void openShowInfoDialog(String title, int iconResId, String msg) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(title);
		builder.setIcon(iconResId);
        builder.setMessage(msg);
        builder.setPositiveButton(R.string.alertButton_ok, null);
        builder.setCancelable(true);
        builder.show();
	}
	
	private String getDataInfo(File selectedData) {
		// get data size.
		long dataSize = FSController.calculateBytesInDirectory(selectedData);
		String size = getString(R.string.fileInfo_size)
				+ FSController.translateBytesToHummanReadable(dataSize,
						getString(R.string.fileInfo_unit_byte),
						getString(R.string.fileInfo_unit_kByte),
						getString(R.string.fileInfo_unit_mByte),
						getString(R.string.fileInfo_unit_gByte))
				+ "\n";
		
		// get last modified info.
		String lastModified = getString(R.string.fileInfo_lastModify) + "\n"
				+ FSController.getDataLastModifiedDate(selectedData) + "\n";
		
		if (selectedData.isDirectory()) {
			// get total files number in directory.
			int numberOfFiles = FSController.calculateFileNumberInDirectory(selectedData);
			String filesNumber = getString(R.string.fileInfo_containingFileNumber) + numberOfFiles +"\n";
			return filesNumber + size + lastModified;
		} else {
			return size + lastModified; 
		}
	}
	
	/*** End of Show Data information ***/
	
	/*** Delete data ***/
	
	/**
	 * use to delete file and directory.
	 * @param deletedData file wait for delete.
	 */
	private void openDeleteDataDialog(final File deletedData) {
		// basic dialog setting.
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.delete_alertTitle);
		builder.setNegativeButton(R.string.alertButton_cancel, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				Toast.makeText(getApplicationContext(), R.string.action_cancel, Toast.LENGTH_SHORT).show();
			}
		});
		builder.setPositiveButton(R.string.delete_deleteButton, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				deleteData(deletedData);
			}
		});
		// set msg according deleted data type(dir or file).
		builder.setMessage(deletedData.isDirectory()?
				R.string.delete_alertDeleteDirMsg: R.string.delete_alertDeleteFileMsg);
		builder.show();
	}
	
	private void deleteData(final File deletedData) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				mHandler.sendEmptyMessage(PROGRESS_SHOW);
				if (FSController.deleteData(deletedData) == true) {
					Message msg = new Message();
					Bundle bundle = new Bundle();
					bundle.putString(AppConstant.KEY_FILE_PATH, deletedData.getName());
					msg.setData(bundle);
					msg.what = DELETE_DATA_SUCCEED;
					mHandler.sendMessage(msg);
				} else {
					Message msg = new Message();
					Bundle bundle = new Bundle();
					bundle.putString(AppConstant.KEY_FILE_PATH, deletedData.getName());
					msg.setData(bundle);
					msg.what = DELETE_DATA_FAILED;
					mHandler.sendMessage(msg);
				}
			}
		}).start();
	}
	
	/*** End of Delete data ***/
	
	/** End of implementation of menu options. **/ 
	
	/** AREA Search function **/
	
	private Object searchingThreadLock = new Object();
	private int numberOfSearchingThread = 0;
	
	private void startSearch(String targetDirectory, final String keyWord){
		if (keyWord.equals("") || keyWord == null) { // check if user enter the file name
			Toast.makeText(this, R.string.search_no_text, Toast.LENGTH_SHORT).show();
		} else {
			final File file = new File(targetDirectory);
			if (file.exists()) { // make sure
				startDeepSearch(file, keyWord);
			}
		}
	}
	
	private void startDeepSearch(final File targetDirectory, final String keyWord) {
		// clean search result first.
		searchResultPathList = new ArrayList<String>();
		final SearchListAdapter newAdapter = new SearchListAdapter(getApplicationContext(), searchResultPathList);
		setListAdapter(newAdapter);
		
		new Thread(new Runnable() {
			@Override
			public void run() {
				synchronized (searchingThreadLock) {
					numberOfSearchingThread++;
				}
				mHandler.sendEmptyMessage(PROGRESS_SHOW);
				
				deepSearchAndNotify(targetDirectory, keyWord, newAdapter);
				
				synchronized (searchingThreadLock) {
					numberOfSearchingThread--;
				}
				mHandler.sendEmptyMessage(PROGRESS_HIDE);
			}
		}).start();
	}
	
	/**
	 * Search files and notify adapter to change the function.
	 * @param targetDirectory directory to search
	 * @param keyWord search keyword
	 * @param adapter adapter which handle the result.
	 */
	private void deepSearchAndNotify(File targetDirectory, String keyWord, final SearchListAdapter adapter) {
		// shadow search, search current directory only.
		File[] listfile = targetDirectory.listFiles(new FileNameFilter(keyWord));
		if (flag_showHiddenFiles == false) {
			listfile = FSController.filterHiddenFiles(listfile);
		}
		listfile = FSController.filterCannotReadFiles(listfile);
		if (listfile != null) {
			for (final File f: listfile) {
				runOnUiThread(new Runnable() { // only UI thread can change adapter, or VM throws illegalState.
					@Override
					public void run() {
						adapter.addFilePath(f.getAbsolutePath());
						adapter.notifyDataSetChanged();
					}
				});
			}
		}
		
		// deep search(use recursive method), search inner directory.
		listfile = targetDirectory.listFiles();
		if (flag_showHiddenFiles == false) {
			listfile = FSController.filterHiddenFiles(listfile);
		}
		listfile = FSController.filterCannotReadFiles(listfile);
		if (listfile != null) {
			listfile = FSController.filterCannotReadFiles(listfile);
			for (File f: listfile) {
				if (f.isDirectory()) { // search recursively.
					deepSearchAndNotify(f, keyWord, adapter);
				}
			}
		}
	}
	
	/**
	 * Used to filter the files name.
	 * @author cha122977
	 */
	private class FileNameFilter implements FileFilter {
		private String acceptableName;
		public FileNameFilter(String passName) {
			this.acceptableName = passName.toLowerCase();
		}
		@Override
		public boolean accept(File inputFile) {
			String inputFileNameLowCase = inputFile.getName().toLowerCase();
			if (inputFileNameLowCase.contains(acceptableName)) {//have subName in this file
				return true;
			} else {
				return false;
			}			
		}
	}
	
	/** End of Search function **/
	
	/** AREA activity action **/

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.search_activity_menu, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_searchSetting:
			startActivity(new Intent(this, SettingActivity.class));
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	/** End of activity action **/
	
	/** AREA back stack implementation **/
	@Override
	public void onBackPressed() {
		if (visitedHistory.isEmpty()) { // back to previous Activity
			super.onBackPressed();
			return; 
		}
		visitedHistory.pop(); // delete current directory(unused)
		if (visitedHistory.isEmpty()) {
			startDeepSearch(new File(AppConstant.PRIMARY_STORAGE_ROOT), 
					et_searchName.getText().toString());
		} else {
			openDirectory(visitedHistory.peek());
		}
	}
	/** End of back stack implementation **/
	
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		if (key.equals(AppConstant.KEY_IF_SHOW_HIDDEN_FILES)) {
			flag_showHiddenFiles = sharedPreferences.getBoolean(AppConstant.KEY_IF_SHOW_HIDDEN_FILES, false);
		}
	}

	/** End of back stack implementation **/
}
