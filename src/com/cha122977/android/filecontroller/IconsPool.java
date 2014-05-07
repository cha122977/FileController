package com.cha122977.android.filecontroller;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;

public class IconsPool {
	
	private enum IconNames {
		OPEN_FOLDER,
		NORMAL_FOLDER,
		FILE,
		AUDIO,
		VIDEO,
		IMAGE,
		TEXT,
		UNKNOW_IMAGE,
	};

	private Map<IconNames, Bitmap> icons; 
	
	private static IconsPool instance;
	
	public synchronized static IconsPool getInstance() {
		return instance;
	}
	
	public static void initIconPool(Context context) {
		instance = new IconsPool(context);
	}
	
	private IconsPool(Context context) {
		prepareIconBitmap(context);
	}
	
	private void prepareIconBitmap(Context context) {
		icons = new HashMap<IconNames, Bitmap>();
		Resources res = context.getResources();
		
		int size = AppConstant.ICON_SIZE;
		
		icons.put(IconNames.OPEN_FOLDER, Utility.decodeSampledBitmapFromResource(res, R.drawable.open_v2, size ,size));
		icons.put(IconNames.NORMAL_FOLDER, Utility.decodeSampledBitmapFromResource(res, R.drawable.folder, size, size));
		icons.put(IconNames.FILE, Utility.decodeSampledBitmapFromResource(res, R.drawable.file, size, size));
		icons.put(IconNames.AUDIO, Utility.decodeSampledBitmapFromResource(res, R.drawable.music, size, size));
		icons.put(IconNames.VIDEO, Utility.decodeSampledBitmapFromResource(res, R.drawable.video, size, size));
		icons.put(IconNames.IMAGE, Utility.decodeSampledBitmapFromResource(res, R.drawable.image, size, size));
		icons.put(IconNames.TEXT, Utility.decodeSampledBitmapFromResource(res, R.drawable.text, size, size));
		icons.put(IconNames.UNKNOW_IMAGE, Utility.decodeSampledBitmapFromResource(res, R.drawable.unknown_image, size, size));
	}
	
	private Bitmap getIcon(IconNames iconName) {
		Bitmap bm = this.icons.get(iconName);
		if (bm == null) {
			throw new IllegalArgumentException("Unknow IconName");
		}
		return bm;
	}

	public Bitmap getOpenedFolderIcon() {
		return getIcon(IconNames.OPEN_FOLDER);
	}

	public Bitmap getNormalFolderIcon() {
		return getIcon(IconNames.NORMAL_FOLDER);
	}

	public Bitmap getFileIcon() {
		return getIcon(IconNames.FILE);
	}

	public Bitmap getAudioIcon() {
		return getIcon(IconNames.AUDIO);
	}

	public Bitmap getVideoIcon() {
		return getIcon(IconNames.VIDEO);
	}

	public Bitmap getImageIcon() {
		return getIcon(IconNames.IMAGE);
	}

	public Bitmap getTextIcon() {
		return getIcon(IconNames.TEXT);
	}

	public Bitmap getUnknowImageIcon() {
		return getIcon(IconNames.UNKNOW_IMAGE);
	}
	
	public void recycle() {
		for (Bitmap bm: icons.values()) {
			bm.recycle();
		}
		instance = null;
	}
}
