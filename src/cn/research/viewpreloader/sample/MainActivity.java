package cn.research.viewpreloader.sample;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.RelativeLayout;
import cn.research.viewpreloader.R;
import cn.research.viewpreloader.ViewLoadListener;

public class MainActivity extends Activity implements ViewLoadListener {

	private UpdateViewHandler mHandler;
	
	private int UPDATE_VIEW = 0;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mHandler = new UpdateViewHandler();
        View rootView = TestApplication.getViewLoader().loadView(R.layout.activity_main, this);
        if (rootView != null) {
        	TestApplication.getViewLoader().attachView(MainActivity.this, rootView);
        }
    }

    private class UpdateViewHandler extends Handler {
    	@Override
    	public void handleMessage(Message msg) {
    		if (msg.what == UPDATE_VIEW) {
    			View view = (View)msg.obj;
    			//initView(view);
    			TestApplication.getViewLoader().attachView(MainActivity.this, view);
    		}
    	}
    }
    
    private void sendViewLoadedMessage(View view) {
    	Message msg = mHandler.obtainMessage(UPDATE_VIEW);
    	msg.obj = view;
    	mHandler.sendMessage(msg);
    }
    
    public void initView(View view) {
    	RelativeLayout childContainter = (RelativeLayout)findViewById(R.id.container);
    	childContainter.setBackgroundColor(MainActivity.this.getResources().getColor(android.R.color.black));
    }


	@Override
	public void onLoadSuccess(View view) {
		sendViewLoadedMessage(view);
	}

	@Override
	public void onLoadFailed(String reason) {
	}
    
}
