package com.cha122977.android.filecontroller;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

public class ListFileProcessor {
	public static File[] filterCannotWriteFile(File[] beFilteredFile){//use to filter the file which cannot be write
		List<File> l = new ArrayList<File>();
    	for(File f: beFilteredFile){
    		if(f.canWrite()){
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
     * Used to open file.
     * @param filePath String of file path(complete path + file name)
     * @param context UI instance
     */
	public static void openFile(String filePath, Context context){//Overload
		openFile(new File(filePath), context);
	}
	
	/**
     * Used to open file.
     * @param f File instance
     * @param context UI instance
     */
	public static void openFile(File f, Context context){
	    Intent intent = new Intent();
	    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	    intent.setAction(android.content.Intent.ACTION_VIEW);
	    
	    /* get file mimetype */
	    String type = MimeType.getMimeType(f.getName());
	    
	    /* open file accroding its mimetype */
	    intent.setDataAndType(Uri.fromFile(f), type);
	    context.startActivity(intent); 
	}
    
    
	/* function used to show file information*/
    public static void showFileInformation(String selectedFilePath, Context context){
    	DecimalFormat df = new DecimalFormat("0.00");
    	
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
    
    private static int calculateFileNumberInDirectory(File calculatedDirectory) {
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
    
    private static long calculateBytesInDirectory(File calculatedDirectory) {
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
    
    private static String translateBytesToReadable(long bytes, Context context) {
    	DecimalFormat df = new DecimalFormat("0.00");
    	
		if (bytes<1000) { //0~1KB
			return bytes + context.getResources().getString(R.string.fileInfo_unit_byte) + "\n";
		} else if (bytes<1000000) { //1KB~1MB
			return df.format(bytes/1000.0) + context.getResources().getString(R.string.fileInfo_unit_kByte) + "\n";
		} else if (bytes<1000000000) { //1MB~1GB
			return df.format(bytes/1000000.0) + context.getResources().getString(R.string.fileInfo_unit_mByte) + "\n";
		} else { //1GB+
			return df.format(bytes/1000000000.0) + context.getResources().getString(R.string.fileInfo_unit_gByte) + "\n";
		}	
    }
}
