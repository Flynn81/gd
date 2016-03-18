package com.woodlawn.globaldominion.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

public class PathView extends View {

	private Path mPath;
	private Paint mPaint;
	private RectF mBounds;
	
	public PathView(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		mPath = new Path();
		mPath.moveTo(0, 0);
		mPath.lineTo(0, 5);
		mPath.lineTo(20, 0);
		mPath.lineTo(0, 0);
		
		mPaint = new Paint();
		//mPaint.setColor(Color.RED);
		mPaint.setAntiAlias(true);
		
		mBounds = new RectF();
		mPath.computeBounds(mBounds, true);
		setMinimumHeight((int)(mBounds.bottom - mBounds.top));
		setMinimumWidth((int)(mBounds.right - mBounds.left));
	}
	
	@Override
	public void onMeasure(int w, int h) {
		setMeasuredDimension((int)(mBounds.right - mBounds.left), (int)(mBounds.bottom - mBounds.top));
	}
	
	@Override
	public void onDraw(Canvas canvas) {
		canvas.drawColor(Color.WHITE);
		canvas.drawPath(mPath, mPaint);
	}
	
}
