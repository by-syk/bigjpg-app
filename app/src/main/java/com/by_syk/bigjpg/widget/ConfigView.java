package com.by_syk.bigjpg.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.by_syk.bigjpg.R;

/**
 * Created by By_syk on 2017-08-06.
 */

public class ConfigView extends FrameLayout {
    private View viewDot;
    private TextView tvConfig;

    public ConfigView(@NonNull Context context) {
        this(context, null);
    }

    public ConfigView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ConfigView(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init(context);

        loadConfig(context, attrs);
    }

    private void loadConfig(@NonNull Context context, @Nullable AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs,
                R.styleable.ConfigView);
        String text = typedArray.getString(R.styleable.ConfigView_text);
        boolean checked = typedArray.getBoolean(R.styleable.ConfigView_checked, false);
        typedArray.recycle();

        if (text != null) {
            setText(text);
        }
        setChecked(checked);
    }

    private void init(Context context) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View viewContent = inflater.inflate(R.layout.config, null);
        addView(viewContent);

        viewDot = viewContent.findViewById(R.id.dot);
        tvConfig = (TextView) viewContent.findViewById(R.id.tv);
    }

    public void setText(CharSequence text) {
        tvConfig.setText(text);
    }

    public void setChecked(boolean checked) {
        if (checked == isChecked()) {
            return;
        }

        if (checked) {
            viewDot.setScaleX(0);
            viewDot.setScaleY(0);
            viewDot.setVisibility(View.VISIBLE);
            viewDot.animate()
                    .scaleX(1.0f)
                    .scaleY(1.0f)
                    .setInterpolator(new OvershootInterpolator())
                    .start();
        } else {
            viewDot.setVisibility(View.GONE);
        }
    }

    public CharSequence getText() {
        return tvConfig.getText();
    }

    public boolean isChecked() {
        return viewDot.getVisibility() == View.VISIBLE;
    }
}
