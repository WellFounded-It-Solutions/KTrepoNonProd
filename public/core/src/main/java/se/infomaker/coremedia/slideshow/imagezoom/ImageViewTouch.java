/*
 * This software is provided under the MIT license.
 * Alessandro Crugnola - alessandro.crugnola@gmail.com
 *
 * The MIT License (MIT)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software
 * and associated documentation files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING
 * BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package se.infomaker.coremedia.slideshow.imagezoom;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Build;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.ScaleGestureDetector.OnScaleGestureListener;
import android.view.ViewConfiguration;

import com.jakewharton.rxrelay2.BehaviorRelay;

import timber.log.Timber;

public class ImageViewTouch extends ImageViewTouchBase {
    static final float SCROLL_DELTA_THRESHOLD = 1.0f;
    /**
     * minimum time between a scale event and a valid fling event
     */
    public static final long MIN_FLING_DELTA_TIME = 150;
    private float mScaleFactor;
    protected ScaleGestureDetector mScaleDetector;
    protected GestureDetector mGestureDetector;
    protected int mTouchSlop;
    protected int mDoubleTapDirection;
    protected OnGestureListener mGestureListener;
    protected OnScaleGestureListener mScaleListener;
    protected boolean mDoubleTapEnabled = true;
    protected boolean mScaleEnabled = true;
    protected boolean mScrollEnabled = true;
    private OnImageViewTouchDoubleTapListener mDoubleTapListener;
    private OnImageViewTouchSingleTapListener mSingleTapListener;
    public  BehaviorRelay<Integer> exitRelay =  BehaviorRelay.create();

    private Boolean dismissing = false;
    private boolean flinging = false;

    public ImageViewTouch(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ImageViewTouch(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void init(Context context, AttributeSet attrs, int defStyle) {
        super.init(context, attrs, defStyle);
        mTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
        mGestureListener = getGestureListener();
        mScaleListener = getScaleListener();

        mScaleDetector = new ScaleGestureDetector(getContext(), mScaleListener);
        mGestureDetector = new GestureDetector(getContext(), mGestureListener, null, true);
        mDoubleTapDirection = 1;
        setQuickScaleEnabled(false);
    }

    @TargetApi(19)
    public void setQuickScaleEnabled(boolean value) {
        if (Build.VERSION.SDK_INT >= 19) {
            mScaleDetector.setQuickScaleEnabled(value);
        }
    }

    @TargetApi(19)
    @SuppressWarnings("unused")
    public boolean getQuickScaleEnabled() {
        if (Build.VERSION.SDK_INT >= 19) {
            return mScaleDetector.isQuickScaleEnabled();
        }
        return false;
    }

    @SuppressWarnings("unused")
    public float getScaleFactor() {
        return mScaleFactor;
    }

    public void setDoubleTapListener(OnImageViewTouchDoubleTapListener listener) {
        mDoubleTapListener = listener;
    }

    public void setSingleTapListener(OnImageViewTouchSingleTapListener listener) {
        mSingleTapListener = listener;
    }

    public void setDoubleTapEnabled(boolean value) {
        mDoubleTapEnabled = value;
    }

    public void setScaleEnabled(boolean value) {
        mScaleEnabled = value;
    }

    public void setScrollEnabled(boolean value) {
        mScrollEnabled = value;
    }

    public boolean getDoubleTapEnabled() {
        return mDoubleTapEnabled;
    }

    protected OnGestureListener getGestureListener() {
        return new GestureListener();
    }

    protected OnScaleGestureListener getScaleListener() {
        return new ScaleListener();
    }

    @Override
    protected void onLayoutChanged(final int left, final int top, final int right, final int bottom) {
        super.onLayoutChanged(left, top, right, bottom);
        if (getDrawable() != null) {
            float imageWidth = getDrawable().getIntrinsicWidth();
            float imageHeight = getDrawable().getIntrinsicHeight();
            float screenWidth = (right - left);
            float screenHeight = (bottom - top);

            float imageScaleFactor = imageWidth / imageHeight;
            float screenScaleFactor = screenWidth / screenHeight;

            //Image scale is wider, scale image to fit height
            if (imageScaleFactor > screenScaleFactor) {
                float imageWidthScreenPercent = screenWidth / imageWidth;
                float scaledImageHeight = imageHeight * imageWidthScreenPercent;
                float imageHeightFillPercent = screenHeight / scaledImageHeight;
                Timber.d(String.valueOf(imageHeightFillPercent));
                mScaleFactor = Math.max(imageHeightFillPercent, 2.0f);
            } else { //Screen scale is wider, scale image to fit width
                float imageHeightScreenPercent = screenHeight / imageHeight;
                float scaledImageWidth = imageWidth * imageHeightScreenPercent;
                float imageWidthFillPercent = screenWidth / scaledImageWidth;
                Timber.d(String.valueOf(imageWidthFillPercent));
                mScaleFactor = Math.max(imageWidthFillPercent, 2.0f);
            }
        } else
            mScaleFactor = ((getMaxScale() - getMinScale()) / 2f) + 0.5f;
    }

    long mPointerUpTime;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (getBitmapChanged()) {
            return false;
        }

        final int action = event.getActionMasked();

        if (action == MotionEvent.ACTION_POINTER_UP) {
            mPointerUpTime = event.getEventTime();
        }

        mScaleDetector.onTouchEvent(event);

        if (!mScaleDetector.isInProgress()) {
            mGestureDetector.onTouchEvent(event);
        }

        switch (action) {
            case MotionEvent.ACTION_UP:
                return onUp(event);
            default:
                break;
        }
        return true;
    }

    @Override
    protected void onZoomAnimationCompleted(float scale) {

        if (DEBUG) {
            Timber.d("onZoomAnimationCompleted. scale: " + scale + ", minZoom: " + getMinScale());
        }

        if (scale < getMinScale()) {
            zoomTo(getMinScale(), 50);
        }
    }

    protected float onDoubleTapPost(float scale, final float maxZoom, final float minScale) {
        if (scale == minScale) {
            return mScaleFactor;
        } else {
            return minScale;
        }
    }

    public boolean onSingleTapConfirmed(MotionEvent e) {
        return true;
    }

    protected void swipeToDismiss(double dy) {
        if (!dismissing) {
            mScrollPoint.set(0f, (float) dy);
            if (mScrollPoint.y != 0) {
                postTranslate(mScrollPoint.x, mScrollPoint.y);
            }
            RectF rect = getCenter(mSuppMatrix, false, true);
            updateBackgroundAlpha(rect.top);
        }
    }

    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        if (getScale() <= 1) {
            // We're not zoomed in so we can swipe to dismiss
            swipeToDismiss(-distanceY);
            sliding = true;
            return true;
        }
        if (!canScroll()) {
            return false;
        }

        if (sliding) {
            return true;
        }

        mUserScaled = true;
        scrollBy(-distanceX, -distanceY);
        invalidate();
        return true;
    }

    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        if (getScale() <= 1F && Math.abs(velocityY) > (mMinFlingVelocity * 4)) {
            flinging = true;
            exitOnFling();
            return true;
        }

        if (!canScroll() || sliding) {
            return false;
        }
        if (DEBUG) {
            Timber.i("onFling");
        }

        if (Math.abs(velocityX) > (mMinFlingVelocity * 4) || Math.abs(velocityY) > (mMinFlingVelocity * 4)) {
            if (DEBUG) {
                Timber.v("velocity: " + velocityY);
                Timber.v("diff: " + (e2.getY() - e1.getY()));
            }

            final float scale = Math.min(Math.max(4f, getScale() / 2), 5f);

            float scaledDistanceX = ((velocityX) / mMaxFlingVelocity) * (getWidth() * scale);
            float scaledDistanceY = ((velocityY) / mMaxFlingVelocity) * (getHeight() * scale);

            if (DEBUG) {
                Timber.v("scale: " + getScale() + ", scale_final: " + scale);
                Timber.v("scaledDistanceX: " + scaledDistanceX);
                Timber.v("scaledDistanceY: " + scaledDistanceY);
            }

            mUserScaled = true;

            double total = Math.sqrt(Math.pow(scaledDistanceX, 2) + Math.pow(scaledDistanceY, 2));

            scrollBy(scaledDistanceX, scaledDistanceY, (long) Math.min(Math.max(300, total / 5), 1500));

            postInvalidate();
            return true;
        }
        return true;
    }

    public boolean onDown(MotionEvent e) {
        return !getBitmapChanged();
    }

    public boolean onUp(MotionEvent e) {
        if (getBitmapChanged()) {
            return false;
        }
        if (sliding) {
            handleScrollComplete();
           return true;
        }
        if (getScale() < getMinScale()) {
            zoomTo(getMinScale(), 50);
        }

        return true;
    }

    private void exitOnFling() {
        RectF rect = getCenter(mSuppMatrix, false, true);
        dismissing = true;
        exitRelay.accept((int) rect.top);
    }

    private void handleScrollComplete() {
        RectF rect = getCenter(mSuppMatrix, false, true);
        if (rect.top > 0) {
            if (rect.top > scrollLimit) {
                dismissing = true;
                exitRelay.accept((int) rect.top);
                return;
            }
        } else {
            if (-rect.top > scrollLimit) {
                dismissing = true;
                exitRelay.accept((int) rect.top);
                return;
            }
        }
        if (flinging) {
            return;
        }
        snapBack(rect.left, rect.top, 100);
    }

    public boolean onSingleTapUp(MotionEvent e) {
        return !getBitmapChanged();
    }

    public boolean canScroll() {
        if (getScale() > 1) {
            return true;
        }
        RectF bitmapRect = getBitmapRect();
        return !mViewPort.contains(bitmapRect);
    }

    /**
     * Determines whether this ImageViewTouch can be scrolled.
     *
     * @param direction - positive direction value means scroll from right to left,
     *                  negative value means scroll from left to right
     * @return true if there is some more place to scroll, false - otherwise.
     */
    @SuppressWarnings("unused")
    public boolean canScroll(int direction) {
        RectF bitmapRect = getBitmapRect();
        updateRect(bitmapRect, mScrollPoint);
        Rect imageViewRect = new Rect();
        getGlobalVisibleRect(imageViewRect);

        if (null == bitmapRect) {
            return false;
        }

        if (bitmapRect.right >= imageViewRect.right) {
            if (direction < 0) {
                return Math.abs(bitmapRect.right - imageViewRect.right) > SCROLL_DELTA_THRESHOLD;
            }
        }

        double bitmapScrollRectDelta = Math.abs(bitmapRect.left - mScrollPoint.x);
        return bitmapScrollRectDelta > SCROLL_DELTA_THRESHOLD;
    }

    public class GestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {

            if (null != mSingleTapListener) {
                mSingleTapListener.onSingleTapConfirmed();
            }

            return ImageViewTouch.this.onSingleTapConfirmed(e);
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            if (DEBUG) {
                Timber.i("onDoubleTap. double tap enabled? " + mDoubleTapEnabled);
            }
            if (mDoubleTapEnabled) {
                if (Build.VERSION.SDK_INT >= 19) {
                    if (mScaleDetector.isQuickScaleEnabled()) {
                        return true;
                    }
                }

                mUserScaled = true;

                float scale = getScale();
                float targetScale = onDoubleTapPost(scale, getMaxScale(), getMinScale());
                zoomTo(targetScale, e.getX(), e.getY(), mDefaultAnimationDuration);
            }

            if (null != mDoubleTapListener) {
                mDoubleTapListener.onDoubleTap();
            }

            return super.onDoubleTap(e);
        }

        @Override
        public void onLongPress(MotionEvent e) {
            if (isLongClickable()) {
                if (!mScaleDetector.isInProgress()) {
                    setPressed(true);
                    performLongClick();
                }
            }
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            if (flinging)
                return false;

            if (!mScrollEnabled) {
                return false;
            }
            if (e1 == null || e2 == null) {
                return false;
            }
            if (e1.getPointerCount() > 1 || e2.getPointerCount() > 1) {
                return false;
            }
            if (mScaleDetector.isInProgress()) {
                return false;
            }
            return ImageViewTouch.this.onScroll(e1, e2, distanceX, distanceY);
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if (!mScrollEnabled) {
                return false;
            }
            if (e1 == null || e2 == null) {
                return false;
            }
            if (e1.getPointerCount() > 1 || e2.getPointerCount() > 1) {
                return false;
            }
            if (mScaleDetector.isInProgress()) {
                return false;
            }

            final long delta = (SystemClock.uptimeMillis() - mPointerUpTime);

            // prevent fling happening just
            // after a quick pinch to zoom
            if (delta > MIN_FLING_DELTA_TIME) {
                return ImageViewTouch.this.onFling(e1, e2, velocityX, velocityY);
            } else {
                return false;
            }
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            return ImageViewTouch.this.onSingleTapUp(e);
        }

        @Override
        public boolean onDown(MotionEvent e) {
            if (DEBUG) {
                Timber.i("onDown");
            }
            stopAllAnimations();

            return ImageViewTouch.this.onDown(e);
        }
    }

    public class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        protected boolean mScaled = false;

        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            float span = detector.getCurrentSpan() - detector.getPreviousSpan();
            float targetScale = getScale() * detector.getScaleFactor();

            if (mScaleEnabled) {
                if (mScaled && span != 0) {
                    mUserScaled = true;
                    targetScale = Math.min(getMaxScale(), Math.max(targetScale, getMinScale() - MIN_SCALE_DIFF));
                    zoomTo(targetScale, detector.getFocusX(), detector.getFocusY());
                    mDoubleTapDirection = 1;
                    invalidate();
                    return true;
                }

                // This is to prevent a glitch the first time
                // image is scaled.
                if (!mScaled) {
                    mScaled = true;
                }
            }
            return true;
        }

    }

    public interface OnImageViewTouchDoubleTapListener {
        void onDoubleTap();
    }

    public interface OnImageViewTouchSingleTapListener {
        void onSingleTapConfirmed();
    }
}
