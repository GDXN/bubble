package com.nkanaev.comics.view;

import android.content.Context;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.graphics.Matrix;
import android.os.Build;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.*;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.widget.ImageView;

import android.widget.OverScroller;
import com.nkanaev.comics.Constants;

public class PageImageView extends ImageView {

    private Constants.PageViewMode mViewMode;
    private Matrix mImageMatrix;
    private boolean mEdited;
    private OnTouchListener mOuterTouchListener;
    private ScaleGestureDetector mScaleGestureDetector;
    private GestureDetector mDragGestureDetector;
    private OverScroller mScroller;
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

    private void init() {
        mImageMatrix = getImageMatrix();
        mEdited = false;
        setScaleType(ScaleType.MATRIX);

        mScaleGestureDetector = new ScaleGestureDetector(getContext(), new PrivateScaleDetector());
        mDragGestureDetector = new GestureDetector(getContext(), new PrivateDragListener());
        super.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mEdited = true;
                mScaleGestureDetector.onTouchEvent(event);
                mDragGestureDetector.onTouchEvent(event);
                if (mOuterTouchListener != null) mOuterTouchListener.onTouch(v, event);
                return true;
            }
        });

        mScroller = new OverScroller(getContext());
        mScroller.setFriction(ViewConfiguration.getScrollFriction() * 2);
    }

    @Override
    public void setOnTouchListener(OnTouchListener l) {
        mOuterTouchListener = l;
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
        Drawable drawable = getDrawable();
        if (drawable == null) return;
        if (mEdited) return;

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
                getHeight() * 0.75f / getDrawable().getIntrinsicHeight());
        mMaxScale = Math.max(
                getWidth() * 2 / getDrawable().getIntrinsicWidth(),
                getHeight() * 2 /getDrawable().getIntrinsicHeight());

        setImageMatrix(mImageMatrix);
    }

    private class PrivateScaleDetector extends ScaleGestureDetector.SimpleOnScaleGestureListener {
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
    }

    private class PrivateDragListener extends SimpleOnGestureListener {
        @Override
        public boolean onDown(MotionEvent e) {
            mScroller.forceFinished(true);
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            mImageMatrix.postTranslate(-distanceX, -distanceY);
            setImageMatrix(mImageMatrix);
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            Point imageSize = computeCurrentImageSize();
            Point offset = computeCurrentOffset();

            int minX = -imageSize.x - PageImageView.this.getWidth();
            int minY = -imageSize.y - PageImageView.this.getHeight();
            int maxX = 0;
            int maxY = 0;

            if (offset.x > 0) {
                minX = offset.x;
                maxX = offset.x;
            }
            if (offset.y > 0) {
                minY = offset.y;
                maxY = offset.y;
            }

            mScroller.fling(
                    offset.x, offset.y,
                    (int) velocityX, (int) velocityY,
                    minX, maxX, minY, maxY);
            ViewCompat.postInvalidateOnAnimation(PageImageView.this);
            return true;
        }
    }

    @Override
    public void computeScroll() {
        if (!mScroller.isFinished() && mScroller.computeScrollOffset()) {
            int curX = mScroller.getCurrX();
            int curY = mScroller.getCurrY();

            mImageMatrix.getValues(mMatrixValues);
            mMatrixValues[Matrix.MTRANS_X] = curX;
            mMatrixValues[Matrix.MTRANS_Y] = curY;
            mImageMatrix.setValues(mMatrixValues);
            setImageMatrix(mImageMatrix);
            ViewCompat.postInvalidateOnAnimation(this);
        }
        super.computeScroll();
    }

    private Point computeCurrentImageSize() {
        final Point size = new Point();
        Drawable d = getDrawable();
        if (d != null) {
            mImageMatrix.getValues(mMatrixValues);

            float scale = mMatrixValues[Matrix.MSCALE_X];
            float width = d.getIntrinsicWidth() * scale;
            float height = d.getIntrinsicHeight() * scale;

            size.set((int)width, (int)height);

            return size;
        }

        size.set(0, 0);
        return size;
    }

    private Point computeCurrentOffset() {
        final Point offset = new Point();

        mImageMatrix.getValues(mMatrixValues);
        float transX = mMatrixValues[Matrix.MTRANS_X];
        float transY = mMatrixValues[Matrix.MTRANS_Y];

        offset.set((int)transX, (int)transY);

        return offset;
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

        Point imageSize = computeCurrentImageSize();

        int imageWidth = imageSize.x;
        int imageHeight = imageSize.y;
        int maxTransX = imageWidth - getWidth();
        int maxTransY = imageHeight - getHeight();

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

        float imageWidth = computeCurrentImageSize().x;
        float offsetX = computeCurrentOffset().x;

        if (offsetX >= 0 && direction < 0) {
            int z = 0;
            return false;
        }
        else if (Math.abs(offsetX) + getWidth() >= imageWidth && direction > 0) {
            int z = 0;
            return false;
        }
        return true;
    }
}