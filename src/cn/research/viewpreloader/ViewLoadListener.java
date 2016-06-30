package cn.research.viewpreloader;

import android.view.View;

public interface ViewLoadListener {	
	public void onLoadSuccess(View view);
	public void onLoadFailed(String reason);
}