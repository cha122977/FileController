package com.cha122977.android.filecontroller;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.cha122977.android.filecontroller.FSController.MimeType;

public class FileListAdapter extends BaseAdapter {
	
	private static final int NOTIFY_CHANGED = 0;
	
	private boolean isAdapterDropped = false;
	
	@SuppressLint("HandlerLeak")
	Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case NOTIFY_CHANGED:
				notifyDataSetChanged();
				break;
			default:
				super.handleMessage(msg);
				break;
			}
		}
	};
	
	private LayoutInflater mLayoutInflater;
	
	private ArrayList<String> filePath;
	private List<Bitmap> fileIcon;
	
	@SuppressLint("unused")
	private Bitmap mIconOpenFolder; // opened folder
	private Bitmap mIconFolder; // folder
	private Bitmap mIconFile; // file
	private Bitmap mIconAudio; // audio
	private Bitmap mIconVideo; // video
	private Bitmap mIconImage; // image
	private Bitmap mIconText; // text
	
	private Bitmap mIconUnknow;// unknown image

	private Object SyncLock = new Object();
	
	public FileListAdapter(Context context, File[] fileList) {
		this.filePath = new ArrayList<String>(fileList.length);
		for (int i = 0; i<fileList.length; i++) {
			filePath.add(i, fileList[i].getPath());
		}
		
		fileIcon = new ArrayList<Bitmap>(fileList.length);
		for (int i=0; i<filePath.size(); i++) {
			fileIcon.add(i, null);
		}
		
		setupImages(context);
	}

	private void setupImages(Context context) {
		mLayoutInflater = LayoutInflater.from(context);
		
		IconsPool pool = IconsPool.getInstance();
		
		mIconOpenFolder = pool.getOpenedFolderIcon();
		mIconFolder = pool.getNormalFolderIcon();
		mIconFile = pool.getFileIcon();
		mIconAudio = pool.getAudioIcon();
		mIconVideo = pool.getVideoIcon();
		mIconImage = pool.getImageIcon();
		mIconText = pool.getTextIcon();
		
		startScaleAllImagesThread(); //run the thread to create scaledImage
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
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if (convertView == null) {
			convertView = mLayoutInflater.inflate(R.layout.file_list_row, null);
			holder = new ViewHolder();
			holder.icon = (ImageView) convertView.findViewById(R.id.icon);
			holder.text = (TextView) convertView.findViewById(R.id.fileName);
			convertView.setTag(holder);

		} else {
			holder = (ViewHolder)convertView.getTag();
		}
		
		File f = new File(filePath.get(position).toString());
		switch (FSController.getMimeType(f)) {
		case DIRECTORY:
			holder.icon.setImageBitmap(mIconFolder);
			break;
		case UNKNOWN:
			holder.icon.setImageBitmap(mIconFile);
			break;
		case AUDIO:
			holder.icon.setImageBitmap(mIconAudio);
			break;
		case VIDEO:
			holder.icon.setImageBitmap(mIconVideo);
			break;
		case IMAGE:
			if (fileIcon.get(position) == null) {
				holder.icon.setImageBitmap(mIconImage);
				startScaleImageThread(position);
			} else {
				holder.icon.setImageBitmap(fileIcon.get(position));
			}
			break;
		case TEXT:
			holder.icon.setImageBitmap(mIconText);
			break;
		default:
			holder.icon.setImageBitmap(mIconFile);
			break;
		}
		holder.text.setText(f.getName());
		return convertView;
	}
	
	static class ViewHolder {
		ImageView icon;
		TextView text;
	}
	
	public void drop() {
		isAdapterDropped = true;
		// recycle resource.
		for (Bitmap bm: fileIcon) {
			if (bm != null) {
				bm.recycle();
			}
		}
	}
	
	// scaled single image
	private void startScaleImageThread(final int position) {	
		Thread scaleThread = new Thread(new Runnable() {
			@Override
			public void run() {
				if (isAdapterDropped) {
					return;
				}
				String fp = filePath.get(position);
				scaledImage(fp, position);
				mHandler.sendEmptyMessage(NOTIFY_CHANGED);
			}
		});
		scaleThread.setPriority(Thread.NORM_PRIORITY + 2); // priority is higher than all images.
		scaleThread.start();
	}
	
	// scaled all image. if we set all icon here, performance will bad.
	private void startScaleAllImagesThread() {  
		new Thread(new Runnable() {
			@Override
			public void run() {
				for (int i=0; i<filePath.size(); i++) {
					if (isAdapterDropped) {
						return;
					}
					String fp = filePath.get(i);
					if (FSController.getMimeType(new File(fp)) == MimeType.IMAGE) {
						scaledImage(fp, i);	
						mHandler.sendEmptyMessage(NOTIFY_CHANGED);
					}
				}
			}
		}).start();
	}
	
	private void scaledImage(String filePath, int indexOfFileIcon) {
		synchronized (SyncLock) { // scale one image at one time.
			if (fileIcon.get(indexOfFileIcon) == null) { // if scaled by other thread, ignore scaling.
				Bitmap bm = Utility.decodeSampledBitmapFromFilePath(filePath, AppConstant.ICON_SIZE, AppConstant.ICON_SIZE);
				fileIcon.set(indexOfFileIcon, bm != null? bm: mIconUnknow);
				mHandler.sendEmptyMessage(NOTIFY_CHANGED);
			}
		}
	}
}