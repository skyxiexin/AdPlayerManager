package com.reako.adplayermanager;

public class Debug {
	public static final boolean  DEBUG = true;
	private static final String DEBUG_PROPERTY = "persist.sys.apms.debug";
	
	/**
	 * 调试开关，返回true表示打开
	 */
	public static boolean debug() {
		if ((DEBUG) || getDebugProp()) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * 从Android property中获取persist.sys.bootad.debug的值
	 * @return true表示persist.sys.bootad.debug=1
	 *         false表示其他
	 */
	private static boolean getDebugProp() {
		boolean ret = false;
		String val = System.getProperty(DEBUG_PROPERTY, "0");
		if ("1".equals(val)) {
			ret = true;
		}
		
		return ret;
	}
}
