package com.reako.adplayermanager.net;

import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.util.EntityUtils;
import com.reako.adplayermanager.Debug;

import android.os.AsyncTask;
import android.util.Log;

public class HttpPostTask extends AsyncTask<String, String, HttpTaskResult>{
	
	private static final String TAG = "adplayermanager.HttpPostTask";
	
	private OnHttpTaskFinished mFinishedListener = null;
	
	/** 链接池超时*/
	private static final int HTTP_MANAGER_CONNECTION_TIME_OUT = 1000;//1s
	
	/** HTTP连接超时 */
	private static final int HTTP_CONNECT_TIME_OUT = 6 * 1000;//3s
	
	/** HTTP服务器返回超时 */
	private static final int HTTP_READ_TIME_OUT = 3 * 1000;//3s
	
	private static final int MAX_RETRY = 3;
	
	private String mUrl = null;
	
	private List<NameValuePair> mParameters = null;
	

	@Override
	protected HttpTaskResult doInBackground(String... params) {
		HttpTaskResult result = new HttpTaskResult();
		int retry_times = 0;
		do {
			if (run(result)) {
				break;
			} else {
				retry_times++;
			}
		} while (retry_times < MAX_RETRY);
		
		if (mFinishedListener != null) {
			mFinishedListener.onHttpTaskFinished(result);
		}
		return null;
	}
	
	private boolean run(HttpTaskResult result) {
		boolean ret = false;
		try {
			HttpPost httpRequest = new HttpPost(mUrl);
			HttpParams httpParams = new BasicHttpParams();
			HttpProtocolParams.setContentCharset(httpParams, "UTF-8");
			HttpProtocolParams.setVersion(httpParams, HttpVersion.HTTP_1_1);
			ConnManagerParams.setTimeout(httpParams, HTTP_MANAGER_CONNECTION_TIME_OUT);
			HttpConnectionParams.setConnectionTimeout(httpParams, HTTP_CONNECT_TIME_OUT);
			HttpConnectionParams.setSoTimeout(httpParams, HTTP_READ_TIME_OUT);
			HttpClient client = new DefaultHttpClient(httpParams);
			if (mParameters != null) {
				httpRequest.setEntity(new UrlEncodedFormEntity(mParameters, "UTF-8"));
			}
			
			if (Debug.debug()) {
				Log.d(TAG, "HTTP POST  URL=" + httpRequest.getURI().toString());
			}
			HttpResponse response = client.execute(httpRequest);
				
			if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				result.mErrCode = HttpTaskResult.ERR_CODE_SUCCEED;
				result.mRequestBuff = EntityUtils.toString(response.getEntity(), "UTF-8").getBytes();
				ret = true;
			} else {
				Log.e(TAG, "====HTTP POST=== Handler failed message.  response code=" 
					+ response.getStatusLine().getStatusCode());
				result.mErrCode = HttpTaskResult.ERR_CODE_CONNECT_TIME_OUT;
			}
		} catch (Exception e) {
			result.mErrCode = HttpTaskResult.ERR_CODE_CONNECT_TIME_OUT;
			Log.e(TAG, e.getMessage());
			e.printStackTrace();
		}
		
		return ret;
	}

	public HttpPostTask(OnHttpTaskFinished finishedListener) {
		mFinishedListener = finishedListener;
	}
	
	public void setUrl(String url) {
		mUrl = url;
		if (Debug.debug()) {
			Log.d(TAG, "======url=" + url);
		}
	}
	
	public void setParameters(List<NameValuePair> list) {
		mParameters = list;
	}

}
