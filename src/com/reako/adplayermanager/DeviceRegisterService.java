package com.reako.adplayermanager;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;

import com.avatar.ott.sdk.installer.cross.models.infos.FixedDeviceInfo;
import com.avatar.ott.sdk.installer.cross.models.paras.Register_Para;
import com.avatar.ott.sdk.installer.cross.models.paras.Register_ResultPara;
import com.reako.adplayermanager.net.HttpPostTask;
import com.reako.adplayermanager.net.HttpTaskResult;
import com.reako.adplayermanager.net.OnHttpTaskFinished;


public class DeviceRegisterService extends Service {
	
	private static final String TAG = "DeviceRegisterService";
	
	private static final int MSG_REGISTER_DEVICE = 0x01;
	
	private static final long RETRY_TIME = 2 * 60 * 60 * 1000; // 2 hours
	
	private Context mContext = null;

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void onCreate() { 
		super.onCreate();
		mContext = this.getApplicationContext();
		mMainHandler.sendEmptyMessage(MSG_REGISTER_DEVICE);
	}
	
	private Handler mMainHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case MSG_REGISTER_DEVICE:
					if (Debug.debug()) {
						Log.d(TAG, "==========MSG_REGISTER_DEVICE===========");
					}
					regDevices();
					break;
					
				default:
					Log.e(TAG, "handle a unknown message. msg.what=" + msg.what);
					break;
			}
		}
	};
	
	private void regDevices() {
		String url = "http://" + com.avatar.ott.sdk.cross.base.Config.SERVER_ADDRESS + "/"
				+ com.avatar.ott.sdk.cross.base.Config.SERVER_SERVLET_Register + "/";
		
		FixedDeviceInfo info = getDeviceInfo();
		Register_Para para = new Register_Para();
		para.setFixedDeviceInfo(info);
		NameValuePair pair = new BasicNameValuePair("a", para.toString());
		List<NameValuePair> parameters = new ArrayList<NameValuePair>();
		parameters.add(pair);
		OnHttpTaskFinished finishedListener = newOnHttpTaskFinished();
		
		HttpPostTask taskRegisterDevice = new HttpPostTask(finishedListener);
		taskRegisterDevice.setUrl(url);
		taskRegisterDevice.setParameters(parameters);
		
		taskRegisterDevice.execute("register");	
	}
	
	private OnHttpTaskFinished newOnHttpTaskFinished() {
		return new OnHttpTaskFinished() {

			@Override
			public void onHttpTaskFinished(HttpTaskResult result) {
				if (HttpTaskResult.ERR_CODE_SUCCEED == result.mErrCode) {
					Register_ResultPara para = new Register_ResultPara();
					try {
						para.parse("a", result.mRequestBuff, Register_ResultPara.class);
						if (Debug.debug()) {
							Log.d(TAG, "Register_ResultPara.UserID=" + para.getUserId());
						}
						Configure.getInstant(mContext).setUserID(para.getUserId());
					} catch (Exception e) {						
						e.printStackTrace();
					}
				} else {
					Log.e(TAG, "===========Register Device Failed.============");
					result.dumpErrCode();
					mMainHandler.sendEmptyMessageDelayed(MSG_REGISTER_DEVICE, RETRY_TIME);
				}
			}
			
		};
	}
	
	private FixedDeviceInfo getDeviceInfo() {
		WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
		FixedDeviceInfo info = new FixedDeviceInfo();
		info.setBrand(System.getProperty("ro.product.brand"));
		DisplayMetrics metric = new DisplayMetrics();
		wm.getDefaultDisplay().getMetrics(metric);
		info.setDeviceDensity(metric.density);
		info.setDeviceHeight(wm.getDefaultDisplay().getHeight());
		info.setDeviceWidth(wm.getDefaultDisplay().getWidth());
		info.setDeviceId(System.getProperty("ro.serialno"));
		info.setMac(Uitls.getMac());
		info.setModel(System.getProperty("ro.product.model"));
		info.setTotalRam(Uitls.getTotalRam());
		info.setTotalRomMemory(Uitls.getTotalRom());
		return info;
	}
	
	
	
	
}
