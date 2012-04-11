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
	private Bitmap mIcon5;//video
	private Bitmap mIcon6;//picture
	
	public FileListAdapter(Context context, List<String> filePath) {
		mLayoutInflater = LayoutInflater.from(context);//不用就拿不到原生Activity(FindMusicActivity)的Layout
		this.filePath=filePath;

		mIcon1=BitmapFactory.decodeResource(context.getResources(), R.drawable.document_open_folder);
		mIcon2=BitmapFactory.decodeResource(context.getResources(), R.drawable.folder);
		mIcon3=BitmapFactory.decodeResource(context.getResources(), R.drawable.file);
		mIcon4=BitmapFactory.decodeResource(context.getResources(), R.drawable.music);
		mIcon5=BitmapFactory.decodeResource(context.getResources(), R.drawable.video);
		mIcon6=BitmapFactory.decodeResource(context.getResources(), R.drawable.image);
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
		switch(MimeType.getMimeType(f)){
		case MimeType.TYPE_DIRECTORY://directory
			holder.icon.setImageBitmap(mIcon2);
			break;
		case MimeType.TYPE_UNKNOWN://file(unknown)
			holder.icon.setImageBitmap(mIcon3);
			break;
		case MimeType.TYPE_AUDIO://audio
			holder.icon.setImageBitmap(mIcon4);
			break;
		case MimeType.TYPE_VIDEO://video
			holder.icon.setImageBitmap(mIcon5);
			break;
		case MimeType.TYPE_IMAGE://image
			holder.icon.setImageBitmap(mIcon6);
			break;
		default://actually, this will not happen.
			holder.icon.setImageBitmap(mIcon3);
			break;
		}
		holder.text.setText(f.getName());
		return convertView;
	}

	static class ViewHolder{
		ImageView icon;
		TextView text;
	}
}
