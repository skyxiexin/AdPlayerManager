package com.reako.adplayermanager.net;

import android.util.Log;

public class HttpTaskResult {
	public static final int ERR_CODE_SUCCEED = 1;
	
	public static final int ERR_CODE_CONNECT_TIME_OUT = 2;
	
	public static final int ERR_CODE_READ_TIME_OUT = 3;
	
	public static final int ERR_CODE_URL_NULL = 4;
	
	public static final int ERR_CODE_OTHERS = 5;

	
	public int mErrCode = ERR_CODE_OTHERS;
	
	public byte[] mRequestBuff = null;
	
	public void dumpErrCode() {
		String ret = null;
		switch (mErrCode) {
			case ERR_CODE_SUCCEED:
				ret = "Succeed";
				break;
				
			case ERR_CODE_CONNECT_TIME_OUT:
				ret = "connect time out";
				break;
				
			case ERR_CODE_READ_TIME_OUT:
				ret = "read time out";
				break;
				
			case ERR_CODE_URL_NULL:
				ret = "url is null";
				break;
				
			case ERR_CODE_OTHERS:
				ret = "others";
				break;
				
			default:
				ret = "unknown";
				break;
		}
		
		Log.i("HttpTaskResult", ret);
	}
}
