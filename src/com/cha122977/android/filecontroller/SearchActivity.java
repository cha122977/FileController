package com.cha122977.android.filecontroller;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Stack;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Environment;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class SearchActivity extends ListActivity{
	
	private EditText et_searchName;
	private Button bt_searchButton;
	
	private Stack<String> visitedHistory; // history of visited directories 
	
	private ArrayList<String> searchResultPathList;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
	    setContentView(R.layout.searchlayout);
		
		setTitle(R.string.search_activity_title);
		
		initial();
		setViews();
		setListeners();
	}
	
	private void initial() {
		visitedHistory = new Stack<String>();
	}
	
	private void setViews(){
		et_searchName = (EditText) findViewById(R.id.searchNameEdit);
		bt_searchButton = (Button) findViewById(R.id.searchButton);
	}
	
	private void setListeners(){
		bt_searchButton.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				String externalRoot = Environment.getExternalStorageDirectory().getAbsolutePath();
				String keyWord = et_searchName.getText().toString();
				startSearch(externalRoot, keyWord);
			}
		});
		
		et_searchName.setOnKeyListener(new OnKeyListener(){
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if(keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN){
					String externalRoot = Environment.getExternalStorageDirectory().getAbsolutePath();
					String keyWord = et_searchName.getText().toString();
					startSearch(externalRoot, keyWord);
					return true;
				}
				return false;
			}
		});
		
		getListView().setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapter, View view, int which, long id) {
				String filePath = searchResultPathList.get(which);
				if(new File(filePath).isDirectory()){
					visitedHistory.push(filePath);
					openTopOrDown(filePath);
				} else {
					FSController.openFile(new File(filePath), getApplicationContext());
				}
			}
		});
		
		getListView().setOnItemLongClickListener(new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> adapter, View view, int which, long id) {
				String filePath = searchResultPathList.get(which);
				if (new File(filePath).isDirectory()) {
					openDirectory(filePath);
				} else {
					openFile(filePath);
				}
				return true;
			}
		});
	}
	
	private void openTopOrDown(String filePath) {
		searchResultPathList = new ArrayList<String>();
		for(File f: new File(filePath).listFiles()) {
			searchResultPathList.add(f.getPath());
		}
		setListAdapter(new SearchListAdapter(this, searchResultPathList));
	}
	
	private void openDirectory(String filePath){
		String[] s = getResources().getStringArray(R.array.alert_searchListDirectoryOption);
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	builder.setTitle(filePath);
		builder.setItems(s, new DirectoryDialog(filePath));
		builder.show();
	}
	
	private void openFile(final String filePath){
		String[] s = getResources().getStringArray(R.array.alert_searchListFileOption);
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	builder.setTitle(filePath);
    	builder.setItems(s, new FileDialog(filePath));
		builder.show();
	}
	
	private void startSearch(String targetDirectory, final String keyWord){
		if(keyWord.equals("") || keyWord==null){//check if user enter the file name
			Toast.makeText(getApplicationContext(), R.string.search_no_text, Toast.LENGTH_SHORT).show();
		} else {
			final File file = new File(targetDirectory);
			if(file != null){//make sure
				new Thread(new Runnable() {
					@Override
					public void run() {
						SearchActivity.this.runOnUiThread(new Runnable() {
							@Override
							public void run() {
								setProgressBarIndeterminateVisibility(true);
							}
						});
						searchResultPathList = deepSearch(file, keyWord);
						SearchActivity.this.runOnUiThread(new Runnable() {
							@Override
							public void run() {
								setListAdapter(new SearchListAdapter(SearchActivity.this, searchResultPathList));
								setProgressBarIndeterminateVisibility(false);
							}
						});
					}
				}).start();
			}
		}
	}
	
	private ArrayList<String> deepSearch(File targetDirectory, String keyWord){
		ArrayList<String> result = new ArrayList<String>();
		//shadow search
		File[] listfile = targetDirectory.listFiles(new FileNameFilter(keyWord));
		listfile = FSController.filterCannotWriteFile(listfile);
		for(File f: listfile){
			result.add(f.getAbsolutePath());
		}
		
		//deep search(use recursive method)
		listfile = targetDirectory.listFiles();
		listfile = FSController.filterCannotWriteFile(listfile);
		for(File f: listfile){
			if(f.isDirectory()){
				result.addAll(deepSearch(f, keyWord));
			}
		}
		return result;
	}	
	
	private class FileNameFilter implements FileFilter{
		private String acceptableName;
		public FileNameFilter(String passName){
			this.acceptableName = passName.toLowerCase();
		}
		@Override
		public boolean accept(File inputFile) {
			String inputFileNameLowCase = inputFile.getName().toLowerCase();
			if(inputFileNameLowCase.contains(acceptableName)){//have subName in this file
				return true;
			} else {
				return false;
			}			
		}
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {//use to change the orientation of view.
    	super.onConfigurationChanged(newConfig);    	
    	if(newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE){
    		//DO nothing
    	} else {
    		//Do nothing
    	}
	}
	
	private class DirectoryDialog implements DialogInterface.OnClickListener{
		private String filePath;
		public DirectoryDialog(String filePath){
			this.filePath = filePath;
		}
		@Override
		public void onClick(DialogInterface dialog, int which) {
			switch(which){
			case 0://open in Top window
				setResultAndFinish(AppConstant.RESULT_CODE_OPEN_TOP, filePath);
				break;
			case 1://open in Bottom window
				setResultAndFinish(AppConstant.RESULT_CODE_OPEN_BOTTOM, filePath);
				break;
			case 2://Show file info
				FSController.showFileInformation(filePath, SearchActivity.this);
				break;
			case 3://Delete
				AlertDialog.Builder builder = new AlertDialog.Builder(SearchActivity.this);
				builder.setMessage(R.string.delete_alertDeleteFileMsg);
	    		builder.setPositiveButton(R.string.delete_deleteButton, new DialogInterface.OnClickListener(){
					@Override
					public void onClick(DialogInterface dialog, int which) {
						pureDeleteDirectory(filePath);
					}
				});
	    		builder.setNegativeButton(R.string.alertButton_cancel, null);
				builder.show();
				break;
			case 4://Cancel
				//do nothing
				break;
			default:
				//do nothing
				break;
			}
		}
	}
	
	private class FileDialog implements DialogInterface.OnClickListener{
		private String filePath;
		public FileDialog(String filePath){
			this.filePath = filePath;
		}
		@Override
		public void onClick(DialogInterface dialog, int which) {
			switch(which){
			case 0://open file.
				FSController.openFile(new File(filePath), getApplicationContext());
				break;
			case 1://Show in Top window
				setResultAndFinish(AppConstant.RESULT_CODE_OPEN_TOP, filePath);
				break;
			case 2://Show in Bottom window
				setResultAndFinish(AppConstant.RESULT_CODE_OPEN_BOTTOM, filePath);
				break;
			case 3://more information
				FSController.showFileInformation(filePath, SearchActivity.this);
				break;
			case 4://Delete
				AlertDialog.Builder builder = new AlertDialog.Builder(SearchActivity.this);
				builder.setMessage(R.string.delete_alertDeleteDirMsg);
	    		builder.setPositiveButton(R.string.delete_deleteButton, new DialogInterface.OnClickListener(){
					@Override
					public void onClick(DialogInterface dialog, int which) {
						pureDeleteFile(filePath);
					}
				});
	    		builder.setNegativeButton(R.string.alertButton_cancel, null);
				builder.show();
				break;
			case 5://Cancel
				// do nothing
				break;
			default:
				//do nothing
				break;
			}
		}
	}
	
	private void setResultAndFinish(int resultCode, String path){
		Intent intent = new Intent();
		intent.putExtra("path", new File(path).getParent());
		setResult(resultCode, intent);
		finish();;
	}
	
	// Delete function
	private void pureDeleteFile(String beDeletedFilePath){
    	File f = new File(beDeletedFilePath);
    	boolean result = f.delete();
    	if(result == true){
    		Toast.makeText(getApplicationContext(), beDeletedFilePath + getString(R.string.delete_deleteDataSucceed), Toast.LENGTH_LONG).show();
    	} else {
    		Toast.makeText(getApplicationContext(), f.getName() + getString(R.string.delete_deleteDataFailure), Toast.LENGTH_LONG).show();
    	}
    	String externalRoot = Environment.getExternalStorageDirectory().getAbsolutePath();
		String keyWord = et_searchName.getText().toString();
		startSearch(externalRoot, keyWord);
    }
    
    private void pureDeleteDirectory(String beDeletedPath){
    	if(deleteDirectoryNested(beDeletedPath) == true){
    		String externalRoot = Environment.getExternalStorageDirectory().getAbsolutePath();
			String keyWord = et_searchName.getText().toString();
			startSearch(externalRoot, keyWord);
    		Toast.makeText(getApplicationContext(), R.string.delete_deleteDataSucceed, Toast.LENGTH_LONG).show();
    	} else {
    		Toast.makeText(getApplicationContext(), R.string.delete_deleteDataFailure, Toast.LENGTH_LONG).show();
    	}
    }
    private boolean deleteDirectoryNested(String inputPath){
    	File f = new File(inputPath);
    	if(f.isFile()){//path is file
    		return f.delete();
    	} else {//path is directory
    		File[] fl = f.listFiles();
    		for(File i: fl){
    			if(deleteDirectoryNested(i.getAbsolutePath()) == false){
    				return false;
    			}
    		}
    		return f.delete();
    	}
    }
    
    @Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_BACK){
			if (visitedHistory.isEmpty()) { // back to previous Activity
				return super.onKeyDown(keyCode, event);
			}
			visitedHistory.pop(); // delete current directory(unused)
			if (visitedHistory.isEmpty()) {
				new Thread(new Runnable() {
					@Override
					public void run() {
						SearchActivity.this.runOnUiThread(new Runnable() {
							@Override
							public void run() {
								setProgressBarIndeterminateVisibility(true);
							}
						});
						searchResultPathList = deepSearch(new File(Environment.getExternalStorageDirectory().getAbsolutePath()),
								  						  et_searchName.getText().toString());
						SearchActivity.this.runOnUiThread(new Runnable() {
							@Override
							public void run() {
								setListAdapter(new SearchListAdapter(SearchActivity.this, searchResultPathList));
								setProgressBarIndeterminateVisibility(false);
							}
						});
					}
				}).start();
			} else {
				openTopOrDown(visitedHistory.peek());
			}
			return false;
		} 
		return super.onKeyDown(keyCode, event);
	}
}
