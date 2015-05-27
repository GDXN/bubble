package com.nkanaev.comics.view;

import android.content.Context;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.graphics.Matrix;
import android.os.Build;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.ImageView;

import com.nkanaev.comics.Constants;

public class PageImageView extends ImageView {

    public interface OnPageTouchListener {
        void onPageClicked(float x, float y);
    }

    private Constants.PageViewMode mViewMode;
    private Matrix mImageMatrix;
    private boolean mEdited;
    private OnPageTouchListener mPageTouchListener;
    private ScaleGestureDetector mScaleGestureDetector;
    private GestureDetector mDragGestureDetector;
    private float[] mMatrixValues = new float[9];
    private float mMinScale, mMaxScale;

    public PageImageView(Context context) {
        super(context);
        init();
    }

    public PageImageView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        init();
    }

    public void setViewMode(Constants.PageViewMode viewMode) {
        mViewMode = viewMode;
        scale();
    }

    public void setOnPageTouchListener(OnPageTouchListener listener) {
        mPageTouchListener = listener;
    }

    private void init() {
        mImageMatrix = getImageMatrix();
        mEdited = false;
        setScaleType(ScaleType.MATRIX);

        mScaleGestureDetector = new ScaleGestureDetector(getContext(), new PrivateScaleDetector());
        mDragGestureDetector = new GestureDetector(getContext(), new PrivateDragListener());
        super.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mScaleGestureDetector.onTouchEvent(event);
                mDragGestureDetector.onTouchEvent(event);
                return true;
            }
        });
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        scale();
    }

    @Override
    public void setImageDrawable(Drawable drawable) {
        super.setImageDrawable(drawable);
        scale();
    }

    private void scale() {
        if (mEdited) return;
        Drawable drawable = getDrawable();
        if (drawable == null) return;

        int dwidth = drawable.getIntrinsicWidth();
        int dheight = drawable.getIntrinsicHeight();

        int vwidth = getWidth();
        int vheight = getHeight();

        if (mViewMode == Constants.PageViewMode.ASPECT_FILL) {
            float scale;
            float dx = 0, dy = 0;

            if (dwidth * vheight > vwidth * dheight) {
                scale = (float) vheight / (float) dheight;
                dx = (vwidth - dwidth * scale) * 0.5f;
            } else {
                scale = (float) vwidth / (float) dwidth;
                dy = (vheight - dheight * scale) * 0.5f;
            }

            mImageMatrix.setScale(scale, scale);
            mImageMatrix.postTranslate((int) (dx + 0.5f), (int) (dy + 0.5f));
        }
        else if (mViewMode == Constants.PageViewMode.ASPECT_FIT) {
            RectF mTempSrc = new RectF(0, 0, dwidth, dheight);
            RectF mTempDst = new RectF(0, 0, vwidth, vheight);

            mImageMatrix.setRectToRect(mTempSrc, mTempDst, Matrix.ScaleToFit.CENTER);
        }
        else if (mViewMode == Constants.PageViewMode.FIT_WIDTH) {
            float widthScale = (float)getWidth()/drawable.getIntrinsicWidth();
            mImageMatrix.setScale(widthScale, widthScale);
            mImageMatrix.postTranslate(0, 0);
        }

        mMinScale = Math.min(
                getWidth() * 0.75f / getDrawable().getIntrinsicWidth(),
                getHeight() * 0.75f /getDrawable().getIntrinsicHeight());
        mMaxScale = Math.max(
                getWidth() * 2 / getDrawable().getIntrinsicWidth(),
                getHeight() * 2 /getDrawable().getIntrinsicHeight());

        setImageMatrix(mImageMatrix);
    }

    private class PrivateScaleDetector implements ScaleGestureDetector.OnScaleGestureListener {

        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            return true;
        }

        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            mImageMatrix.getValues(mMatrixValues);
            if (mMaxScale - mMatrixValues[Matrix.MSCALE_X] < 0.001 && detector.getScaleFactor() > 1) {
                return false;
            }

            mImageMatrix.postScale(
                    detector.getScaleFactor(), detector.getScaleFactor(),
                    detector.getFocusX(), detector.getFocusY());
            setImageMatrix(mImageMatrix);
            return true;
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {

        }
    }

    private class PrivateDragListener extends SimpleOnGestureListener {
        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            if (mPageTouchListener == null)
                return false;
            mPageTouchListener.onPageClicked(e.getX(), e.getY());
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            mImageMatrix.postTranslate(-distanceX, -distanceY);
            setImageMatrix(mImageMatrix);
            return true;
        }
    }

    @Override
    public void setImageMatrix(Matrix matrix) {
        super.setImageMatrix(fixMatrix(matrix));
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.JELLY_BEAN) {
            postInvalidate();
        }
    }

    private Matrix fixMatrix(Matrix matrix) {
        if (getDrawable() == null)
            return matrix;

        matrix.getValues(mMatrixValues);
        float scale = mMatrixValues[Matrix.MSCALE_X];
        scale = Math.max(mMinScale, Math.min(scale, mMaxScale));
        mMatrixValues[Matrix.MSCALE_X] = scale;
        mMatrixValues[Matrix.MSCALE_Y] = scale;

        float imageWidth = getDrawable().getIntrinsicWidth() * scale;
        float imageHeight = getDrawable().getIntrinsicHeight() * scale;
        float maxTransX = imageWidth - getWidth();
        float maxTransY = imageHeight - getHeight();

        if (imageWidth > getWidth())
            mMatrixValues[Matrix.MTRANS_X] = Math.min(0, Math.max(mMatrixValues[Matrix.MTRANS_X], -maxTransX));
        else
            mMatrixValues[Matrix.MTRANS_X] = getWidth() / 2 - imageWidth / 2;
        if (imageHeight > getHeight())
            mMatrixValues[Matrix.MTRANS_Y] = Math.min(0, Math.max(mMatrixValues[Matrix.MTRANS_Y], -maxTransY));
        else
            mMatrixValues[Matrix.MTRANS_Y] = getHeight() / 2 - imageHeight / 2;

        matrix.setValues(mMatrixValues);
        return matrix;
    }

    @Override
    public boolean canScrollHorizontally(int direction) {
        if (getDrawable() == null)
            return false;
        mImageMatrix.getValues(mMatrixValues);
        float x = mMatrixValues[Matrix.MTRANS_X];
        float scale = mMatrixValues[Matrix.MSCALE_X];
        float imageWidth = getDrawable().getIntrinsicWidth() * scale;

        if (imageWidth < getWidth()) {
            return false;

        } else if (x >= -1 && direction < 0) {
            return false;

        } else if (Math.abs(x) + getWidth() + 1 >= imageWidth && direction > 0) {
            return false;
        }
        return true;
    }
}