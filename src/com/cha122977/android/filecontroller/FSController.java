package com.cha122977.android.filecontroller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

public class FSController {
	
	private static final String LOG_TAG = "FSController";
	
	/**
     * Used to open file.
     * @param f File instance
     * @param context Android context.
     */
	public static boolean openFile(File f, Context context) {
		Log.i(LOG_TAG, "open file:" + f.getPath());
		if (!f.exists() || !f.canRead()) { // if file doesn't exit, or file can't read.
			return false;
		}
	    Intent intent = new Intent();
	    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	    intent.setAction(android.content.Intent.ACTION_VIEW);
	    
	    /* get file mimetype */
	    String type = getMimeTypeString(f.getName());
	    
	    /* open file accroding its mimetype */
	    intent.setDataAndType(Uri.fromFile(f), type);
	    context.startActivity(intent);
	    return true;
	}
	
	/**
     * Used to open file.
     * @param f File instance
     * @param activity Android context.
     * @param requestCode used as requestCode in startActivityForResult
     */
	public static boolean openFileForResult(File f, Activity activity, int requestCode) {
		Log.i(LOG_TAG, "open file:" + f.getPath());
		if (!f.exists() || !f.canRead()) { // if file doesn't exit, or file can't read.
			return false;
		}
	    Intent intent = new Intent();
	    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	    intent.setAction(android.content.Intent.ACTION_VIEW);
	    
	    /* get file mimetype */
	    String type = getMimeTypeString(f.getName());
	    
	    /* open file accroding its mimetype */
	    intent.setDataAndType(Uri.fromFile(f), type);
	    activity.startActivityForResult(intent, requestCode);
	    return true;
	}
	
	public static boolean createDirectory(File parentDir, String childDirName) {
		if (!parentDir.exists()) {
			Log.i(LOG_TAG, parentDir.getPath() + "does not exist");
			return false;
		}
    	File newDir = new File(parentDir.getPath() + "/" + childDirName);
		Log.i(LOG_TAG, "create dir:" + newDir.getAbsolutePath() + "");
    	
		if (newDir.mkdir() == true) {
    		newDir.setReadable(true);
    		newDir.setWritable(true);
    		newDir.setExecutable(true);
    		return true;
    	} else {
    		return false;
    	}
	}
	
	public static boolean deleteData(File deletedData) {
		if (deletedData.isFile()) { // data is file
			if (deletedData.exists() && deletedData.canWrite()) {
				return deletedData.delete();
			}
			return false;
		} else { // data is directory
			File[] childData = deletedData.listFiles();
			for (File file: childData) {
				// delete all children first.
				if (deleteData(file) == false) { // if delete any child failed, return false immediately.
					return false;
				}
			}
			// delete directory at the end.
			return deletedData.delete();
		}
	}
	
	public static enum RenameResult {DATA_ALREADY_EXIST, RENAME_FAILED, RENAME_SUCCEED};
	
	public static RenameResult renameData(File renamedData, String newName) { //check if file name is exist and rename file
		File newFile = new File(renamedData.getParent() + File.separator + newName);
		if (newFile.exists()) {
			return RenameResult.DATA_ALREADY_EXIST;
		} else {
			Log.i(LOG_TAG, "RenameFile " + renamedData.getName() + " to " + newName);
			if (renamedData.canWrite()) {
				return RenameResult.RENAME_SUCCEED;
			} else {
				return RenameResult.RENAME_FAILED;
			}
		}
	}
	
	public static boolean copyData(File copieerData, File destData) {
		Log.i(LOG_TAG, "copy data: " + copieerData.getPath() + " to " + destData.getPath());
		// check if target exist a file.
		if (copieerData.isFile() == true) {
			FileInputStream in;
			FileOutputStream out;
			byte[] buffer;
			try {
				in = new FileInputStream(copieerData);
				out = new FileOutputStream(destData);
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
				Log.e("TAG", "Copy file " + copieerData.getName() + " to " + destData.getPath() + " ERROR");
				return false;
			}
		} else { // copieerData is directory
			destData.mkdir();//create directory.
			File[] fList = copieerData.listFiles();
			for (File file: fList) {
				if (!copyData(file, new File(destData.getPath() + File.separator + file.getName()))) {
					return false;
				}
			}
			return true;
		}
	}
	
	// AREA MimeType
	
	/* Support Mime type list */
	public static enum MimeType {DIRECTORY, UNKNOWN, AUDIO, VIDEO, IMAGE, TEXT};

	public static MimeType getMimeType(File f) {
		if (f.isDirectory()) {
			return MimeType.DIRECTORY;
		}
		String fName = f.getName();
		String end = fName.substring(fName.lastIndexOf(".") + 1, fName.length()).toLowerCase();

		/* check Mimetype */
		if (end.equals("m4a") || end.equals("mp3") || end.equals("mid")
				|| end.equals("xmf") || end.equals("ogg") || end.equals("wav")) {
			return MimeType.AUDIO;
		} else if (end.equals("3gp") || end.equals("mp4")) {
			return MimeType.VIDEO;
		} else if (end.equals("jpg") || end.equals("gif") || end.equals("png")
				|| end.equals("jpeg") || end.equals("bmp")) {
			return MimeType.IMAGE;
		} else if (end.equals("text")) {
			return MimeType.TEXT;
		} else {
			return MimeType.UNKNOWN;
		}
	}

	public static String getMimeTypeString(String fileName) {
		int pointIndex = fileName.lastIndexOf(".");
		String type;
		if (pointIndex > 0) {// != -1 && != 0
			String attachment = fileName.substring(pointIndex + 1).toLowerCase(); // set attachment name.
			if (attachment.equals("m4a") || attachment.equals("mp3")
					|| attachment.equals("mid") || attachment.equals("xmf")
					|| attachment.equals("ogg") || attachment.equals("wav")) {
				type = "audio";
			} else if (attachment.equals("3gp") || attachment.equals("mp4")) {
				type = "video";
			} else if (attachment.equals("jpg") || attachment.equals("gif")
					|| attachment.equals("png") || attachment.equals("jpeg")
					|| attachment.equals("bmp")) {
				type = "image";
			} else if (attachment.equals("txt")) { // not sure if android support type "text"
				type = "text";
			} else {
				type = "*";
			}
		} else {
			type = "*";
		}
		type += "/*";
		return type;
	}
	
	public static File[] filterCannotReadFile(File[] beFilteredFile){//use to filter the file which cannot be write
		List<File> l = new ArrayList<File>();
    	for (File f: beFilteredFile) {
    		if (f.canRead()) {
    			l.add(f);
    		}
		}
    	File[] result = new File[l.size()];
    	l.toArray(result);
    	return result;
    }
    
    public static File[] reSort(File[] fileList){//Bubble Sort of file list. which ignore Case and put directory at front 
    	ArrayList<File> list = new ArrayList<File>();
		for (File f: fileList) {
			list.add(f);
		}
		
		Comparator<File> cpt = new Comparator<File>() {
			@Override
			public int compare(File lhs, File rhs) {
				if (lhs.isDirectory() && rhs.isFile()) { // file > directory. 
					return -1;
				} else if (lhs.isFile() && rhs.isDirectory()) {
					return 1;
				} else {
					return (lhs.getName()).compareToIgnoreCase(rhs.getName());
				}
			}
		};
		
		Collections.sort(list, cpt);
		
		File[] resultFilelist = new File[list.size()];
		resultFilelist = list.toArray(resultFilelist);
		return resultFilelist;
    }
    
    /**
     * Used to get last modified data of data
     * @param data selected data
     * @return readable string of last modified.
     */
    public static String getDataLastModifiedDate(File data) {
    	Date d = new Date(data.lastModified());
    	return d.toString();
    }
    
	/* function used to show file information*/
	public static void showFileInformation(String selectedFilePath, Context context){
    	File f = new File(selectedFilePath);
    	AlertDialog.Builder builder = new AlertDialog.Builder(context);  
    	builder.setTitle(f.getAbsoluteFile()+"");
    	
    	StringBuilder info = new StringBuilder("");
    	long fileBytes = 0;
    	if (f.isDirectory()) {
    		builder.setIcon(R.drawable.open);
    		int numberOfFile = calculateFileNumberInDirectory(f);
    		info.append(context.getResources().getString(R.string.fileInfo_containingFileNumber) + numberOfFile +"\n");
    		fileBytes = calculateBytesInDirectory(f);
    	} else {
    		builder.setIcon(R.drawable.file);
    		fileBytes = f.length();
    	}
    	
    	String sizeOfFile = translateBytesToReadable(fileBytes, context);
    	
    	info.append(context.getResources().getString(R.string.fileInfo_size) + sizeOfFile);
    	
    	Date d = new Date(f.lastModified());
    	info.append(context.getResources().getString(R.string.fileInfo_lastModify)
    					+ (1900 + d.getYear()) +"/"+ d.getMonth() +"/"+ d.getDay() + " "
    					+ d.getHours() +":"+ d.getMinutes() +"\n");
        builder.setCancelable(true);
        builder.setMessage(info);
        builder.setPositiveButton(R.string.alertButton_ok, null);
        builder.show();
    }
    
    public static int calculateFileNumberInDirectory(File calculatedDirectory) {
		File[] fList = calculatedDirectory.listFiles();
		if (fList==null) {
			return 0;
		}
		int counter=0;
		for (File f: fList) {
			counter++;//file and directory both be calculated
			if (f.isDirectory()) { // deep calculate the file number in sub-director
				counter += calculateFileNumberInDirectory(f);
			}
		}
		return counter;
	}
    
    public static long calculateBytesInDirectory(File calculatedDirectory) {
    	File[] fList = calculatedDirectory.listFiles();
    	if (fList==null) {
    		return 0;
    	}
    	long counter = 0;
    	for (File f: fList) {
    		counter += f.isDirectory()? calculateBytesInDirectory(f) : f.length();
    	}
    	return counter;
    }
    
    public static String translateBytesToHummanReadable(long bytes,
    		String unitGB, String unitMB, String unitKB, String unitByte) {
    	
    	DecimalFormat df = new DecimalFormat("0.00");
    	if (bytes<1000) { //0~1KB
			return bytes + unitByte + "\n";
		} else if (bytes<1000000) { //1KB~1MB
			return df.format(bytes/1000.0) + unitKB + "\n";
		} else if (bytes<1000000000) { //1MB~1GB
			return df.format(bytes/1000000.0) + unitMB + "\n";
		} else { //1GB+
			return df.format(bytes/1000000000.0) + unitGB + "\n";
		}
    }
    
    public static String translateBytesToReadable(long bytes, Context context) {
    	return translateBytesToHummanReadable(bytes,
    			context.getString(R.string.fileInfo_unit_byte),
    			context.getString(R.string.fileInfo_unit_kByte),
    			context.getString(R.string.fileInfo_unit_mByte),
    			context.getString(R.string.fileInfo_unit_gByte));
    }
}
