package com.woodlawn.globaldominion;

import java.util.List;
import java.util.Random;

import com.woodlawn.globaldominion.game.GameController;
import com.woodlawn.globaldominion.map.GraphEdge;
import com.woodlawn.globaldominion.map.Site;
import com.woodlawn.globaldominion.map.TestMapView;
import com.woodlawn.globaldominion.map.Voronoi;
import com.woodlawn.globaldominion.utils.DimUtil;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;

public class GameActivity extends Activity implements OnClickListener, OnTouchListener {
    
	public static final String EXTRA_NUMBER_OF_PLAYERS = "EXTRA_NUMBER_OF_PLAYERS";
	public static final String EXTRA_GAME_SIZE = "EXTRA_GAME_SIZE";
	
	public static final int PLAYER_ID = 1;
	public static final int COMPUTER_ID = 2;
	
	private int mTurn = PLAYER_ID;
	private int mSelectedNode = -1;
	
	private GameController controller;
	
	private int mNumberOfPlayers = 2;
	
	private int mGameSize = TestMapView.SMALL;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        Intent intent = getIntent();
        if(intent != null) {
        	mNumberOfPlayers = intent.getIntExtra(EXTRA_NUMBER_OF_PLAYERS, 2);
        	mGameSize = intent.getIntExtra(EXTRA_GAME_SIZE, TestMapView.SMALL);
        }
        
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        DimUtil.setHeight(metrics.heightPixels);
        DimUtil.setWidth(metrics.widthPixels);

//        Cursor cursor = getContentResolver().query(VoronoiProvider.getCONTENT_URI(VoronoiProvider.SITE_TABLE), null, null, null, null);
//        if(cursor != null) {
//        	Log.d("TK","cursor count is " + cursor.getCount());
//        	if(cursor.getCount() == 0) {
        		//generate and persist a new map
        GenTask task = new GenTask();
        task.execute();
//        	}
//        	else {
//        		//Retreive and repopulate a map
//        		((TestMapView)findViewById(R.id.test_map)).retrieve(getApplicationContext());
//        	}
//        	cursor.close();
//        }
        
        ((TestMapView)findViewById(R.id.test_map)).setOnTouchListener(this);
        
    }

	public void onClick(View v) {
		
	}
	
	public boolean onTouch(View v, MotionEvent event) {
		TestMapView tmv = (TestMapView) findViewById(R.id.test_map);
		return tmv.onTouchEvent(event);
	}
	
	private class GenTask extends AsyncTask<Void, Void, Void> {

		private List<GraphEdge> edges;
		private double[] xValuesIn;
		private double[] yValuesIn;
		private Site[] sites;
		
		@Override
		protected Void doInBackground(Void... arg0) {
	        Log.d("TK","starting generation");
    		Voronoi v = new Voronoi(2);
            xValuesIn = new double[DimUtil.getSize()];
            yValuesIn = new double[DimUtil.getSize()];
            Log.d("TK","starting gen points");
            Random r = new Random();
            for(int i=0; i<DimUtil.getSize(); i++) {
            	xValuesIn[i] = r.nextInt(DimUtil.getWidth()-5) + 5;
            	double minDistance = 5;
            	for(int j=0; j<i; j++) {
            		if(Math.abs(xValuesIn[j]-xValuesIn[i]) < minDistance) {
            			xValuesIn[i] = r.nextInt(DimUtil.getWidth()-5) + 5;
            		}
            	}
            	yValuesIn[i] = r.nextInt(DimUtil.getHeight()-5) + 5;
            	for(int j=0; j<i; j++) {
            		if(Math.abs(yValuesIn[j]-yValuesIn[i]) < minDistance) {
            			yValuesIn[i] = r.nextInt(DimUtil.getHeight()-5) + 5;
            		}
            	}
            }
            Log.d("TK","done gen points");
            Log.d("TK","start gen voronoi");
            edges = v.generateVoronoi(xValuesIn, yValuesIn, 
            		0d,//minX, 
            		DimUtil.getWidth(),//maxX, 
            		0d,//minY, 
            		DimUtil.getHeight());//maxY)
            
            Log.d("TK","end gen voronoi");
            
            sites = v.getSites();
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
            Log.d("TK","end iterate sites");
            
			return null;
		}
		
		@Override
		public void onPostExecute(Void result) {
			((TestMapView)findViewById(R.id.test_map)).setEdges(edges, xValuesIn, yValuesIn, sites);
			Log.d("TK","after set edges");
            ((TestMapView)findViewById(R.id.test_map)).relax(false);
            Log.d("TK","after relax 1");
            ((TestMapView)findViewById(R.id.test_map)).relax(false);
            Log.d("TK","after relax 2");
            ((TestMapView)findViewById(R.id.test_map)).relax(true);
            Log.d("TK","after relax 3");
            ((TestMapView)findViewById(R.id.test_map)).initSiteInformation(mGameSize, mNumberOfPlayers);
            //((TestMapView)findViewById(R.id.test_map)).persist(getApplicationContext());
            Log.d("TK","end generation");
		}
	}
	
}