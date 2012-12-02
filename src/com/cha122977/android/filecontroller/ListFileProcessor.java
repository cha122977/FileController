package com.cha122977.android.filecontroller;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
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
    
    /* function used to open file */
	public static void openFile(String s, Context context){//Overload
		openFile(new File(s), context);
	}
	public static void openFile(File f, Context context){
	    Intent intent = new Intent();
	    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	    intent.setAction(android.content.Intent.ACTION_VIEW);
	    
	    /* ��oType */
	    String type = MimeType.getMimeType(f.getName());
	    
	    /* �]�wintent��file�Ptype */
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
    	if(f.isDirectory()){
    		builder.setIcon(R.drawable.open);
    		//TODO calculate the file number in direction
    		int numberOfFile = calculateFileNumberInDirectory(f);
    		info.append(context.getResources().getString(R.string.fileInfo_containingFileNumber)
    						+ numberOfFile +"\n");
    	} else {
    		builder.setIcon(R.drawable.file);
    		long size = f.length();
    		if(size<1000){//0~1KB
    			info.append(context.getResources().getString(R.string.fileInfo_size) 
    							+ f.length()
    							+ context.getResources().getString(R.string.fileInfo_unit_byte) + "\n");
    		} else if(size<1000000) {//1KB~1MB
    			
    			
    			
    			info.append(context.getResources().getString(R.string.fileInfo_size) 
    							+ df.format(f.length()/1000.0)
    							+ context.getResources().getString(R.string.fileInfo_unit_kByte) + "\n");
    		} else if(size<1000000000) {//1MB~1GB
    			info.append(context.getResources().getString(R.string.fileInfo_size)
    							+ df.format(f.length()/1000000.0)
    							+ context.getResources().getString(R.string.fileInfo_unit_mByte) + "\n");
    		} else {//1GB+
    			info.append(context.getResources().getString(R.string.fileInfo_size)
    							+ df.format(f.length()/1000000000.0) 
    							+ context.getResources().getString(R.string.fileInfo_unit_gByte) + "\n");
    		}	
    	}
    	
    	Date d = new Date(f.lastModified());
    	info.append(context.getResources().getString(R.string.fileInfo_lastModify)
    					+ (1900 + d.getYear()) +"/"+ d.getMonth() +"/"+ d.getDay() + " "
    					+ d.getHours() +":"+ d.getMinutes() +"\n");
        builder.setCancelable(true);
        builder.setMessage(info);
        builder.setPositiveButton(R.string.alertButton_ok, null);
        builder.show();
    }
    
    private static int calculateFileNumberInDirectory(File calculatedFile) {
		File[] fList = calculatedFile.listFiles();
		int counter=0;
		for(File f: fList){
			counter++;//file and directory both be calculated
			if(f.isDirectory()){//deep calculate the file number in sub-director
				counter += calculateFileNumberInDirectory(f);
			}
		}
		return counter;
	}
}
