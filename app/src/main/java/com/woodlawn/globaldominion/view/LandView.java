package com.woodlawn.globaldominion.view;

import com.woodlawn.globaldominion.GameActivity;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class LandView extends View {

	public int occupantType = GameActivity.COMPUTER_ID;
	
	private float mStrength;
	private Paint mPaint;
	
	public LandView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mPaint = new Paint();
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}
	
	@Override
	public void onDraw(Canvas canvas) {
		
		if(occupantType == GameActivity.COMPUTER_ID) {
			canvas.drawColor(Color.RED);
		}
		else if(occupantType == GameActivity.PLAYER_ID) {
			canvas.drawColor(Color.BLUE);
		}
		
		mPaint.setColor(Color.WHITE);
		canvas.drawText("" + mStrength, 20, 20, mPaint);
		
		if(isSelected()) {
			mPaint.setStyle(Paint.Style.STROKE);
			mPaint.setStrokeWidth(5);
			canvas.drawRect(0, 0, getWidth(), getHeight(), mPaint);
			mPaint.setStyle(Paint.Style.FILL);
		}
	}
	
	public void updateUnitStrength(float strength) {
		mStrength = strength;
		invalidate();
	}
	
}
