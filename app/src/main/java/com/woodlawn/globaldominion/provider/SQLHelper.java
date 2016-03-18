package com.woodlawn.globaldominion.provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class SQLHelper extends SQLiteOpenHelper {

	public SQLHelper(Context context, String name, CursorFactory factory, int version) {
		super(context, name, factory, version);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {

		db.execSQL("CREATE TABLE " + VoronoiProvider.SITE_TABLE + " (" +
				SiteColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
				SiteColumns.SITE_ID + " INTEGER," + 
				SiteColumns.IS_WATER + " INTEGER," + 
				SiteColumns.COORD_X + " REAL," +
				SiteColumns.COORD_Y + " REAL," +
				SiteColumns.PLAYER_INDEX + " INTEGER);");
		
		db.execSQL("CREATE TABLE " + VoronoiProvider.RELATED_SITE_TABLE + " (" +
				RelatedSiteColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
				RelatedSiteColumns.RELATED_SITE_ID + " INTEGER," +
				RelatedSiteColumns.SITE_ID + " INTEGER);");
		
		db.execSQL("CREATE TABLE " + VoronoiProvider.GRAPH_EDGE_TABLE + " (" +
				GraphEdgeColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
				GraphEdgeColumns.SITE_ID + " INTEGER," +
				GraphEdgeColumns.SITE1 + " INTEGER," +
				GraphEdgeColumns.SITE2 + " INTEGER," +
				GraphEdgeColumns.X1 + " REAL," +
				GraphEdgeColumns.X2 + " REAL," + 
				GraphEdgeColumns.Y1 + " REAL," +
				GraphEdgeColumns.Y2 + " REAL);");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		if(oldVersion<newVersion){
			//Do nothing for now, an updated database version is not a valid scenario yet.
		}

	}

}
