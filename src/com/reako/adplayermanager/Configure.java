package com.reako.adplayermanager;

import java.util.ArrayList;
import java.util.List;

import com.reako.adplayermanager.player.PlayerViewParam;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class Configure {
	private static final String TAG = "AdPlayerManager.Configure";
	
	
	private static final String PERFER_FILE_NAME = "configure";
	
	
	private static final String KEY_USER_ID = "UserID";
	
	private static final String KEY_ACTIVE_AD = "ActivePlayer";
	
	private static final String KEY_SPLASH_CONFIG = "SplashConfig";
	
	private static final String KEY_VIDEO_CONFIG = "VideoConfig";
	
	private static final String APP_ID = "Reako.AdManager";
	private static final String SUB_ID = "0.0.1";
	
	
	private static Configure myConfig = null;
	private Context mContext = null;
	private Editor mEditor = null; 
	
	private SharedPreferences mPreferences = null;
	
	private PlayersDB mPlayersDB = null;
	
	private Configure(Context context) {
		mContext = context;
		mPreferences = context.getSharedPreferences(PERFER_FILE_NAME, Context.MODE_PRIVATE);
		mEditor = mPreferences.edit();
		mPlayersDB = new PlayersDB(mContext, PlayersDB.DB_FILE_NAME, null, PlayersDB.DATABASE_VERSION);
	}

	public static Configure getInstant(Context context) {
		if (myConfig == null) {
			myConfig = new Configure(context);
		}
		
		return myConfig;
	}
	
	public String getSplashConfig() {
		return mPreferences.getString(KEY_SPLASH_CONFIG, null);
	}
	
	public void setSplashConfig(String val) {
		mEditor.putString(KEY_SPLASH_CONFIG, val);
		mEditor.commit();
	}
	
	public String getVideoConfig() {
		return mPreferences.getString(KEY_VIDEO_CONFIG, null);
	}
	
	public void setVideoConfig(String val) {
		mEditor.putString(KEY_VIDEO_CONFIG, val);
		mEditor.commit();
	}
	
	public String getAvatarServerAddr() {
		String ret = null;
		return ret;
	}
	
	public void setUserID(String id) {
		mEditor.putString(KEY_USER_ID, id);
		mEditor.commit();
	}
	
	public String getUserID() {
		return mPreferences.getString(KEY_USER_ID, null);
	}
	
	public String getActiveAd() {
		return mPreferences.getString(KEY_ACTIVE_AD, null);
	}
	
	public void setActiveAd(String playerName) {
		mEditor.putString(KEY_ACTIVE_AD, playerName);
		mEditor.commit();
	}
	
	public void addAdPlayer(PlayerViewParam param) {
		mPlayersDB.addPlayer(param);
	}
	
	public PlayerViewParam getPlayerViewParam(String playerName) {
		return mPlayersDB.getPlayer(playerName);
	}
	
	public void removePlayer(String playerName) {
		mPlayersDB.deletePlayer(playerName);
	}
	
	public List<PlayerViewParam> getAllPlayer() {
		return mPlayersDB.getAllPlayer();
	}
	
	
	public class PlayersDB extends SQLiteOpenHelper {
		
		public static final int DATABASE_VERSION = 1;
		public static final String DB_FILE_NAME = "config.db";
		public static final String PLAYERS_TABLE = "players_table";
		/**
		 * 广告Player的名称
		 */
		public static final String CLUMN_PLAYER_NAME = "name";
		
		public static final String CLUMN_PLAYER_PARAM = "palyer_param";
		
		/**
		 * 数据库表创建语句
		 */
		public static final String CREATE_PLAYERS_TABLE = "CREATE TABLE "
				+ PLAYERS_TABLE + "("
				+ "id" + " INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ CLUMN_PLAYER_NAME + " TEXT, "
				+ CLUMN_PLAYER_PARAM + " TEXT "
				+ " )";
		
		public static final String DELETE_PLAYER_TABLE = "DROP TABLE IF EXISTS "
				+ PLAYERS_TABLE;

		public PlayersDB(Context context, String name,
				CursorFactory factory, int version) {
			super(context, DB_FILE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			try {
				if(Debug.debug()) {
					Log.d(TAG, "============create table in DB==============");
				}
				db.execSQL(CREATE_PLAYERS_TABLE);
			} catch (Exception e) {
				Log.e(TAG, e.getMessage());
				e.printStackTrace();
			}
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			try {
				db.execSQL(DELETE_PLAYER_TABLE);
				onCreate(db);
			} catch (Exception e) {
				Log.e(TAG, e.getMessage());
				e.printStackTrace();
			}
		}
		
		
		public PlayerViewParam getPlayer(String playerName) {
			PlayerViewParam ret = null;
			String[] columns = new String[] {
				"id", CLUMN_PLAYER_NAME, 
				CLUMN_PLAYER_PARAM};
			
			final String selection = CLUMN_PLAYER_NAME + "=?";
			final String[] selectionArgs = {playerName};
			try {
				SQLiteDatabase db = this.getWritableDatabase();
				if (db.isOpen()) {
					Cursor cursor = db.query(PLAYERS_TABLE, 
							columns, selection, 
							selectionArgs, null, null, null);
					
					if ((cursor != null) && (cursor.moveToFirst())) {
						ret = new PlayerViewParam();
						cursorToParam(ret, cursor);
					}
					cursor.close();
				}
				db.close();
			} catch (Exception e) {
				Log.e(TAG, e.getMessage());
				e.printStackTrace();
			}
			
			return ret;
		}
		
		private void cursorToParam(PlayerViewParam param, Cursor cursor) {
			String player_param = cursor.getString(cursor.getColumnIndex(CLUMN_PLAYER_PARAM));
			param.fromString(player_param);
		}
		
		
		public void addPlayer(PlayerViewParam param) {
			String[] columns = new String[] {
				"id", CLUMN_PLAYER_NAME, CLUMN_PLAYER_PARAM };
			
			final String selection = CLUMN_PLAYER_NAME + "=?";
			final String[] selectionArgs = {param.mAdPlayerName};
			try {
				SQLiteDatabase db = this.getWritableDatabase();
				if (db.isOpen()) {
					Cursor cursor = db.query(PLAYERS_TABLE, 
							columns, selection, 
							selectionArgs, null, null, null);
					
					if ((cursor != null) && (cursor.moveToFirst())) {
						//update player.
						String whereClause = CLUMN_PLAYER_NAME + "=?";
						String[] whereArgs = {param.mAdPlayerName};
						ContentValues values = paramToValues(param);
						db.update(PLAYERS_TABLE, values, whereClause, whereArgs);
					} else {
						//insert player.
						db.insert(PLAYERS_TABLE, null, paramToValues(param));
					}
					cursor.close();
				}
				db.close();
			} catch (Exception e) {
				Log.e(TAG, e.getMessage());
				e.printStackTrace();
			}
		}
		
		private ContentValues paramToValues(PlayerViewParam param) {
			ContentValues values = new ContentValues();
			values.put(CLUMN_PLAYER_NAME, param.mAdPlayerName);
			values.put(CLUMN_PLAYER_PARAM, param.toString());
			return values;
		}
		
		public void deletePlayer(String playerName) {
			try {
				SQLiteDatabase db = this.getWritableDatabase();
				if (db.isOpen()) {
					String whereClause = CLUMN_PLAYER_NAME + "=?";
					String[] whereArgs = {playerName};
					db.delete(PLAYERS_TABLE, whereClause, whereArgs);
				}
				db.close();
			} catch (Exception e) {
				Log.e(TAG, e.getMessage());
				e.printStackTrace();
			}
		}
		
		public List<PlayerViewParam> getAllPlayer() {
			List<PlayerViewParam> ret = new ArrayList<PlayerViewParam>();
			String[] columns = new String[] {
					"id", CLUMN_PLAYER_NAME, 
					CLUMN_PLAYER_PARAM };
				try {
					SQLiteDatabase db = this.getWritableDatabase();
					if (db.isOpen()) {
						Cursor cursor = db.query(PLAYERS_TABLE, 
								columns, null, 
								null, null, null, null);
						
						if ((cursor != null) && (cursor.moveToFirst())) {
							PlayerViewParam item = new PlayerViewParam();
							cursorToParam(item, cursor);
							ret.add(item);
						}
						cursor.close();
					}
					db.close();
				} catch (Exception e) {
					Log.e(TAG, e.getMessage());
					e.printStackTrace();
				}
			return ret;
		}
		
	}
	
	public String getAppID() {
		String ret = APP_ID;
		String val = System.getProperty("ro.adplayer.appid", null);
		if (val != null) {
			ret = val;
		}
		
		return ret;
	}
	
	
	public String getSubID() {
		String ret = SUB_ID;
		String val = System.getProperty("ro.adplayer.subid", null);
		if (val != null) {
			ret = val;
		}
		
		return ret;
	}
}
