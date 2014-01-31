package com.cha122977.android.filecontroller;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.cha122977.android.filecontroller.FSController.MimeType;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class SearchListAdapter extends BaseAdapter {
	
	private static final int NOTIFY_CHANGED = 0;
	
	@SuppressLint("HandlerLeak")
	Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case NOTIFY_CHANGED:
				notifyDataSetChanged();
				break;
			}
		}
	};
	
	private LayoutInflater mLayoutInflater;
	
	private List<String> filePath;
	private List<Bitmap> fileIcon;
	
	@SuppressWarnings("unused")
	private Bitmap mIcon1; // opened folder
	private Bitmap mIcon2; // folder
	private Bitmap mIcon3; // file
	private Bitmap mIcon4; // audio
	private Bitmap mIcon5; // video
	private Bitmap mIcon6; // picture
	private Bitmap mIcon7; // text
	
	private Bitmap mIcon_m1; // -1, unknown image
	
	public SearchListAdapter(Context context, List<String> filePath) {
		mLayoutInflater = LayoutInflater.from(context);
		this.filePath = filePath;
		fileIcon = new ArrayList<Bitmap>();
		for (int i=0; i<filePath.size(); i++) {
			fileIcon.add(i, null);
		}
		mIcon1=BitmapFactory.decodeResource(context.getResources(), R.drawable.open_v2);
		mIcon2=BitmapFactory.decodeResource(context.getResources(), R.drawable.folder);
		mIcon3=BitmapFactory.decodeResource(context.getResources(), R.drawable.file);
		mIcon4=BitmapFactory.decodeResource(context.getResources(), R.drawable.music);
		mIcon5=BitmapFactory.decodeResource(context.getResources(), R.drawable.video);
		mIcon6=BitmapFactory.decodeResource(context.getResources(), R.drawable.image);
		mIcon7=BitmapFactory.decodeResource(context.getResources(), R.drawable.text);
		
		mIcon_m1=BitmapFactory.decodeResource(context.getResources(), R.drawable.unknown_image);
		
		
		
		processScaledAllImage();
	}
	
	private Object addSyncLock = new Object();
	
	public void addFilePath(String addedPath) {
		synchronized(addSyncLock) {
			this.filePath.add(addedPath);
			this.fileIcon.add(null);
			mHandler.sendEmptyMessage(NOTIFY_CHANGED);
		}
	}
	
	public void addFiles(List<String> addedPaths) {
		synchronized(addSyncLock) {
			this.filePath.addAll(addedPaths);

			ArrayList<Bitmap> newIcons = new ArrayList<Bitmap>();
			for (int i=0; i<addedPaths.size(); i++) {
				newIcons.add(i, null);
			}
			this.fileIcon.addAll(newIcons);
			mHandler.sendEmptyMessage(NOTIFY_CHANGED);
			
			mHandler.sendEmptyMessage(NOTIFY_CHANGED);
		}
	}
	
	@Override
	public int getCount() {
		synchronized (addSyncLock) {
			return filePath.size();
		}
	}

	@Override
	public Object getItem(int position) {
		synchronized (addSyncLock) {
			return filePath.get(position);
		}
	}

	@Override
	public long getItemId(int position) {
		synchronized (addSyncLock) {
			return position;
		}
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		synchronized (addSyncLock) {
			ViewHolder holder;
			if (convertView==null) {
				convertView = mLayoutInflater.inflate(R.layout.search_list_row, null);
				holder = new ViewHolder();
				holder.icon = (ImageView)convertView.findViewById(R.id.fileIcon);
				holder.fileName = (TextView)convertView.findViewById(R.id.fileName);
				holder.filePath = (TextView)convertView.findViewById(R.id.filePath);
				convertView.setTag(holder);

			} else {
				holder = (ViewHolder)convertView.getTag();
			}

			File f = new File(filePath.get(position).toString());
			switch (FSController.getMimeType(f)) {
			case DIRECTORY://directory
				holder.icon.setImageBitmap(mIcon2);
				break;
			case UNKNOWN://file(unknown)
				holder.icon.setImageBitmap(mIcon3);
				break;
			case AUDIO://audio
				holder.icon.setImageBitmap(mIcon4);
				break;
			case VIDEO://video
				holder.icon.setImageBitmap(mIcon5);
				break;
			case IMAGE://image
				if (fileIcon.get(position) == null) {
					holder.icon.setImageBitmap(mIcon6);
					processScaledImage(position);
				} else {
					holder.icon.setImageBitmap(fileIcon.get(position));
				}
				break;
			case TEXT://text
				holder.icon.setImageBitmap(mIcon7);
				break;
			default://actually, this will not happen.
				holder.icon.setImageBitmap(mIcon3);
				break;
			}
			holder.fileName.setText(f.getName());
			holder.filePath.setText(f.getAbsolutePath());
			return convertView;
		}
	}
	
	static class ViewHolder {
		ImageView icon;
		TextView fileName;
		TextView filePath;
	}
	
	private boolean isAdapterDropped = false;
	
	public void drop() {
		isAdapterDropped = true;
	}
	
	// just for set scaled visable image
	private void processScaledImage(final int position) {	
		Thread scaleThread = new Thread(new Runnable() {
			@Override
			public void run() {
				if (isAdapterDropped) {
					return;
				}
				String fp = filePath.get(position);
				synchronized (SyncLock) {
					if (fileIcon.get(position) == null) { // maybe scaled from other thread, thus ignore scaling.
						// MimiType already checked when call this function. So skip to check if image.
						Bitmap bm = Utility.decodeSampledBitmapFromFilePath(fp, 48, 48);
						fileIcon.set(position, bm != null? bm: mIcon_m1);
						mHandler.sendEmptyMessage(NOTIFY_CHANGED);
					}
				}
			}
		});
		scaleThread.setPriority(Thread.NORM_PRIORITY+1); // set priority higher than normal.
		scaleThread.start();
	}
	
	//just for set scaled image. if we set all icon here, performance will bad.
	private void processScaledAllImage() {  
		new Thread(new Runnable() {
			@Override
			public void run() {
				for (int i=0; i<filePath.size(); i++) {
					if (isAdapterDropped) {
						return;
					}
					String fp = filePath.get(i);
					if (FSController.getMimeType(new File(fp)) == MimeType.IMAGE) {
						synchronized (SyncLock) {
							if (fileIcon.get(i) == null) { // only scaled when there is no icon.
								Bitmap bm = Utility.decodeSampledBitmapFromFilePath(fp, 48, 48);
								fileIcon.set(i, bm != null? bm: mIcon_m1);
								mHandler.sendEmptyMessage(NOTIFY_CHANGED);
							}
						}
					}
				}
			}
		}).start(); // Run as normal priority.
	}
	
	private Object SyncLock = new Object();
}