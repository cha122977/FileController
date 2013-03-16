package com.cha122977.android.filecontroller;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

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

public class FileListAdapter extends BaseAdapter {
	
	Handler mHandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			switch(msg.what){
			case 0:
				notifyDataSetChanged();
				break;
			default:
				//do nothing
			}
			super.handleMessage(msg);
		}
	};
	
	private LayoutInflater mLayoutInflater;
	
	private ArrayList<String> filePath;
	private List<Bitmap> fileIcon;
	
	private Bitmap mIcon1;//opened folder
	private Bitmap mIcon2;//folder
	private Bitmap mIcon3;//file
	private Bitmap mIcon4;//audio
	private Bitmap mIcon5;//video
	private Bitmap mIcon6;//picture
	private Bitmap mIcon7;//text
	
	private Bitmap mIcon_m1;//-1, unknown image
	
	@SuppressWarnings("unchecked")
	public FileListAdapter(Context context, ArrayList<String> filePath) {
		mLayoutInflater = LayoutInflater.from(context);
		this.filePath = (ArrayList<String>)filePath.clone();
		fileIcon = new ArrayList<Bitmap>();
		for(int i=0; i<filePath.size(); i++){
			fileIcon.add(i, null);
		}
		
		mIcon1=Utility.decodeSampledBitmapFromResource(context.getResources(), R.drawable.open_v2, 48 ,48);
		mIcon2=Utility.decodeSampledBitmapFromResource(context.getResources(), R.drawable.folder, 48, 48);
		mIcon3=Utility.decodeSampledBitmapFromResource(context.getResources(), R.drawable.file, 48, 48);
		mIcon4=Utility.decodeSampledBitmapFromResource(context.getResources(), R.drawable.music, 48, 48);
		mIcon5=Utility.decodeSampledBitmapFromResource(context.getResources(), R.drawable.video, 48, 48);
		mIcon6=Utility.decodeSampledBitmapFromResource(context.getResources(), R.drawable.image, 48, 48);
		mIcon7=Utility.decodeSampledBitmapFromResource(context.getResources(), R.drawable.text, 48, 48);
		
		mIcon_m1=Utility.decodeSampledBitmapFromResource(context.getResources(), R.drawable.unknown_image, 48, 48);
		
//		mIcon1=BitmapFactory.decodeResource(context.getResources(), R.drawable.open_v2);
//		mIcon2=BitmapFactory.decodeResource(context.getResources(), R.drawable.folder);
//		mIcon3=BitmapFactory.decodeResource(context.getResources(), R.drawable.file);
//		mIcon4=BitmapFactory.decodeResource(context.getResources(), R.drawable.music);
//		mIcon5=BitmapFactory.decodeResource(context.getResources(), R.drawable.video);
//		mIcon6=BitmapFactory.decodeResource(context.getResources(), R.drawable.image);
//		mIcon7=BitmapFactory.decodeResource(context.getResources(), R.drawable.text);
		
//		mIcon_m1=BitmapFactory.decodeResource(context.getResources(), R.drawable.unknown_image);
		processScaledImage();//run the thread to create scaledImage
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
			convertView = mLayoutInflater.inflate(R.layout.file_list_row, null);
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
			if(fileIcon.get(position) == null){
				holder.icon.setImageBitmap(mIcon6);
			} else {
				holder.icon.setImageBitmap(fileIcon.get(position));
			}
			break;
		case MimeType.TYPE_TEXT://text
			holder.icon.setImageBitmap(mIcon7);
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
	
	private boolean isListDropped = false;
	
	public void setListDropped() {
		isListDropped = true;
	}
	
	private void processScaledImage(){//just for set scaled image. if we set all icon here, performance will bad. 
		new Thread(new Runnable() {
			@Override
			public void run() {
				for(int i=0; i<filePath.size(); i++){
					if (isListDropped) {
						break;
					}
					String fp = filePath.get(i);
					if(MimeType.getMimeType(new File(fp)) == MimeType.TYPE_IMAGE){
//						try{
//							BitmapFactory.Options options = new BitmapFactory.Options();
//							options.inJustDecodeBounds = true; //limit the image to bounds
//							BitmapFactory.decodeFile(fp, options);//get the height & width of image(save in options)
//							
//							final int newHeight = mIcon6.getHeight();
//							final int newWidth  = mIcon6.getWidth();
//							
//							int width=options.outWidth;
//					        int height=options.outHeight;
//							
//					        int scaledPower = 1;
//					        
//					        while(height/2 >= newHeight || width/2 >= newWidth){
//					        	scaledPower++;
//					        	height /= 2;
//					        	width  /= 2;
//					        }
//					        
//					        options = new BitmapFactory.Options();//new options
//					        options.inSampleSize=scaledPower;
//					        Bitmap vBitmap = BitmapFactory.decodeFile(fp, options);
//					        
//							if(vBitmap == null){//avoid crash.
//								fileIcon.set(i, mIcon_m1);//set to unknown_image
//								continue;
//							}
//							// scaled image
//							Bitmap vB2 = Bitmap.createScaledBitmap(vBitmap, mIcon6.getHeight(), mIcon6.getWidth(), true);
//							fileIcon.set(i, vB2);//add icon to fileIcon.
//							mHandler.sendEmptyMessage(0);//notify data set change
//						} catch(Exception e){
//							fileIcon.set(i, mIcon_m1);
//							continue;
//						}
						
//						if(vBitmap == null){//avoid crash.
////							fileIcon.set(i, mIcon_m1);//set to unknown_image
////							continue;
////						}
						
						Bitmap bm = Utility.decodeSampledBitmapFromFilePath(fp, 48, 48);
						
						if (bm != null) {
							fileIcon.set(i, bm);
						} else {
							fileIcon.set(i, mIcon_m1);
						}
					}
				}
			}
		}).start();
	}
}