package com.reako.adplayermanager;

import java.io.File;
import java.lang.reflect.Method;

import android.content.Context;
import android.content.pm.IPackageDeleteObserver;
import android.content.pm.IPackageInstallObserver;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

public class MyPackageInstallser {
	private static final String TAG = "MyPackageInstallser";
	public static final int INSTALL_REPLACE_EXISTING = 0x00000002;
	
	public void install(Context context, File pkg, IPackageInstallObserver installerListener) {
		InstallerTask task = new InstallerTask();
		task.context = context;
		task.appFile = pkg;
		task.installObserver = installerListener;
		task.type = 1;
		task.execute("in");
	}
	
	private void installapp(Context context, File pkg, IPackageInstallObserver installerListener) {
		PackageManager pm = context.getPackageManager();
		try {
			Class<?> cls = Class.forName(PackageManager.class.getName()); 
			Method installMethod = cls.getMethod("installPackage", 
					Uri.class, 
					IPackageInstallObserver.class, 
					int.class, 
					String.class);
			Object obj = new Object();
			Uri pkgURI = Uri.fromFile(pkg);
			installMethod.invoke(pm, pkgURI, installerListener, INSTALL_REPLACE_EXISTING, null);
		} catch (Exception e) {
			Log.e(TAG, "=========" + e.getMessage() + "=========");
			e.printStackTrace();
		}
	}
	
	
	public void uninstall(Context context, String pkgName, IPackageDeleteObserver uninstallerListener) {
		InstallerTask task = new InstallerTask();
		task.context = context;
		task.pkgName = pkgName;
		task.uninstallObserver = uninstallerListener;
		task.type = 2;
		task.execute("un");
	}
	
	private void uninstallApp(Context context, String pkgName, IPackageDeleteObserver uninstallerListener) {
		PackageManager pm = context.getPackageManager();
		try {
			Class<?> cls = Class.forName(PackageManager.class.getName()); 
			Method installMethod = cls.getMethod("deletePackage", 
					String.class, 
					IPackageDeleteObserver.class, 
					int.class);
			installMethod.invoke(pm, pkgName, uninstallerListener, 0);
		} catch (Exception e) {
			Log.e(TAG, "=========" + e.getMessage() + "=========");
			e.printStackTrace();
		}
	}


	public class InstallerTask extends AsyncTask {
		public Context context = null;
		public File appFile = null;
		public IPackageInstallObserver installObserver = null;
		public IPackageDeleteObserver uninstallObserver = null;
		public String pkgName = null;
		public int type = 0;
		

		@Override
		protected Object doInBackground(Object... params) {
			if (type == 1) {
				installapp(context, appFile, installObserver);
			} else if (type == 2) {
				uninstallApp(context, pkgName, uninstallObserver);
			}
			return null;
		}
		
		
		
	}
	
}
