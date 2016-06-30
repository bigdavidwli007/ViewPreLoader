package cn.research.viewpreloader;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

public class ViewLoader {
	
	public static final String TAG = "ViewLoader";
	
	private ExecutorService mLoaderViewThreadPool;	
	private LayoutInflater mLayoutInflater;
	private ConcurrentHashMap<Integer, View> mCachedView;
	private ConcurrentHashMap<Integer, ViewLoadListener> mViewLoadListeners;
	private List<Integer> mLoadingView;
	private Object mLoadingViewSync = new Object();
	private static ViewLoader sInstance;
	
	public synchronized static ViewLoader getInstance(Context context) {
	    if (sInstance == null) {
	    	sInstance = new ViewLoader(context);
	    }
	    return sInstance;
	}
	
	private boolean addViewToLoading(int layoutResId) {
		synchronized(mLoadingViewSync) {
			if (mLoadingView.contains(layoutResId)) {
				return false;
			}
			mLoadingView.add(layoutResId);
			return true;
		}
	}
	
	private boolean removeViewFromLoading(int layoutResId) {
		synchronized(mLoadingViewSync) {
			if (!mLoadingView.contains(layoutResId)) {
				return false;
			}
			mLoadingView.remove((Integer)layoutResId);
			return true;
		}
	}
	
	private boolean isViewInLoading(int layoutResId) {
		synchronized(mLoadingViewSync) {
			if (mLoadingView.contains(layoutResId)) {
				return true;
			}
			return false;
		}
	}
	
	private ViewLoader(Context context) {
		mLoaderViewThreadPool = Executors.newFixedThreadPool(1);  
		mLayoutInflater = LayoutInflater.from(context.getApplicationContext());
		mCachedView = new ConcurrentHashMap<Integer, View>();
		mLoadingView = Collections.synchronizedList(new ArrayList<Integer>());
		mViewLoadListeners = new ConcurrentHashMap<Integer, ViewLoadListener>();
	}
	
	public void InflateView(int layoutResId) {
		InflateView(layoutResId, null);
	}
	
	public void InflateView(final int layoutResId, final ViewLoadListener listener) {
		if (isViewInLoading(layoutResId) == true) {
			Log.d(TAG, "View " + layoutResId + " is in Loading, Please wait ...");
			return;
		}
		
		addViewToLoading(layoutResId);
		if (listener != null) {
		    mViewLoadListeners.put(layoutResId, listener);
		}
		mLoaderViewThreadPool.execute(new Runnable(){
			@Override
			public void run() {
				View result;
				result = mLayoutInflater.inflate(layoutResId, null);
				ViewLoadListener monitor = mViewLoadListeners.get(layoutResId);
				if (result != null) {
					mCachedView.put(layoutResId, result);
					removeViewFromLoading(layoutResId);
					if (monitor != null) {
						monitor.onLoadSuccess(result);
					}
				} else {
					removeViewFromLoading(layoutResId);
					if (monitor != null) {
						monitor.onLoadFailed(null);
					}
				}
				if (monitor != null) {
				    mViewLoadListeners.remove(layoutResId);
				}
			}
	    });
	}
	
    public View loadView(int layoutResId, ViewLoadListener listener) {
    	if (mCachedView.containsKey(layoutResId)) {
    		Log.d(TAG, "use loaded view for key: " + layoutResId);
    		return mCachedView.get(layoutResId);
    	}
    	
    	if (isViewInLoading(layoutResId) && listener != null) {
    		mViewLoadListeners.put(layoutResId, listener);
    	}
    	return null;
    }
    
    public void attachView(Activity activity, View view) {
    	if (activity == null) {
    		return;
    	}
    	
    	if(clearViewParent(view) == true){
		    activity.setContentView(view);
    	}
    }
    
    public void clearCachedView() {
    	mCachedView.clear();
    }
    
	private static boolean clearViewParent(View view) {
		boolean result = false;
		try {
			Class<View> clazz = View.class;
			Field field = clazz.getDeclaredField("mParent");
			field.setAccessible(true);
			field.set(view, null);
			field.setAccessible(false);
			result = true;
		} catch (Exception e) {
			result = false;
            Log.e(TAG, e.toString());
		}
		return result;
	}
}