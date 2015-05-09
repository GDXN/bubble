package com.nkanaev.comics.view;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;

public class CoverImageView extends ImageView {

    public CoverImageView(Context context) {
        super(context);
    }

    public CoverImageView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    @Override
    public void setImageDrawable(Drawable drawable) {
        super.setImageDrawable(drawable);
        scale();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        scale();
    }

    private void scale() {
        Drawable drawable = getDrawable();
        if (drawable != null) {
            int width = drawable.getIntrinsicWidth();
            int height = drawable.getIntrinsicHeight();
            double ratio = (double)height/(double)width;
            if (Math.abs(ratio - 1.5d) < 0.1) {
                setScaleType(ScaleType.CENTER_CROP);
            }
            else {
                setScaleType(ScaleType.FIT_CENTER);
            }
        }
    }
}
