package com.woodlawn.globaldominion.map;

import com.woodlawn.globaldominion.provider.GraphEdgeColumns;

import android.content.ContentValues;
import android.database.Cursor;

public class GraphEdge
{
    public double x1, y1, x2, y2;

    public int site1;
    public int site2;
    
    public boolean equals(GraphEdge in) {    	
    	return x1 == in.x1 && y1 == in.y1 && x2 == in.x2 && y2 == in.y2;
    }
    
    public ContentValues getValues(int siteId) {
    	ContentValues values = new ContentValues();
    	values.put(GraphEdgeColumns.SITE1, site1);
    	values.put(GraphEdgeColumns.SITE2, site2);
    	values.put(GraphEdgeColumns.X1, x1);
    	values.put(GraphEdgeColumns.X2, x2);
    	values.put(GraphEdgeColumns.Y1, y1);
    	values.put(GraphEdgeColumns.Y2, y2);
    	values.put(GraphEdgeColumns.SITE_ID, siteId);
    	return values;
    }
    
    public static GraphEdge convertCursor(Cursor c) {
    	GraphEdge g = new GraphEdge();
    	g.site1 = c.getInt(c.getColumnIndex(GraphEdgeColumns.SITE1));
    	g.site2 = c.getInt(c.getColumnIndex(GraphEdgeColumns.SITE2));
    	g.x1 = c.getFloat(c.getColumnIndex(GraphEdgeColumns.X1));
    	g.x2 = c.getFloat(c.getColumnIndex(GraphEdgeColumns.X2));
    	g.y1 = c.getFloat(c.getColumnIndex(GraphEdgeColumns.Y1));
    	g.y2 = c.getFloat(c.getColumnIndex(GraphEdgeColumns.Y2));
    	return g;
    }
}
