package com.woodlawn.globaldominion;

import com.woodlawn.globaldominion.map.TestMapView;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.view.View;
import android.view.View.OnClickListener;

public class MainMenuActivity extends Activity implements OnClickListener {
    
	private int mGameSize = TestMapView.SMALL;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_menu);
        
        findViewById(R.id.p).setOnClickListener(this);
        findViewById(R.id.small).setOnClickListener(this);
        findViewById(R.id.medium).setOnClickListener(this);
        findViewById(R.id.large).setOnClickListener(this);
    }

	public void onClick(View v) {
		if(v.getId() == R.id.p) {
			EditText edit = (EditText) findViewById(R.id.npe);
			int numPlayers = Integer.valueOf(edit.getText().toString());
			Intent intent = new Intent(MainMenuActivity.this, GameActivity.class);
			intent.putExtra(GameActivity.EXTRA_NUMBER_OF_PLAYERS, numPlayers);
			intent.putExtra(GameActivity.EXTRA_GAME_SIZE, mGameSize);
			startActivity(intent);
		}
		else if(v.getId() == R.id.small){
			mGameSize = TestMapView.SMALL;
		}
		else if(v.getId() == R.id.medium){
			mGameSize = TestMapView.MEDIUM;
		}
		else if(v.getId() == R.id.large){
			mGameSize = TestMapView.LARGE;
		}
	}
	
}