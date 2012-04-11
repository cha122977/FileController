package com.cha122977.android.filecontroller;

import java.io.File;

public class MimeType {
	/* �P�_�ɮ�MimeType��method */
	public static final int TYPE_DIRECTORY = 2;//directory icon(not open)
	public static final int TYPE_UNKNOWN = 3;//file icon(unknown type)
	public static final int TYPE_AUDIO = 4;
	public static final int TYPE_VIDEO = 5;
	public static final int TYPE_IMAGE = 6;
	
	public static int getMimeType(File f){
		if(f.isDirectory()){
			return TYPE_DIRECTORY;
		}
	    String fName=f.getName();
	    /* ���o���ɦW */
	    String end=fName.substring(fName.lastIndexOf(".")+1,
	                               fName.length()).toLowerCase(); 
	    
	    /* �̪��ɦW�������M�wMimeType */
	    if(end.equals("m4a")||end.equals("mp3")||end.equals("mid")||
	       end.equals("xmf")||end.equals("ogg")||end.equals("wav"))
	    {
	      return TYPE_AUDIO;
	    }
	    else if(end.equals("3gp")||end.equals("mp4"))
	    {
	      return TYPE_VIDEO;
	    }
	    else if(end.equals("jpg")||end.equals("gif")||end.equals("png")||
	            end.equals("jpeg")||end.equals("bmp"))
	    {
	      return TYPE_IMAGE;
	    }
	    else
	    {
	      //return ???
	    }
	    /* �p�G�L�k�����}�ҡA�N���X�n��M�浹�ϥΪ̿�� */
	    return TYPE_UNKNOWN;
	}
	
	public static String getMimeType(String fileName){
	    int pointIndex = fileName.lastIndexOf(".");
	    String type;
	    if(pointIndex > 0){// != -1 && != 0
	    	String attachment = fileName.substring(pointIndex+1).toLowerCase();//set attachment name.
	    	if(attachment.equals("m4a")||attachment.equals("mp3")||attachment.equals("mid")||
	    			attachment.equals("xmf")||attachment.equals("ogg")||attachment.equals("wav"))
    		    {
    		    	type = "audio";
    		    }
    		    else if(attachment.equals("3gp")||attachment.equals("mp4"))
    		    {
    		    	type = "video";
    		    }
    		    else if(attachment.equals("jpg")||attachment.equals("gif")||attachment.equals("png")||
    		            attachment.equals("jpeg")||attachment.equals("bmp"))
    		    {
    		    	type = "image";
    		    }
    		    else
    		    {
    		    	type = "*";
    		    }
	    } else {
	    	/* �p�G�L�k�����}�ҡA�N���X�n��M�浹�ϥΪ̿�� */
	    	type = "*";
	    }
	    type += "/*";
	    return type;
	}
}
