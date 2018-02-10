package com.reako.adplayermanager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.os.Environment;
import android.os.storage.StorageManager;

public class Uitls {
	public static String getMac() {
		String ret = null;
		
		FileInputStream fis = null;
		try {
			byte[] buffer = new byte[20];
			File file = new File("/sys/class/net/eth0/address");
			fis = new FileInputStream(file);
			fis.read(buffer);
			ret = buffer.toString();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (fis != null) {
				try {
					fis.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		
		return ret;
	}
	
	
	public static long getTotalRam() {
		String path = "/proc/meminfo";
	    String firstLine = null;
	    long totalRam = 0 ;
	    try{
	        FileReader fileReader = new FileReader(path);
	        BufferedReader br = new BufferedReader(fileReader,8192);
	        firstLine = br.readLine().split("\\s+")[1];
	        br.close();
	        fileReader.close();
	    }catch (Exception e){
	        e.printStackTrace();
	    }
	    if (firstLine != null) {
	    	totalRam = Long.valueOf(firstLine);
	    }
	    
	    return totalRam;
	}
	
	public static long getTotalRom() {
		return 8 * 1024 * 1024;
	}
	
	public static long getAvailRom() {
		File ov = new File("/data/data/");
		long o = ov.getFreeSpace() / 1024;
		return o;
	}
	
	
	private static long getSDCardsize() {
		
	}
	
	public static List<String> getExteralStorage(Context context) {
		ArrayList <String> ret = new ArrayList <String> ();
		StorageManager sm = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
		Class<?> storageVolumeClazz = null; 
		try {
			storageVolumeClazz = Class.forName("android.os.storage.StorageVolume");
			Method getVolumeList = sm.getClass().getMethod("getVolumeList");
			Method getPath = storageVolumeClazz.getMethod("getPath");
			Method isRemovable = storageVolumeClazz.getMethod("isRemovable");
			Object result = getVolumeList.invoke(sm);
			final int length = Array.getLength(result);
			for (int i = 0; i < length; i++) {
				Object storageVolumeElement = Array.get(result, i);
				String path = (String) getPath.invoke(storageVolumeElement);
				boolean removable = (Boolean) isRemovable.invoke(storageVolumeElement);
				if (removable) {
					ret.add(path);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ret;
	}
	
	public static boolean isExteralStorageMounted() {
		return Environment.getExternalStorageState().contains(Environment.MEDIA_MOUNTED);
	}
}
