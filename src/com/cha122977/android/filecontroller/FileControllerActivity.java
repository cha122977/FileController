package com.cha122977.android.filecontroller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Stack;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Surface;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class FileControllerActivity extends Activity {
	
	public static final String PREFS_NAME = "UserPrefs";
	public static final String OPEN_FIRST = "OpenAppFirst";

	public static final int REQUEST_CODE_SEARCH = 11; //use to start searchActivity.
	public static final int RESULT_CODE_OPEN_TOP = 11;
	public static final int RESULT_CODE_OPEN_BOTTOM = 12;
	
	private LinearLayout ll_screen; //use to change orientation
	
	private final static String SDCARD_ROOT = Environment.getExternalStorageDirectory().getAbsolutePath();
	private final static String ROOT = "/";
	ImageView iv_topDirImg, iv_bottomDirImg;
    TextView tv_topDir, tv_bottomDir; //textView to show where folder user is.
	ListView lv_topListView, lv_bottomListView; //listView to show all the file in folder where user is.
		
	ArrayList<String> topFilePath, bottomFilePath; //save file's path of top folder, which will use in OnLongClickEvent in ListView item
	
	AlertDialog waitingAlertDialog;
	
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch(msg.what) {
				case 0:
					waitingAlertDialog.show();
					break;
				case 1: // copy succeed
					Toast.makeText(getApplicationContext(), R.string.copy_copyDataSucceed, Toast.LENGTH_SHORT).show();
					waitingAlertDialog.dismiss();
					refreshListView();
					break;
				case 2: // copy failure
					Toast.makeText(getApplicationContext(), R.string.copy_copyDataFailure, Toast.LENGTH_SHORT).show();
					waitingAlertDialog.dismiss();
					break;
				case 3:
					Bundle b3 = msg.getData();
					String beDeletedFilePath3 = b3.getString("beDeletedFilePath");
					Toast.makeText(getApplicationContext(), beDeletedFilePath3 + getString(R.string.delete_deleteDataSucceed), Toast.LENGTH_LONG).show();
					waitingAlertDialog.dismiss();
					refreshListView();
					break;
				case 4:
					Bundle b4 = msg.getData();
					String beDeletedFilePath4 = b4.getString("beDeletedFilePath");
					Toast.makeText(getApplicationContext(), beDeletedFilePath4 + getString(R.string.delete_deleteDataFailure), Toast.LENGTH_LONG).show();
					waitingAlertDialog.dismiss();
					break;
				case 5: // delete directory succeed.
					Toast.makeText(getApplicationContext(), R.string.delete_deleteDataSucceed, Toast.LENGTH_LONG).show();
					waitingAlertDialog.dismiss();
					refreshListView();
					break;
				case 6:
					Toast.makeText(getApplicationContext(), R.string.delete_deleteDataFailure, Toast.LENGTH_LONG).show();
					waitingAlertDialog.dismiss();
					break;
				default: // do nothing.
					break;
			}
		}
	};
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE); // clear title bar
        setContentView(R.layout.main);
        
        ll_screen = (LinearLayout)findViewById(R.id.screanLayout);        
        
        int rotation = ((WindowManager) getSystemService(WINDOW_SERVICE)).getDefaultDisplay().getRotation();
        if (rotation == Surface.ROTATION_90 || rotation == Surface.ROTATION_270) {//if phone rotation
        	ll_screen.setOrientation(LinearLayout.HORIZONTAL);
        }
        
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
	        
        setViews();//connect view object to layout widget(in .xml file).
        initial();//construct need object.
        setListeners();//set listener to widget
        
        // initial directory.
        openDefaultDirectory();
        
        setWaitingAlertDialog(); 
        
        // if first time open app, show help dialog.
        if (settings.getBoolean(OPEN_FIRST, true)) {
        	showHelpDialog();
        	settings.edit().putBoolean(OPEN_FIRST, false).apply(); // apply() performance is better than commit.
        }
    }
    
    private void openDefaultDirectory() {
    	openDefaultTopDirectory();
    	openDefaultBottomDirectory();
    }
    
    private void openDefaultTopDirectory() {
    	if (Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)) {
        	// sdcard exist
        	openTopFile(false, SDCARD_ROOT);
        } else {
        	// sdcard doesn't exist
        	openTopFile(false, ROOT);
        }
    }
    
    private void openDefaultBottomDirectory() {
    	if (Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)) {
        	// sdcard exist
            openBottomFile(false, SDCARD_ROOT);
        } else {
        	// sdcard doesn't exist
            openBottomFile(false, ROOT);
        }
    }
	
    @Override
	public void onConfigurationChanged(Configuration newConfig) {//use to change the orientation of view.
    	super.onConfigurationChanged(newConfig);    	
    	if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
    		ll_screen.setOrientation(LinearLayout.HORIZONTAL);
    	} else {
    		ll_screen.setOrientation(LinearLayout.VERTICAL);
    	}
	}
    
    @Override
	protected void onResume() {
		super.onResume();
		refreshListView();
	}
    
    private void setViews() { //connect view object to layout widget(in .xml file).
    	iv_topDirImg = (ImageView)findViewById(R.id.topDirImage);
    	iv_bottomDirImg = (ImageView)findViewById(R.id.bottomDirImage);
    	tv_topDir = (TextView)findViewById(R.id.topTextView);
    	tv_bottomDir = (TextView)findViewById(R.id.bottomTextView);
    	lv_topListView = (ListView)findViewById(R.id.topListView);
    	lv_bottomListView = (ListView)findViewById(R.id.bottomListView);
    }
    private void initial() { //construct need object.
    	topFilePath = new ArrayList<String>();
    	bottomFilePath = new ArrayList<String>();
    }
    private void setListeners() { //set listener to widget
    	iv_topDirImg.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				readyToLeaveApp = false;
				String s = tv_topDir.getText().toString();
				File f = new File(s);
				s = f.getParent();
				openTopFile(true, s);
			}
    	});
    	iv_bottomDirImg.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				readyToLeaveApp = false;
				String s = tv_bottomDir.getText().toString();
				File f = new File(s);
				s = f.getParent();
				openBottomFile(true, s);
			}
    	});
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
				if(new File(topFilePath.get(arg2)).isDirectory()){
					openTopFile(true, topFilePath.get(arg2));
				}else{
					FSController.openFile(new File(topFilePath.get(arg2)), getApplicationContext());
				}
			}
		});
    	
    	lv_bottomListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				readyToLeaveApp = false;
				if (new File(bottomFilePath.get(arg2)).isDirectory()) {
					openBottomFile(true, bottomFilePath.get(arg2));
				}else{
					FSController.openFile(new File(bottomFilePath.get(arg2)), getApplicationContext());
				}
			}
		});
    	
    	lv_topListView.setOnItemLongClickListener(new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				if (new File(topFilePath.get(arg2)).isDirectory()) {//long click item is directory 
					openTopOptionsDialog(arg2);
				} else {//long click item is file
					openTopOptionsDialog(arg2);
				}
				return true;
			}
		});
    	
    	lv_bottomListView.setOnItemLongClickListener(new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				if (new File(bottomFilePath.get(arg2)).isDirectory()) {//long click item is directory 
					openBottomOptionsDialog(arg2);
				} else {//long click item is file
					openBottomOptionsDialog(arg2);
				}
				return true;
			}
		});
    }
    
    private void setWaitingAlertDialog() {
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.alert_waitingDialogTitle);
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.waiting_dialog, null);
        builder.setView(view);
    	builder.setCancelable(false); // can't not be cancel
    	waitingAlertDialog = builder.create();
    }
    
	//Core Function
    private void openTopFile(boolean ifSave, String dir) { //function to show directory's content.( use in Top Window)
    	if (dir!=null) {
	    	File f = new File(dir);
	    	if (f.canRead()) {
	    		if (ifSave) { //need save history
		    		history.push(new HistoryItem(true, tv_topDir.getText().toString()));
		    	}
		    	File[] fList = f.listFiles();
		    	
		    	fList = FSController.filterCannotWriteFile(fList);//filter the file which can't read and write
		    	
		    	fList = FSController.reSort(fList); //reSort FileList
		    	topFilePath.clear(); //clear the list
		    	for(File i: fList) {
		    		topFilePath.add(i.getPath());
		    	}
		    	tv_topDir.setText(dir);
		    	
	        	FileListAdapter fa = (FileListAdapter)(lv_topListView.getAdapter());
	        	if (fa!=null) fa.drop(); // stop process image thread of dropped adapter 
	        	
		    	lv_topListView.setAdapter(new FileListAdapter(this, topFilePath));
		    	
		    } else {
		    	if (f.exists() == false) { //can't read file because file is not exist.
		    		int indexHelper = dir.lastIndexOf("/");
		    		if (indexHelper!=0 && indexHelper!=-1) {
		    			openTopFile(ifSave, dir.substring(0, indexHelper));
		    		} else {
		    			openTopFile(ifSave, ROOT);
		    		}
	    		} else { //can't read file because file cannot be read(no permission)
	    			Toast.makeText(this, R.string.noPermission, Toast.LENGTH_SHORT).show();
	    		}
		    }
    	}
    }
    private void openBottomFile(boolean ifSave, String dir) {//function to show directory's content.( use in Bottom Window)
    	if (dir!=null) {
	    	File f = new File(dir);
	    	if(f.canRead()) {
	    		if(ifSave){//need save history
	        		history.push(new HistoryItem(false, tv_bottomDir.getText().toString()));
	        	}
	    		File[] fList = f.listFiles();
	    		
	    		fList = FSController.filterCannotWriteFile(fList);//filter the file which can't read and write
	    		
	    		fList = FSController.reSort(fList);//reSort FileList
	        	bottomFilePath.clear();//clear the list
	        	for (File i: fList) {
	        		bottomFilePath.add(i.getPath());
	        	}
	        	tv_bottomDir.setText(dir);
	        	
	        	FileListAdapter fa = (FileListAdapter)(lv_bottomListView.getAdapter());
	        	if (fa!=null) fa.drop(); // stop process image thread of dropped adapter
	        	
	        	lv_bottomListView.setAdapter(new FileListAdapter(this, bottomFilePath));
	    	} else {
	    		if (f.exists() == false) { //can't read file because file is not exist.
		    		int indexHelper = dir.lastIndexOf("/");
		    		if (indexHelper!=0 && indexHelper!=1) {
		    			openBottomFile(ifSave, dir.substring(0, indexHelper));
		    		} else {
		    			openBottomFile(ifSave, ROOT);
		    		}
	    		} else { //can't read file because file cannot be read(no permission)
	    			Toast.makeText(this, R.string.noPermission, Toast.LENGTH_SHORT).show();
	    		}
	    	}
    	}
    }
    
    private void openTopOptionsDialog(int position) {//run this function when top listView clickItemLongClick(it will show menu to choose action)
    	final String selectedFilePath = topFilePath.get(position);
    	String[] s = getResources().getStringArray(R.array.alert_fileSelectedOption);
    	s[3] += " " + tv_bottomDir.getText().toString();//set the string of item
    	s[4] += " " + tv_bottomDir.getText().toString();
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(topFilePath.get(position));
		builder.setItems(s, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch (which) {
				case 0: //open file.
					if (new File(selectedFilePath).isDirectory()) {
						openTopFile(true, selectedFilePath);
					} else {
						FSController.openFile(new File(selectedFilePath), getApplicationContext());
					}
					break;
				case 1://Rename file.
					renameFile(selectedFilePath);
					break;
				case 2://Show file info
					FSController.showFileInformation(selectedFilePath, FileControllerActivity.this);
					break;
				case 3://Move
					moveFile(selectedFilePath, tv_bottomDir.getText().toString());
					break;
				case 4://Copy file to other side.
					copyFile(selectedFilePath, tv_bottomDir.getText().toString());
					break;
				case 5://Delete
					openDeleteCheckDialog(selectedFilePath);
					break;
				case 6://Cancel
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
    
    private void openBottomOptionsDialog(int position) {//run this function when bottom listView clickItemLongClick(it will show menu to choose action)
    	final String selectedFilePath = bottomFilePath.get(position);
    	String[] s = getResources().getStringArray(R.array.alert_fileSelectedOption);
    	s[3] += " " + tv_topDir.getText().toString();//set the string of item
    	s[4] += " " + tv_topDir.getText().toString();
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(bottomFilePath.get(position));
		builder.setItems(s, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch (which) {
				case 0://open file.
					if(new File(selectedFilePath).isDirectory()){
						openTopFile(true, selectedFilePath);
					} else {
						FSController.openFile(new File(selectedFilePath), getApplicationContext());
					}
					break;
				case 1://Rename file.
					renameFile(selectedFilePath);
					break;
				case 2://show file information
					FSController.showFileInformation(selectedFilePath, FileControllerActivity.this);
					break;
				case 3://Move
					moveFile(selectedFilePath, tv_topDir.getText().toString());
					break;
				case 4://Copy file to other side.
					copyFile(selectedFilePath, tv_topDir.getText().toString());
					break;
				case 5://Delete
					openDeleteCheckDialog(selectedFilePath);
					break;
				case 6://Cancel
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
    
    private void moveFile(String movedFile, String target) {
    	final File file = new File(movedFile);//source file
    	final File targetFilePath = new File(target + "/" + (new File(movedFile).getName()));
    	if (targetFilePath.exists()) {
    		/**
    		 * There are same name file in target directory, need to change name or cancel move!
    		 */
    		AlertDialog.Builder builder = new AlertDialog.Builder(this);//use to select option
        	builder.setTitle(R.string.move_fileAlreadyExist);
    		builder.setItems(R.array.alert_moveFileSameName, new DialogInterface.OnClickListener() {
    			@Override
    			public void onClick(DialogInterface dialog, int which) {
    				switch (which) {
    				case 0://Replace file: 
    					boolean result = file.renameTo(targetFilePath);
    					if (result == true) {
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
    		if (result == true) { // copy succeed
    			Toast.makeText(getApplicationContext(), R.string.move_moveFileSucceed, Toast.LENGTH_LONG).show();
    			if ( file.exists() ) { // if origin file still exist.
    				file.delete();
    			}
    		} else {//copy failure
    			Toast.makeText(getApplicationContext(), R.string.move_moveFileFailure, Toast.LENGTH_LONG).show();
    		}
    	}
		refreshListView();
    }
    
    private void renameFile(final String renamedFilePath) {//use to show dialog to get new file name, positive button will call function to rename file.
    	//show a dialog to get new name.
    	LayoutInflater inflater = LayoutInflater.from(this);
    	View renameDialogView = inflater.inflate(R.layout.rename_dialog, null);
    	final EditText et_renameInput = (EditText)renameDialogView.findViewById(R.id.input);
    	et_renameInput.setText(new File(renamedFilePath).getName());
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
    
    private void copyFile(final String copieer, final String target) { //copy file to target(directory) as same name.
    	new Thread(new Runnable() {
			@Override
			public void run() {
				mHandler.sendEmptyMessage(0);
		    	
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
//		    		Log.d("TAG", "Index of . is: " + pointIndex);
		        	while (true) {
		        		if (pointIndex != -1) {//file have attachment 
		        			temp = copieerFileName.substring(0,pointIndex-1) + "(" + fileNameCounter + ")" + copieerFileName.substring(pointIndex);//�s"."�@�_�ɤW
		        		} else {//file does not have attachment
		        			temp = copieerFileName + "(" + fileNameCounter + ")";
		        		}
		        		if (new File(target + "/" + temp).exists() == false) {//new file name is independence
		        			newFileName = temp;
		        			break;
		        		}
		        		fileNameCounter++;
		        	}
		        	s[1] += "\n" + newFileName;//setting new fileName to option.
		        	 
		        	AlertDialog.Builder builder = new AlertDialog.Builder(FileControllerActivity.this);//use to select option
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
		}).start();
    	
    }
    
    private void openDeleteCheckDialog(final String selectedPath) {//use to delete file and directory.
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	builder.setTitle(R.string.delete_alertTitle)
				.setNegativeButton(R.string.alertButton_cancel, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						Toast.makeText(getApplicationContext(), R.string.action_cancel, Toast.LENGTH_SHORT).show();
					}
				});
    	if (new File(selectedPath).isFile()) {//if selected one is file
    		builder.setMessage(R.string.delete_alertDeleteFileMsg)
		    		.setPositiveButton(R.string.delete_deleteButton, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							pureDeleteFile(selectedPath);
						}
					})
    				.show();
    	} else { //if selected one is directory
    		builder.setMessage(R.string.delete_alertDeleteDirMsg)
		    		.setPositiveButton(R.string.delete_deleteButton, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							pureDeleteDirectory(selectedPath);
						}
					})
					.show();
    	}
    }
    
    //------------Be call in File Option function-----//
    
    private void checkFileNameAndRename(String renamedFilePath, String newFileName) {//check if file name is exist and rename file
    	File newFile = new File(new File(renamedFilePath).getParent() +"/"+ newFileName);
    	if(newFile.exists()) {
    		Toast.makeText(getApplicationContext(), newFileName + getString(R.string.rename_fileAlreadyExist), Toast.LENGTH_LONG).show();
    	} else {
    		File renamedFile = new File(renamedFilePath);
    		boolean result = renamedFile.renameTo(newFile);
//    		Log.d("TAG", "rename file result: " + result );
    		if (result==true) {
    			Toast.makeText(getApplicationContext(), R.string.rename_renameFileSucceed, Toast.LENGTH_SHORT).show();
    		} else {
    			Toast.makeText(getApplicationContext(), R.string.rename_renameFileFailure, Toast.LENGTH_SHORT).show();
    		}    		
    		refreshListView();
    	}
    }
    private void doCopyFile(String copieerFilePath, String targetFilePath) {//start point of copy file function.
    	boolean result = pureCopyFile(copieerFilePath, targetFilePath);
    	if (result == true) {
    		//show information to user.
    		mHandler.sendEmptyMessage(1);
//            refreshListView();
    	} else {
    		mHandler.sendEmptyMessage(2);
    	}
    }
    private boolean pureCopyFile(String copieerFilePath, String targetFilePath) {//copy "copieerFilePath"(file) to "targetFilePath"(file).
    	File copieerFile = new File(copieerFilePath);
    	if (copieerFile.isFile() == true) {
        	FileInputStream in;
        	FileOutputStream out;
        	byte[] buffer;
        	try {
    			in = new FileInputStream(copieerFilePath);
    			out = new FileOutputStream(targetFilePath);
    			buffer = new byte[1024];
    	        int read;
    	        while ((read = in.read(buffer)) != -1) {
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
    
    private void pureDeleteFile(final String beDeletedFilePath) {
    	new Thread(new Runnable() {
			@Override
			public void run() {
				mHandler.sendEmptyMessage(0);
				File f = new File(beDeletedFilePath);
		    	boolean result = f.delete();
		    	if(result == true){
		    		Message msg = new Message();
		    		Bundle bundle = new Bundle();
		    		bundle.putString("beDeletedFilePath", beDeletedFilePath);
		    		msg.setData(bundle);
		    		msg.what = 3;
		    		mHandler.sendMessage(msg);
		    	} else {
		    		Message msg = new Message();
		    		Bundle bundle = new Bundle();
		    		bundle.putString("beDeletedFilePath", beDeletedFilePath);
		    		msg.setData(bundle);
		    		msg.what = 4;
		    		mHandler.sendMessage(msg);
		    	}
			}
		}).start();
    }
    
    private void pureDeleteDirectory(final String beDeletedPath) {
    	new Thread(new Runnable() {
			@Override
			public void run() {
				mHandler.sendEmptyMessage(0);
				if(deleteDirectoryNested(beDeletedPath) == true) {
//		    		refreshListView();
		    		mHandler.sendEmptyMessage(5);
		    	} else {
		    		mHandler.sendEmptyMessage(6);
		    	}
			}
		}).start();
    }
    private boolean deleteDirectoryNested(String inputPath) {
    	File f = new File(inputPath);
    	if (f.isFile()) { //path is file
    		return f.delete();
    	} else { //path is directory
    		File[] fl = f.listFiles();
    		for (File i: fl) {
    			if (deleteDirectoryNested(i.getAbsolutePath()) == false) {
    				return false;
    			}
    		}
    		return f.delete();
    	}
    }
    
    //---------Create menu.-------//
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, Menu.FIRST  , 1, R.string.menu_createNewDirInTop)
				.setIcon(R.drawable.add_folder);
		menu.add(0, Menu.FIRST+1, 2, R.string.menu_createNewDirInBottom)
				.setIcon(R.drawable.add_folder);
		menu.add(0, Menu.FIRST+2, 3, R.string.menu_search)
				.setIcon(R.drawable.search);
		menu.add(0, Menu.FIRST+3, 3, R.string.menu_helpTitle)
				.setIcon(R.drawable.help);
		menu.add(0, Menu.FIRST+4, 3, R.string.menu_aboutTitle)
				.setIcon(R.drawable.about);
		
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
		case Menu.FIRST: //create a folder on top directory
			makeDirectory(tv_topDir.getText().toString());
			break;
		case Menu.FIRST+1: //create a folder on bottom directory
			makeDirectory(tv_bottomDir.getText().toString());
			break;
		case Menu.FIRST+2:
			Intent intent = new Intent(FileControllerActivity.this, SearchActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivityForResult(intent, REQUEST_CODE_SEARCH);
			break;
		case Menu.FIRST+3: //Help
			showHelpDialog();
			break;
		case Menu.FIRST+4: //About...
			showAboutDialog();
			break;
		default:
			//Do nothing
			break;
		}
		return super.onOptionsItemSelected(item);
	}
	
	//------Menu function----//
	private void makeDirectory(final String sourceDirPath) {
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
	
	private void showHelpDialog() {		
		LayoutInflater inflater = LayoutInflater.from(this);
		View view = inflater.inflate(R.layout.alertdialog_help, null);
		
		AlertDialog d = new AlertDialog.Builder(this)
			.setView(view)
			.setPositiveButton(R.string.alertButton_ok, null)
			.create();
		d.show();
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
		
//		// Linkify the message
//	    SpannableString s = new SpannableString(getResources().getString(R.string.about_msg));
//	    Linkify.addLinks(s, Linkify.WEB_URLS);
//		
//	    final TextView myView = new TextView(this);
//	    myView.setText(s);
//	    myView.setTextSize(15);
//	    myView.setTextScaleX((float)1.1);
//	    myView.setLineSpacing(6, 1);
//	    myView.setPadding(30, 10, 10, 10);
//	    
//		AlertDialog d = new AlertDialog.Builder(this)
//        		.setIcon(R.drawable.about)
//        		.setTitle(R.string.about_title)
//        		.setView(myView)
//        		.setPositiveButton(R.string.alertButton_ok, null)
//        		.create();
//		d.show();
//		// Make the textview clickable. Must be called after show()
////	    ((TextView)d.findViewById(android.R.id.message)).setMovementMethod(LinkMovementMethod.getInstance());
////		myView.setMovementMethod(LinkMovementMethod.getInstance());
	}
	
	//----use in Menu function----//
	private void pureMakeDir(String sourceDir, String newDirName) {
    	File newDir = new File(sourceDir + "/" + newDirName);
//    	Log.d("TAG", newDir.getAbsolutePath() + "");
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
	private void refreshListView() { //refresh the file in list view(actually, reload)
		openTopFile(false, tv_topDir.getText().toString());
		openBottomFile(false, tv_bottomDir.getText().toString());
	}
	
	Stack<HistoryItem> history = new Stack<HistoryItem>();
	private class HistoryItem {
		boolean topOrBottom;
		String openedDir;
		public HistoryItem(boolean tb, String path) {
			topOrBottom = tb;
			openedDir = path;
		}
	}
	
	boolean readyToLeaveApp = false; //use in double click leave app. mechanism.
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (readyToLeaveApp) {
				return super.onKeyDown(keyCode, event);
			}
			if (!history.isEmpty()) { // if hostory is not empty
				HistoryItem hi = history.pop();
				if (hi.topOrBottom == true) { //top
					openTopFile(false, hi.openedDir);
				} else {//bottom
					openBottomFile(false, hi.openedDir);
				}
				refreshListView();
			} else {
				Toast.makeText(getApplicationContext(), R.string.doubleClickMsg, Toast.LENGTH_LONG).show();
				readyToLeaveApp = true;
			}
			return false;
		} else if (keyCode == KeyEvent.KEYCODE_SEARCH) {
			readyToLeaveApp = false;
			Intent intent = new Intent(FileControllerActivity.this, SearchActivity.class);
//			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivityForResult(intent, REQUEST_CODE_SEARCH);
			Log.d("TAG", "Just click the Search Button");
			return false;//Override the original search button
		}
		readyToLeaveApp = false;
		return super.onKeyDown(keyCode, event);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == REQUEST_CODE_SEARCH) {
			switch (resultCode) {
			case RESULT_CODE_OPEN_TOP:
				String path1 = data.getStringExtra("path");
				if (new File(path1).isDirectory()) {
					openTopFile(true, path1);
				} else {
					openTopFile(true, new File(path1).getParent());
				}
				break;
			case RESULT_CODE_OPEN_BOTTOM:
				String path2 = data.getStringExtra("path");
				if (new File(path2).isDirectory()) {
					openTopFile(true, path2);
				} else {
					openTopFile(true, new File(path2).getParent());
				}
				break;
			default:
				//Do nothing
				break;
			}
		}
	}
}