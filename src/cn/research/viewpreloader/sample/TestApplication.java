package cn.research.viewpreloader.sample;

import android.app.Application;
import cn.research.viewpreloader.R;
import cn.research.viewpreloader.ViewLoader;

public class TestApplication extends Application {
	
	public static final String TAG = "TestApplication";
	public static ViewLoader mViewLoader;

	
	public static final String  VIEW_KEY_MAIN = "main";
	
	@Override 
	public void onCreate() {
		mViewLoader = ViewLoader.getInstance(this);		
		mViewLoader.InflateView(R.layout.activity_main);
	}

    public static ViewLoader getViewLoader() {
    	return mViewLoader;
    }
}