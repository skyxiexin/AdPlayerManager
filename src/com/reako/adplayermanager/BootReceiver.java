package com.reako.adplayermanager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootReceiver extends BroadcastReceiver{

	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
			//start manager service
			try {
				Intent i = new Intent();
				i.setAction("com.reako.adplayermanager.start");
				context.startService(i);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

}
