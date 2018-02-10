package com.reako.adplayermanager.player;

import org.json.JSONException;
import org.json.JSONObject;

public class PlayerViewParam {
	
	public static final String NULL = "NULL"; 
	
	public String mAdPlayerName = NULL;
	public String mBootAdActivityName = NULL;
	public String mBootAdPkgName = NULL;
	public String mAppStartAdActivityName = NULL;
	public String mAppStartAdPkgName = NULL;
	public String mMediaPlayAdViewName = NULL;
	public String mMediaPauseAdViewName = NULL;
	public String mMediaPopViewName= NULL;
	public String mSysPopViewName = NULL;
	public String mSysUdfView1Name = NULL;
	public String mSysUdfView2Name = NULL;
	public String mSysUdfView3Name = NULL;
	
	
	public static final String AD_PLAYER_NAME = "AdPlayerName";
	public static final String BOOT_ACTIVITY_NAME = "BootAdActivityName";
	public static final String BOOT_PKG_NAME = "BootAdPkgName";
	public static final String APP_START_ACTIVITY_NAME = "AppStartAdActivityName";
	public static final String APP_START_PKG_NAME = "AppStartAdPkgName";
	public static final String MEDIA_PLAY_NAME = "MediaPlayAdViewName";
	public static final String MEDIA_PAUSE_NAME = "MediaPauseAdViewName";
	public static final String MEDIA_POP_NAME = "MediaPopViewName";
	public static final String SYS_POP_NAME = "SysPopViewName";
	public static final String SYS_UDF_1_NAME = "SysUdfView1Name";
	public static final String SYS_UDF_2_NAME = "SysUdfView2Name";
	public static final String SYS_UDF_3_NAME = "SysUdfView3Name";
	
	public static final int TYPE_BOOT_AD = 1;
	public static final int TYPE_START_APP = 2;
	public static final int TYPE_MEDIA_PLAY = 3;
	public static final int TYPE_MEDIA_PAUSE = 4;
	public static final int TYPE_MEDIA_POP = 5;
	public static final int TYPE_SYS_POP = 6;
	
	public boolean isNULL(String value) {
		return NULL.equals(value);
	}
	
	public String toString() {
		return this.toJSONObj().toString();
	}
	
	public JSONObject toJSONObj() {
		JSONObject obj = new JSONObject();
		try {
			obj.put(AD_PLAYER_NAME, mAdPlayerName);
			obj.put(BOOT_ACTIVITY_NAME, mBootAdActivityName);
			obj.put(BOOT_PKG_NAME, mBootAdPkgName);
			obj.put(APP_START_ACTIVITY_NAME, mAppStartAdActivityName);
			obj.put(APP_START_PKG_NAME, mAppStartAdPkgName);
			obj.put(MEDIA_PLAY_NAME, mMediaPlayAdViewName);
			obj.put(MEDIA_PAUSE_NAME, mMediaPauseAdViewName);
			obj.put(MEDIA_POP_NAME, mMediaPopViewName);
			obj.put(SYS_POP_NAME, mSysPopViewName);
			obj.put(SYS_UDF_1_NAME, mSysUdfView1Name);
			obj.put(SYS_UDF_2_NAME, mSysUdfView2Name);
			obj.put(SYS_UDF_3_NAME, mSysUdfView3Name);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return obj;
	}
	
	public PlayerViewParam fromJSONObj(JSONObject obj) {
		try {
			this.mAdPlayerName = obj.getString(AD_PLAYER_NAME);
			this.mBootAdActivityName = obj.getString(BOOT_ACTIVITY_NAME);
			this.mBootAdPkgName = obj.getString(BOOT_PKG_NAME);
			this.mAppStartAdActivityName = obj.getString(APP_START_ACTIVITY_NAME);
			this.mAppStartAdPkgName = obj.getString(APP_START_PKG_NAME);
			this.mMediaPlayAdViewName = obj.getString(MEDIA_PLAY_NAME);
			this.mMediaPauseAdViewName = obj.getString(MEDIA_PAUSE_NAME);
			this.mMediaPopViewName = obj.getString(MEDIA_POP_NAME);
			this.mSysPopViewName = obj.getString(SYS_POP_NAME);
			this.mSysUdfView1Name = obj.getString(SYS_UDF_1_NAME);
			this.mSysUdfView2Name = obj.getString(SYS_UDF_2_NAME);
			this.mSysUdfView3Name = obj.getString(SYS_UDF_3_NAME);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return this;
	}
	
	public PlayerViewParam fromString(String jsonStr) {
		try {
			JSONObject obj = new JSONObject(jsonStr);
			if (obj != null) {
				this.fromJSONObj(obj);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return this;
	}
}
