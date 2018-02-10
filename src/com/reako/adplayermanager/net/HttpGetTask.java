package com.reako.adplayermanager.net;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import com.reako.adplayermanager.Debug;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

public class HttpGetTask extends AsyncTask<String, String, HttpTaskResult> {
	private static final String TAG = "HttpGetTask";
	
	/** 链接池超时*/
	private static final int HTTP_MANAGER_CONNECTION_TIME_OUT = 1000;//1s
	
	/** HTTP连接超时 */
	private static final int HTTP_CONNECT_TIME_OUT = 6 * 1000;//3s
	
	/** HTTP服务器返回超时 */
	private static final int HTTP_READ_TIME_OUT = 3 * 1000;//3s
	
	private static final int MAX_RETRY = 3;
	
	private OnHttpTaskFinished mFinishedListener = null;
	private String mUrl = null;
	private File mOutputFile = null;
	private int BUFF_SIZE = 10 * 1024;
	private Context mContext = null;
	
	
	
	@Override
	protected HttpTaskResult doInBackground(String... params) {
		int retry_times = 0;
		HttpTaskResult result = new HttpTaskResult();
		do {
			if (run(result)) {
				Log.e(TAG, "========HTTP GET Succeed=======");
				break;
			} else {
				retry_times++;
				Log.e(TAG, "========HTTP GET failed. try it again. retry_times=" + retry_times);
			}
		} while (retry_times <= MAX_RETRY);
		if (mFinishedListener != null) {
			mFinishedListener.onHttpTaskFinished(result);
		}
		
		return null;
	}
	
	private boolean run(HttpTaskResult result) {
		boolean ret = false;
		
		try {
			HttpGet httpRequest = new HttpGet(mUrl);
			HttpParams httpParams = new BasicHttpParams();
			HttpProtocolParams.setContentCharset(httpParams, "UTF-8");
			HttpProtocolParams.setVersion(httpParams, HttpVersion.HTTP_1_1);
			ConnManagerParams.setTimeout(httpParams, HTTP_MANAGER_CONNECTION_TIME_OUT);
			HttpConnectionParams.setConnectionTimeout(httpParams, HTTP_CONNECT_TIME_OUT);
			HttpConnectionParams.setSoTimeout(httpParams, HTTP_READ_TIME_OUT);
			HttpClient client = new DefaultHttpClient(httpParams);
			
			if (Debug.debug()) {
				Log.d(TAG, "HTTP POST  URL=" + httpRequest.getURI().toString());
			}
			HttpResponse response = client.execute(httpRequest);
				
			if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				byte[] buffer = new byte[BUFF_SIZE];
				if (mOutputFile.isFile() && mOutputFile.exists()) {
					mOutputFile.delete();
				}
				
				//setEveryoneReadable(mContext, mOutputFile.getAbsolutePath());
				FileOutputStream fos = mContext.openFileOutput(mOutputFile.getPath(), Context.MODE_WORLD_READABLE |  Context.MODE_WORLD_WRITEABLE);
				InputStream is = response.getEntity().getContent();
				int offset = 0;
				int byteCount = 0;
				byteCount = is.read(buffer);
				while (byteCount > 0) {
					fos.write(buffer, 0, byteCount);
					offset += byteCount;
					byteCount = is.read(buffer);
				}
				is.close();
				fos.close();
				ret = true;
				result.mErrCode = HttpTaskResult.ERR_CODE_SUCCEED;
			} else {
				Log.e(TAG, "====HTTP POST=== Handler failed message.  response code=" 
			     + response.getStatusLine().getStatusCode());
				result.mErrCode = HttpTaskResult.ERR_CODE_CONNECT_TIME_OUT;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return ret;
	}
	
	public HttpGetTask(Context context, String url, File outputFile, OnHttpTaskFinished finishedListener) {
		mUrl = url;
		mOutputFile = outputFile;
		mFinishedListener = finishedListener;
		mContext = context;
	}
	
	private void setEveryoneReadable(Context context, String name) {
		try {
			FileOutputStream fos = context.openFileOutput(name, Context.MODE_WORLD_READABLE);
			fos.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
