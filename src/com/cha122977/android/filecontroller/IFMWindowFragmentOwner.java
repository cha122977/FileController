package com.cha122977.android.filecontroller;

import java.io.File;

public interface IFMWindowFragmentOwner {
	/**
	 * Used to refresh all file lists window.
	 */
	public void refreshAllWindow();
	public void refreshOtherWindows(FileManagerWindowFragment requester);
	public FileManagerWindowFragment getAnotherWindow(FileManagerWindowFragment requester);
	public File getAnotherWindowDir(FileManagerWindowFragment requester);
	/**
	 * Used to sync the all file lists.
	 * @param requester the window who send the sync reuqestment.
	 */
	public void syncLists(FileManagerWindowFragment requester);
	
	/**
	 * Used to push the history of opened directory. 
	 * @param requester
	 * @param directory
	 */
	public void pushDirHistory(FileManagerWindowFragment requester, File directory);
}
