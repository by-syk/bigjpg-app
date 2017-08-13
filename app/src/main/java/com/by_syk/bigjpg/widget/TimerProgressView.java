package com.by_syk.bigjpg.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.annotation.AttrRes;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;

import com.by_syk.bigjpg.R;

/**
 * Created by By_syk on 2017-08-05.
 */

public class TimerProgressView extends View {
    private Paint paint;

    private int sec = 1;
    private float progress = 1;

    private boolean reqStop = false;

    public TimerProgressView(@NonNull Context context) {
        this(context, null);
    }

    public TimerProgressView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TimerProgressView(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        setColor(ContextCompat.getColor(context, R.color.progress));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (progress <= 0) {
            return;
        }

        int height = getHeight();
        float progressWidth = getWidth() * (progress / sec);
        canvas.drawRect(0, 0, progressWidth, height, paint);
    }

    public void setColor(@ColorInt int color) {
        paint = new Paint();
        paint.setColor(color);
    }

    public void start(final int SEC) {
        stop();
        postDelayed(new Runnable() {
            @Override
            public void run() {
                sec = SEC;
                progress = sec;
                reqStop = false;
                post(new Task());
            }
        }, 100);
    }

    public void stop() {
        reqStop = true;
    }

    private class Task implements Runnable {
        @Override
        public void run() {
            if (reqStop) {
                sec = 1;
                progress = 1;
                invalidate();
                return;
            }
            progress -= 0.04f;
            invalidate();
            if (progress <= 0) {
                return;
            }
            postDelayed(this, 40);
        }
    }
}
