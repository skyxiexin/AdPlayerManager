package com.reako.adplayermanager;

import java.io.File;
import java.util.List;

import com.reako.adplayermanager.net.HttpGetTask;
import com.reako.adplayermanager.net.HttpTaskResult;
import com.reako.adplayermanager.net.OnHttpTaskFinished;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.IPackageDeleteObserver;
import android.content.pm.IPackageInstallObserver;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;


public class TestActivity extends Activity {
	private static final String TAG = "TestActivity";
    private Button mBtnRegDevice = null;
    private Button mBtnDownloadApp = null;
    private Button mBtnInstallApp = null;
    private Button mBtnUninstallApp = null;
    private Button mBtnStartBootAd = null;
    private Button mBtnStartSettings = null;
    private Button mBtnVideoPlay = null;
    private Button mBtnVideoPause = null;
    private Button mBtnChannelChange = null;
    
    private TextView mRegDeviceStatus = null;
    private TextView mDownloadAppStatus = null;
    private TextView mInstallAppStatus = null;
    private TextView mUninstallAppStatus = null;
    
    private Context mContext = null;
    
    private ServiceConnection mAdPlayerManagerConnection = null;
    private IAdPlayerManagerService mAdPlayerManagerService = null;
    
    private static final int MSG_REG_DEVICE_FINISHED = 1;
    private static final int MSG_DOWNLOAD_APP_FINISHED = 2;
    private static final int MSG_INSTALL_APP_FINISHED = 3;
    private static final int MSG_UNINSTALL_APP_FINISHED = 4;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        
        mContext = getApplicationContext();
        initServiceConnection();
        initWindowsController();
        setBtnRegDeviceListener();
        setBtnDownloadAppListener();
        setBtnInstallAppListener();
        setBtnUninstallAppListener();
        setBtnBootAdListener();
        setBtnStartSettingsListener();
        setBtnVideoPlayListener();

        //bind AD player manager service.
        bindAdPlayerService();
    }
    
    
    @Override
    protected void onDestroy() {
    	unbindAdPlayerService();
    	super.onDestroy();
    }
    
    private void bindAdPlayerService() {
    	Intent intent = new Intent("com.reako.adplayermanager.IAdPlayerManagerService");
    	bindService(intent, mAdPlayerManagerConnection, Context.BIND_AUTO_CREATE);
    }
    
    private void unbindAdPlayerService() {
    	if (mAdPlayerManagerConnection != null)	{
    		unbindService(mAdPlayerManagerConnection);
    	}
    }
    
    private void initServiceConnection() {
    	mAdPlayerManagerConnection = new ServiceConnection() {
			
			@Override
			public void onServiceDisconnected(ComponentName name) {
				mAdPlayerManagerService = null;
			}
			
			@Override
			public void onServiceConnected(ComponentName name, IBinder service) {
				if (Debug.debug()) {
					Log.d(TAG, "==========bind Ad Player Manager Service Succeed!");
				}
				mAdPlayerManagerService = IAdPlayerManagerService.Stub.asInterface(service);
				
				//regAdPlayer();
			}
		};
    }
    
    private boolean isAdPlayerManagerConnected() {
    	return (mAdPlayerManagerService != null);
    }
    
    
    private void initWindowsController() {
    	mRegDeviceStatus = (TextView) findViewById(R.id.reg_status_text);
    	mDownloadAppStatus = (TextView) findViewById(R.id.download_status_text);
    	mInstallAppStatus = (TextView) findViewById(R.id.install_status_text);
    	mUninstallAppStatus = (TextView) findViewById(R.id.uninstall_status_text);
    	
    	mBtnRegDevice = (Button) findViewById(R.id.btn_start_reg_dev);
    	mBtnDownloadApp = (Button) findViewById(R.id.btn_start_download_apk);
    	mBtnInstallApp = (Button) findViewById(R.id.btn_start_install_app);
    	mBtnUninstallApp = (Button) findViewById(R.id.btn_start_uninstall_app);
    	mBtnStartBootAd = (Button) findViewById(R.id.btn_start_start_boot_ad);
    	mBtnStartSettings = (Button) findViewById(R.id.btn_start_start_settings);
    	mBtnVideoPlay = (Button) findViewById(R.id.btn_video_play);
    	mBtnVideoPause = (Button) findViewById(R.id.btn_video_pause);
    	mBtnChannelChange = (Button) findViewById(R.id.btn_channel_change);
    }
    
    
    private void setBtnRegDeviceListener() {
    	mBtnRegDevice.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				
			}
		});
    }
    
    private void setBtnDownloadAppListener() {
    	mBtnDownloadApp.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				String url = "http://192.168.1.160:18080/Apps/pandapow1.0.0.apk";
				File outputFile = new File("DownloadApps.apk");
				
				
				OnHttpTaskFinished onFinished = new OnHttpTaskFinished() {
					
					@Override
					public void onHttpTaskFinished(HttpTaskResult result) {
						Message msg = new Message();
						msg.what = MSG_DOWNLOAD_APP_FINISHED;
						if ((result != null) && (result.mErrCode == HttpTaskResult.ERR_CODE_SUCCEED)) {
							msg.arg1 = 1;
						} else {
							msg.arg1 = 2;
						}
						mMainHandler.sendMessage(msg);
					}
				};
				
				HttpGetTask task = new HttpGetTask(mContext, url, outputFile, onFinished);
				task.execute("start");
				mDownloadAppStatus.setText("Downloading!");
			}
		});
    }
    
    private void setBtnInstallAppListener() {
    	mBtnInstallApp.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				File app = new File("/data/data/com.reako.adplayermanager/files/DownloadApps.apk");
				
				MyPackageInstallser installer = new MyPackageInstallser();
				MyPackageInstallObserver observer = new MyPackageInstallObserver();
				installer.install(mContext, app, observer);
				mInstallAppStatus.setText("Installing!");
			}
		});
    }
    
    
    private void setBtnUninstallAppListener() {
    	mBtnUninstallApp.setOnClickListener(new OnClickListener() {		
			@Override
			public void onClick(View v) {
				String uninstallPkgName = "org.pandapow.vpn";
				MyPackageInstallser uninstaller = new MyPackageInstallser();
				MyPackageUninstallserObserver observer = new MyPackageUninstallserObserver();
				uninstaller.uninstall(mContext, uninstallPkgName, observer);
				mUninstallAppStatus.setText("Uninstalling!");
			}
		});
    }
    
    private void setBtnBootAdListener() {
    	mBtnStartBootAd.setOnClickListener(new OnClickListener() {		
			@Override
			public void onClick(View v) {
				if (isAdPlayerManagerConnected()) {
					try {
						mAdPlayerManagerService.displayBootAd();
					} catch (RemoteException e) {
						e.printStackTrace();
					}
				}
			}
		});
    }
    
    private void setBtnStartSettingsListener() {
    	mBtnStartSettings.setOnClickListener(new OnClickListener() {		
			@Override
			public void onClick(View v) {
				if (isAdPlayerManagerConnected()) {
					
					try {
						mAdPlayerManagerService.displayAppStartAd("com.android.settings", "com.android.settings.Settings");
					} catch (RemoteException e) {
						e.printStackTrace();
					}

				}
			}
		});
    }
    
    public class MyPackageInstallObserver extends IPackageInstallObserver.Stub {

		@Override
		public void packageInstalled(String packageName,
				int returnCode) throws RemoteException {
			Message msg = new Message();
			msg.what = MSG_INSTALL_APP_FINISHED;
			if (returnCode == 1) {
				Log.d(TAG, "==========App Install Succeed!==========");
				msg.arg1 = 1;
			} else {
				Log.d(TAG, "==========App Install Failed!==========");
				msg.arg1 = 2;
			}
			mMainHandler.sendMessage(msg);
		}
		
	};
	
	public class MyPackageUninstallserObserver extends IPackageDeleteObserver.Stub {

		@Override
		public void packageDeleted(String packageName, int returnCode)
				throws RemoteException {
			Message msg = new Message();
			msg.what = MSG_UNINSTALL_APP_FINISHED;
			if (returnCode == 1) {
				Log.d(TAG, "==========App Uninstall Succeed!==========");
				msg.arg1 = 1;
			} else {
				Log.d(TAG, "==========App Uninstall Failed!==========");
				msg.arg1 = 2;
			}
			mMainHandler.sendMessage(msg);
			
		}
		
	}
    
    

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.test, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    
    private Handler mMainHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			int result = msg.arg1;
			switch (msg.what) {
				case MSG_REG_DEVICE_FINISHED:
					if (Debug.debug()) {
						Log.d(TAG, "==========MSG_REG_DEVICE_FINISHED===========");
					}
					result = msg.arg1;
					if (result == 1) {
						mRegDeviceStatus.setText("Register Device Succeed!");
					} else {
						mRegDeviceStatus.setText("Register Device Failed!");
					}
					break;
					
				case MSG_DOWNLOAD_APP_FINISHED:
					if (Debug.debug()) {
						Log.d(TAG, "==========MSG_DOWNLOAD_APP_FINISHED===========");
					}
					result = msg.arg1;
					if (result == 1) {
						mDownloadAppStatus.setText("Download Succeed!");
					} else {
						mDownloadAppStatus.setText("Download Failed!");
					}
					break;
					
				case MSG_INSTALL_APP_FINISHED:
					if (Debug.debug()) {
						Log.d(TAG, "==========MSG_INSTALL_APP_FINISHED===========");
					}
					result = msg.arg1;
					if (result == 1) {
						mInstallAppStatus.setText("Install App Succeed!");
					} else {
						mInstallAppStatus.setText("Install App Failed!");
					}
					break;
					
				case MSG_UNINSTALL_APP_FINISHED:
					if (Debug.debug()) {
						Log.d(TAG, "==========MSG_UNINSTALL_APP_FINISHED===========");
					}
					result = msg.arg1;
					if (result == 1) {
						mUninstallAppStatus.setText("Uninstall App Succeed!");
					} else {
						mUninstallAppStatus.setText("Uninstall App Succeed!");
					}
					break;
					
				default:
					Log.e(TAG, "handle a unknown message. msg.what=" + msg.what);
					break;
			}
		}
	};
	
	private void regAdPlayer() {
		try {
			Log.d(TAG, "====================regAdPlayer====================");
			mAdPlayerManagerService.regBootAdPlayer("dangbei", "com.reako.bootad", "com.reako.bootad.AdActivity");
			mAdPlayerManagerService.regAppStartAdPlayer("dangbei", "com.reako.appstartad", "com.reako.appstartad.AppStartAdActivity");
			mAdPlayerManagerService.setActiveAdPlayerName("dangbei");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void startTest() {
		
		String pkgName = "com.avatar.ott.ott_test";
		PackageManager pm = mContext.getPackageManager();
		
		Intent intent = new Intent("android.intent.action.MAIN");
		intent.addCategory("android.intent.category.LAUNCHER");
		List<ResolveInfo> appList = pm.queryIntentActivities(intent, 0);
		if ((appList != null) && (appList.size() > 0)) {
			for (ResolveInfo info : appList) {
				if ((info.activityInfo != null) && (info.activityInfo.packageName != null)) {
					if (info.activityInfo.packageName.equals(pkgName)) {
						startActivity(pkgName, info.activityInfo.name);
						break;
					}
				}
			}
		}
	}
	
	private void startActivity(String pkgName, String className) {
		ComponentName c = new ComponentName(pkgName, className);
		Intent i = new Intent();
		i.setComponent(c);
		i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		try {
			mContext.startActivity(i);
		} catch (Exception e) {
			Log.e(TAG, "=======start App Failled====== message=" + e.getMessage());
			e.printStackTrace();
		}
	}
	
	private void setBtnVideoPlayListener() {
		mBtnVideoPlay.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mAdPlayerManagerService != null) {
					try {
						mAdPlayerManagerService.displayMediaPlayAd();
					} catch (RemoteException e) {
						e.printStackTrace();
					}
				}
			}
		});
	}
}
