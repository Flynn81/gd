package com.woodlawn.globaldominion.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.net.Uri;

public class VoronoiProvider extends ContentProvider {
	
	private static DataSource sDataSource;
	private static Context sContext;
	private static final UriMatcher sUriMatcher = new UriMatcher(0);
	private static String sAuthority;
	
	public static final String GRAPH_EDGE_TABLE = "graph_edge";
	public static final String SITE_TABLE = "site";
	public static final String RELATED_SITE_TABLE = "related_site";
	
	private static final int GRAPH_EDGE = 1;
	private static final int SITE = 2;
	private static final int RELATED_SITE = 3;

	public static Uri getCONTENT_URI(String table) {

		Uri authorityUri = Uri.parse("content://com.woodlawn.globaldominion");
		Uri contentUri = Uri.withAppendedPath(authorityUri,table);

		return contentUri;
	}
	
	@Override
	public boolean onCreate() {
		sContext = getContext();
		sAuthority = "com.woodlawn.globaldominion";
		initUriMatchers();
		sDataSource = DataSource.get(sContext);
		return true;
	}

	private String matchUri(Uri uri) {
		switch (sUriMatcher.match(uri)) {
		case SITE:
			return SITE_TABLE;
		case GRAPH_EDGE:
			return GRAPH_EDGE_TABLE;
		case RELATED_SITE:
			return RELATED_SITE_TABLE;
		default:
            throw new IllegalArgumentException("Could not match Uri:	" + uri);
		}
	}

	private void initUriMatchers() {
		//These paths MUST NOT have a leading slash, eg: "path" not "/path"
		sUriMatcher.addURI(sAuthority, SITE_TABLE, SITE);
		sUriMatcher.addURI(sAuthority, GRAPH_EDGE_TABLE, GRAPH_EDGE);
		sUriMatcher.addURI(sAuthority, RELATED_SITE_TABLE, RELATED_SITE);
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		// Match the incoming uri to the data source.
		String dataLocation = matchUri(uri);
		int count = sDataSource.delete(dataLocation, selection, selectionArgs);
		sContext.getContentResolver().notifyChange(uri, null);
		return count;
	}

	@Override
	public String getType(Uri uri) {
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		// Match the incoming uri to the data source.
		try{
			String dataLocation = matchUri(uri);
			Long rowId = sDataSource.insert(dataLocation, null, values);
			if (rowId > 0) {
				Uri modifiedUri = ContentUris.withAppendedId(uri, rowId);
				sContext.getContentResolver().notifyChange(modifiedUri, null);
				return modifiedUri;
			} else {
				throw new SQLException("Failed to insert row for:	" + uri);
			}
		}catch(SQLException sqle){
			//sqle.printStackTrace();
			return null;
		}		
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		//Match the incoming uri to the data source.
		String dataLocation = matchUri(uri);
		Cursor cursor = sDataSource.query(sContext, dataLocation, projection, selection, selectionArgs, null, null, sortOrder);
		cursor.setNotificationUri(sContext.getContentResolver(), uri);
        return cursor;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		// Match the incoming uri to the data source.
		String dataLocation = matchUri(uri);
		int count = (values.size() > 0) ? sDataSource.update(dataLocation, values, selection, selectionArgs) : 0;
		if (count > 0) {
			sContext.getContentResolver().notifyChange(uri, null);
		}
		return count;
	}
}
