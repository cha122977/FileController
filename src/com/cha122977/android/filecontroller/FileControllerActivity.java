package com.cha122977.android.filecontroller;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class FileControllerActivity extends Activity {
	private final String ROOT = "/sdcard/";

    TextView tv_topDir, tv_bottomDir;
	ListView lv_topListView, lv_bottomListView;
	
	List<String> topFilePath;
	List<String> bottomFilePath;
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);//¥h±¼Activity¦WºÙÄæ¦ì
        setContentView(R.layout.main);
        
        setView();
        initial();
        setListener();
        
        openTopFile(ROOT);//at initial: open the SD-card root
        openBottomFile(ROOT);//at initial: open the SD-card root
    }
    private void setView(){
    	tv_topDir = (TextView)findViewById(R.id.topTextView);
    	tv_bottomDir = (TextView)findViewById(R.id.bottomTextView);
    	lv_topListView = (ListView)findViewById(R.id.topListView);
    	lv_bottomListView = (ListView)findViewById(R.id.bottomListView);
    }
    private void initial(){
    	topFilePath = new ArrayList<String>();
    	bottomFilePath = new ArrayList<String>();
    }
    private void setListener(){
    	tv_topDir.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String s = tv_topDir.getText().toString();
				File f = new File(s);
				s = f.getParent();
				openTopFile(s);
				f = null;
				s = null;
			}
		});
    	tv_bottomDir.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String s = tv_bottomDir.getText().toString();
				File f = new File(s);
				s = f.getParent();
				openBottomFile(s);
				f = null;
				s = null;
			}
		});
    	
    	lv_topListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				File f = new File(topFilePath.get(arg2));
				if(f.isDirectory()){
					openTopFile(topFilePath.get(arg2));
				}else{
					Toast.makeText(getApplicationContext(), "This is File", Toast.LENGTH_SHORT).show();
				}
			}
		});
    	
    	lv_bottomListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				File f = new File(bottomFilePath.get(arg2));
				if(f.isDirectory()){
					openBottomFile(bottomFilePath.get(arg2));
				}else{
					Toast.makeText(getApplicationContext(), "This is File", Toast.LENGTH_SHORT).show();
				}
			}
		});
    	
    	lv_topListView.setOnItemLongClickListener(new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				openTopOptionsDialog(arg2);
				return true;
			}
		});
    	
    	lv_bottomListView.setOnItemLongClickListener(new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				openButtomOptionsDialog(arg2);
				return true;
			}
		});
    }
    
    private void openTopFile(String dir){
    	if(dir!=null){
	    	File f = new File(dir);
	    	if(f.canRead()){
		    	File[] fl = f.listFiles();
		    	File[] fList = reSort(fl);//reSort FileList
		    	topFilePath.clear();//clear the list
		    	for(File i: fList){
		    		topFilePath.add(i.getPath());
		    	}
		    	tv_topDir.setText(dir);
		    	lv_topListView.setAdapter(new FileListAdapter(this, topFilePath));
		    }else{
		    	Toast.makeText(this, "No Premission", Toast.LENGTH_SHORT).show();
		    }
    	}
    }
    private void openBottomFile(String dir){
    	if(dir!=null){
	    	File f = new File(dir);
	    	if(f.canRead()){
	    		File[] fList = f.listFiles();
	    		fList = reSort(fList);//reSort FileList
	        	bottomFilePath.clear();//clear the list
	        	for(File i: fList){
	        		bottomFilePath.add(i.getPath());
	        	}
	        	tv_bottomDir.setText(dir);
	        	lv_bottomListView.setAdapter(new FileListAdapter(this, bottomFilePath));
	    	}else{
	    		Toast.makeText(this, "No Premission", Toast.LENGTH_SHORT).show();
	    	}
    	}
    }
    
    private void openTopOptionsDialog(int position){//run this function when top listView clickItemLongClick(it will show menu to choose action)
    	String[] s = getResources().getStringArray(R.array.alert_option);
    	s[0] += " " + tv_bottomDir.getText().toString();
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(topFilePath.get(position));
		builder.setItems(s, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch(which){
				case 0://Rename
					//TODO Rename the file
					break;
				case 1://Move
					//TODO Move function
					break;
				case 2://Copy
					//TODO copy file to the other side
					break;
				case 3://Delete
					//TODO delete file, on more Dialog to make sure the user want to delete the file
					openDeleteCheckDialog();
					break;
				case 4://Cancel
					//Do nothing
					break;
				}
				//test line
				Toast.makeText(getApplicationContext(), which+"", Toast.LENGTH_SHORT).show();
			}
		});
		builder.show();
    }
    
    private void openButtomOptionsDialog(int position){//run this function when buttom listView clickItemLongClick(it will show menu to choose action)
    	
    }

    
    //-----------<File Option function--------//
    private void renameFile(String renamedFile){
    	File file = new File(renamedFile);
    	//TODO open a dialog and a editText to write new name.
    	
    }
    private void copyFile(String copieer, String target){
    	File file = new File(copieer);
    	
    }
    //------------File Option function/>-----//
    
    private void openDeleteCheckDialog(){
    	new AlertDialog.Builder(this)
    			.setTitle("ALERT")
    			.setMessage("The file will be removed forever.\n" + "This command can't be undo")
    			.setPositiveButton("Delete", new DialogInterface.OnClickListener(){
					@Override
					public void onClick(DialogInterface dialog, int which) {
						Toast.makeText(getApplicationContext(), "File XXX removed", Toast.LENGTH_SHORT).show();
						//TODO remove file
					}
				})
				.setNegativeButton("Cancel", new DialogInterface.OnClickListener(){
					@Override
					public void onClick(DialogInterface dialog, int which) {
						Toast.makeText(getApplicationContext(), "Cancel", Toast.LENGTH_SHORT).show();
					}
				})
				.show();
    }
    
    private File[] reSort(File[] fileList){//Bubble Sort of file list. which ignore Case and put directory at front 
    	File[] fList = fileList;
    	File temp = null;
    	for(int i=fList.length-1; i>0; i--){
    		for(int j=0; j<i; j++){
    			if(fList[j].isDirectory() && fList[j+1].isDirectory()){//both of files are director
    				if(fList[j].getName().compareToIgnoreCase(fList[j+1].getName()) > 0){//switch fList[j] and fList[j+1]
        				temp = fList[j];
        				fList[j] = fList[j+1];
        				fList[j+1] = temp;
        			}
    			} else if(fList[j].isDirectory() || fList[j+1].isDirectory()){
    				if(!fList[j].isDirectory() && fList[j+1].isDirectory()){//former is not directory, latter is directory  
        				temp = fList[j];
        				fList[j] = fList[j+1];
        				fList[j+1] = temp;
    				}
    			} else{//traditional 
    				if(fList[j].getName().compareToIgnoreCase(fList[j+1].getName()) > 0){//switch fList[j] and fList[j+1]
        				temp = fList[j];
        				fList[j] = fList[j+1];
        				fList[j+1] = temp;
        			}
    			}
    		}
    	}
    	temp = null;
    	return fList;
    }
    
//	@Override
//	public boolean onCreateOptionsMenu(Menu menu) {
//		
//		return super.onCreateOptionsMenu(menu);
//	}
//	@Override
//	public boolean onOptionsItemSelected(MenuItem item) {
//		switch(item.getItemId()){
//		case Menu.FIRST:
//			break;
//		case Menu.FIRST+1://make device discoverable in 60 second.
//			break;
//		default:
//			break;
//		}
//		return super.onOptionsItemSelected(item);
//	}
}