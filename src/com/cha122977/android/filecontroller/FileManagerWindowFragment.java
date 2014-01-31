package com.cha122977.android.filecontroller;

import java.io.File;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.cha122977.android.filecontroller.FSController.RenameResult;

public class FileManagerWindowFragment extends Fragment implements PopupMenu.OnMenuItemClickListener {

	private static final String LOG_TAG = "FileManagerWindow";

	public static final int REQUEST_CODE_OPEN_FILE = 0;
	
	private Activity activity;
	private IFMWindowFragmentOwner owner;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		
		this.activity = activity;
		this.owner = (IFMWindowFragmentOwner) activity;
	}

	public FileManagerWindowFragment() {
	}
	
	// UI widget.
	private ImageView iv_dirImage;
	private TextView tv_filePath;

	private ListView lv_fileList;

	private static final int SYNC_ALL_LISTS = -4;
	private static final int REFRESH_OTHER_WINDWOS = -3;
	private static final int REFRESH_ALL_LISTS = -2;
	private static final int REFRESH = -1;
	private static final int PROCESSING_SHOW = 0;
	private static final int PROCESSING_HIDE = 1;
	private static final int COPY_DATA_SUCCEED = 11;
	private static final int COPY_DATA_FAILED = 12; 
	private static final int DELETE_DATA_SUCCEED = 13;
	private static final int DELETE_DATA_FAILED = 14;
	private static final int SHOW_INFO_DIALOG = 15;
	
	private static final String NAME_PROCESSED_DATA = AppConstant.KEY_FILE_PATH;
	
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case SYNC_ALL_LISTS:
				owner.syncLists(FileManagerWindowFragment.this);
				break;
			case REFRESH_OTHER_WINDWOS:
				owner.refreshOtherWindows(FileManagerWindowFragment.this);
				break;
			case REFRESH_ALL_LISTS:
				owner.refreshAllWindow();
				break;
			case REFRESH:
				refresh();
				break;
			case PROCESSING_SHOW:
				activity.setProgressBarIndeterminateVisibility(true);
				break;
			case PROCESSING_HIDE:
				activity.setProgressBarIndeterminateVisibility(false);
				break;
			case COPY_DATA_SUCCEED: // copy succeed
				Toast.makeText(activity, R.string.copy_copyDataSucceed, Toast.LENGTH_SHORT).show();
				sendEmptyMessage(REFRESH_OTHER_WINDWOS);
				sendEmptyMessage(PROCESSING_HIDE);
				break;
			case COPY_DATA_FAILED: // copy failure
				Toast.makeText(activity, R.string.copy_copyDataFailure, Toast.LENGTH_SHORT).show();
				sendEmptyMessage(PROCESSING_HIDE);
				break;
			case DELETE_DATA_SUCCEED:
				String deleteSucceedDataPath = msg.getData().getString(NAME_PROCESSED_DATA);
				Toast.makeText(activity, deleteSucceedDataPath + getString(R.string.delete_deleteDataSucceed), Toast.LENGTH_LONG).show();
				sendEmptyMessage(SYNC_ALL_LISTS);
				sendEmptyMessage(PROCESSING_HIDE);
				break;
			case DELETE_DATA_FAILED:
				String deleteFailedDataPath = msg.getData().getString(NAME_PROCESSED_DATA);
				Toast.makeText(activity, deleteFailedDataPath + getString(R.string.delete_deleteDataFailure), Toast.LENGTH_LONG).show();
				sendEmptyMessage(PROCESSING_HIDE);
				break;
			case SHOW_INFO_DIALOG:
				Bundle infoArgs = msg.getData();
				openShowInfoDialog(infoArgs.getString(KEY_INFO_TITLE), infoArgs.getInt(KEY_INFO_ICON_RES_ID), infoArgs.getString(KEY_INFO_MSG));
				break;
			default: // do nothing.
				break;
			}
		}
	};
	
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
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		// initial variable first to avoid parameters are used in the following functions.
		initialVariable();
		
		// set listeners.
		setListeners();

		// open default directory at last.
		openDefaultDirectory();
	}
	
	@Override
	public void onResume() {
		super.onResume();
		mHandler.sendEmptyMessage(REFRESH);
	}
	
	private void initialVariable() {

	}

	private void setListeners() {
		iv_dirImage.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				File openedFile = dirFile.getParentFile();
				boolean succeed = openDirectory(openedFile);
				if (succeed) {
					owner.pushDirHistory(FileManagerWindowFragment.this, openedFile);
				} else {
					Toast.makeText(activity, R.string.noPermission, Toast.LENGTH_SHORT).show();
				}
			}
		});

		tv_filePath.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				File currentDir = dirFile;
				boolean succeed = openData(dirFile.getParentFile());
				if (succeed) {
					owner.pushDirHistory(FileManagerWindowFragment.this, currentDir);
				}
				// TODO open keyboard and modify file path by userself.
			}
		});
		
		tv_filePath.setOnLongClickListener(new OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				showParentDirPopupMenu(v);
				return true;
			}
		});

		lv_fileList.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				File currentDir = dirFile;
				File openedData = listFilesOfDirFile[position];
				boolean succeed = openData(openedData);
				if (succeed && openedData.isDirectory()) { // only push when opened data is directory.(changed path)
					owner.pushDirHistory(FileManagerWindowFragment.this, currentDir);
				}
			}
		});
		
		lv_fileList.setOnItemLongClickListener(new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				showListPopupMenu(view, position);
				return true;
			}
		});
	}

	private void openDefaultDirectory() {
		String defaultDirPath;
		if (Environment.getExternalStorageState().equals(
				android.os.Environment.MEDIA_MOUNTED)) {
			// sdcard exist
			defaultDirPath = AppConstant.PRIMARY_STORAGE_ROOT;
		} else {
			// sdcard doesn't exist
			defaultDirPath = AppConstant.ROOT;
		}

		openDirectory(new File(defaultDirPath));
	}

	/** AREA Open File **/

	/**
	 * File or Directory must be opened from this function.
	 * All of opened file must call this function, including file or directory.
	 * Note: "DON'T" push back stack in this function.
	 * 
	 * @param data
	 * @return true if open succeed, false else.
	 */
	public boolean openData(File data) {
		Log.i(LOG_TAG, "Select file: " + data.getPath());
		if (!data.canRead()) {
			Toast.makeText(activity, R.string.fileCannotRead, Toast.LENGTH_SHORT).show();
			return false;
		} else if (!data.exists()) { // file doesn't exist. (This may happened when delete data from other app)
			// open parent directory insteed.
			String parentPath = data.getParent();
			if (parentPath != null) {
				openDirectory(new File(parentPath));
			} else {
				openDefaultDirectory();
			}
			Toast.makeText(activity, R.string.fileDoesNotExists, Toast.LENGTH_SHORT).show();
			return false;
		}

		if (data.isDirectory()) {
			return openDirectory(data);
		} else {
			return openFile(data);
		}
	}

	/**
	 * function to open directory's content.
	 * For complexity and architecture reason,
	 * <b>**Only openData() and openDefaultDir() can call this function**</b>
	 * @param dir opened directory.
	 * @return true if open directory succeed, false else.
	 */
	public boolean openDirectory(File dir) {
		if (dir.canRead()) {
			File[] fList = dir.listFiles();

			fList = FSController.reSort(fList); // reSort FileList

			// set variable.
			this.dirFile = dir;
			this.listFilesOfDirFile = fList;

			// set UI.
			tv_filePath.setText(dir.getPath());

			FileListAdapter fa = (FileListAdapter) lv_fileList.getAdapter();
			if (fa != null) {
				fa.drop(); // stop process image thread of dropped adapter
			}

			lv_fileList.setAdapter(new FileListAdapter(activity, fList));
			
			return true;
		}
		return false;
	}

	/**
	 * function to open file content.
	 * For complexity and architecture reason, <b>Only openData can call this function.</b>
	 * @param file opened file
	 * @return true if open file succeed, false else.
	 */
	public boolean openFile(File file) {
		return FSController.openFile(file, activity);
	}

	public void refresh() {
		// must check if exist, because current directory may be delete possibly by other app.
		if (dirFile.exists()) { // open when exist
			if (dirFile.canRead()) {
				// prepare scroll value. 
				int topChirdPosition = lv_fileList.getFirstVisiblePosition();
				View topVisableChild = lv_fileList.getChildAt(0);
				int offset = topVisableChild != null?
								topVisableChild.getTop() : 0;
				
				if (openDirectory(dirFile)) {
					lv_fileList.setSelectionFromTop(topChirdPosition, offset);
				} else { // open directory failed, open parent instead.
					openDirectory(dirFile.getParentFile());
				}
			} else { // permission have been changed by other app, open the parent directory.
				openDirectory(dirFile.getParentFile());
			}
		} else { // un-exist, open default directory.
			openDefaultDirectory();
		}
	}

	/** End of Open File **/
	
	/**
	 * Scroll listview to specify file. If the file doesn't exist, listview won't scroll.
	 * @param file
	 */
	public void scrollToFile(File file, int topOffset) {
		Log.d("TAG", "try to scroll to " + file);
		for (int i=0; i<listFilesOfDirFile.length; i++) {
			File comparedFile = listFilesOfDirFile[i];
			if (file.equals(comparedFile)) {
				Log.d("TAG", "Find Same File");
				lv_fileList.setSelectionFromTop(i, topOffset); // scroll the listview.
				return;
			}
		}
		Log.d("TAG", file + "did not find");
	}
	
	public void scrollToFile(File file) {
		scrollToFile(file, 0);
	}
	
	/** AREA options menu **/
	
	private void showParentDirPopupMenu(View v) {
		PopupMenu popup = new PopupMenu(activity, v);
		MenuInflater inflater = popup.getMenuInflater();
		inflater.inflate(R.menu.file_manager_window_parent_options_menu, popup.getMenu());
		popup.setOnMenuItemClickListener(this);
		popup.show();
	}
	
	private int selectedListFilePosition;
	private void showListPopupMenu(View v, int position) {
	    PopupMenu popup = new PopupMenu(activity, v);
	    MenuInflater inflater = popup.getMenuInflater();
	    if (listFilesOfDirFile[position].isDirectory()) {
	    	inflater.inflate(R.menu.file_manager_window_directory_options_menu, popup.getMenu());
	    } else {
	    	inflater.inflate(R.menu.file_manager_window_file_options_menu, popup.getMenu());
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
		case R.id.menu_parentCreateDir:
			createDirectory(dirFile);
			return true;
		case R.id.menu_showParentDirInfo:
			showDataInfo(dirFile);
			return true;
		case R.id.menu_createDir:
			createDirectory(listFilesOfDirFile[selectedListFilePosition]);
			return true;
		case R.id.menu_renameDir:
		case R.id.menu_renameFile:
			openRenameDataDialog(listFilesOfDirFile[selectedListFilePosition]);
			return true;
		case R.id.menu_showDirInfo:
		case R.id.menu_showFileInfo:
			showDataInfo(listFilesOfDirFile[selectedListFilePosition]);
			return true;
		case R.id.menu_moveDirToOtherSide:
		case R.id.menu_moveFileToOtherSide:
			openMoveDataDialog(listFilesOfDirFile[selectedListFilePosition], owner.getAnotherWindowDir(this));
			return true;
		case R.id.menu_copyDirToOtherSide:
		case R.id.menu_copyFileToOtherSide:
			openCopyDataDialog(listFilesOfDirFile[selectedListFilePosition], owner.getAnotherWindowDir(this));
			return true;
		case R.id.menu_deleteDir:
		case R.id.menu_deleteFile:
			openDeleteDataDialog(listFilesOfDirFile[selectedListFilePosition]);
			return true;
		default:
			return false;
		}
	}
	
	/**
	 * @NOTE
	 * Context Menu have bug:
	 *   long touching the bottom list,
	 *   create correct context menu (for bottom), but map to wrong menu item (for top).
	 *   Thus app have strengh behavior.
	 */
//	@Override
//	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
//		super.onCreateContextMenu(menu, v, menuInfo);
//		Log.i(LOG_TAG, "ID: " + FileManagerWindowFragment.this.getId());
//		MenuInflater inflater = activity.getMenuInflater();
//		Log.i(LOG_TAG, "ceate context menu...");
//		if (v == lv_fileList) {
//			AdapterContextMenuInfo acmi = (AdapterContextMenuInfo) menuInfo;
//			Log.d(LOG_TAG, "listFilesOfDirFile: " + listFilesOfDirFile[acmi.position].getPath());
//			if (listFilesOfDirFile[acmi.position].isDirectory()) {
//				inflater.inflate(R.menu.file_manager_window_directory_options_menu, menu);
//			} else {
//				inflater.inflate(R.menu.file_manager_window_file_options_menu, menu);
//			}
//		} else if (v == tv_filePath) {
//			inflater.inflate(R.menu.file_manager_window_parent_options_menu, menu);
//		}
//	}
//
//	@Override
//	public boolean onContextItemSelected(MenuItem item) {
//		AdapterContextMenuInfo acmi = (AdapterContextMenuInfo) item.getMenuInfo();
//		Log.i(LOG_TAG, "ID: " + FileManagerWindowFragment.this.getId());
//		switch (item.getItemId()) {
//		case R.id.menu_parentCreateDir:
//			createDirectory(dirFile);
//			return true;
//		case R.id.menu_showParentDirInfo:
//			return true;
//		case R.id.menu_createDir:
//			createDirectory(listFilesOfDirFile[acmi.position]);
//			return true;
//		case R.id.menu_renameDir:
//		case R.id.menu_renameFile:
//			openRenameDataDialog(listFilesOfDirFile[acmi.position]);
//			return true;
//		case R.id.menu_showDirInfo:
//		case R.id.menu_showFileInfo:
//			return true;
//		case R.id.menu_moveDirToOtherSide:
//		case R.id.menu_moveFileToOtherSide:
//			openMoveDataDialog(listFilesOfDirFile[acmi.position], owner.getAnotherWindowDir(this));
//			return true;
//		case R.id.menu_copyDirToOtherSide:
//		case R.id.menu_copyFileToOtherSide:
//			openCopyDataDialog(listFilesOfDirFile[acmi.position], owner.getAnotherWindowDir(this));
//			return true;
//		case R.id.menu_deleteDir:
//		case R.id.menu_deleteFile:
//			openDeleteDataDialog(listFilesOfDirFile[acmi.position]);
//			return true;
//		default:
//			return super.onContextItemSelected(item);
//		}
//	}
	
	/*** Create directory ***/

	public void createDirectory(final File parentDir) {
		// show a dialog to get new name.
		LayoutInflater inflater = LayoutInflater.from(activity);
		View renameDialogView = inflater.inflate(R.layout.rename_dialog, null);
		final EditText et_renameInput = (EditText) renameDialogView
				.findViewById(R.id.input);
		AlertDialog.Builder builder = new AlertDialog.Builder(activity);
		builder.setCancelable(false);
		builder.setTitle(R.string.createDir_createNewDirectory);
		builder.setView(renameDialogView);
		builder.setPositiveButton(R.string.alertButton_ok,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						createDirectory(parentDir, et_renameInput.getText().toString());
					}
				});
		builder.setNegativeButton(R.string.alertButton_cancel,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						Toast.makeText(activity, R.string.createDir_createDirCancel, Toast.LENGTH_SHORT).show();
					}
				});
		builder.show();
	}

	private void createDirectory(File parentDir, String childDirName) {
		if (FSController.createDirectory(parentDir, childDirName)) {
			Toast.makeText(activity, R.string.createDir_createDirSucceed, Toast.LENGTH_LONG).show();
			// Since Other window may looking at same directory, so
			// refresh all window to avoid unsynchronized.
			mHandler.sendEmptyMessage(SYNC_ALL_LISTS);
		} else {
			Toast.makeText(activity, R.string.createDir_createDirFailure, Toast.LENGTH_LONG).show();
		}
	}
	
	/*** End of Create directory ***/
	
	/*** Move data ***/
	
	/**
	 * Move data(dir/file) to target file.
	 * @param movedFile source file
	 * @param targetDir targetDirectory.
	 */
	private void openMoveDataDialog(final File movedFile, File targetDir) {
    	final File destFile = new File(targetDir.getPath() + File.separator + movedFile.getName());
    	if (destFile.exists()) {
    		// There are same name file in target directory,
    		// ask for replace or cancel the move action.
    		AlertDialog.Builder builder = new AlertDialog.Builder(activity);//use to select option
        	builder.setTitle(R.string.move_fileAlreadyExist);
    		builder.setItems(R.array.alert_moveFileSameName, new DialogInterface.OnClickListener() {
    			@Override
    			public void onClick(DialogInterface dialog, int which) {
    				switch (which) {
    				case 0: // Replace file: 
    					moveData(movedFile, destFile);
    					break;
    				case 1: // Cancel 
    					Toast.makeText(activity, R.string.move_moveFileCancel, Toast.LENGTH_LONG).show();
    					break;
    				default:
    					// Do nothing
    				}
    			}
    		});
    		builder.show();
    		
    	} else { // there is no same name file
    		boolean result = movedFile.renameTo(destFile);
    		if (result == true) { // copy succeed
    			Toast.makeText(activity, R.string.move_moveFileSucceed, Toast.LENGTH_LONG).show();
    			if (movedFile.exists()) { // if origin file still exist.
    				movedFile.delete();
    			}
    		} else { //copy failure
    			Toast.makeText(activity, R.string.move_moveFileFailure, Toast.LENGTH_LONG).show();
    		}
    		mHandler.sendEmptyMessage(SYNC_ALL_LISTS);
    	}
    }
	
	private void moveData(File movedFile, File destFile) {
		boolean result = movedFile.renameTo(destFile);
		if (result == true) {
			Toast.makeText(activity, R.string.move_replaceSucceed, Toast.LENGTH_LONG).show();
		} else {
			Toast.makeText(activity, R.string.move_replaceFailure, Toast.LENGTH_LONG).show();
		}
		mHandler.sendEmptyMessage(SYNC_ALL_LISTS);
	}
	
	/*** End of Move data ***/

	/*** Rename data ***/
	
	private void openRenameDataDialog(final File renamedData) {
		// use to show dialog to get new file name,
		// positive button will call function to rename file.
		// show a dialog to get new name.
		LayoutInflater inflater = LayoutInflater.from(activity);
		View renameDialogView = inflater.inflate(R.layout.rename_dialog, null);
		final EditText et_renameInput = (EditText)renameDialogView.findViewById(R.id.input);
		et_renameInput.setText(renamedData.getName());
		AlertDialog.Builder builder = new AlertDialog.Builder(activity);
		builder.setCancelable(false);
		builder.setTitle(R.string.rename_renameAlertTitle);
		builder.setView(renameDialogView);
		builder.setPositiveButton(R.string.alertButton_ok, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				renameData(renamedData, et_renameInput.getText().toString());
			}
		});
		builder.setNegativeButton(R.string.alertButton_cancel, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				//do nothing
				Toast.makeText(activity, R.string.rename_renameFileCancel, Toast.LENGTH_SHORT).show();
			}
		});
		builder.show();
	}
	
	private void renameData(File renamedData, String newName) {
		RenameResult ret = FSController.renameData(renamedData, newName);
		switch (ret) {
		case DATA_ALREADY_EXIST:
			Toast.makeText(activity, newName + getString(R.string.rename_fileAlreadyExist), Toast.LENGTH_LONG).show();
			break;
		case RENAME_SUCCEED:
			Toast.makeText(activity, R.string.rename_renameFileSucceed, Toast.LENGTH_SHORT).show();
			mHandler.sendEmptyMessage(SYNC_ALL_LISTS);
			break;
		case RENAME_FAILED:
			Toast.makeText(activity, R.string.rename_renameFileFailure, Toast.LENGTH_SHORT).show();
			break;
		}
	}
	
	/*** End of Rename data ***/
	
	/** Copy data **/

	// copy file to target(directory) as same name.
	private void openCopyDataDialog(final File copieerData, final File destParentDir) {
		File newData = new File(destParentDir.getPath() + File.separator + copieerData.getName());
		//Avoid replace the presence data which have the same file name in target directory
		if (!newData.exists()) { //there is no file have same name at target path.
			copyData(copieerData, newData); //copy file
		} else { // have same file name in target directory.
			String[] s = getResources().getStringArray(R.array.alert_sameFileNameOption);
			s[0] += newData.getPath(); // setting pre-replaced file name
			
			String originName = copieerData.getName();
			String newName;//use to find usable fileName.
			int fileNameCounter=1;
			int pointIndex = originName.lastIndexOf(".");
			while (true) {
				if (pointIndex != -1) { // file have attachment
					newName = originName.substring(0,pointIndex-1)
								+ "(" + fileNameCounter + ")"
								+ originName.substring(pointIndex);
				} else { // file does not have attachment
					newName = originName + "(" + fileNameCounter + ")";
				}
				if (!new File(destParentDir.getPath() + File.separator + newName).exists()) { //new file name is independence
					break;
				}
				fileNameCounter++;
			}
			s[1] += "\n" + newName; // setting new fileName to option.
			final String final_newName = newName;
			
			AlertDialog.Builder builder = new AlertDialog.Builder(activity);
			builder.setTitle(R.string.copy_fileSameName);
			builder.setItems(s, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					File targetData;
					switch (which) {
					case 0: // Replace file:
						targetData = new File(destParentDir.getPath() + File.separator + copieerData.getName());
						copyData(copieerData, targetData);
						break;
					case 1: // Copied file rename as:
						targetData = new File(destParentDir.getPath() + File.separator + final_newName);
						copyData(copieerData, targetData);
						break;
					case 2: // Cancel copy
						// do nothing
						break;
					default:
						// do nothing
						break;
					}
				}
			});
			builder.show();
		}
	}
	
	private void copyData(final File copieerData, final File destData) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				mHandler.sendEmptyMessage(PROCESSING_SHOW);
				if (FSController.copyData(copieerData, destData)) {
					//show information to user.
					mHandler.sendEmptyMessage(COPY_DATA_SUCCEED);
				} else {
					mHandler.sendEmptyMessage(COPY_DATA_FAILED);
				}
			}
		}).start();
	}
	
	/*** End of Copy data ***/
	
	/*** Delete data ***/
	
	/**
	 * use to delete file and directory.
	 * @param deletedData file wait for delete.
	 */
	private void openDeleteDataDialog(final File deletedData) {
		// basic dialog setting.
		AlertDialog.Builder builder = new AlertDialog.Builder(activity);
		builder.setTitle(R.string.delete_alertTitle);
		builder.setNegativeButton(R.string.alertButton_cancel, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				Toast.makeText(activity, R.string.action_cancel, Toast.LENGTH_SHORT).show();
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
				mHandler.sendEmptyMessage(PROCESSING_SHOW);
				if (FSController.deleteData(deletedData) == true) {
					Message msg = new Message();
					Bundle bundle = new Bundle();
					bundle.putString(NAME_PROCESSED_DATA, deletedData.getName());
					msg.setData(bundle);
					msg.what = DELETE_DATA_SUCCEED;
					mHandler.sendMessage(msg);
				} else {
					Message msg = new Message();
					Bundle bundle = new Bundle();
					bundle.putString(NAME_PROCESSED_DATA, deletedData.getName());
					msg.setData(bundle);
					msg.what = DELETE_DATA_FAILED;
					mHandler.sendMessage(msg);
				}
			}
		}).start();
	}
	
	/*** End of Delete data ***/
	
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
				mHandler.sendEmptyMessage(PROCESSING_SHOW);
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
				mHandler.sendEmptyMessage(PROCESSING_HIDE);
			}
		}).start();
	}
	
	private void openShowInfoDialog(String title, int iconResId, String msg) {
		AlertDialog.Builder builder = new AlertDialog.Builder(activity);
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
						activity.getString(R.string.fileInfo_unit_byte),
						activity.getString(R.string.fileInfo_unit_kByte),
						activity.getString(R.string.fileInfo_unit_mByte),
						activity.getString(R.string.fileInfo_unit_gByte))
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
	/** End of options menu **/
	
	/** AREA public parameter getter. **/
	/**
	 * get current opened directory.
	 * @return current directory file
	 */
	public File getDirectory() {
		return this.dirFile;
	}
	/**
	 * get current opened directory's path.
	 * @return path of current directory.
	 */
	public String getDirectoryPath() {
		return this.dirFile.getPath();
	}
	
	/** End of public parameter getter. **/
}
