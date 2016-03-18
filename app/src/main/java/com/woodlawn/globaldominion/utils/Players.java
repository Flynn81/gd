package com.woodlawn.globaldominion.utils;

import android.graphics.Color;

public class Players {

	private static final int[] colors = new int[]{Color.RED, Color.BLUE, Color.GREEN, Color.CYAN, Color.GRAY, Color.MAGENTA, Color.YELLOW};
	
	public static int color(int player) {
		if(player >= colors.length || player < 0) {
			return Color.TRANSPARENT;
		}
		return colors[player];
	}
	
}
