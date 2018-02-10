package com.reako.adplayermanager;

interface IAdPlayerManagerService {
	void regBootAdPlayer(String playerName, String pkgName, String className);
	void regAppStartAdPlayer(String playerName, String pkgName, String className);
	void regMediaPlayAdPlayer(String playerName, String pkgName, String className);
	void regMediaPauseAdPlayer(String playerName,String pkgName, String className);
	void onAdFinished(int type, int errCode, String message);
	void displayBootAd();
	void displayAppStartAd(String pkg, String className);
	void displayMediaPlayAd();
	void displayMediaPauseAd();
	void setActiveAdPlayerName(String playerName);
	void onTvChangeChannel(String oldChannelName, String newChannelName);
	void onKeyDown(int keyCode, String pkgName, int type);
}