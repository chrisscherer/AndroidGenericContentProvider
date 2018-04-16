package com.cardinal.mda.Models.DatabaseContracts;

import android.database.Cursor;
import android.net.Uri;

/**
 * Created by Chris Scherer on 4/10/18.
 */
public class BaseDatabaseContract {

	//Unique authority string for the content provider
	public static final String CONTENT_AUTHORITY = "com.cardinal.mda";

	/* Helpers to retrieve column values */
	public static String getColumnString(Cursor cursor, String columnName) {
		return cursor.getString( cursor.getColumnIndex(columnName) );
	}

	public static int getColumnInt(Cursor cursor, String columnName) {
		return cursor.getInt( cursor.getColumnIndex(columnName) );
	}

	public static long getColumnLong(Cursor cursor, String columnName) {
		return cursor.getLong( cursor.getColumnIndex(columnName) );
	}

	public static Uri getUriForTable(String tableName) {
		//Base content Uri for accessing the provider
		return  new Uri.Builder().scheme("content")
				.authority(CONTENT_AUTHORITY)
				.appendPath(tableName)
				.build();
	}
}
