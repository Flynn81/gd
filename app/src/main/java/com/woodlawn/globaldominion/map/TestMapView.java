package com.woodlawn.globaldominion.map;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import com.woodlawn.globaldominion.provider.GraphEdgeColumns;
import com.woodlawn.globaldominion.provider.RelatedSiteColumns;
import com.woodlawn.globaldominion.provider.VoronoiProvider;
import com.woodlawn.globaldominion.utils.DimUtil;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Paint.Style;
import android.util.AttributeSet;
import android.util.FloatMath;
import android.util.Log;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.ScaleGestureDetector.OnScaleGestureListener;
import android.view.View;
import android.widget.Toast;

public class TestMapView extends View implements OnScaleGestureListener{

	public static final int SMALL = 1;
	public static final int MEDIUM = 2;
	public static final int LARGE = 3;
	
	/**
	 * Only used in the relax method.
	 */
	private double[] mXPoints;
	/**
	 * Only used in the relax method.
	 */
	private double[] mYPoints;
	
	private SparseArray<Site> mSites;
	private Paint mPaint = new Paint();
	private ArrayList<Site> mSelectedSites = new ArrayList<Site>();
	private ArrayList<Country> mCountries = new ArrayList<Country>();
	private ScaleGestureDetector mGestureDetector;
	
	public TestMapView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mGestureDetector = new ScaleGestureDetector(context, this);
	}

	public void setEdges(List<GraphEdge> edges, double[] xPoints, double[] yPoints, Site[] sites) {
		mPaint = new Paint();
		mXPoints = xPoints;
		mYPoints = yPoints;
		
		mSites = new SparseArray<Site>();
        for(Site s : sites) {
        	mSites.put(s.sitenbr, s);
        }
	}
	
	public void onDraw(Canvas c) {

		Log.d("TK","start on draw");
		if(mScalePointX == -1) {
			c.scale(mScale, mScale);
		}
		else {
			c.scale(mScale, mScale, mScalePointX, mScalePointY);
		}
		
			mPaint.setStrokeWidth(0f);
			mPaint.setAntiAlias(true);
			mPaint.setAlpha(80);
			//c.drawColor(Color.LTGRAY);
			c.drawColor(Color.BLUE);
			mPaint.setAlpha(255);
			Log.d("TK","yoda 1");
//			int[] colors = new int[]{
//					Color.parseColor("#565902"),
//					Color.parseColor("#898C1C"),
//					Color.parseColor("#403C01"),
//					Color.parseColor("#A6831C"),
//					Color.parseColor("#593825")
//			};
//			Random r = new Random();
			Log.d("TK","yoda 12");
			mPaint.setColor(Color.GREEN);
			mPaint.setAlpha(70);
			mPaint.setStyle(Paint.Style.FILL);
			if(mSites == null) {
				return;
			}
			ArrayList<Site> adjacent = new ArrayList<Site>();
			Log.d("TK","yoda 123");
			for(int i = 0; i<mSites.size(); i++) {
				Site site = mSites.get(mSites.keyAt(i));
				if(mSelectedSites.contains(site)) {
					for(Site s : site.getRelatedSites()) {
						if(!mSelectedSites.contains(s)) {
							adjacent.add(s);
						}
					}
					site.draw(c, true, false);
				}
				else {
					site.draw(c, false, false);
				}
			}
			Log.d("TK","yoda 1234");  //ABOVE AND BELOW ARE BIG TIME SINKS
			for(Site site : adjacent) {
				ArrayList<Site> a = new ArrayList<Site>();
				selectedHelper(site, a);
				for(Site as : a) {
					as.draw(c, false, true);	
				}
			}
			Log.d("TK","yoda 12345");
			mPaint.setAlpha(255);
			
			mPaint.setColor(Color.WHITE);
			for(Country country : mCountries) {
				try {
				c.drawText(country.siteIds.size() + "", (float)mSites.get(country.capital).coord.x, (float)mSites.get(country.capital).coord.y, mPaint);
				}catch(Exception e) {e.printStackTrace();}
			}
			
//		if(pressedX > -1 && pressedY > -1) {
//			mPaint.setColor(Color.BLACK);
//			c.drawRect(pressedX - 75, pressedY, pressedX + 75, pressedY + 48, mPaint);
//		}
		
		
			mPaint.setStyle(Style.STROKE);
			mPaint.setColor(Color.WHITE);
			mPaint.setStrokeWidth(3);
			for(Country country : mCountries) {
				Path testPath = country.path;
				if(testPath != null) {
					c.drawPath(testPath, mPaint);
				}
			}
			mPaint.setStyle(Style.FILL);
			
		Log.d("TK","end ondraw");
	}
	
	private void selectedHelper(Site current, List<Site> sites) {
		sites.add(current);
		for(Site site : current.getRelatedSites()) {
			if(site.color == current.color && !sites.contains(site)) {
				selectedHelper(site, sites);
			}
		}
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		Log.d("TK","start on touch");
		mGestureDetector.onTouchEvent(event);
		
		if(event.getAction() != MotionEvent.ACTION_UP) {
			return true;
		}
		
		float y = event.getY();
		float x = event.getX();
		
		y = y * (1f/mScale);
		x = x * (1f/mScale);
		
		boolean pressed = false;
		Site oldSite = null;
		for(int i = 0; i<mSites.size(); i++) {
			Site site = mSites.get(mSites.keyAt(i));
			pressed = pressed || site.press(x, y);

			if(pressed) {
				boolean toggle = false;
				
					for(Site cc : mSelectedSites){
						if(site.sitenbr == cc.sitenbr) {
							toggle = true;
							break;
						}
					}
				
				//Only allow one at a time for now.
				/* code below allows one Site
				if(mSelectedSites.size() > 0) {
					oldSite = mSelectedSites.get(0);
				}
				mSelectedSites.clear();
				mSelectedSites.add(site); */
				if(toggle) {
					mSelectedSites.clear();
					break;
				}
				//Allow only one connected site
				if(mSelectedSites.size() > 0) {
					oldSite = mSelectedSites.get(0);
				}
				mSelectedSites.clear();
				selectedHelper(site, mSelectedSites);
				
				invalidate();
				
				Log.d("TK","start COUNTRIES");
				for(Country c : mCountries) {
					for(int sid : c.siteIds) {
						if(sid == site.sitenbr) {
							for(GraphEdge e : c.edges) {
								Log.d("TK", e.site1 + "," + e.site2 + " -> " + e.x1 + "," + e.y1 + " - " + e.x2 + "," + e.y2);
							}
						}
					}
				}
				Log.d("TK","start SITES");
				for(Country c : mCountries) {
					for(int sid : c.siteIds) {
						if(sid == site.sitenbr) {
							for(int s : c.siteIds) {
								try {
								for(GraphEdge e : mSites.get(s).getEdges()) {
									Log.d("TK", e.site1 + "," + e.site2 + " -> " + e.x1 + "," + e.y1 + " - " + e.x2 + "," + e.y2);
								}
								}catch(Exception e) {
									e.printStackTrace();
								}
							}
						}
					}
				}
				
				Log.d("TK","on touch END loop");
				return pressed;
			}
		}
		
		invalidate();
		
		Log.d("TK","on touch END end");
		
		return pressed;
	}
	
	public void initSiteInformation(int size, int numberOfPlayers) {
		Log.d("TK","start init site info");
		
		Random r = new Random();
		
		//create land
		int divider = 10;
		for(int i=0; i<DimUtil.getSize()/25; i++) {
			int index = r.nextInt(mSites.size());
			mSites.get(mSites.keyAt(index)).isWater = false;
			mSites.get(mSites.keyAt(index)).color = Color.YELLOW;
			for(Site related : mSites.get(mSites.keyAt(index)).getRelatedSites()) {
				related.isWater = false;
				related.color = Color.YELLOW;
				if(r.nextInt() % 3 == 0 || i % divider == 0) {
					for(Site relatedrelated : related.getRelatedSites()) {
						relatedrelated.isWater = false;
						relatedrelated.color = Color.YELLOW;
						if(i % divider == 0) {
							for(Site rrr : relatedrelated.relatedSites) {
								rrr.isWater = false;
								rrr.color = Color.YELLOW;
							}
						}
					}
				}
			}
		}
		
		int maxOfCountries = 47;
		int numberOfCountries = 0;
		int numberOfSites = mSites.size();
		
		//insert oceans here
		int which = r.nextInt(6);
		if(which == 0) {
			//top left, bottom right
			float x = r.nextInt((int)(0.1 * DimUtil.getWidth())) + DimUtil.getWidth() / 4;
			float y = r.nextInt((int)(0.1 * DimUtil.getHeight())) + DimUtil.getHeight() / 4;
			float major = r.nextInt((int)(0.1 * DimUtil.getHeight())) + DimUtil.getHeight() / 6;
			float minor = r.nextInt((int)(0.1 * DimUtil.getHeight())) + DimUtil.getHeight() / 10;
			ocean(x, y, major, minor);
			//smaller
			x = r.nextInt((int)(0.1 * DimUtil.getWidth())) + (int)(DimUtil.getWidth() * 0.75);
			y = r.nextInt((int)(0.1 * DimUtil.getHeight())) + (int)(DimUtil.getHeight() * 0.75);
			major = r.nextInt((int)(0.1 * DimUtil.getHeight())) + DimUtil.getHeight() / 8;
			minor = r.nextInt((int)(0.1 * DimUtil.getHeight())) + DimUtil.getHeight() / 13;
			ocean(x, y, major, minor);
		}
		else if(which == 1) {
			//top right, bottom left
			float x = r.nextInt((int)(0.1 * DimUtil.getWidth())) + (int)(DimUtil.getWidth() * 0.75);
			float y = r.nextInt((int)(0.1 * DimUtil.getHeight())) + DimUtil.getHeight() / 4;
			float major = r.nextInt((int)(0.1 * DimUtil.getHeight())) + DimUtil.getHeight() / 6;
			float minor = r.nextInt((int)(0.1 * DimUtil.getHeight())) + DimUtil.getHeight() / 10;
			ocean(x, y, major, minor);
			//smaller
			x = r.nextInt((int)(0.1 * DimUtil.getWidth())) + DimUtil.getWidth() / 4;
			y = r.nextInt((int)(0.1 * DimUtil.getHeight())) + (int)(DimUtil.getHeight() * 0.75);
			major = r.nextInt((int)(0.1 * DimUtil.getHeight())) + DimUtil.getHeight() / 8;
			minor = r.nextInt((int)(0.1 * DimUtil.getHeight())) + DimUtil.getHeight() / 13;
			ocean(x, y, major, minor);
		}
		else if(which == 2) {
			//oval middle
			float x = r.nextInt((int)(0.1 * DimUtil.getWidth())) + DimUtil.getWidth() / 2;
			float y = r.nextInt((int)(0.1 * DimUtil.getHeight())) + DimUtil.getHeight() / 2;
			float minor = r.nextInt((int)(0.1 * DimUtil.getHeight())) + (int)(DimUtil.getHeight() * 0.5);
			float major = r.nextInt((int)(0.1 * DimUtil.getHeight())) + DimUtil.getHeight() / 7;
			ocean(x, y, major, minor);
		}
		else if(which == 3) {
			//left
			float x = r.nextInt((int)(0.1 * DimUtil.getWidth())) + (int)(DimUtil.getWidth() * 0.29);
			float y = r.nextInt((int)(0.1 * DimUtil.getHeight())) + DimUtil.getHeight() / 2;
			float minor = r.nextInt((int)(0.1 * DimUtil.getHeight())) + (int)(DimUtil.getHeight() * 0.4);
			float major = r.nextInt((int)(0.1 * DimUtil.getHeight())) + DimUtil.getHeight() / 7;
			ocean(x, y, major, minor);
		}
		else if(which == 4) {
			//right
			float x = r.nextInt((int)(0.1 * DimUtil.getWidth())) + (int)(DimUtil.getWidth() * 0.65);
			float y = r.nextInt((int)(0.1 * DimUtil.getHeight())) + DimUtil.getHeight() / 2;
			float minor = r.nextInt((int)(0.1 * DimUtil.getHeight())) + (int)(DimUtil.getHeight() * 0.4);
			float major = r.nextInt((int)(0.1 * DimUtil.getHeight())) + DimUtil.getHeight() / 7;
			ocean(x, y, major, minor);
		}
		else if(which == 5) {
			//2 long vertical ovals
			float x = r.nextInt((int)(0.1 * DimUtil.getWidth())) + (int)(DimUtil.getWidth() * 0.27);
			float y = r.nextInt((int)(0.1 * DimUtil.getHeight())) + DimUtil.getHeight() / 2;
			float minor = r.nextInt((int)(0.1 * DimUtil.getHeight())) + (int)(DimUtil.getHeight() * 0.4);
			float major = r.nextInt((int)(0.1 * DimUtil.getHeight())) + DimUtil.getHeight() / 11;
			ocean(x, y, major, minor);
			
			x = r.nextInt((int)(0.1 * DimUtil.getWidth())) + (int)(DimUtil.getWidth() * 0.65);
			y = r.nextInt((int)(0.1 * DimUtil.getHeight())) + DimUtil.getHeight() / 2;
			minor = r.nextInt((int)(0.1 * DimUtil.getHeight())) + (int)(DimUtil.getHeight() * 0.4);
			major = r.nextInt((int)(0.1 * DimUtil.getHeight())) + DimUtil.getHeight() / 11;
			ocean(x, y, major, minor);
		}
		
		//create countries
		int currentColor = Color.GREEN + 5000;
		int f = 2;
		if(size == SMALL) {
			divider = 2;//r.nextInt(7) + 1;//used for large countries
		}
		else if(size == MEDIUM) {
			divider = 3;
		}
		else if(size == LARGE) {
			divider = 4;
		}
		int playerIndex = 0;
		for(int ii = 0; ii<mSites.size(); ii++) {
			Site site = mSites.get(mSites.keyAt(ii));
			if(site.isWater == false && site.color == Color.YELLOW) {
				Country country = new Country();
				country.siteIds.add(site.sitenbr);
				site.color = currentColor;
				site.playerIndex = playerIndex;
				for(Site related : site.relatedSites) {
					if(related.isWater == false && related.color == Color.YELLOW) {
						boolean found = false;
						for(Integer ig : country.siteIds) {
							if(ig.intValue() == related.sitenbr) {
								found = true;
								break;
							}
						}
						if(!found) {
							country.siteIds.add(related.sitenbr);
						}
						related.color = currentColor;
						related.playerIndex = playerIndex;
						if(related.sitenbr % 2 == 0 || related.sitenbr % divider == 0 || (r.nextInt() % f == 0 && size == SMALL)) {
							for(Site rr : related.relatedSites) {
								if(rr.isWater == false && rr.color == Color.YELLOW) {
									found = false;
									for(Integer ig : country.siteIds) {
										if(ig.intValue() == rr.sitenbr) {
											found = true;
											break;
										}
									}
									if(!found) {
										country.siteIds.add(rr.sitenbr);
									}
									rr.color = currentColor;
									rr.playerIndex = playerIndex;
									if(rr.sitenbr % divider == 0 || (r.nextInt() % f == 0 && size == SMALL)) {
										for(Site rrr : rr.relatedSites) {
											if(rrr.isWater == false && rrr.color == Color.YELLOW) {
												found = false;
												for(Integer ig : country.siteIds) {
													if(ig.intValue() == rrr.sitenbr) {
														found = true;
														break;
													}
												}
												if(!found) {
													country.siteIds.add(rrr.sitenbr);
												}
												rrr.color = currentColor;
												rrr.playerIndex = playerIndex;
												if(rrr.sitenbr % divider == 0) {
													for(Site r4 : rrr.relatedSites) {
														if(r4.isWater == false) {
															found = false;
															for(Integer ig : country.siteIds) {
																if(ig.intValue() == r4.sitenbr) {
																	found = true;
																	break;
																}
															}
															if(!found) {
																country.siteIds.add(r4.sitenbr);
															}
															for(Country c2 : mCountries) {
																for(int i=0; i<c2.siteIds.size(); i++) {
																	Integer si = c2.siteIds.get(i); 
																	if(si.intValue() == r4.sitenbr) {
																		c2.siteIds.remove(i);
																		break;
																	}
																}
															}
															r4.color = currentColor;
															r4.playerIndex = playerIndex;
															for(Site r5 : r4.relatedSites) {
																if(r5.isWater == false) {
																	found = false;
																	for(Integer ig : country.siteIds) {
																		if(ig.intValue() == r5.sitenbr) {
																			found = true;
																			break;
																		}
																	}
																	if(!found) {
																		country.siteIds.add(r5.sitenbr);
																	}
																	for(Country c2 : mCountries) {
																		for(int i=0; i<c2.siteIds.size(); i++) {
																			Integer si = c2.siteIds.get(i); 
																			if(si.intValue() == r5.sitenbr) {
																				c2.siteIds.remove(i);
																				break;
																			}
																		}
																	}
																	r5.color = currentColor;
																	r5.playerIndex = playerIndex;
																}
															}
														}
													}
												}
											}
										}
									}
								}
							}
						}
					}
				}
				currentColor = currentColor - 5000;
				playerIndex++;
				if(playerIndex == numberOfPlayers) {
					playerIndex = 0;
				}
				mCountries.add(country);
			}
		}
		
		int caps = 0;
		for(Country country : mCountries) {
			if(!country.siteIds.isEmpty()) {
//			int prevCaps = caps;
			for(int i = 0; i<mSites.size(); i++) {
				Site site = mSites.get(mSites.keyAt(i));
				if(country.siteIds.contains(site.sitenbr)) {
					site.isCapital = true;
					country.capital = site.sitenbr;
//					caps++;
					break;
				}
			}
//			if(prevCaps == caps) {
//				Log.d("TK","test");
//			}
			}
		}
		Log.d("TK2", "caps/countries = " + caps + "/" + mCountries.size() + " and divider is " + divider);
		
		//int coastGreen = Color.parseColor("#4DB849");
		int[] greens = new int[]{
				Color.parseColor("#4DB849"),
				Color.parseColor("#69B764"),
				Color.parseColor("#83B780"),
				Color.parseColor("#34B72D"),
				Color.parseColor("#9DB79C"),
				Color.parseColor("#1AB712")
		};
		int greenIndex = 0;
		//Determine coastal areas.  2 sites away from land is coastal.
		for(int i = 0; i<mSites.size(); i++) {
			Site site = mSites.get(mSites.keyAt(i));
			if(site.isWater) {
				boolean landFound = false;
				for(Site related : site.relatedSites) {
					if(related.isWater == false) {
						landFound = true;
						break;
					}
				}
				if(landFound) {
					site.color = greens[greenIndex];
					site.playerIndex = -1;
					greenIndex++;
					if(greenIndex == greens.length) {
						greenIndex = 0;
					}
					site.coast = true;
				}
			}
		}
		
		//Determine ocean areas.
		int[] blues = new int[]{
				Color.parseColor("#4991B7"),
				Color.parseColor("#485AB5"),
				Color.parseColor("#4872B5"),
				Color.parseColor("#4896B5"),
				Color.parseColor("#1E62B5"),
				Color.parseColor("#4F91B5"),
				Color.parseColor("#0075B5")
			};
		int bi = 0;
		for(int i = 0; i<mSites.size(); i++) {
			Site site = mSites.get(mSites.keyAt(i));
			if(site.isWater && site.color == Color.BLUE) {
				site.color = blues[bi];
				site.playerIndex = -1;
				for(Site related : site.relatedSites) {
					if(related.isWater && related.color == Color.BLUE) {
						related.color = blues[bi];
						related.playerIndex = -1;
						if(related.sitenbr % 3 == 0) {
							for(Site rr : related.relatedSites) {
								if(rr.isWater == true && rr.color == Color.BLUE) {
									rr.color = blues[bi];
									rr.playerIndex = -1;
								}
							}
						}
					}
				}
				bi++;
				if(bi == blues.length) {
					bi = 0;
				}
			}
		}

		for(int i = 0; i<mSites.size(); i++) {
			Site site = mSites.get(mSites.keyAt(i));
			if(site.coast) {
				HashMap<Integer, Integer> sids = new HashMap<Integer, Integer>();
				int sid = 0;
				for(Site a : site.getRelatedSites()) {
					if(!a.isWater && !a.coast) {
						sids.put(a.sitenbr, a.sitenbr);
						sid = a.playerIndex;
					}
				}
//				Log.d("TK","size " + sids.size());
				if(sids.size() == 1) {
					site.playerIndex = sid;
				}
			}
		}
		
//		for(int i = 0; i<mSites.size(); i++) {
//			Site site = mSites.get(mSites.keyAt(i));
//			if(site.coast) {
//				Log.d("TK","site number = " + mSites);
//			}
//		}
		for(int i = 0; i<mSites.size(); i++) {
			Site site = mSites.get(mSites.keyAt(i));
			site.getPath();
			site.press(-10, -10);
		}
	
		for(Country c : mCountries) {
			ArrayList<GraphEdge> edges = new ArrayList<GraphEdge>();
			if(c.siteIds == null || c.siteIds.size() == 0) {
				continue;
			}
			for(int sid : c.siteIds) {
				Site s = mSites.get(sid);
				if(s == null) {
					continue;
				}
				edges.addAll(s.getEdges());
			}
			for(GraphEdge e : edges) {
				boolean foundOne = false;
				boolean foundTwo = false;
				for(int i : c.siteIds) {
					if(i == e.site1) {
						foundOne = true;
					}
					if(i == e.site2) {
						foundTwo = true;
					}
				}
				if(!(foundOne && foundTwo)) {
					c.edges.add(e);
				}
			}
			Log.d("TK","...........start..........");
			for(GraphEdge e : c.edges) {
				Log.d("TK", e.site1 + "," + e.site2 + " -> " + e.x1 + "," + e.y1 + " - " + e.x2 + "," + e.y2);
			}
			Log.d("TK","............end...........");
			c.path = Site.getPath(c.edges);
		}
		Log.d("TK","end init site info");
	}
	
	private void ocean(float x, float y, float major, float minor) {
		for(int i = 0; i<mSites.size(); i++) {
			Site site = mSites.get(mSites.keyAt(i));
			float h = (float) site.coord.x;
			float k = (float) site.coord.y;
			if(!site.isWater) {
				site.isWater = (((x - h) * (x - h))/(major * major)) + (((y - k) * (y - k))/(minor * minor)) <= 1;
				if(site.isWater) {
					site.color = Color.BLUE;
				}
			}
		}
	}

	public void relax(boolean lastTime) {
		//relax the points
		for(int i = 0; i<mSites.size(); i++) {
			Site site = mSites.get(mSites.keyAt(i));
        	site.relax();
        }
		for(int i = 0; i<mSites.size(); i++) {
			Site site = mSites.get(mSites.keyAt(i));
        	mXPoints[i] = site.getCoord().x;
        	mYPoints[i] = site.getCoord().y;
        }
        Voronoi v = new Voronoi(2);
        List<GraphEdge> edges = v.generateVoronoi(mXPoints, mYPoints, 
        		0d,//minX, 
        		DimUtil.getWidth(),//maxX, 
        		0d,//minY, 
        		DimUtil.getHeight());//maxY)
        
        Site[] sites = v.getSites();
        
        SparseArray<Site> hash = new SparseArray<Site>();
        for(Site s : sites) {
        	hash.put(s.sitenbr, s);
        }
        
        Log.d("TK","start iterating through edges");
        for(GraphEdge edge : edges) {
			hash.get(edge.site1).addEdge(edge);
			hash.get(edge.site2).addEdge(edge);
		}
        Log.d("TK","end iterate edges");
        
        Log.d("TK","start iterate sites");
        if(lastTime) {
	        //first find all on edge sites and remove them, then set adjacent to water
        	Random r = new Random();
        	int mod = r.nextInt(6) + 3;
        	int mCount = 0;
	        int nulls = 0;
	        for(int i=0; i<sites.length; i++) {
	        	if(sites[i] != null && sites[i].onTheEdge()) {
	        		if(mCount % mod == 0) {
	        			for(int j=0; j<sites[i].getRelatedSites().size(); j++) {
	        				for(int k=0; k<sites.length; k++) {
	        					if(sites[k] != null && sites[i].sitenbr == sites[i].getRelatedSites().get(j).sitenbr) {
	        						sites[k] = null;
	        						nulls++;
	        					}
	        				}
	        			}
	        		}
	        		sites[i] = null;
	        		nulls++;
	        		mCount++;
	        	}
	        }
	        Site[] temp = new Site[sites.length-nulls];
	        int tindex = 0;
	        for(int i=0; i<sites.length; i++) {
	        	if(sites[i] != null) {
	        		temp[tindex] = sites[i];
	        		tindex++;
	        	}
	        }
	        sites = temp;
        }
        
        for(Site s : sites) {
        	for(GraphEdge g : s.getEdges()) {
        		int cNbr = g.site1;
        		if(cNbr == s.sitenbr) {
        			cNbr = g.site2;
        		}
        		if(cNbr == s.sitenbr) {
        			continue;
        		}
        		s.addSite(hash.get(cNbr));
        	}
        }
        
        this.setEdges(edges, mXPoints, mYPoints, sites);
        invalidate();
	}
	
	public void retrieve(Context context) {
		Log.d("TK","starting retrieve");
		
		Cursor c2 = context.getContentResolver().query(VoronoiProvider.getCONTENT_URI(VoronoiProvider.GRAPH_EDGE_TABLE), 
				null,
				null, 
				null, null);
		ArrayList<GraphEdge> edges = new ArrayList<GraphEdge>();
		if(c2 != null && c2.moveToFirst()) {
			do {
				edges.add(GraphEdge.convertCursor(c2));
			}while(c2.moveToNext());
		}
		if(c2 != null) {
			c2.close();
		}
		
		Cursor c = context.getContentResolver().query(VoronoiProvider.getCONTENT_URI(VoronoiProvider.SITE_TABLE), 
				null, null, null, null);
		if(c != null && c.moveToFirst()) {
			mSites = new SparseArray<Site>();
			Site s;
			int i = 0;
			do {
				s = Site.convertCursor(c);
				for(GraphEdge ge : edges) {
					if(ge.site1 == s.sitenbr || ge.site2 == s.sitenbr) {
						s.addEdge(ge);
					}
				}
//				Cursor c2 = context.getContentResolver().query(VoronoiProvider.getCONTENT_URI(VoronoiProvider.GRAPH_EDGE_TABLE), 
//						null,
//						GraphEdgeColumns.SITE_ID + " = ?", 
//						new String[]{String.valueOf(s.sitenbr)}, null);
//				if(c2 != null && c2.moveToFirst()) {
//					do {
//						s.addEdge(GraphEdge.convertCursor(c2));
//					}while(c2.moveToNext());
//				}
//				if(c2 != null) {
//					c2.close();
//				}
				
				mSites.put(s.sitenbr, s);

				i++;
			}while(c.moveToNext());
		}
		if(c != null) {
			c.close();
		}
		Log.d("TK","now we have all the sites");
		//Now that we have all the sites, go through each and add the related sites.
		for(int i = 0; i<mSites.size(); i++) {
			Site s = mSites.get(mSites.keyAt(i));
			c = context.getContentResolver().query(VoronoiProvider.getCONTENT_URI(VoronoiProvider.RELATED_SITE_TABLE), 
					null, 
					RelatedSiteColumns.SITE_ID + " = ?", 
					new String[]{String.valueOf(s.sitenbr)}, null);
			if(c != null && c.moveToFirst()) {
				do {
					int relatedSite = c.getInt(c.getColumnIndex(RelatedSiteColumns.RELATED_SITE_ID));
					for(int ii = 0; ii<mSites.size(); ii++) {
						Site candidate = mSites.get(mSites.keyAt(ii));
						if(candidate.sitenbr == relatedSite) {
							s.relatedSites.add(candidate);
							break;
						}
					}
				}while(c.moveToNext());
			}
			if(c != null) {
				c.close();
			}
		}
		Log.d("TK","end retrieve");
	}
	
	public void persist(Context context) {
		for(int i = 0; i<mSites.size(); i++) {
			Site s = mSites.get(mSites.keyAt(i));
			//Save the site first.
			context.getContentResolver().insert(
					VoronoiProvider.getCONTENT_URI(VoronoiProvider.SITE_TABLE), s.getValues());
			int siteId = s.sitenbr;
			for(Site r : s.getRelatedSites()) {
				//Save related sites next.
				ContentValues values = new ContentValues();
				values.put(RelatedSiteColumns.SITE_ID, siteId);
				values.put(RelatedSiteColumns.RELATED_SITE_ID, r.sitenbr);
				context.getContentResolver().insert(
						VoronoiProvider.getCONTENT_URI(VoronoiProvider.RELATED_SITE_TABLE), values);
			}
			for(GraphEdge g : s.getEdges()) {
				//Save the graph edges last.
				context.getContentResolver().insert(
						VoronoiProvider.getCONTENT_URI(VoronoiProvider.GRAPH_EDGE_TABLE), 
						g.getValues(siteId));
			}
		}
	}

	private float mScale = 1f;
	private float mScalePointX = -1f;
	private float mScalePointY = -1f;
	
	public boolean onScale(ScaleGestureDetector detector) {
		Log.d("TK","ON SCALE");
		mScale = detector.getScaleFactor();
		mScalePointX = detector.getFocusX();
		mScalePointY = detector.getFocusY();
		Toast.makeText(getContext(), "SCALE: " + mScale, Toast.LENGTH_SHORT).show();
		invalidate();
		return false;
	}

	public boolean onScaleBegin(ScaleGestureDetector detector) {
		Log.d("TK","ON SCALE BEGIN");
		return true;
	}

	public void onScaleEnd(ScaleGestureDetector detector) {
		Log.d("TK","ON SCALE END");
	}
}
