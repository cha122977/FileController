package com.cha122977.android.filecontroller;

import java.io.File;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class FileListAdapter extends BaseAdapter {
	
	private LayoutInflater mLayoutInflater;
	
	private List<String> filePath;
	
	private Bitmap mIcon1;//opened folder
	private Bitmap mIcon2;//folder
	private Bitmap mIcon3;//file
	private Bitmap mIcon4;//audio
	
	public FileListAdapter(Context context, List<String> filePath) {
		mLayoutInflater = LayoutInflater.from(context);//���δN��������Activity(FindMusicActivity)��Layout
		this.filePath=filePath;

		mIcon1=BitmapFactory.decodeResource(context.getResources(), R.drawable.document_open_folder);
		mIcon2=BitmapFactory.decodeResource(context.getResources(), R.drawable.folder);
		mIcon3=BitmapFactory.decodeResource(context.getResources(), R.drawable.file);
		mIcon4=BitmapFactory.decodeResource(context.getResources(), R.drawable.music);
	}

	@Override
	public int getCount() {
		return filePath.size();
	}

	@Override
	public Object getItem(int position) {
		return filePath.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if(convertView==null){
			convertView = mLayoutInflater.inflate(R.layout.list_row, null);
			holder = new ViewHolder();
			holder.icon = (ImageView)convertView.findViewById(R.id.icon);
			holder.text = (TextView)convertView.findViewById(R.id.fileName);
			convertView.setTag(holder);

		}else{
			holder = (ViewHolder)convertView.getTag();
		}
		
		File f = new File(filePath.get(position).toString());
		if (f.isDirectory()){
			holder.icon.setImageBitmap(mIcon2);
		}else{
			if(getMIMEType(f).equals("audio")){
				holder.icon.setImageBitmap(mIcon4);
			}else{
				holder.icon.setImageBitmap(mIcon3);
			}
		}
		holder.text.setText(f.getName());
		
		return convertView;
	}

	static class ViewHolder{
		ImageView icon;
		TextView text;
	}
	
	/* �P�_�ɮ�MimeType��method */
	private String getMIMEType(File f){
	    String type="";
	    String fName=f.getName();
	    /* ���o���ɦW */
	    String end=fName.substring(fName.lastIndexOf(".")+1,
	                               fName.length()).toLowerCase(); 
	    
	    /* �̪��ɦW�������M�wMimeType */
	    if(end.equals("m4a")||end.equals("mp3")||end.equals("mid")||
	       end.equals("xmf")||end.equals("ogg")||end.equals("wav"))
	    {
	      type = "audio"; 
	    }
	    else if(end.equals("3gp")||end.equals("mp4"))
	    {
	      type = "video";
	    }
	    else if(end.equals("jpg")||end.equals("gif")||end.equals("png")||
	            end.equals("jpeg")||end.equals("bmp"))
	    {
	      type = "image";
	    }
	    else
	    {
	      type="*";
	    }
	    /* �p�G�L�k�����}�ҡA�N���X�n��M�浹�ϥΪ̿�� */
//	    type += "/*"; 
	    return type; 
	}
}
