package com.woodlawn.globaldominion.game;

import java.util.ArrayList;
import java.util.List;

import com.woodlawn.globaldominion.R;

import android.content.Context;
import android.util.SparseArray;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;

public class GameController implements MoveTakenListener {
	
	private static GameController mController;
	
	/**
	 * Returns an instance of a GameController.  May return null if {@link #getInstance(List)} has 
	 * not already been called before.
	 * @return
	 */
	public static GameController getInstance() {
		return mController;
	}
	
	public static GameController getInstance(List<Player> players) {
		mController = new GameController();
		
		mController.mPlayerOrder = new int[players.size()];
		int i = 0;		
		for(Player player : players) {
			player.setOnMoveTakenListener(mController);
			mController.mPlayers.put(player.id, player);
			mController.mPlayerOrder[i] = player.id;
			i++;
		}
		return mController;
	}
	
	private SparseArray<Player> mPlayers;
	private int[] mPlayerOrder;
	private int mIndex = 0;
	private List<Action> mCurrentActions = new ArrayList<Action>();
	
	public GameController() {
		
	}
		
	public void start() {
		mPlayers.get(mPlayerOrder[mIndex]).takeTurn();
	}
	
	public void pause() {
		
	}

	public void moveTaken(int playerId, List<Action> actions) {
		mIndex++;
		mCurrentActions.addAll(actions);
		if(mIndex == mPlayers.size()) {
			mIndex = 0;
			//Round is over, now execute.
			executeActions();
		}
		else {
			mPlayers.get(mPlayerOrder[mIndex]).takeTurn();
		}
	}
	
	private void executeActions() {
		//do something with mcurrentactions
		
		//determine conflicts
		
		//resolve conflicts
		
		//execute actions
		
		//done executing
		mCurrentActions.clear();
		mPlayers.get(mPlayerOrder[mIndex]).takeTurn();
	}

}
