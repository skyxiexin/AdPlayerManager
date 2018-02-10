package com.reako.adplayermanager;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.json.JSONObject;

import com.avatar.ott.sdk.android.keep.Settings;
import com.avatar.ott.sdk.cross.base.SharedConstants;
import com.avatar.ott.sdk.cross.interfaces.HttpDataReceiver;
import com.avatar.ott.sdk.installer.cross.models.infos.AppInfo;
import com.avatar.ott.sdk.installer.cross.models.infos.ChangeableDeviceInfo;
import com.avatar.ott.sdk.installer.cross.models.infos.CmdInfo;
import com.avatar.ott.sdk.installer.cross.models.infos.CmdRunStatusInfo;
import com.avatar.ott.sdk.installer.cross.models.infos.FixedDeviceInfo;
import com.avatar.ott.sdk.installer.cross.models.paras.GetCmd_Para;
import com.avatar.ott.sdk.installer.cross.models.paras.GetCmd_ResultPara;
import com.avatar.ott.sdk.installer.cross.models.paras.Register_Para;
import com.avatar.ott.sdk.manager.cross.models.infos.SdkConfigInfo;
import com.avatar.ott.sdk.manager.cross.models.infos.SdkInfo;
import com.avatar.ott.sdk.manager.cross.models.paras.UpdateSdkConfig_Para;
import com.avatar.ott.sdk.manager.cross.models.paras.UpdateSdkConfig_ResultPara;
import com.avatar.ott.sdk.cross.interfaces.HttpParaGetter;
import com.avatar.ott.sdk.analyzer.interfaces.AdListener;
import com.avatar.ott.sdk.analyzer.interfaces.AppListener;
import com.avatar.ott.sdk.analyzer.interfaces.TvListener;

import com.reako.adplayermanager.net.HttpGetTask;
import com.reako.adplayermanager.net.HttpTaskResult;
import com.reako.adplayermanager.net.OnHttpTaskFinished;
import com.reako.adplayermanager.player.PlayerViewParam;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageDeleteObserver;
import android.content.pm.IPackageInstallObserver;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.WindowManager;

public class AdPlayerManagerService extends Service {
	
	private static final String TAG = "AdPlayerManagerService";
	
	private Context mContext = null;
	
	private Map <String, PlayerViewParam> mPlayerMaps = null;
	private String mActivePlayerName = null;
	private String mAppStartPkgName = null;
	private String mAppStartClassName = null;
	
	//for Avatar AD sdk
	private Settings mAvatarSettings = null;
	private HttpParaGetter mHttpParaGetter = null;
	private HttpDataReceiver mHttpDataReceiver = null;
	private SdkConfigInfo mSplashConfig = null;
	private SdkConfigInfo mVideoConfig = null;
	private  AdListener mAdListener = null;
	private AppListener mAppListener = null;
	private TvListener mTvListener = null;
	private List<CmdInfo> mUpdateList = new ArrayList<CmdInfo>();
	private ArrayList<CmdRunStatusInfo> mCmdRunStatusList = new ArrayList<CmdRunStatusInfo>();
	
	private static final int MSG_CHECK_UPDAYTE = 1;
	private static final int MSG_START_DOWNLOAD = 2;
	private static final int MSG_DOWNLOAD_FINISHED =3;
	private static final int MSG_START_INSTALL = 4;
	private static final int MSG_INSTALL_FINISHED = 5;
	private static final int MSG_START_UNINSTALL = 6;
	private static final int MSG_UNINSTALL_FINISHED = 7;
	private static final int MSG_RUN_CMDS = 8;
	private static final int MSG_RUN_CMDS_FINISHED = 9;
	
	
	
	private static final int MODE_SPLASH = 1;
	private static final int MODE_VIDEO = 2;
	
	
	

	@Override
	public IBinder onBind(Intent intent) {
		return new AdPlayerManagerServiceImpl();
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		mContext = this.getApplicationContext();
		initADPlayerMaps();
		initHttpParaGetter();
		initHttpDataReceiver();
		initAvatarSettings();
		initAdPlayerConfig();
		initReceiver();
		sendPlayerRegisterBroadcast();
		reportSystemTurnOn();
	}
	
	public class AdPlayerManagerServiceImpl extends IAdPlayerManagerService.Stub {
		@Override
		public void onAdFinished(int type, int errCode, String message)
				throws RemoteException {
			//TODO report message to server.
			if (type == PlayerViewParam.TYPE_START_APP) {
				startActivity(mAppStartPkgName, mAppStartClassName);
			}
		}

		@Override
		public void displayBootAd() throws RemoteException {
			updateActiveSplashPlayer();
			if ((mActivePlayerName != null) && (mPlayerMaps != null) && (mPlayerMaps.size() > 0)) {
				if (mPlayerMaps.containsKey(mActivePlayerName)) {
					PlayerViewParam param = mPlayerMaps.get(mActivePlayerName);
					startActivity(param.mBootAdPkgName, param.mBootAdActivityName);
				} else {
					Log.e(TAG, "==============The Active player name is failed============mActivePlayerName=" + mActivePlayerName);
					updateActiveSplashPlayer();
					PlayerViewParam param = mPlayerMaps.get(mActivePlayerName);
					startActivity(param.mBootAdPkgName, param.mBootAdActivityName);
				}
			} else {
				Log.d(TAG, "Can not filte the Boot AD");
			}
		}

		@Override
		public void displayAppStartAd(String pkg, String className)
				throws RemoteException {
			// TODO Auto-generated method stub
			boolean displayAd = false;
			updateActiveSplashPlayer();
			if ((mActivePlayerName != null) && (mPlayerMaps != null) && (mPlayerMaps.size() > 0)) {
				if (mPlayerMaps.containsKey(mActivePlayerName)) {
					PlayerViewParam param = mPlayerMaps.get(mActivePlayerName);
					mAppStartPkgName = pkg;
					mAppStartClassName = className;
					startAppAdActivity(param.mAppStartAdPkgName, param.mAppStartAdActivityName, pkg, className);
					displayAd = true;
					reportAppOpen(pkg);
				} else {
					Log.e(TAG, "========display app start failed because mActivePlayerName is failed. mActivePlayerName=" + mActivePlayerName);
					updateActiveSplashPlayer();
					PlayerViewParam param = mPlayerMaps.get(mActivePlayerName);
					mAppStartPkgName = pkg;
					mAppStartClassName = className;
					startAppAdActivity(param.mAppStartAdPkgName, param.mAppStartAdActivityName, pkg, className);
					displayAd = true;
					reportAppOpen(pkg);
				}
			}
			
			if (!displayAd) {
				startActivity(pkg, className);
			}
		}

		@Override
		public void displayMediaPlayAd() throws RemoteException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void displayMediaPauseAd() throws RemoteException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void regBootAdPlayer(String playerName, String pkgName, String className)
				throws RemoteException {
			if ((playerName == null)
					|| (pkgName == null)
					|| (className == null)) {
					return;
				}
				
				PlayerViewParam param = null;
				boolean update = false;
				if ((mPlayerMaps != null) && (mPlayerMaps.size() > 0)) {
					if (mPlayerMaps.containsKey(playerName)) {
						param = mPlayerMaps.get(playerName);
						param.mBootAdPkgName = pkgName;
						param.mBootAdActivityName = className;
						update = true;
					}
				}
				if (!update) {
					param = new PlayerViewParam();
					param.mAdPlayerName = playerName;
					param.mBootAdPkgName = pkgName;
					param.mBootAdActivityName = className;
				}
				Log.d(TAG, "===================Add boot ad player==================");
				mPlayerMaps.put(playerName, param);
				Configure.getInstant(mContext).addAdPlayer(param);
		}

		@Override
		public void regAppStartAdPlayer(String playerName, String pkgName, String className)
				throws RemoteException {
			if ((playerName == null)
				|| (pkgName == null)
				|| (className == null)) {
				return;
			}
			
			PlayerViewParam param = null;
			boolean update = false;
			if ((mPlayerMaps != null) && (mPlayerMaps.size() > 0)) {
				if (mPlayerMaps.containsKey(playerName)) {
					param = mPlayerMaps.get(playerName);
					param.mAppStartAdPkgName = pkgName;
					param.mAppStartAdActivityName = className;
					update = true;
				}
			}
			if (!update) {
				param = new PlayerViewParam();
				param.mAdPlayerName = playerName;
				param.mAppStartAdPkgName = pkgName;
				param.mAppStartAdActivityName = className;
			}
			Log.d(TAG, "===================Add app start player==================");
			mPlayerMaps.put(playerName, param);
			Configure.getInstant(mContext).addAdPlayer(param);
		}

		@Override
		public void regMediaPlayAdPlayer(String playerName, String pkgName, String className)
				throws RemoteException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void regMediaPauseAdPlayer(String playerName, String pkgName, String className)
				throws RemoteException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void setActiveAdPlayerName(String playerName)
				throws RemoteException {
			if (playerName != null) {
				mActivePlayerName = playerName;
				Configure.getInstant(mContext).setActiveAd(playerName);
			}
		}

		@Override
		public void onTvChangeChannel(String oldChannelName,
				String newChannelName) throws RemoteException {
			reportTvChangeChannel(oldChannelName, newChannelName);
		}

		@Override
		public void onKeyDown(int keyCode, String pkgName, int type) throws RemoteException {
			
			if (mAdListener != null) {
				if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
					mAdListener.onOkPressed(pkgName, type);
				}
				
				if (keyCode == KeyEvent.KEYCODE_BACK) {
					mAdListener.onReturnPressed(pkgName, type);
				}
			}
		}
		
		
	}
	
	private void startActivity(String pkgName, String className) {
		if (Debug.debug()) {
			Log.d(TAG, "================Start Activity==============" + pkgName + "/" + className);
		}
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
	
	private void startAppAdActivity(String pkgName, String className, String appPkgName, String appClsName) {
		if (Debug.debug()) {
			Log.d(TAG, "================Start Activity==============" + pkgName + "/" + className);
		}
		ComponentName c = new ComponentName(pkgName, className);
		Intent i = new Intent();
		i.putExtra("AppPackName", appPkgName);
		i.putExtra("AppActivityName", appClsName);
		i.setComponent(c);
		i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		try {
			mContext.startActivity(i);
		} catch (Exception e) {
			Log.e(TAG, "=======start App Failled====== message=" + e.getMessage());
			e.printStackTrace();
		}
	}
	
	private void initADPlayerMaps() {
		mPlayerMaps = new HashMap <String, PlayerViewParam>();
		List<PlayerViewParam> list = Configure.getInstant(mContext).getAllPlayer();
		if ((list != null) && (list.size() > 0)) {
			mPlayerMaps.clear();
			for (PlayerViewParam item: list) {
				mPlayerMaps.put(item.mAdPlayerName, item);
			}
		}
		
		mActivePlayerName = Configure.getInstant(mContext).getActiveAd();
	}
	
	private Handler mMainHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case MSG_CHECK_UPDAYTE:
					if (Debug.debug()) {
						Log.d(TAG, "==========MSG_CHECK_UPDAYTE==========");
					}
					break;
					
				case MSG_START_DOWNLOAD:
					if (Debug.debug()) {
						Log.d(TAG, "==========MSG_START_DOWNLOAD==========");
					}
					dealDownloadMessage();
					break;
					
				case MSG_DOWNLOAD_FINISHED:
					if (Debug.debug()) {
						Log.d(TAG, "==========MSG_DOWNLOAD_FINISHED==========");
					}
					dealDownloadFinishedMsg(msg);
					break;
					
				case MSG_START_INSTALL:
					if (Debug.debug()) {
						Log.d(TAG, "==========MSG_START_INSTALL==========");
					}
					break;
					
				case MSG_INSTALL_FINISHED:
					if (Debug.debug()) {
						Log.d(TAG, "==========MSG_INSTALL_FINISHED==========");
					}
					dealInstallFinishedMsg(msg);
					break;
					
				case MSG_START_UNINSTALL:
					if (Debug.debug()) {
						Log.d(TAG, "==========MSG_START_UNINSTALL==========");
					}
					dealUninstallMsg();
					break;
					
				case MSG_UNINSTALL_FINISHED:
					if (Debug.debug()) {
						Log.d(TAG, "==========MSG_UNINSTALL_FINISHED==========");
					}
					dealUninstallFinishMsg(msg);
					break;
					
				case MSG_RUN_CMDS:
					if (Debug.debug()) {
						Log.d(TAG, "==========MSG_RUN_CMDS==========");
					}
					break;
					
				case MSG_RUN_CMDS_FINISHED:
					if (Debug.debug()) {
						Log.d(TAG, "==========MSG_RUN_CMDS_FINISHED==========");
					}
					break;
					
				default:
					Log.e(TAG, "Error message. message.what=" + msg.what);
					break;
			}
		}
	};
	
	private void initHttpParaGetter() {
		mHttpParaGetter = new com.avatar.ott.sdk.cross.interfaces.HttpParaGetter() {

			@Override
			public GetCmd_Para getCmdPara() {
				Log.d(TAG, "=============getCmdPara==============");
				ChangeableDeviceInfo info = new ChangeableDeviceInfo();
				info.setAvailRomMemory(5 * 1024 * 1024);
				info.setAvailSDMemory(0);
				info.setBuildSdkVersion(Build.VERSION.SDK_INT);
				info.setLanguage("eng");
				info.setTotalSDMemory(0);
				
				GetCmd_Para para = new GetCmd_Para();
				para.setAppInfos(getAppInfo());
				para.setChangeableDeviceInfo(info);
				return para;
			}

			@Override
			public Register_Para getRegisterPara() {
				Log.d(TAG, "=============getRegisterPara==============");
				Register_Para para = new Register_Para();
				para.setFixedDeviceInfo(getDeviceInfo());
				return para;
			}

			@Override
			public UpdateSdkConfig_Para getUpdateSdkPara() {
				// TODO Auto-generated method stub
				return new UpdateSdkConfig_Para();
			}
			
		};
	}
	
	private void initHttpDataReceiver() {
		mHttpDataReceiver = new HttpDataReceiver() {

			@Override
			public void onReceivedGetCmdResult(GetCmd_ResultPara resultPara) {
				Log.d(TAG, "=============onReceivedGetCmdResult==============");
				if (resultPara != null) {
					List<CmdInfo> cmdInfos = resultPara.getCmdInfos();
	                if (cmdInfos != null) {
	                    for (CmdInfo cmdInfo : cmdInfos) {
	                        if (cmdInfo != null) {
	                            Log.d(TAG,"cmdInfo="+cmdInfo.toString());
	                            mUpdateList.add(cmdInfo);
	                        }
	                    }
	                    dealNextUpdateItem();
	                }
				}
			}

			@Override
			public void onReceivedUpdateSdkConfigResult(
					UpdateSdkConfig_ResultPara result) {
				if (Debug.debug()) {
					Log.d(TAG, "=========onReceivedUpdateSdkConfigResult=========");
				}
				
				if (result != null) {
					mSplashConfig = result.getSplashConfig();
					mVideoConfig = result.getVideoConfig();
					
					//save it
					Configure.getInstant(mContext).setSplashConfig(mSplashConfig.toJsonObject().toString());
					Configure.getInstant(mContext).setVideoConfig(mVideoConfig.toJsonObject().toString());
				}
				
				if (Debug.debug()) {
					Log.d(TAG, "=========mSplashConfig" + mSplashConfig.toJsonObject().toString() + "=========");
					Log.d(TAG, "=========mVideoConfig" + mVideoConfig.toJsonObject().toString() + "=========");
				}
			}
			
		};
	}
	
	private void initAvatarSettings() {
		Settings.init(mContext, 
					Configure.getInstant(mContext).getAppID(), 
					Configure.getInstant(mContext).getSubID(), 
					mHttpParaGetter, mHttpDataReceiver);
		mAdListener = Settings.getAdListener();
		mAppListener = Settings.getAppListener();
		mTvListener = Settings.getTvListener();
	}
	
	private List<AppInfo> getAppInfo() {
		List<AppInfo> ret = new ArrayList<AppInfo>();
		PackageManager pm = mContext.getPackageManager();
		List<ApplicationInfo> infos = pm.getInstalledApplications(0);
		for (ApplicationInfo item: infos) {
			AppInfo appInfo = new AppInfo();
			appInfo.setFirstInstallTime(0);
			appInfo.setFlag(item.flags);
			appInfo.setName(item.name);
			appInfo.setPackageName(item.packageName);
			appInfo.setSize(getAppSize(item.packageName));
			ret.add(appInfo);
		}
		return ret;
	}
	
	private long getAppSize(String pkgName) {
		long ret = 0;
		
		return ret;
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
	
	private void dealDownloadMessage() {
		File outputFile = new File("DownloadApps.apk");	
		OnHttpTaskFinished onFinished = new OnHttpTaskFinished() {
			@Override
			public void onHttpTaskFinished(HttpTaskResult result) {
				Message msg = new Message();
				msg.what =MSG_DOWNLOAD_FINISHED;
				if ((result != null) && (result.mErrCode == HttpTaskResult.ERR_CODE_SUCCEED)) {
					msg.arg1 = 1;
				} else {
					msg.arg1 = 2;
				}
				mMainHandler.sendMessage(msg);
			}
			
		};
		HttpGetTask task = new HttpGetTask(mContext, 
				mUpdateList.get(0).getDownloadUrl(), 
				outputFile, onFinished);
		
		task.execute("start");
		if (Debug.debug()) {
			Log.d(TAG, "==========start download " 
					+ mUpdateList.get(0).getDownloadUrl() 
					+ "=================");
		}
	}
	
	private void dealDownloadFinishedMsg(Message msg) {
		if (msg.arg1 == 1) {
			File app = new File("/data/data/com.reako.adplayermanager/files/DownloadApps.apk");
			MyPackageInstallser installer = new MyPackageInstallser();
			MyPackageInstallObserver observer = new MyPackageInstallObserver();
			installer.install(mContext, app, observer);
			if (Debug.debug()) {
				Log.d(TAG, "==========start install " 
						+ mUpdateList.get(0).getPackageName()
						+ "=================");
			}
			reportInstallPkg(mUpdateList.get(0).getPackageName());
		} else {
			CmdRunStatusInfo info = new CmdRunStatusInfo();
			info.setCmdKey(mUpdateList.get(0).getCmdKey());
			info.setInfo("Download App Failed");
			info.setSuccess(false);
			mCmdRunStatusList.add(info);
			mUpdateList.remove(0);
			dealNextUpdateItem();
		}
	}
	
	public class MyPackageInstallObserver extends IPackageInstallObserver.Stub {

		@Override
		public void packageInstalled(String packageName,
				int returnCode) throws RemoteException {
			Message msg = new Message();
			msg.what = MSG_INSTALL_FINISHED;
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
			msg.what = MSG_UNINSTALL_FINISHED;
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
	
	private void dealNextUpdateItem() {
		if (mUpdateList.size() > 0) {
			switch (mUpdateList.get(0).getCmdType()) {
				case CmdInfo.CmdType_Install:
					mMainHandler.sendEmptyMessage(MSG_START_DOWNLOAD);
					break;
					
				case CmdInfo.CmdType_Invalid:
					Log.e(TAG, "============Do nothing=========");
					break;
					
				case CmdInfo.CmdType_RunCmdOnly:
					break;
					
				case CmdInfo.CmdType_Uninstall:
					mMainHandler.sendEmptyMessage(MSG_START_UNINSTALL);
					break;
					
				default:
					break;
			}
		} else {
			if (mCmdRunStatusList.size() > 0) {
				mAvatarSettings.addCmdRunStatusInfos(mCmdRunStatusList);
			}
		}
	}
	
	private void dealInstallFinishedMsg(Message msg) {
		CmdRunStatusInfo info = new CmdRunStatusInfo();
		info.setCmdKey(mUpdateList.get(0).getCmdKey());
		if (msg.arg1 == 1) {
			if (mUpdateList.get(0).isNeedOpen()) {
				startApp(mUpdateList.get(0).getPackageName());
			}
			info.setInfo("Install Succeed");
			info.setSuccess(true);
		} else {
			info.setInfo("Install App Failed");
			info.setSuccess(false);
		}
		
		mCmdRunStatusList.add(info);
		mUpdateList.remove(0);
		dealNextUpdateItem();
	}
	
	
	private void startApp(String pkgName) {
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
	
	private void dealUninstallMsg() {
		MyPackageInstallser uninstaller = new MyPackageInstallser();
		MyPackageUninstallserObserver observer = new MyPackageUninstallserObserver();
		uninstaller.uninstall(mContext, mUpdateList.get(0).getPackageName(), observer);
	}
	
	private void dealUninstallFinishMsg(Message msg) {
		CmdRunStatusInfo info = new CmdRunStatusInfo();
		info.setCmdKey(mUpdateList.get(0).getCmdKey());
		if (msg.arg1 == 1) {
			if (mUpdateList.get(0).isNeedOpen()) {
				startApp(mUpdateList.get(0).getPackageName());
			}
			info.setInfo("Uninstall Succeed");
			info.setSuccess(true);
		} else {
			info.setInfo("Uninstall App Failed");
			info.setSuccess(false);
		}
		
		mCmdRunStatusList.add(info);
		mUpdateList.remove(0);
		dealNextUpdateItem();
	}
	
	private void initAdPlayerConfig() {
		mSplashConfig = getSdkConfigInfoFormSavedConfig(Configure.getInstant(mContext).getSplashConfig());
		mVideoConfig = getSdkConfigInfoFormSavedConfig(Configure.getInstant(mContext).getVideoConfig());
	}
	
	private SdkConfigInfo getSdkConfigInfoFormSavedConfig(String val) {
		SdkConfigInfo ret = null;
		if (val != null) {
			try {
				JSONObject obj = new JSONObject(val);
				ret.fromJsonObject(obj);
			} catch (Exception e) {
				Log.e(TAG, "===========The config parse failed.==========");
				e.printStackTrace();
			}
		}
		return ret;
	}
	
	private void updateActiveSplashPlayer() {
		if (mSplashConfig != null) {
			String sdk_name = getCandidate(mSplashConfig.getType(), mSplashConfig.getSdkInfos(), MODE_SPLASH);
			//The Stub for test.		
			if (Debug.DEBUG) {
				sdk_name = "com.reako.dangbei";
			}
			if (sdk_name != null) {
				Configure.getInstant(mContext).setActiveAd(sdk_name);
			}
		}
	}
	
	private void updateActiveVideoPlayer() {
		if (mSplashConfig != null) {
			String sdk_name = getCandidate(mVideoConfig.getType(), mVideoConfig.getSdkInfos(), MODE_VIDEO);
			
			if (Debug.DEBUG) {
				sdk_name = "com.reako.dangbei";
			}
			
			if (sdk_name != null) {
				Configure.getInstant(mContext).setActiveAd(sdk_name);
			}
		}
	}
	
	private String getCandidate(int type, List<SdkInfo> list, int mode) {
		String ret = null;
		SdkInfo info = null;
		switch (type) {
			case SdkConfigInfo.ShowType_TakeTurnsNormal:
			case SdkConfigInfo.ShowType_TakeTurnsNormalOverlay:
				info = getInfoTurnsNormal(list, mode);
				if (info != null) {
					ret = info.getPackageName();
				}
				break;
			
			case SdkConfigInfo.ShowType_DispatchByWeight:
			case SdkConfigInfo.ShowType_DispatchByWeightOverlay:
				info = getInfoDispatchByWeight(list, mode);
				if (info != null) {
					ret = info.getPackageName();
				}
				break;
				
			case SdkConfigInfo.ShowType_RandomByWeight:
			case SdkConfigInfo.ShowType_RandomByWeightOverlay:
				info = getInfoRandom(list, mode);
				if (info != null) {
					ret = info.getPackageName();
				}
				break;
				
			default:
				Log.e(TAG, "============Can not find this type=======" + type);
				break;
		}
		
		return ret;
	}
	
	private ArrayList<SdkInfo> getAvaildList(List<SdkInfo> list) {
		ArrayList<SdkInfo> ret = new ArrayList<SdkInfo>();
		if ((mPlayerMaps != null) && (mPlayerMaps.size() > 0)) {
			for (SdkInfo info : list) {
				if (mPlayerMaps.containsKey(info.getPackageName())) {
					ret.add(info);
				}
			}
		}
		return ret;
	}
	
	private SdkInfo getInfoTurnsNormal(List<SdkInfo> list, int mode) {
		SdkInfo ret = null;
		if ((list != null) && (list.size() > 0)) {
			ArrayList<SdkInfo> availdList = getAvaildList(list);
			if ((availdList != null) && (availdList.size() > 0)) {
				String activedPkg = Configure.getInstant(mContext).getActiveAd();
				for (SdkInfo info: availdList) {
					if (info.getPackageName().equals(activedPkg)) {
						int index = availdList.indexOf(info);
						if (index < availdList.size() - 1) {
							ret = availdList.get(index + 1);
						} else {
							ret = availdList.get(0);
						}
						break;
					}
				}
			}
		}
		
		return ret;
	}
	
	private SdkInfo getInfoRandom(List<SdkInfo> list, int mode) {
		SdkInfo ret = null;
		if ((list != null) && (list.size() > 0)) {
			ArrayList<SdkInfo> availdList = getAvaildList(list);
			if ((availdList != null) && (availdList.size() > 0)) {
				if (availdList.size() == 1) {
					ret = availdList.get(0);
				} else {
					int random = new Random().nextInt();
					int index = random % availdList.size();
					ret = availdList.get(index);
				}
			}
		}
		
		return ret;
	}
	
	private SdkInfo getInfoDispatchByWeight(List<SdkInfo> list, int mode) {
		boolean isHit = false;
		SdkInfo tmp = new SdkInfo();
		tmp.setWeight(-1);
		if ((list != null) && (list.size() > 0)) {
			for (SdkInfo info : list) {
				//Query this Ad player is installed.
				if ((mPlayerMaps != null) && (mPlayerMaps.size() > 0)) {
					 if (mPlayerMaps.containsKey(info.getPackageName())) {
						 if (info.getWeight() > tmp.getWeight()) {
							 tmp = info;
							 isHit = true;
						 }
					 } else {
						 // This Player not installed.
						 Log.e(TAG, "=========This Player " + info.getPackageName() + " not installed!==========");
					 }
				} else {
					Log.e(TAG, "==============The mPlayerMaps is null==============");
					break;
				}
			}
			
			//update the hit item weight.
			if (isHit) {
				updateSdkInfoList(list, tmp, mode);
			} else {
				Log.e(TAG, "=============Not hit============");
			}
		}
		

		return isHit ? tmp : null;
	}
	
	private void updateSdkInfoList(List<SdkInfo> list, SdkInfo info, int mode) {
		int index = list.indexOf(info);
		SdkInfo new_info = info;
		int weight = info.getWeight();
		if (weight > 0) {
			weight--;
		} else {
			SdkConfigInfo configs = null;
			if (mode == MODE_SPLASH) {
				configs = getSdkConfigInfoFormSavedConfig(Configure.getInstant(mContext).getSplashConfig());
			} else {
				configs = getSdkConfigInfoFormSavedConfig(Configure.getInstant(mContext).getVideoConfig());
			}
			if (configs != null) {
				weight = configs.getSdkInfos().get(index).getWeight();
			} else {
				weight = -1;
			}
		}
		new_info.setWeight(weight);
		list.set(index, new_info);
	}
	
	private void reportSystemTurnOn() {
		if (mTvListener != null) {
			try {
				mTvListener.onTurnOn();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	private void reportSystemTurnOff() {
		if (mTvListener != null) {
			try {
				mTvListener.onTurnOff();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	private void reportTvChangeChannel(String oldChannelName, String newChannelName) {
		if (mTvListener != null) {
			try {
				mTvListener.onChangeChannel(oldChannelName, newChannelName);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	private void reportAdStart(int mode) {
		if (mAdListener != null) {
			if (mode == MODE_SPLASH) {
				mAdListener.onShowAd(Configure.getInstant(mContext).getActiveAd(), SharedConstants.AdType_splash);
			} else {
				mAdListener.onShowAd(Configure.getInstant(mContext).getActiveAd(), SharedConstants.AdType_video);
			}
		}
	}
	
	private void reportInstallPkg(String packageName) {
		if (mAppListener != null) {
			mAppListener.onStartSilentInstall(packageName);
		}
	}
	
	private void reportInstalledPkg(String packageName) {
		if (mAppListener != null) {
			mAppListener.onStartSilentInstall(packageName);
		}
	}
	
	private void reportUninstallPkg(String packageName) {
		if (mAppListener != null) {
			mAppListener.onUninstall(packageName);
		}
	}
	
	private void reportAppOpen(String packageName) {
		if (mAppListener != null) {
			mAppListener.onOpen(packageName);
		}
	}
	
	private void reportAppClose(String packageName) {
		if (mAppListener != null) {
			mAppListener.onClosed(packageName);
		}
	}
	
	public class InstallerReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			String pkgName = null;
			if (Intent.ACTION_PACKAGE_ADDED.equals(intent.getAction())) {
				pkgName = intent.getData().getSchemeSpecificPart();
				reportInstalledPkg(pkgName);
			} else if (Intent.ACTION_PACKAGE_REMOVED.equals(intent.getAction())) {
				pkgName = intent.getData().getSchemeSpecificPart();
				reportUninstallPkg(pkgName);
			} else {
				//No thing to do.
			}
		}
	}
	
	private InstallerReceiver mInstallerReceiver = null;
	
	private void initReceiver() {
		mInstallerReceiver = new InstallerReceiver();
		IntentFilter iFilter = new IntentFilter();
		iFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
		iFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
		registerReceiver(mInstallerReceiver, iFilter);
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		unregisterReceiver(mInstallerReceiver);
	}
	
	private void sendPlayerRegisterBroadcast() {
		if (mContext != null) {
			mContext.sendBroadcast(new Intent("com.reako.register_ad_player"));
		}
	}
}
