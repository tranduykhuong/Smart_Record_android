package com.devapp.smartrecord;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

public class WaveformView extends View {

    Rect rect;
    Paint paint;

    public WaveformView(Context context) {
        super(context);
    }

    public WaveformView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        rect = new Rect(0, 100, 200, 300);
        paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setStrokeWidth(40);
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawRect(rect, paint);
    }
}




