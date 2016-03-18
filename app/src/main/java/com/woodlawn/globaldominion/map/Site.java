package com.woodlawn.globaldominion.map;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.woodlawn.globaldominion.provider.SiteColumns;
import com.woodlawn.globaldominion.utils.DimUtil;
import com.woodlawn.globaldominion.utils.Players;

import android.content.ContentValues;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.FloatMath;
import android.util.Log;

// used both for sites and for vertices
public class Site
{
	
	public boolean isCapital = false;
	public boolean isWater = true;
    public Point coord;
    public int sitenbr;
    private Path path;
    private ArrayList<GraphEdge> edges = new ArrayList<GraphEdge>();
    public ArrayList<Site> relatedSites = new ArrayList<Site>();
    private int alpha = 70;
    public int playerIndex = -1;
    public int color = Color.BLUE;
    public boolean consildated = false;
    public boolean coast = false;
    
    public static Site convertCursor(Cursor c) {
    	Site s = new Site();
    	s.coord.x = c.getFloat(c.getColumnIndex(SiteColumns.COORD_X));
    	s.coord.y = c.getFloat(c.getColumnIndex(SiteColumns.COORD_Y));
    	s.isWater = c.getInt(c.getColumnIndex(SiteColumns.IS_WATER)) == 1;
    	s.sitenbr = c.getInt(c.getColumnIndex(SiteColumns.SITE_ID));
    	s.playerIndex = c.getInt(c.getColumnIndex(SiteColumns.PLAYER_INDEX));
    	return s;
    }
    
    public ContentValues getValues() {
    	ContentValues values = new ContentValues();
    	values.put(SiteColumns.COORD_X, coord.x);
    	values.put(SiteColumns.COORD_Y, coord.y);
    	int isWaterInt = 0;
    	if(isWater) {
    		isWaterInt = 1;
    	}
    	values.put(SiteColumns.IS_WATER, isWaterInt);
    	values.put(SiteColumns.PLAYER_INDEX, playerIndex);
    	values.put(SiteColumns.SITE_ID, sitenbr);
    	return values;
    }
    
    public Site()
    {
        coord = new Point();

    }

    public List<GraphEdge> getEdges() {
    	return edges;
    }
    
    public void addSite(Site s) {
    	relatedSites.add(s);
    }
    
    public List<Site> getRelatedSites() {
    	return relatedSites;
    }
    
    private Paint mPaint = new Paint();
    
    public void draw(Canvas c, boolean selected, boolean tangent) {
    	if(isWater) {
    		mPaint.setColor(color);
    		mPaint.setAlpha(255);
    	}
//    	else if(tangent) {
//    		mPaint.setColor(Color.RED);
//			mPaint.setAlpha(200);
//    	}
//    	else if(selected) {
//    		mPaint.setColor(Color.GREEN);
//			mPaint.setAlpha(getAlpha());
//    	}
    	else {
    		//draw nation color, irrespective of who controls it
    		mPaint.setColor(color);
    		//mPaint.setAlpha(70);
    		mPaint.setAlpha(255);
    	}
    	c.drawPath(getPath(), mPaint);
    	
    	//Draw border.
    	if(isWater) {
    		mPaint.setStyle(Paint.Style.STROKE);
    		if(tangent) {
        		mPaint.setColor(Color.RED);
    			mPaint.setAlpha(255);
        	}
        	else if(selected) {
        		mPaint.setColor(Color.GREEN);
    			mPaint.setAlpha(255);
        	}
        	else {
        		mPaint.setColor(Color.WHITE);
        		mPaint.setAlpha(255);
        	}
    		mPaint.setAntiAlias(true);
    		c.drawPath(getPath(), mPaint);
    		mPaint.setStyle(Paint.Style.FILL);
    	}
    	else {
//    		if(playerIndex == 0) {
//    			mPaint.setColor(Color.parseColor("#3E87E0"));
//    		}
//    		else if(playerIndex == 1) {
//    			mPaint.setColor(Color.parseColor("#FF6A00"));
//    		}
    		mPaint.setStyle(Paint.Style.STROKE);
    		mPaint.setStrokeWidth(3);
    		if(tangent) {
        		mPaint.setColor(Color.RED);
    			mPaint.setAlpha(255);
        	}
        	else if(selected) {
        		mPaint.setColor(Color.GREEN);
    			mPaint.setAlpha(255);
        	}
        	else {
        		//mPaint.setColor(Color.TRANSPARENT);
        		//mPaint.setAlpha(70);
        	}
    		mPaint.setAntiAlias(true);
    		c.drawPath(getPath(), mPaint);
    		mPaint.setStyle(Paint.Style.FILL);
    	}
		
    	if(isCapital) {
    		//fill is set as style
    		RectF bounds = new RectF();
    		getPath().computeBounds(bounds, false);
    		mPaint.setColor(Players.color(playerIndex));
    		c.drawCircle((bounds.right-bounds.left)/2 + bounds.left, (bounds.top-bounds.bottom)/2 + bounds.bottom, 3, mPaint);
    	}
    	
		//IS IT FASTER TO USE TWO PAINTS, ONE WITH ONE STYLE ONE WITH THE OTHER? TEST THIS!
    }
    
    public void relax() {
    	double totalY = 0;
    	double totalX = 0;
    	double totalPoints = 0;
    	for(GraphEdge e : edges) {
    		totalPoints++;
    		totalY += e.y1 + e.y2;
    		totalX += e.x1 + e.x2;
    	}
    	if(totalPoints == 0) {
    		return;
    	}
    	totalPoints = totalPoints * 2;
    	coord.x = totalX / totalPoints;
    	coord.y = totalY / totalPoints;
    }
    
    public Point getCoord() {
    	return coord;
    }
    
    public int getAlpha() {
    	return alpha;
    }
    
    private RectF bounds;
    private Bitmap bBitmap;
    private Canvas bCanvas;
    
    /**
     * True if a press happened.
     * @param x
     * @param y
     * @return
     */
    public boolean press(float x, float y) {
    	if(path == null) {
    		alpha = 70;
    		return false;
    	}
    	if(bounds == null) {
    		bounds = new RectF();
    		path.computeBounds(bounds, false);
    		try {
    		bBitmap = Bitmap.createBitmap((int)(bounds.right - bounds.left), (int)(bounds.bottom - bounds.top), Bitmap.Config.RGB_565);
    		}catch(Exception e) {
    			alpha = 70;
    			return false;
    		}
    		bBitmap.eraseColor(0xFF000000);
    		bCanvas = new Canvas(bBitmap);
    		Paint p = new Paint();
    		p.setColor(Color.RED);
    		//bCanvas.drawColor(Color.parseColor("#01000000"));
    		
    		Path pp = new Path(path);
    		pp.offset(-bounds.left, -bounds.top);
    		
    		p.setAntiAlias(false);
    		p.setStyle(Paint.Style.FILL);
    		
    		bCanvas.drawPath(pp, p);
    	}
    	boolean pressed = bounds.contains(x, y);
    	if(pressed) {
    		//Ok, we are in the bounding box, what about the bitmap mask?
    		
    		for(int i=0; i<bBitmap.getHeight(); i++) {
    			String row = "";
    			for(int j=0; j<bBitmap.getWidth(); j++) {
    				row += String.valueOf(Color.red(bBitmap.getPixel(j, i)));
    			}
    			//Log.d("TK",row);
    		}
    		
    		try {
    			int a = Color.red(bBitmap.getPixel((int)(x-bounds.left), (int)(y-bounds.top)));
    			if(a < 255) {
    				pressed = false;
    			}
    		}catch(Exception e) {
    			pressed = false;
    		}
    		alpha = 200;
    		//System.out.println("START -----------");
        	//for(GraphEdge g : edges) {
        	//	System.out.println(g.site1 + " - " + g.site2 + " - " + g.x1 + "," + g.y1 + " - " + g.x2 + "," + g.y2);
        	//}
        	//System.out.println("END -------------");
    	}
    	else {
    		alpha = 70;
    	}
//    	if(pressed) {
//    		for(GraphEdge g : edges) {
//    			Log.d("TK","(" + g.x1 + "," + g.y1 + ") - (" + g.x2 + "," + g.y2 + ")");
//    		}
//    	}
    	return pressed;
    }
    
    public void addEdge(GraphEdge edge) {
    	if(!(edge.x1 == edge.x2 && edge.y1 == edge.y2)) {
    		edges.add(edge);
    	}
    }
    
    private double round(double unrounded, int precision, int roundingMode)
    {
        BigDecimal bd = new BigDecimal(unrounded);
        BigDecimal rounded = bd.setScale(precision, roundingMode);
        return rounded.doubleValue();
    }
    
    public void removeDupes() {
    	//Remove duplicate edges.
    	ArrayList<GraphEdge> distinctEdges = new ArrayList<GraphEdge>();
    	for(int i=0; i<edges.size(); i++) {
    		GraphEdge g = edges.get(i);
    		boolean found = false;
    		for(GraphEdge d : distinctEdges) {
    			if((d.x1 == g.x1 && d.y1 == g.y1 && d.x2 == g.x2 && d.y2 == g.y2) ||
    					(d.x1 == g.x2 && d.y1 == g.y2 && d.x2 == g.x1 && d.y2 == g.y1)) {
    				found = true;
    				break;
    			}
    		}
    		if(found) {
    			edges.remove(i);
    			i--;
    		}
    		else {
    			distinctEdges.add(g);
    		}
    	}
    }
    
    public boolean onTheEdge() {
    	for(int i=0; i<edges.size(); i++) {
    		GraphEdge g = edges.get(i);
    		boolean onTop = false;
    		boolean onBottom = false;
    		boolean onLeft = false;
    		boolean onRight = false;
    		if(g.x1 == 0 || g.x2 == 0 || g.y1 == 0 || g.y2 == 0 || g.x1 == DimUtil.getWidth() || g.x2 == DimUtil.getWidth() ||
    				g.y1 == DimUtil.getHeight() || g.y2 == DimUtil.getHeight()) {
//    			Log.d("TK","START ................");
//    			for(GraphEdge ge : edges) {
//    				Log.d("TK",ge.site1 + ", " + ge.site2 + ", " + ge.x1 + ", " + ge.y1 + ", " + ge.x2 + ", " + ge.y2);
//    			}
    			if(g.x1 == 0 || g.x2 == 0) {
    				onLeft = true;
    			}
    			if(g.y1 == 0 || g.y2 == 0) {
    				onTop = true;
    			}
    			if(g.x1 == DimUtil.getWidth() || g.x2 == DimUtil.getWidth()) {
    				onRight = true;
    			}
    			if(g.y1 == DimUtil.getHeight() || g.y2 == DimUtil.getHeight()) {
    				onBottom = true;
    			}
//    			Log.d("TK",onBottom + ", " + onTop + ", " + onLeft + ", " + onRight);
//    			Log.d("TK","END ..................");
    		}
    		if(onTop || onBottom || onLeft || onRight) {
    			color = Color.BLUE;
    			isWater = true;
    			return true;
    		}
    	}
    	return false;
    }
    
    public void resetPath() {
    	path = null;
    }
    
    public Path getPath() {
    	if(onTheEdge()) {
    		return new Path();
    	}
    	if(path != null) {
    		return path;
    	}

    	//System.out.println("START -----------");
    	//for(GraphEdge g : edges) {
    	//	System.out.println(g.site1 + " - " + g.site2 + " - " + g.x1 + "," + g.y1 + " - " + g.x2 + "," + g.y2);
    		
//    		g.x1 = round(g.x1, 0, BigDecimal.ROUND_UP);
//    		g.x2 = round(g.x2, 0, BigDecimal.ROUND_UP);
//    		g.y1 = round(g.y1, 0, BigDecimal.ROUND_UP);
//    		g.y2 = round(g.y2, 0, BigDecimal.ROUND_UP);
    		
    	//}
    	//System.out.println("END -------------");
    	
    	path = new Path();
    	//ArrayList<GraphEdge> pathEdges = new ArrayList<GraphEdge>();
    	GraphEdge startingEdge = null;
    	
    	for(GraphEdge g : edges) {
    		//System.out.println(g.site1 + " - " + g.site2 + " - " + g.x1 + "," + g.y1 + " - " + g.x2 + "," + g.y2);
    		if(g.x1 != g.x2 && g.y1 != g.y2) {
    			startingEdge = g;
    			break;
    		}
    	}
    	
    	boolean end = false;
    	if(startingEdge == null) {
    		return path;
    	}
    	path.moveTo((float)startingEdge.x1, (float)startingEdge.y1);
    	path.lineTo((float)startingEdge.x2, (float)startingEdge.y2);
    	
    	float cx = (float) startingEdge.x2;
    	float cy = (float) startingEdge.y2;
    	float nx = 0;
    	float ny = 0;
    	GraphEdge current = startingEdge;
    	
    	while(!end) {
    		boolean found = false;
    		for(GraphEdge g : edges) {
    			if(g.x1 != g.x2 && g.y1 != g.y2 && !current.equals(g)) {
    				if((float)g.x1 == cx && (float)g.y1 == cy) {
    					//System.out.println(cx + "," + cy + " - " + g.x2 + "," + g.y2);
    					nx = (float) g.x2;
    					ny = (float) g.y2;
    					current = g;
    					found = true;
    					if(g.equals(startingEdge)) {
    						end = true;
    					}
    					break;
    				}
    				else if((float)g.x2 == cx && (float)g.y2 == cy) {
    					//System.out.println(cx + "," + cy + " - " + g.x1 + "," + g.y1);
    					nx = (float) g.x1;
    					ny = (float) g.y1;
    					current = g;
    					found = true;
    					if(g.equals(startingEdge)) {
    						end = true;
    					}
    					break;
    				}
    			}
    		}
    		if(!found) {
    			Log.d("TK","NOT FOUND!!!!!!!!!!!");
    			return path;
    		}
    		path.lineTo(nx, ny);
    		cx = nx;
    		cy = ny;
    	}
    	
    	
    	return path;
    }
    
    public static Path getPath(List<GraphEdge> edges) {

    	//System.out.println("START -----------");
    	//for(GraphEdge g : edges) {
    	//	System.out.println(g.site1 + " - " + g.site2 + " - " + g.x1 + "," + g.y1 + " - " + g.x2 + "," + g.y2);
    		
//    		g.x1 = round(g.x1, 0, BigDecimal.ROUND_UP);
//    		g.x2 = round(g.x2, 0, BigDecimal.ROUND_UP);
//    		g.y1 = round(g.y1, 0, BigDecimal.ROUND_UP);
//    		g.y2 = round(g.y2, 0, BigDecimal.ROUND_UP);
    		
    	//}
    	//System.out.println("END -------------");
    	
    	Path path = new Path();
    	//ArrayList<GraphEdge> pathEdges = new ArrayList<GraphEdge>();
    	GraphEdge startingEdge = null;
    	
    	for(GraphEdge g : edges) {
    		//System.out.println(g.site1 + " - " + g.site2 + " - " + g.x1 + "," + g.y1 + " - " + g.x2 + "," + g.y2);
    		if(g.x1 != g.x2 && g.y1 != g.y2) {
    			startingEdge = g;
    			break;
    		}
    	}
    	
    	boolean end = false;
    	if(startingEdge == null) {
    		return path;
    	}
    	path.moveTo((float)startingEdge.x1, (float)startingEdge.y1);
    	path.lineTo((float)startingEdge.x2, (float)startingEdge.y2);
    	
    	float cx = (float) startingEdge.x2;
    	float cy = (float) startingEdge.y2;
    	float nx = 0;
    	float ny = 0;
    	GraphEdge current = startingEdge;
    	
    	while(!end) {
    		boolean found = false;
    		for(GraphEdge g : edges) {
    			if(g.x1 != g.x2 && g.y1 != g.y2 && !current.equals(g)) {
    				if((float)g.x1 == cx && (float)g.y1 == cy) {
    					//System.out.println(cx + "," + cy + " - " + g.x2 + "," + g.y2);
    					nx = (float) g.x2;
    					ny = (float) g.y2;
    					current = g;
    					found = true;
    					if(g.equals(startingEdge)) {
    						end = true;
    					}
    					break;
    				}
    				else if((float)g.x2 == cx && (float)g.y2 == cy) {
    					//System.out.println(cx + "," + cy + " - " + g.x1 + "," + g.y1);
    					nx = (float) g.x1;
    					ny = (float) g.y1;
    					current = g;
    					found = true;
    					if(g.equals(startingEdge)) {
    						end = true;
    					}
    					break;
    				}
    			}
    		}
    		if(!found) {
    			Log.d("TK","NOT FOUND!!!!!!!!!!! " + cx + "," + cy);
//    			//We most likely have a rounding issue due to double to float conversion.
//    			//Find the best possible match to candidate coordinate.
//    			float closestDistance = DimUtil.getWidth();
//    			for(GraphEdge e : edges) {
//    				//Run through all of the edges and find the closest point that matches and use that.
//    				if(e.x1 != cx || e.y1 != cy) {
//    					float dx = (float) (e.x1 - cx);
//    					float dy = (float) (e.y1 - cy);
//    					float distance = FloatMath.sqrt((dx * dx) + (dy * dy));
//    					if(distance < closestDistance) {
//    						nx = (float)e.x1;
//    						ny = (float)e.y1;
//    						closestDistance = distance;
//    					}
//    				}
//    				else if(e.x2 != cx || e.y2 != cy) {
//    					float dx = (float) (e.x2 - cx);
//    					float dy = (float) (e.y2 - cy);
//    					float distance = FloatMath.sqrt((dx * dx) + (dy * dy));
//    					if(distance < closestDistance) {
//    						nx = (float)e.x2;
//    						ny = (float)e.y2;
//    						closestDistance = distance;
//    					}
//    				}
//    			}
    			
    			return path; //comment out if we get the code above working, currently it loops forever.
    		}
    		path.lineTo(nx, ny);
    		cx = nx;
    		cy = ny;
    	}
    	
    	
    	return path;
    }
    
}

