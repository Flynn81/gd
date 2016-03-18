package com.woodlawn.globaldominion.game;

import java.util.List;

public interface MoveTakenListener {
	void moveTaken(int playerId, List<Action> actions);
}
