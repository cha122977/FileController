package com.cha122977.android.filecontroller;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;

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
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class SearchActivity extends ListActivity{

	private EditText et_searchName;
	private Button bt_searchButton;
	
	private ArrayList<String> searchResultPathList;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.searchlayout);
		setTitle(R.string.search_activity_title);
		
		setViews();
		setListeners();
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
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				//TODO search activity dialog
				String filePath = searchResultPathList.get(arg2);
				if(new File(filePath).isDirectory()){
					openDirectory(filePath);
				}else{
					openFile(filePath);
				}
			}
		});
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
	
	private void startSearch(String targetDirectory, String keyWord){
		if(keyWord.equals("") || keyWord==null){//check if user enter the file name
			Toast.makeText(getApplicationContext(), R.string.search_no_text, Toast.LENGTH_SHORT).show();
		} else {
			File file = new File(targetDirectory);
			if(file != null){//make sure
				searchResultPathList = deepSearch(file, keyWord);
				setListAdapter(new SearchListAdapter(this, searchResultPathList));
			}
		}
	}
	
	private ArrayList<String> deepSearch(File targetDirectory, String keyWord){
		ArrayList<String> result = new ArrayList<String>();
		
		//shadow search
		File[] listfile = targetDirectory.listFiles(new FileNameFilter(keyWord));
		listfile = ListFileProcessor.filterCannotWriteFile(listfile);
		for(File f: listfile){
			result.add(f.getAbsolutePath());
		}
		
		//deep search(use recursive method)
		listfile = targetDirectory.listFiles();
		listfile = ListFileProcessor.filterCannotWriteFile(listfile);
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
				setResultAndFinish(FileControllerActivity.RESULT_CODE_OPEN_TOP, filePath);
				break;
			case 1://open in Bottom window
				setResultAndFinish(FileControllerActivity.RESULT_CODE_OPEN_BOTTOM, filePath);
				break;
			case 2://Show file info
				ListFileProcessor.showFileInformation(filePath, SearchActivity.this);
				break;
			case 3://Cancel
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
				ListFileProcessor.openFile(filePath, getApplicationContext());
				break;
			case 1://Show in Top window
				setResultAndFinish(FileControllerActivity.RESULT_CODE_OPEN_TOP, filePath);
				break;
			case 2://Show in Bottom window
				setResultAndFinish(FileControllerActivity.RESULT_CODE_OPEN_BOTTOM, filePath);
				break;
			case 3://more information
				ListFileProcessor.showFileInformation(filePath, SearchActivity.this);
				break;
			case 4://Cancel
				//Do nothing
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
		finish();
	}
		
}
