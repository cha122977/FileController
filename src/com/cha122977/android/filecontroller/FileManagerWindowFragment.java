package com.cha122977.android.filecontroller;

import java.io.File;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class FileManagerWindowFragment extends Fragment {

	private Context context;
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		context = activity;
	}
	
	public FileManagerWindowFragment() {
	}

	// UI widget.
	private ImageView iv_dirImage;
	private TextView tv_filePath;
	
	private ListView lv_fileList;
	
	// Class variable.
	private File dirFile;
	private File[] listFilesOfDirFile;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.file_manager_window_fragment_layout, container, false);
		
		iv_dirImage = (ImageView) rootView.findViewById(R.id.dirImage_FileManagerWindow_ImageView);
		tv_filePath = (TextView) rootView.findViewById(R.id.dirPath_FileManagerWindow_ImageView);
		lv_fileList = (ListView) rootView.findViewById(R.id.fileList_FileManagerWindow_ImageView);
		
		return rootView;
	}
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		// initial variable first to avoid parameters are used in the following functions.
		initialVariable();
		setListeners();
		
		// open default directory at last.
		openDefaultDirectory();
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		// TODO check this function.
	}
	
	private void initialVariable() {
		
	}
	
	private void setListeners() {
		iv_dirImage.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				openDirectory(dirFile.getParentFile());
			}
    	});
		
		tv_filePath.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				openDirectory(dirFile.getParentFile());
				// TODO open keyboard and modify file path by userself.
			}
		});
		
		lv_fileList.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				if (listFilesOfDirFile[position].isDirectory()) { // item is directory.
					openDirectory(listFilesOfDirFile[position]);
				} else { // item is file
					openFile(listFilesOfDirFile[position]);
				}
				openData(listFilesOfDirFile[position]);
			}
		});
		
		lv_fileList.setOnItemLongClickListener(new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				if (listFilesOfDirFile[position].isDirectory()) { // long click directory.
					// TODO open directory's option dialog.
				} else { // long click file.
					// TODO open file's option dialog.
				}
				return true;
			}
		});
	}
	
	private void openDefaultDirectory() {
		String defaultDirPath;
    	if (Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)) {
        	// sdcard exist
    		defaultDirPath = AppConstant.SDCARD_ROOT;
        } else {
        	// sdcard doesn't exist
        	defaultDirPath = AppConstant.ROOT;
        }
    	openDirectory(new File(defaultDirPath));
    }
	
	/**
	 * File or Directory must be opened from this function.
	 * @param file
	 * @return true if open succeed, false else.
	 */
	private boolean openData(File file) {
		if (!file.canRead()) {
			Toast.makeText(context, R.string.fileCannotRead, Toast.LENGTH_SHORT).show();
			return false;
		} else if (!file.exists()) { //file doesn't exist. (This may happened when delete data from other app)
			// open parent directory insteed.
			String parentPath = file.getParent();
			if (parentPath != null) {
				openDirectory(new File(parentPath));
    		} else {
    			openDefaultDirectory();
    		}
    		Toast.makeText(context, R.string.fileDoesNotExists, Toast.LENGTH_SHORT).show();
    		return false;
		}
		
		if (file.isDirectory()) {
			return openDirectory(file);
		} else {
			return openFile(file);
		}
	}
	
	/**
	 * function to open directory's content.
	 * @param dir
	 * @return true if open directory succeed, false else.
	 */
    private boolean openDirectory(File dir) {
    	if (dir.canRead()) {
	    	File[] fList = dir.listFiles();
	    	
	    	fList = ListFileProcessor.reSort(fList); //reSort FileList
	    	
	    	// set variable.
	    	this.dirFile = dir;
	    	this.listFilesOfDirFile = fList;
	    	
	    	// set UI.
	    	tv_filePath.setText(dir.getPath());
	    	
        	FileListAdapter fa = (FileListAdapter)lv_fileList.getAdapter();
        	if (fa!=null) {
        		fa.drop(); // stop process image thread of dropped adapter 
        	}
        	
	    	lv_fileList.setAdapter(new FileListAdapter(context, fList));
	    	
	    	return true;
	    }
    	return false;
    }
    
    /**
	 * function to open file content.
	 * @param filePath
	 * @return true if open file succeed, false else.
	 */
    private boolean openFile(File file) {
    	Intent intent = new Intent();
	    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	    intent.setAction(android.content.Intent.ACTION_VIEW);
	    
	    /* get file mimetype */
	    String type = MimeType.getMimeType(file.getName());
	    
	    /* open file accroding its mimetype */
	    intent.setDataAndType(Uri.fromFile(file), type);
	    context.startActivity(intent); 
    	return true;
    }
    
    private void refreshListView() {
    	openDirectory(dirFile);
    }
    
    
//    
//    //------Menu function----//
//  	private void makeDirectory(final String sourceDirPath) {
//  		//show a dialog to get new name.
//      	LayoutInflater inflater = LayoutInflater.from(this);
//      	View renameDialogView = inflater.inflate(R.layout.rename_dialog, null);
//      	final EditText et_renameInput = (EditText)renameDialogView.findViewById(R.id.input);
//      	AlertDialog.Builder builder = new AlertDialog.Builder(this);  
//          builder.setCancelable(false);   
//          builder.setTitle(R.string.createDir_createNewDirectory);  
//          builder.setView(renameDialogView);  
//          builder.setPositiveButton(R.string.alertButton_ok, new DialogInterface.OnClickListener() {  
//          			public void onClick(DialogInterface dialog, int whichButton) {  
//          				pureMakeDir(sourceDirPath, et_renameInput.getText().toString());
//                      }  
//                  });
//          builder.setNegativeButton(R.string.alertButton_cancel, new DialogInterface.OnClickListener() {  
//                      public void onClick(DialogInterface dialog, int whichButton) {
//                      	//do nothing
//                      	Toast.makeText(getApplicationContext(), R.string.createDir_createDirCancel, Toast.LENGTH_SHORT).show();
//                      }  
//                  });  
//          builder.show();
//  	}
//    
//    
//    private void openBottomOptionsDialog(int position) {//run this function when bottom listView clickItemLongClick(it will show menu to choose action)
//    	final String selectedFilePath = bottomFilePath.get(position);
//    	String[] s = getResources().getStringArray(R.array.alert_fileSelectedOption);
//    	s[3] += " " + tv_topDir.getText().toString();//set the string of item
//    	s[4] += " " + tv_topDir.getText().toString();
//    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
//		builder.setTitle(bottomFilePath.get(position));
//		builder.setItems(s, new DialogInterface.OnClickListener() {
//			@Override
//			public void onClick(DialogInterface dialog, int which) {
//				switch (which) {
//				case 0://open file.
//					if(new File(selectedFilePath).isDirectory()){
//						openTopFile(true, selectedFilePath);
//					} else {
//						ListFileProcessor.openFile(selectedFilePath, getApplicationContext());
//					}
//					break;
//				case 1://Rename file.
//					renameFile(selectedFilePath);
//					break;
//				case 2://show file information
//					ListFileProcessor.showFileInformation(selectedFilePath, FileControllerActivity.this);
//					break;
//				case 3://Move
//					moveFile(selectedFilePath, tv_topDir.getText().toString());
//					break;
//				case 4://Copy file to other side.
//					copyFile(selectedFilePath, tv_topDir.getText().toString());
//					break;
//				case 5://Delete
//					openDeleteCheckDialog(selectedFilePath);
//					break;
//				case 6://Cancel
//					//Do nothing
//					break;
//				default:
//					//Do nothing
//					break;
//				}
//			}
//		});
//		builder.show();
//    }
//    
//    private void moveFile(String movedFile, String target) {
//    	final File file = new File(movedFile);//source file
//    	final File targetFilePath = new File(target + "/" + (new File(movedFile).getName()));
//    	if (targetFilePath.exists()) {
//    		/**
//    		 * There are same name file in target directory, need to change name or cancel move!
//    		 */
//    		AlertDialog.Builder builder = new AlertDialog.Builder(this);//use to select option
//        	builder.setTitle(R.string.move_fileAlreadyExist);
//    		builder.setItems(R.array.alert_moveFileSameName, new DialogInterface.OnClickListener() {
//    			@Override
//    			public void onClick(DialogInterface dialog, int which) {
//    				switch (which) {
//    				case 0://Replace file: 
//    					boolean result = file.renameTo(targetFilePath);
//    					if (result == true) {
//    						Toast.makeText(getApplicationContext(), R.string.move_replaceSucceed, Toast.LENGTH_LONG).show();
//    					} else {
//    						Toast.makeText(getApplicationContext(), R.string.move_replaceFailure, Toast.LENGTH_LONG).show();
//    					}
//    					refreshListView();
//    					break;
//    				case 1://Cancel 
//    					//Do nothing
//    					Toast.makeText(getApplicationContext(), R.string.move_moveFileCancel, Toast.LENGTH_LONG);
//    					break;
//    				default:
//    					//Do nothing
//    				}
//    			}
//    		});
//    		builder.show();
//    		
//    	} else { //there is no same name file
//    		boolean result = file.renameTo(targetFilePath);
//    		if (result == true) { // copy succeed
//    			Toast.makeText(getApplicationContext(), R.string.move_moveFileSucceed, Toast.LENGTH_LONG).show();
//    			if ( file.exists() ) { // if origin file still exist.
//    				file.delete();
//    			}
//    		} else {//copy failure
//    			Toast.makeText(getApplicationContext(), R.string.move_moveFileFailure, Toast.LENGTH_LONG).show();
//    		}
//    	}
//		refreshListView();
//    }
//    
//    private void renameFile(final String renamedFilePath) {//use to show dialog to get new file name, positive button will call function to rename file.
//    	//show a dialog to get new name.
//    	LayoutInflater inflater = LayoutInflater.from(this);
//    	View renameDialogView = inflater.inflate(R.layout.rename_dialog, null);
//    	final EditText et_renameInput = (EditText)renameDialogView.findViewById(R.id.input);
//    	et_renameInput.setText(new File(renamedFilePath).getName());
//    	AlertDialog.Builder builder = new AlertDialog.Builder(this);  
//        builder.setCancelable(false);   
//        builder.setTitle(R.string.rename_renameAlertTitle);  
//        builder.setView(renameDialogView);  
//        builder.setPositiveButton(R.string.alertButton_ok, new DialogInterface.OnClickListener() {  
//        			public void onClick(DialogInterface dialog, int whichButton) {  
//        				checkFileNameAndRename(renamedFilePath, et_renameInput.getText().toString());
//                    }  
//                });
//        builder.setNegativeButton(R.string.alertButton_cancel, new DialogInterface.OnClickListener() {  
//                    public void onClick(DialogInterface dialog, int whichButton) {
//                    	//do nothing
//                    	Toast.makeText(getApplicationContext(), R.string.rename_renameFileCancel, Toast.LENGTH_SHORT).show();
//                    }  
//                });  
//        builder.show();
//    }
//    
//    private void copyFile(final String copieer, final String target) { //copy file to target(directory) as same name.
//    	new Thread(new Runnable() {
//			@Override
//			public void run() {
//				mHandler.sendEmptyMessage(0);
//		    	
//		    	String copieerFileName = new File(copieer).getName();//find the file name of copied file name
//		    	final String completeTargetFilePath = target + "/" + copieerFileName;//[aaa/bbb/ccc.xxx]
//		    	//Avoid replace the presence data which have the same file name in target directory
//		    	if(new File(completeTargetFilePath).exists() == false){//there is no file have same name at target path.
//		    		doCopyFile(copieer, completeTargetFilePath);//copy file
//		    	} else {//have same file name in target directory.
//		        	String[] s = getResources().getStringArray(R.array.alert_sameFileNameOption);
//		        	s[0] += completeTargetFilePath;//setting pre-replaced file name
//		        	int fileNameCounter=1;
//		        	final String newFileName;
//		        	String temp;//use to find usable fileName.
//		        	int pointIndex = copieerFileName.lastIndexOf(".");
////		    		Log.d("TAG", "Index of . is: " + pointIndex);
//		        	while (true) {
//		        		if (pointIndex != -1) {//file have attachment 
//		        			temp = copieerFileName.substring(0,pointIndex-1) + "(" + fileNameCounter + ")" + copieerFileName.substring(pointIndex);//�s"."�@�_�ɤW
//		        		} else {//file does not have attachment
//		        			temp = copieerFileName + "(" + fileNameCounter + ")";
//		        		}
//		        		if (new File(target + "/" + temp).exists() == false) {//new file name is independence
//		        			newFileName = temp;
//		        			break;
//		        		}
//		        		fileNameCounter++;
//		        	}
//		        	s[1] += "\n" + newFileName;//setting new fileName to option.
//		        	 
//		        	AlertDialog.Builder builder = new AlertDialog.Builder(FileControllerActivity.this);//use to select option
//		        	builder.setTitle(R.string.copy_fileSameName);
//		    		builder.setItems(s, new DialogInterface.OnClickListener() {
//		    			@Override
//		    			public void onClick(DialogInterface dialog, int which) {
//		    				switch(which){
//		    				case 0://Replace file: 
//		    					doCopyFile(copieer, completeTargetFilePath);
//		    					break;
//		    				case 1://Copied file rename as: 
//		    					doCopyFile(copieer, target +"/"+ newFileName);
//		    					break;
//		    				case 2://Cancel copy
//		    					//Do nothing
//		    					break;
//		    				default:
//		    					//Do nothing
//		    				}
//		    			}
//		    		});
//		    		builder.show();
//		    	}
//			}
//		}).start();
//    	
//    }
//    
//    private void openDeleteCheckDialog(final String selectedPath) {//use to delete file and directory.
//    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
//    	builder.setTitle(R.string.delete_alertTitle)
//				.setNegativeButton(R.string.alertButton_cancel, new DialogInterface.OnClickListener() {
//					@Override
//					public void onClick(DialogInterface dialog, int which) {
//						Toast.makeText(getApplicationContext(), R.string.action_cancel, Toast.LENGTH_SHORT).show();
//					}
//				});
//    	if (new File(selectedPath).isFile()) {//if selected one is file
//    		builder.setMessage(R.string.delete_alertDeleteFileMsg)
//		    		.setPositiveButton(R.string.delete_deleteButton, new DialogInterface.OnClickListener() {
//						@Override
//						public void onClick(DialogInterface dialog, int which) {
//							pureDeleteFile(selectedPath);
//						}
//					})
//    				.show();
//    	} else { //if selected one is directory
//    		builder.setMessage(R.string.delete_alertDeleteDirMsg)
//		    		.setPositiveButton(R.string.delete_deleteButton, new DialogInterface.OnClickListener() {
//						@Override
//						public void onClick(DialogInterface dialog, int which) {
//							pureDeleteDirectory(selectedPath);
//						}
//					})
//					.show();
//    	}
//    }
//    
//    //------------Be call in File Option function-----//
//    
//    private void checkFileNameAndRename(String renamedFilePath, String newFileName) {//check if file name is exist and rename file
//    	File newFile = new File(new File(renamedFilePath).getParent() +"/"+ newFileName);
//    	if(newFile.exists()) {
//    		Toast.makeText(getApplicationContext(), newFileName + getString(R.string.rename_fileAlreadyExist), Toast.LENGTH_LONG).show();
//    	} else {
//    		File renamedFile = new File(renamedFilePath);
//    		boolean result = renamedFile.renameTo(newFile);
////    		Log.d("TAG", "rename file result: " + result );
//    		if (result==true) {
//    			Toast.makeText(getApplicationContext(), R.string.rename_renameFileSucceed, Toast.LENGTH_SHORT).show();
//    		} else {
//    			Toast.makeText(getApplicationContext(), R.string.rename_renameFileFailure, Toast.LENGTH_SHORT).show();
//    		}    		
//    		refreshListView();
//    	}
//    }
//    private void doCopyFile(String copieerFilePath, String targetFilePath) {//start point of copy file function.
//    	boolean result = pureCopyFile(copieerFilePath, targetFilePath);
//    	if (result == true) {
//    		//show information to user.
//    		mHandler.sendEmptyMessage(1);
////            refreshListView();
//    	} else {
//    		mHandler.sendEmptyMessage(2);
//    	}
//    }
//    private boolean pureCopyFile(String copieerFilePath, String targetFilePath) {//copy "copieerFilePath"(file) to "targetFilePath"(file).
//    	File copieerFile = new File(copieerFilePath);
//    	if (copieerFile.isFile() == true) {
//        	FileInputStream in;
//        	FileOutputStream out;
//        	byte[] buffer;
//        	try {
//    			in = new FileInputStream(copieerFilePath);
//    			out = new FileOutputStream(targetFilePath);
//    			buffer = new byte[1024];
//    	        int read;
//    	        while ((read = in.read(buffer)) != -1) {
//    	          out.write(buffer, 0, read);
//    	        }
//    	        in.close();
//    	        out.flush();
//    	        out.close();
//    	        return true;
//    		} catch (Exception e) {
//    			Log.d("TAG", "Copy file " + copieerFilePath + " to " + targetFilePath + " ERROR");
//    			return false;
//    		}
//    	} else {
//    		File newDir = new File(targetFilePath);
//    		newDir.mkdir();//create directory.
//    		File[] fList = copieerFile.listFiles();
//    		for(File f: fList){
//    			boolean temp;
//    			temp = pureCopyFile(f.getPath(), targetFilePath + "/" + f.getName());
//    			if(temp == false){
//    				return false;
//    			}
//    		}
//    		return true;
//    	}
//    }
//    
//    private void pureDeleteFile(final String beDeletedFilePath) {
//    	new Thread(new Runnable() {
//			@Override
//			public void run() {
//				mHandler.sendEmptyMessage(0);
//				File f = new File(beDeletedFilePath);
//		    	boolean result = f.delete();
//		    	if(result == true){
//		    		Message msg = new Message();
//		    		Bundle bundle = new Bundle();
//		    		bundle.putString("beDeletedFilePath", beDeletedFilePath);
//		    		msg.setData(bundle);
//		    		msg.what = 3;
//		    		mHandler.sendMessage(msg);
//		    	} else {
//		    		Message msg = new Message();
//		    		Bundle bundle = new Bundle();
//		    		bundle.putString("beDeletedFilePath", beDeletedFilePath);
//		    		msg.setData(bundle);
//		    		msg.what = 4;
//		    		mHandler.sendMessage(msg);
//		    	}
//			}
//		}).start();
//    }
//    
//    private void pureDeleteDirectory(final String beDeletedPath) {
//    	new Thread(new Runnable() {
//			@Override
//			public void run() {
//				mHandler.sendEmptyMessage(0);
//				if(deleteDirectoryNested(beDeletedPath) == true) {
////		    		refreshListView();
//		    		mHandler.sendEmptyMessage(5);
//		    	} else {
//		    		mHandler.sendEmptyMessage(6);
//		    	}
//			}
//		}).start();
//    }
//    private boolean deleteDirectoryNested(String inputPath) {
//    	File f = new File(inputPath);
//    	if (f.isFile()) { //path is file
//    		return f.delete();
//    	} else { //path is directory
//    		File[] fl = f.listFiles();
//    		for (File i: fl) {
//    			if (deleteDirectoryNested(i.getAbsolutePath()) == false) {
//    				return false;
//    			}
//    		}
//    		return f.delete();
//    	}
//    }
//    
//    //---------Create menu.-------//
//	@Override
//	public boolean onCreateOptionsMenu(Menu menu) {
//		menu.add(0, Menu.FIRST  , 1, R.string.menu_createNewDirInTop)
//				.setIcon(R.drawable.add_folder);
//		menu.add(0, Menu.FIRST+1, 2, R.string.menu_createNewDirInBottom)
//				.setIcon(R.drawable.add_folder);
//		menu.add(0, Menu.FIRST+2, 3, R.string.menu_search)
//				.setIcon(R.drawable.search);
//		menu.add(0, Menu.FIRST+3, 3, R.string.menu_helpTitle)
//				.setIcon(R.drawable.help);
//		menu.add(0, Menu.FIRST+4, 3, R.string.menu_aboutTitle)
//				.setIcon(R.drawable.about);
//		
//		return super.onCreateOptionsMenu(menu);
//	}
//	
//	@Override
//	public boolean onOptionsItemSelected(MenuItem item) {
//		switch(item.getItemId()){
//		case Menu.FIRST: //create a folder on top directory
//			makeDirectory(tv_topDir.getText().toString());
//			break;
//		case Menu.FIRST+1: //create a folder on bottom directory
//			makeDirectory(tv_bottomDir.getText().toString());
//			break;
//		case Menu.FIRST+2:
//			Intent intent = new Intent(FileControllerActivity.this, SearchActivity.class);
//			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//			startActivityForResult(intent, REQUEST_CODE_SEARCH);
//			break;
//		case Menu.FIRST+3: //Help
//			showHelpDialog();
//			break;
//		case Menu.FIRST+4: //About...
//			showAboutDialog();
//			break;
//		default:
//			//Do nothing
//			break;
//		}
//		return super.onOptionsItemSelected(item);
//	}
}
