package com.woodlawn.globaldominion.game;

import java.util.ArrayList;

public abstract class Player {
	//public ArrayList<Node> nodes = new ArrayList<Node>();
	
	public int id;
	protected MoveTakenListener mListener;
	
	public void setOnMoveTakenListener(MoveTakenListener moveTakenListener) {
		mListener = moveTakenListener;
	}
	
	public abstract void takeTurn();
}
