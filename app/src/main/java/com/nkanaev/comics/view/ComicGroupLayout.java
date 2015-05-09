package com.nkanaev.comics.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;


public class ComicGroupLayout extends LinearLayout {

    private Integer[] images;

    public ComicGroupLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ComicGroupLayout(Context context) {
        super(context);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec);
    }
}
