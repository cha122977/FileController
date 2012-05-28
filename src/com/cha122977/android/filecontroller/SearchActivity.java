package com.cha122977.android.filecontroller;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;

import android.app.ListActivity;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

public class SearchActivity extends ListActivity{

	EditText et_searchName;
	Button bt_searchButton;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.searchlayout);
		
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
				startSearch();
			}
		});
	}
	
	private void startSearch(){
		String fileNameToSearch = et_searchName.getText().toString();
		if(fileNameToSearch.equals("") || fileNameToSearch==null){//check if user enter the file name
			Toast.makeText(getApplicationContext(), "Please enter file name", Toast.LENGTH_SHORT);
		} else {
			File externalRoot = new File(Environment.getExternalStorageDirectory().getAbsolutePath());
			
			File[] searchResult = externalRoot.listFiles(new FileNameFilter(fileNameToSearch));
			List<String> searchResultFileName = new ArrayList<String>();
			List<String> searchResultFilePath = new ArrayList<String>();
			for(File f : searchResult){
				searchResultFileName.add(f.getName());
				searchResultFilePath.add(f.getAbsolutePath());
			}
			
			setListAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, searchResultFileName));
		}
	}
	
	class FileNameFilter implements FileFilter{

		private String acceptableName;
		
		private  FileNameFilter(){}//private default constructor
		public FileNameFilter(String passName){
			this.acceptableName = passName;
		}
		
		@Override
		public boolean accept(File inputFile) {
			//TODO use recursive method to find all file in input file name.
			//     use resort function???
			String inputFileName = inputFile.getName();
			if(inputFileName.contains(acceptableName)){//have subName in this file
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
}
