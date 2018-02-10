package com.reako.adplayermanager.inter;

import com.reako.adplayermanager.IAdPlayerManagerService;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;

public abstract class AdPlayerActivity extends Activity{
	private IAdPlayerManagerService mAdPlayerManagerService = null;
	private ServiceConnection mAdPlayerManagerConnection = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	
	@Override
    protected void onDestroy() {
		unbindAdPlayerManager();
		super.onDestroy();
	}
	 
	protected void bindAdPlayerManager() {
		Intent intent = new Intent("com.reako.adplayermanager.IAdPlayerManagerService");
		bindService(intent, mAdPlayerManagerConnection, Context.BIND_AUTO_CREATE);
	}
		
	protected void unbindAdPlayerManager() {
		if (mAdPlayerManagerService !=  null) {
			unbindService(mAdPlayerManagerConnection);
		}
	}
		
	protected void initAdPlayerManagerConnection() {
		mAdPlayerManagerConnection = new ServiceConnection() {
			
			@Override
			public void onServiceDisconnected(ComponentName name) {
				// TODO Auto-generated method stub
				mAdPlayerManagerService = null;
			}
				
			@Override
			public void onServiceConnected(ComponentName name, IBinder service) {
				mAdPlayerManagerService = IAdPlayerManagerService.Stub.asInterface(service);
			}
		};
	}
		
	public boolean isAdPlayerManagerConnected() {
		return (mAdPlayerManagerService != null);
	}
	
	public void onAdFinished(int type, int errCode, String message) {
		if (isAdPlayerManagerConnected()) {
			try {
				mAdPlayerManagerService.onAdFinished(type, errCode, message);
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
