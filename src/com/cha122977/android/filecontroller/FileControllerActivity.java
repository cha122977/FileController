package com.cha122977.android.filecontroller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class FileControllerActivity extends Activity {
	private final String ROOT = Environment.getExternalStorageDirectory().getAbsolutePath();
	
    TextView tv_topDir, tv_bottomDir;//textView to show where folder user is.
	ListView lv_topListView, lv_bottomListView;//listView to show all the file in folder where user is.
	
	List<String> topFilePath, bottomFilePath;//save file's path of top folder, which will use in OnLongClickEvent in ListView item 
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);//去掉Activity名稱欄位
        setContentView(R.layout.main);
        
        setViews();//connect view object to layout widget(in .xml file).
        initial();//construct need object.
        setListeners();//set listener to widget
        restorePrefs();
        
    }
    private void setViews(){//connect view object to layout widget(in .xml file).
    	tv_topDir = (TextView)findViewById(R.id.topTextView);
    	tv_bottomDir = (TextView)findViewById(R.id.bottomTextView);
    	lv_topListView = (ListView)findViewById(R.id.topListView);
    	lv_bottomListView = (ListView)findViewById(R.id.bottomListView);
    }
    private void initial(){//construct need object.
    	topFilePath = new ArrayList<String>();
    	bottomFilePath = new ArrayList<String>();
    }
    private void setListeners(){//set listener to widget
    	tv_topDir.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				readyToLeaveApp = false;
				String s = tv_topDir.getText().toString();
				File f = new File(s);
				s = f.getParent();
				openTopFile(true, s);
			}
		});
    	tv_bottomDir.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				readyToLeaveApp = false;
				String s = tv_bottomDir.getText().toString();
				File f = new File(s);
				s = f.getParent();
				openBottomFile(true, s);
			}
		});
    	
    	lv_topListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				readyToLeaveApp = false;
				File f = new File(topFilePath.get(arg2));
				if(f.isDirectory()){
					openTopFile(true, topFilePath.get(arg2));
				}else{
					openFile(f);
				}
			}
		});
    	
    	lv_bottomListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				readyToLeaveApp = false;
				File f = new File(bottomFilePath.get(arg2));
				if(f.isDirectory()){
					openBottomFile(true, bottomFilePath.get(arg2));
				}else{
//					Toast.makeText(getApplicationContext(), R.string.isFile, Toast.LENGTH_SHORT).show();
					openFile(f);
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
    //Preferences Code
    private String PREF		   = "filePreferences";
    private String PREF_TOP    = "lastestOpenedTopDir";
    private String PREF_BOTTOM = "lastestOpenedBottomDir";
    private void restorePrefs(){
    	SharedPreferences settings = getSharedPreferences(PREF, 0);
    	openTopFile   (false, settings.getString(PREF_TOP,    ROOT));
        openBottomFile(false, settings.getString(PREF_BOTTOM, ROOT));
    }
    @Override
	protected void onPause() {
		super.onPause();
		SharedPreferences settings = getSharedPreferences(PREF, 0);
		settings.edit()
			.putString(PREF_TOP, tv_topDir.getText().toString())
			.putString(PREF_BOTTOM, tv_bottomDir.getText().toString())
			.commit();
	}
	//Core Function
    private void openTopFile(boolean ifSave, String dir){//function to show directory's content.( use in Top Window)
    	if(dir!=null){
	    	File f = new File(dir);
	    	if(f.canRead()){
	    		if(ifSave){//need save history
		    		history.add(new HistoryItem(true, tv_topDir.getText().toString()));
		    	}
		    	File[] fl = f.listFiles();
		    	File[] fList = reSort(fl);//reSort FileList
		    	topFilePath.clear();//clear the list
		    	for(File i: fList){
		    		topFilePath.add(i.getPath());
		    	}
		    	tv_topDir.setText(dir);
		    	lv_topListView.setAdapter(new FileListAdapter(this, topFilePath));
		    }else{
		    	if(f.exists() == false){//can't read file because file is not exist.
		    		int indexHelper = dir.lastIndexOf("/");
		    		if(indexHelper!=0){
		    			openTopFile(ifSave, dir.substring(0, indexHelper));
		    		}
	    		} else {//can't read file because file cannot be read(no permission)
	    			Toast.makeText(this, R.string.noPermission, Toast.LENGTH_SHORT).show();
	    		}
		    }
    	}
    }
    private void openBottomFile(boolean ifSave, String dir){//function to show directory's content.( use in Bottom Window)
    	if(dir!=null){
	    	File f = new File(dir);
	    	if(f.canRead()){
	    		if(ifSave){//need save history
	        		history.add(new HistoryItem(false, tv_bottomDir.getText().toString()));
	        	}
	    		File[] fList = f.listFiles();
	    		fList = reSort(fList);//reSort FileList
	        	bottomFilePath.clear();//clear the list
	        	for(File i: fList){
	        		bottomFilePath.add(i.getPath());
	        	}
	        	tv_bottomDir.setText(dir);
	        	lv_bottomListView.setAdapter(new FileListAdapter(this, bottomFilePath));
	    	}else{
	    		if(f.exists() == false){//can't read file because file is not exist.
		    		int indexHelper = dir.lastIndexOf("/");
		    		if(indexHelper!=0){
		    			openBottomFile(ifSave, dir.substring(0, indexHelper));
		    		}
	    		} else {//can't read file because file cannot be read(no permission)
	    			Toast.makeText(this, R.string.noPermission, Toast.LENGTH_SHORT).show();
	    		}
	    	}
    	}
    }
    
    private void openTopOptionsDialog(int position){//run this function when top listView clickItemLongClick(it will show menu to choose action)
    	final String selectedFilePath = topFilePath.get(position);
    	String[] s = getResources().getStringArray(R.array.alert_fileSelectedOption);
    	s[1] += " " + tv_bottomDir.getText().toString();//set the string of item
    	s[2] += " " + tv_bottomDir.getText().toString();
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(topFilePath.get(position));
		builder.setItems(s, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch(which){
				case 0://Rename file.
					renameFile(selectedFilePath);
					break;
				case 1://Move
					moveFile(selectedFilePath, tv_bottomDir.getText().toString());
					break;
				case 2://Copy file to other side.
					copyFile(selectedFilePath, tv_bottomDir.getText().toString());
					break;
				case 3://Delete
					openDeleteCheckDialog(selectedFilePath);
					break;
				case 4://Cancel
					//Do nothing
					break;
				default:
					//Do nothing
					break;
				}
			}
		});
		builder.show();
    }
    
    private void openButtomOptionsDialog(int position){//run this function when bottom listView clickItemLongClick(it will show menu to choose action)
    	final String selectedFilePath = bottomFilePath.get(position);
    	String[] s = getResources().getStringArray(R.array.alert_fileSelectedOption);
    	s[1] += " " + tv_topDir.getText().toString();//set the string of item
    	s[2] += " " + tv_topDir.getText().toString();
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(bottomFilePath.get(position));
		builder.setItems(s, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch(which){
				case 0://Rename file.
					renameFile(selectedFilePath);
					break;
				case 1://Move
					moveFile(selectedFilePath, tv_topDir.getText().toString());
					break;
				case 2://Copy file to other side.
					copyFile(selectedFilePath, tv_topDir.getText().toString());
					break;
				case 3://Delete
					openDeleteCheckDialog(selectedFilePath);
					break;
				case 4://Cancel
					//Do nothing
					break;
				default:
					//Do nothing
					break;
				}
			}
		});
		builder.show();
    }
    
    //-----------File Option function--------//
    private void moveFile(String movedFile, String target){
    	final File file = new File(movedFile);//source file
    	final File targetFilePath = new File(target + "/" + (new File(movedFile).getName()));
    	if(targetFilePath.exists()){//Already have same name file in target directory.
    		AlertDialog.Builder builder = new AlertDialog.Builder(this);//use to select option
        	builder.setTitle(R.string.move_fileAlreadyExist);
    		builder.setItems(R.array.alert_moveFileSameName, new DialogInterface.OnClickListener() {
    			@Override
    			public void onClick(DialogInterface dialog, int which) {
    				switch(which){
    				case 0://Replace file: 
    					boolean result = file.renameTo(targetFilePath);
    					if(result == true){
    						Toast.makeText(getApplicationContext(), R.string.move_replaceSucceed, Toast.LENGTH_LONG).show();
    					} else {
    						Toast.makeText(getApplicationContext(), R.string.move_replaceFailure, Toast.LENGTH_LONG).show();
    					}
    					refreshListView();
    					break;
    				case 1://Cancel 
    					//Do nothing
    					Toast.makeText(getApplicationContext(), R.string.move_moveFileCancel, Toast.LENGTH_LONG);
    					break;
    				default:
    					//Do nothing
    				}
    			}
    		});
    		builder.show();
    		
    	} else { //there is no same name file
    		boolean result = file.renameTo(targetFilePath);
    		if(result == true){//copy succeed
    			Toast.makeText(getApplicationContext(), R.string.move_moveFileSucceed, Toast.LENGTH_LONG).show();
    		} else {//copy failure
    			Toast.makeText(getApplicationContext(), R.string.move_moveFileFailure, Toast.LENGTH_LONG).show();
    		}
    	}
		refreshListView();
    }
    
    private void renameFile(final String renamedFilePath){//use to show dialog to get new file name, positive button will call function to rename file.
    	//show a dialog to get new name.
    	LayoutInflater inflater = LayoutInflater.from(this);
    	View renameDialogView = inflater.inflate(R.layout.rename_dialog, null);
    	final EditText et_renameInput = (EditText)renameDialogView.findViewById(R.id.input);
    	et_renameInput.setText(new File(renamedFilePath).getName());
    	//TODO 反白檔名部份，使改檔名更快
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);  
        builder.setCancelable(false);   
        builder.setTitle(R.string.rename_renameAlertTitle);  
        builder.setView(renameDialogView);  
        builder.setPositiveButton(R.string.alertButton_ok, new DialogInterface.OnClickListener() {  
        			public void onClick(DialogInterface dialog, int whichButton) {  
        				checkFileNameAndRename(renamedFilePath, et_renameInput.getText().toString());
                    }  
                });
        builder.setNegativeButton(R.string.alertButton_cancel, new DialogInterface.OnClickListener() {  
                    public void onClick(DialogInterface dialog, int whichButton) {
                    	//do nothing
                    	Toast.makeText(getApplicationContext(), R.string.rename_renameFileCancel, Toast.LENGTH_SHORT).show();
                    }  
                });  
        builder.show();
    }
    
    private void copyFile(final String copieer, final String target){ //copy file to target(directory) as same name.
    	String copieerFileName = new File(copieer).getName();//find the file name of copied file name
    	final String completeTargetFilePath = target + "/" + copieerFileName;//[aaa/bbb/ccc.xxx]
    	//Avoid replace the presence data which have the same file name in target directory
    	if(new File(completeTargetFilePath).exists() == false){//there is no file have same name at target path.
    		doCopyFile(copieer, completeTargetFilePath);//copy file
    	} else {//have same file name in target directory.
        	String[] s = getResources().getStringArray(R.array.alert_sameFileNameOption);
        	s[0] += completeTargetFilePath;//setting pre-replaced file name
        	int fileNameCounter=1;
        	final String newFileName;
        	String temp;//use to find usable fileName.
        	int pointIndex = copieerFileName.lastIndexOf(".");
    		Log.d("TAG", "Index of . is: " + pointIndex);
        	while(true){
        		if(pointIndex != -1){//file have attachment 
        			temp = copieerFileName.substring(0,pointIndex-1) + "(" + fileNameCounter + ")" + copieerFileName.substring(pointIndex);//連"."一起補上
        		} else {//file does not have attachment
        			temp = copieerFileName + "(" + fileNameCounter + ")";
        		}
        		if(new File(target + "/" + temp).exists() == false){//new file name is independence
        			newFileName = temp;
        			break;
        		}
        		fileNameCounter++;
        	}
        	s[1] += "\n" + newFileName;//setting new fileName to option.
        	 
        	AlertDialog.Builder builder = new AlertDialog.Builder(this);//use to select option
        	builder.setTitle(R.string.copy_fileSameName);
    		builder.setItems(s, new DialogInterface.OnClickListener() {
    			@Override
    			public void onClick(DialogInterface dialog, int which) {
    				switch(which){
    				case 0://Replace file: 
    					doCopyFile(copieer, completeTargetFilePath);
    					break;
    				case 1://Copied file rename as: 
    					doCopyFile(copieer, target +"/"+ newFileName);
    					break;
    				case 2://Cancel copy
    					//Do nothing
    					break;
    				default:
    					//Do nothing
    				}
    			}
    		});
    		builder.show();
    	}
    }
    
    private void openDeleteCheckDialog(final String selectedPath){//use to delete file and directory.
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	builder.setTitle(R.string.delete_alertTitle)
				.setNegativeButton(R.string.alertButton_cancel, new DialogInterface.OnClickListener(){
					@Override
					public void onClick(DialogInterface dialog, int which) {
						Toast.makeText(getApplicationContext(), R.string.action_cancel, Toast.LENGTH_SHORT).show();
					}
				});
    	if(new File(selectedPath).isFile()){//if selected one is file
    		builder.setMessage(R.string.delete_alertDeleteFileMsg)
		    		.setPositiveButton(R.string.delete_deleteButton, new DialogInterface.OnClickListener(){
						@Override
						public void onClick(DialogInterface dialog, int which) {
							pureDeleteFile(selectedPath);
						}
					})
    				.show();
    	} else {//if selected one is directory
    		builder.setMessage(R.string.delete_alertDeleteDirMsg)
		    		.setPositiveButton(R.string.delete_deleteButton, new DialogInterface.OnClickListener(){
						@Override
						public void onClick(DialogInterface dialog, int which) {
							pureDeleteDirectory(selectedPath);
						}
					})
					.show();
    	}
    }
    
    //------------Be call in File Option function-----//
    
    private void checkFileNameAndRename(String renamedFilePath, String newFileName){//check if file name is exist and rename file
    	File newFile = new File(new File(renamedFilePath).getParent() +"/"+ newFileName);
    	if(newFile.exists()){
    		Toast.makeText(getApplicationContext(), newFileName + getString(R.string.rename_fileAlreadyExist), Toast.LENGTH_LONG).show();
    	} else {
    		File renamedFile = new File(renamedFilePath);
    		boolean result = renamedFile.renameTo(newFile);
    		Log.d("TAG", "rename file result: " + result );
    		if(result==true){
    			Toast.makeText(getApplicationContext(), R.string.rename_renameFileSucceed, Toast.LENGTH_SHORT).show();
    		} else{
    			Toast.makeText(getApplicationContext(), R.string.rename_renameFileFailure, Toast.LENGTH_SHORT).show();
    		}    		
    		refreshListView();
    	}
    }
    private void doCopyFile(String copieerFilePath, String targetFilePath){//start point of copy file function.
    	boolean result = pureCopyFile(copieerFilePath, targetFilePath);
    	if(result == true){
    		//show information to user.
            Toast.makeText(getApplicationContext(), R.string.copy_copyFileSucceed, Toast.LENGTH_SHORT).show();
            refreshListView();
    	} else {
    		Toast.makeText(getApplicationContext(), R.string.copy_copyFileFailure, Toast.LENGTH_SHORT).show();
    	}
    }
    private boolean pureCopyFile(String copieerFilePath, String targetFilePath){//copy "copieerFilePath"(file) to "targetFilePath"(file).
    	File copieerFile = new File(copieerFilePath);
    	if(copieerFile.isFile() == true){
    		//TODO 如果檔案太大，是否用progress bar顯示進度？
        	FileInputStream in;
        	FileOutputStream out;
        	byte[] buffer;
        	try {
    			in = new FileInputStream(copieerFilePath);
    			out = new FileOutputStream(targetFilePath);
    			buffer = new byte[1024];
    	        int read;
    	        while((read = in.read(buffer)) != -1){
    	          out.write(buffer, 0, read);
    	        }
    	        in.close();
    	        out.flush();
    	        out.close();
    	        return true;
    		} catch (Exception e) {
    			Log.d("TAG", "Copy file " + copieerFilePath + " to " + targetFilePath + " ERROR");
    			return false;
    		}
    	} else {
    		File newDir = new File(targetFilePath);
    		newDir.mkdir();//create directory.
    		File[] fList = copieerFile.listFiles();
    		for(File f: fList){
    			boolean temp;
    			temp = pureCopyFile(f.getPath(), targetFilePath + "/" + f.getName());
    			if(temp == false){
    				return false;
    			}
    		}
    		return true;
    	}
    }
    
    private void pureDeleteFile(String beDeletedFilePath){
    	File f = new File(beDeletedFilePath);
    	boolean result = f.delete();
    	if(result == true){
    		Toast.makeText(getApplicationContext(), beDeletedFilePath + getString(R.string.delete_deleteFileSucceed), Toast.LENGTH_LONG).show();
    	} else {
    		Toast.makeText(getApplicationContext(), f.getName() + getString(R.string.delete_deleteFileFailure), Toast.LENGTH_LONG).show();
    	}
    	refreshListView();
    }
    
    private void pureDeleteDirectory(String beDeletedPath){
    	if(deleteDirectoryNested(beDeletedPath) == true){
    		refreshListView();
    		Toast.makeText(getApplicationContext(), R.string.delete_deleteDirectorySucceed, Toast.LENGTH_LONG).show();
    	} else {
    		Toast.makeText(getApplicationContext(), R.string.delete_deleteDirectoryFailure, Toast.LENGTH_LONG).show();
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
    	return fList;
    }
    
    //---------Create menu.-------//
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, Menu.FIRST  , 1, R.string.menu_createNewDirInTop)
				.setIcon(R.drawable.folder);
		menu.add(0, Menu.FIRST+1, 2, R.string.menu_createNewDirInBottom)
				.setIcon(R.drawable.folder);
		menu.add(0, Menu.FIRST+2, 3, R.string.menu_helpTitle)
				.setIcon(android.R.drawable.ic_menu_help);
		menu.add(0, Menu.FIRST+3, 3, R.string.menu_aboutTitle)
				.setIcon(android.R.drawable.ic_dialog_alert);
		return super.onCreateOptionsMenu(menu);
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
		case Menu.FIRST://create a folder on top directory
			makeDirectory(tv_topDir.getText().toString());
			break;
		case Menu.FIRST+1://create a folder on bottom directory
			makeDirectory(tv_bottomDir.getText().toString());
			break;
		case Menu.FIRST+2:
			//TODO help Dialog
			showHelpDialog();
			break;
		case Menu.FIRST+3://About...
			showAboutDialog();
			break;
		default:
			//Do nothing
			break;
		}
		return super.onOptionsItemSelected(item);
	}
	
	//------Menu function----//
	private void makeDirectory(final String sourceDirPath){
		//show a dialog to get new name.
    	LayoutInflater inflater = LayoutInflater.from(this);
    	View renameDialogView = inflater.inflate(R.layout.rename_dialog, null);
    	final EditText et_renameInput = (EditText)renameDialogView.findViewById(R.id.input);
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);  
        builder.setCancelable(false);   
        builder.setTitle(R.string.createDir_createNewDirectory);  
        builder.setView(renameDialogView);  
        builder.setPositiveButton(R.string.alertButton_ok, new DialogInterface.OnClickListener() {  
        			public void onClick(DialogInterface dialog, int whichButton) {  
        				pureMakeDir(sourceDirPath, et_renameInput.getText().toString());
                    }  
                });
        builder.setNegativeButton(R.string.alertButton_cancel, new DialogInterface.OnClickListener() {  
                    public void onClick(DialogInterface dialog, int whichButton) {
                    	//do nothing
                    	Toast.makeText(getApplicationContext(), R.string.createDir_createDirCancel, Toast.LENGTH_SHORT).show();
                    }  
                });  
        builder.show();
	}
	
	private void showHelpDialog(){
		new AlertDialog.Builder(this)
				.setCancelable(false)
				.setIcon(android.R.drawable.ic_menu_help)
				.setTitle(R.string.help_title)
				.setMessage(R.string.help_msg)
				.setPositiveButton(R.string.alertButton_ok, null)
				.show();
	}
	
	private void showAboutDialog(){
		new AlertDialog.Builder(this)
        		.setCancelable(false)
        		.setIcon(android.R.drawable.ic_dialog_alert)
        		.setTitle(R.string.about_title)
        		.setMessage(R.string.about_msg)
        		.setPositiveButton(R.string.alertButton_ok, null)
        		.show();
	}
	
	//----use in Menu function----//
	private void pureMakeDir(String sourceDir, String newDirName){
    	File newDir = new File(sourceDir + "/" + newDirName);
    	Log.d("TAG", newDir.getAbsolutePath() + "");
    	if(newDir.mkdir()==true){
    		newDir.setReadable(true);
    		newDir.setWritable(true);
    		newDir.setExecutable(true);
    		Toast.makeText(getApplicationContext(), R.string.createDir_createDirSucceed, Toast.LENGTH_LONG).show();
    	} else {
    		Toast.makeText(getApplicationContext(), R.string.createDir_createDirFailure, Toast.LENGTH_LONG).show();
    	}
    	refreshListView();
    }
	
	//-----general function---//
	/* 在手機上開啟檔案的method */
	private void openFile(File f){
	    Intent intent = new Intent();
	    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	    intent.setAction(android.content.Intent.ACTION_VIEW);
	    
	    /* 取得Type */
	    String type = MimeType.getMimeType(f.getName());
	    
	    /* 設定intent的file與type */
	    intent.setDataAndType(Uri.fromFile(f), type);
	    startActivity(intent); 
	}
	
	private void refreshListView(){//refresh the file in list view(actually, reload)
		openTopFile(false, tv_topDir.getText().toString());
		openBottomFile(false, tv_bottomDir.getText().toString());
	}
	
	List<HistoryItem> history = new ArrayList<HistoryItem>();
	private class HistoryItem{
		boolean topOrBottom;
		String openedDir;
		public HistoryItem(boolean tb, String path){
			topOrBottom = tb;
			openedDir = path;
		}
	}
	boolean readyToLeaveApp = false;//use in double click leave app. mechanism.
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_BACK){
			if(readyToLeaveApp){
				return super.onKeyDown(keyCode, event);
			}
			int itemNumber = history.size();
			if(itemNumber != 0){
				HistoryItem hi = history.get(itemNumber-1);
				history.remove(itemNumber-1);
				if(hi.topOrBottom == true){//top
					openTopFile(false, hi.openedDir);
				} else {//bottom
					openBottomFile(false, hi.openedDir);
				}
				refreshListView();
			} else {
				Toast.makeText(getApplicationContext(), "Click again to finish", Toast.LENGTH_LONG).show();
				readyToLeaveApp = true;
			}
			return false;
		}
		readyToLeaveApp = false;
		return super.onKeyDown(keyCode, event);
	}
	
}