package com.reako.adplayermanager;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

public class ReportInfoService extends Service{
	public static final int REPORT_TIME = 60 * 1000; //60s
	public static final int MSG_REPORT = 1;
	
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
	
	public void onCreate() {
		super.onCreate();
		
		mMainHandler.sendEmptyMessage(MSG_REPORT);
	}
	
	private Handler mMainHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case MSG_REPORT:
					if (Debug.debug()) {
						Log.d("ReportInfoService", "==========MSG_REPORT===========");
					}
					sendReportInfo();
					mMainHandler.sendEmptyMessageDelayed(MSG_REPORT, REPORT_TIME);
					break;
					
				default:
					Log.e("ReportInfoService", "handle a unknown message. msg.what=" + msg.what);
					break;
			}
		}
	};
	
	private void sendReportInfo() {
		if (Debug.debug()) {
			Log.d("ReportInfoService", "==========Send report info succeed!===========");
		}
	}

}
