package com.cha122977.android.filecontroller;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.ListView;
import android.widget.TextView;

public class FileManagerWindowFragment extends Fragment {

	private Context context;
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		context = activity;
	}
	
	public FileManagerWindowFragment() {
	}

	private TextView tv_filePath;
	private ListView lv_fileList;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.file_manager_window_fragment_layout, container, false);
		
		tv_filePath = (TextView) rootView.findViewById(R.id.dirPath_FileManagerWindow_ImageView);
		lv_fileList = (ListView) rootView.findViewById(R.id.fileList_FileManagerWindow_ImageView);
		
		return rootView;
	}
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		// TODO Auto-generated method stub
		super.onCreateContextMenu(menu, v, menuInfo);
	}

}
