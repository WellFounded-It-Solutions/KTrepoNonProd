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

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.ViewConfiguration;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;

import androidx.appcompat.widget.AppCompatImageView;

import com.jakewharton.rxrelay2.BehaviorRelay;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import se.infomaker.coremedia.slideshow.imagezoom.graphics.FastBitmapDrawable;
import se.infomaker.coremedia.slideshow.imagezoom.utils.IDisposable;
import timber.log.Timber;

/**
 * Base View to manage image zoom/scrool/pinch operations
 *
 * @author alessandro
 */
public abstract class ImageViewTouchBase extends AppCompatImageView implements IDisposable {
    public static final float MIN_SCALE_DIFF = 0.1f;
    public static final float MINIMUM_SLIDE_EXIT_DISTANCE = 0.25f;

    public interface OnDrawableChangeListener {
        /**
         * Callback invoked when a new drawable has been
         * assigned to the view
         *
         * @param drawable
         */
        void onDrawableChanged(Drawable drawable);
    }

    public interface OnLayoutChangeListener {
        /**
         * Callback invoked when the layout bounds changed
         */
        void onLayoutChanged(boolean changed, int left, int top, int right, int bottom);
    }

    /**
     * Use this to change the {@link ImageViewTouchBase#setDisplayType(DisplayType)} of
     * this View
     *
     * @author alessandro
     */
    public enum DisplayType {
        /**
         * Image is not scaled by default
         */
        NONE,
        /**
         * Image will be always presented using this view's bounds
         */
        FIT_TO_SCREEN,
        /**
         * Image will be scaled only if bigger than the bounds of this view
         */
        FIT_IF_BIGGER
    }

    public static final String TAG = "ImageViewTouchBase";
    @SuppressWarnings("checkstyle:staticvariablename")
    protected static boolean DEBUG = false;
    public static final float ZOOM_INVALID = -1f;
    protected Matrix mBaseMatrix = new Matrix();
    protected Matrix mSuppMatrix = new Matrix();
    protected Matrix mNextMatrix;
    protected Runnable mLayoutRunnable = null;
    protected boolean mUserScaled = false;
    protected float mMaxZoom = ZOOM_INVALID;
    protected float mMinZoom = ZOOM_INVALID;
    // true when min and max zoom are explicitly defined
    protected boolean mMaxZoomDefined;
    protected boolean mMinZoomDefined;
    protected final Matrix mDisplayMatrix = new Matrix();
    protected final float[] mMatrixValues = new float[9];
    protected DisplayType mScaleType = DisplayType.FIT_IF_BIGGER;
    protected boolean mScaleTypeChanged;
    protected boolean mBitmapChanged;
    protected int mDefaultAnimationDuration;
    protected int mMinFlingVelocity;
    protected int mMaxFlingVelocity;
    protected PointF mCenter = new PointF();
    protected RectF mBitmapRect = new RectF();
    protected RectF mBitmapRectTmp = new RectF();
    protected RectF mCenterRect = new RectF();
    protected PointF mScrollPoint = new PointF();
    protected RectF mViewPort = new RectF();
    protected RectF mViewPortOld = new RectF();
    protected boolean sliding = false;
    protected Float scrollLimit;
    private Animator mCurrentAnimation;
    private OnDrawableChangeListener mDrawableChangeListener;
    private OnLayoutChangeListener mOnLayoutChangeListener;
    public BehaviorRelay<Float> alphaRelay =  BehaviorRelay.create();

    public ImageViewTouchBase(Context context) {
        this(context, null);
    }

    public ImageViewTouchBase(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ImageViewTouchBase(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs, defStyle);
    }

    public boolean getBitmapChanged() {
        return mBitmapChanged;
    }

    public void setOnDrawableChangedListener(OnDrawableChangeListener listener) {
        mDrawableChangeListener = listener;
    }

    public void setOnLayoutChangeListener(OnLayoutChangeListener listener) {
        mOnLayoutChangeListener = listener;
    }

    protected void init(Context context, AttributeSet attrs, int defStyle) {
        ViewConfiguration configuration = ViewConfiguration.get(context);
        mMinFlingVelocity = configuration.getScaledMinimumFlingVelocity();
        mMaxFlingVelocity = configuration.getScaledMaximumFlingVelocity();
        mDefaultAnimationDuration = getResources().getInteger(android.R.integer.config_shortAnimTime);
        setScaleType(ScaleType.MATRIX);
    }

    /**
     * Clear the current drawable
     */
    public void clear() {
        setImageBitmap(null);
    }

    /**
     * Change the display type
     *
     * @type
     */
    public void setDisplayType(DisplayType type) {
        if (type != mScaleType) {
            if (DEBUG) {
                Timber.i("setDisplayType: " + type);
            }
            mUserScaled = false;
            mScaleType = type;
            mScaleTypeChanged = true;
            requestLayout();
        }
    }

    public DisplayType getDisplayType() {
        return mScaleType;
    }

    protected void setMinScale(float value) {
        if (DEBUG) {
            Timber.d("setMinZoom: " + value);
        }

        mMinZoom = value;
    }

    protected void setMaxScale(float value) {
        if (DEBUG) {
            Timber.d("setMaxZoom: " + value);
        }
        mMaxZoom = value;
    }

    protected void onViewPortChanged(float left, float top, float right, float bottom) {
        mViewPort.set(left, top, right, bottom);
        mCenter.x = mViewPort.centerX();
        mCenter.y = mViewPort.centerY();
        scrollLimit = mViewPort.height() * MINIMUM_SLIDE_EXIT_DISTANCE;
    }

    @SuppressWarnings("checkstyle:cyclomaticcomplexity")
    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        if (DEBUG) {
            Timber.e("onLayout: " + changed + ", bitmapChanged: " + mBitmapChanged + ", scaleChanged: " + mScaleTypeChanged);
        }

        float deltaX = 0;
        float deltaY = 0;

        if (changed) {
            mViewPortOld.set(mViewPort);
            onViewPortChanged(left, top, right, bottom);

            deltaX = mViewPort.width() - mViewPortOld.width();
            deltaY = mViewPort.height() - mViewPortOld.height();
        }

        super.onLayout(changed, left, top, right, bottom);

        Runnable r = mLayoutRunnable;

        if (r != null) {
            mLayoutRunnable = null;
            r.run();
        }

        final Drawable drawable = getDrawable();

        if (drawable != null) {

            if (changed || mScaleTypeChanged || mBitmapChanged) {

                if (mBitmapChanged) {
                    mUserScaled = false;
                    mBaseMatrix.reset();
                    if (!mMinZoomDefined) {
                        mMinZoom = ZOOM_INVALID;
                    }
                    if (!mMaxZoomDefined) {
                        mMaxZoom = ZOOM_INVALID;
                    }
                }

                float scale = 1;

                // retrieve the old values
                float oldDefaultScale = getDefaultScale(getDisplayType());
                float oldMatrixScale = getScale(mBaseMatrix);
                float oldScale = getScale();
                float oldMinScale = Math.min(1f, 1f / oldMatrixScale);

                getProperBaseMatrix(drawable, mBaseMatrix, mViewPort);

                float newMatrixScale = getScale(mBaseMatrix);

                if (DEBUG) {
                    Timber.d("old matrix scale: " + oldMatrixScale);
                    Timber.d("new matrix scale: " + newMatrixScale);
                    Timber.d("old min scale: " + oldMinScale);
                    Timber.d("old scale: " + oldScale);
                }

                // 1. bitmap changed or scaletype changed
                if (mBitmapChanged || mScaleTypeChanged) {

                    if (DEBUG) {
                        Timber.d("display type: " + getDisplayType());
                        Timber.d("newMatrix: " + mNextMatrix);
                    }

                    if (mNextMatrix != null) {
                        mSuppMatrix.set(mNextMatrix);
                        mNextMatrix = null;
                        scale = getScale();
                    } else {
                        mSuppMatrix.reset();
                        scale = getDefaultScale(getDisplayType());
                    }

                    setImageMatrix(getImageViewMatrix());

                    if (scale != getScale()) {
                        if (DEBUG) {
                            Timber.v("scale != getScale: " + scale + " != " + getScale());
                        }
                        zoomTo(scale);
                    }

                } else if (changed) {

                    // 2. layout size changed

                    if (!mMinZoomDefined) {
                        mMinZoom = ZOOM_INVALID;
                    }
                    if (!mMaxZoomDefined) {
                        mMaxZoom = ZOOM_INVALID;
                    }

                    setImageMatrix(getImageViewMatrix());
                    postTranslate(-deltaX, -deltaY);

                    if (!mUserScaled) {
                        scale = getDefaultScale(getDisplayType());
                        if (DEBUG) {
                            Timber.v("!userScaled. scale=" + scale);
                        }
                        zoomTo(scale);
                    } else {
                        if (Math.abs(oldScale - oldMinScale) > MIN_SCALE_DIFF) {
                            scale = (oldMatrixScale / newMatrixScale) * oldScale;
                        }
                        if (DEBUG) {
                            Timber.v("userScaled. scale=" + scale);
                        }
                        zoomTo(scale);
                    }

                    if (DEBUG) {
                        Timber.d("old min scale: " + oldDefaultScale);
                        Timber.d("old scale: " + oldScale);
                        Timber.d("new scale: " + scale);
                    }
                }

                if (scale > getMaxScale() || scale < getMinScale()) {
                    // if current scale if outside the min/max bounds
                    // then restore the correct scale
                    zoomTo(scale);
                }

                center(true, true);

                if (mBitmapChanged) {
                    onDrawableChanged(drawable);
                }
                if (changed || mBitmapChanged || mScaleTypeChanged) {
                    onLayoutChanged(left, top, right, bottom);
                }

                if (mScaleTypeChanged) {
                    mScaleTypeChanged = false;
                }
                if (mBitmapChanged) {
                    mBitmapChanged = false;
                }

                if (DEBUG) {
                    Timber.d("scale: " + getScale() + ", minScale: " + getMinScale() + ", maxScale: " + getMaxScale());
                }
            }
        } else {
            // drawable is null
            if (mBitmapChanged) {
                onDrawableChanged(drawable);
            }
            if (changed || mBitmapChanged || mScaleTypeChanged) {
                onLayoutChanged(left, top, right, bottom);
            }

            if (mBitmapChanged) {
                mBitmapChanged = false;
            }
            if (mScaleTypeChanged) {
                mScaleTypeChanged = false;
            }
        }
    }

    @Override
    protected void onConfigurationChanged(final Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        if (DEBUG) {
            Timber.i(
                    "onConfigurationChanged. scale: " + getScale() + ", minScale: " + getMinScale() + ", mUserScaled: " + mUserScaled
            );
        }

        if (mUserScaled) {
            mUserScaled = Math.abs(getScale() - getMinScale()) > MIN_SCALE_DIFF;
        }

        if (DEBUG) {
            Timber.v("mUserScaled: " + mUserScaled);
        }
    }

    /**
     * Restore the original display
     */
    public void resetDisplay() {
        mBitmapChanged = true;
        requestLayout();
    }

    public void resetMatrix() {
        if (DEBUG) {
            Timber.i("resetMatrix");
        }
        mSuppMatrix = new Matrix();

        float scale = getDefaultScale(getDisplayType());
        setImageMatrix(getImageViewMatrix());

        if (DEBUG) {
            Timber.d("default scale: " + scale + ", scale: " + getScale());
        }

        if (scale != getScale()) {
            zoomTo(scale);
        }

        postInvalidate();
    }

    protected float getDefaultScale(DisplayType type) {
        if (type == DisplayType.FIT_TO_SCREEN) {
            // always fit to screen
            return 1f;
        } else if (type == DisplayType.FIT_IF_BIGGER) {
            // normal scale if smaller, fit to screen otherwise
            return Math.min(1f, 1f / getScale(mBaseMatrix));
        } else {
            // no scale
            return 1f / getScale(mBaseMatrix);
        }
    }

    @Override
    public void setImageResource(int resId) {
        setImageDrawable(getContext().getResources().getDrawable(resId));
    }

    /**
     * {@inheritDoc} Set the new image to display and reset the internal matrix.
     *
     * @param bitmap the {@link Bitmap} to display
     * @see {@link ImageView#setImageBitmap(Bitmap)}
     */
    @Override
    public void setImageBitmap(final Bitmap bitmap) {
        setImageBitmap(bitmap, null, ZOOM_INVALID, ZOOM_INVALID);
    }

    /**
     * @param bitmap
     * @param matrix
     * @param minZoom
     * @param maxZoom
     * @see #setImageDrawable(Drawable, Matrix, float, float)
     */
    public void setImageBitmap(final Bitmap bitmap, Matrix matrix, float minZoom, float maxZoom) {
        if (bitmap != null) {
            setImageDrawable(new FastBitmapDrawable(bitmap), matrix, minZoom, maxZoom);
        } else {
            setImageDrawable(null, matrix, minZoom, maxZoom);
        }
    }

    @Override
    public void setImageDrawable(Drawable drawable) {
        setImageDrawable(drawable, null, ZOOM_INVALID, ZOOM_INVALID);
    }

    /**
     * Note: if the scaleType is FitToScreen then min_zoom must be <= 1 and max_zoom must be >= 1
     *
     * @param drawable      the new drawable
     * @param initialMatrix the optional initial display matrix
     * @param minZoom       the optional minimum scale, pass {@link #ZOOM_INVALID} to use the default min_zoom
     * @param maxZoom       the optional maximum scale, pass {@link #ZOOM_INVALID} to use the default max_zoom
     */
    public void setImageDrawable(final Drawable drawable, final Matrix initialMatrix, final float minZoom, final float maxZoom) {
        final int viewWidth = getWidth();

        if (viewWidth <= 0) {
            mLayoutRunnable = new Runnable() {
                @Override
                public void run() {
                    setImageDrawable(drawable, initialMatrix, minZoom, maxZoom);
                }
            };
            return;
        }
        setImageDrawableInternal(drawable, initialMatrix, minZoom, maxZoom);
    }

    protected void setImageDrawableInternal(final Drawable drawable, final Matrix initialMatrix, float minZoom, float maxZoom) {
        mBaseMatrix.reset();
        super.setImageDrawable(drawable);

        if (minZoom > 0 && maxZoom > 0) {
            mMaxZoom = Math.max(minZoom, maxZoom);
            mMinZoom = Math.min(minZoom, maxZoom);
        } else {
            mMinZoom = minZoom;
            mMaxZoom = maxZoom;
        }
        mMinZoomDefined = mMinZoom != ZOOM_INVALID;
        mMaxZoomDefined = mMaxZoom != ZOOM_INVALID;


        if (initialMatrix != null) {
            mNextMatrix = new Matrix(initialMatrix);
        }
        if (DEBUG) {
            Timber.v("mMinZoom: " + mMinZoom + ", mMaxZoom: " + mMaxZoom);
        }

        mBitmapChanged = true;
        updateDrawable(drawable);
        requestLayout();
    }

    protected void updateDrawable(Drawable newDrawable) {
        if (null != newDrawable) {
            mBitmapRect.set(0, 0, newDrawable.getIntrinsicWidth(), newDrawable.getIntrinsicHeight());
        } else {
            mBitmapRect.setEmpty();
        }
    }

    /**
     * Fired as soon as a new Bitmap has been set
     *
     * @param drawable
     */
    protected void onDrawableChanged(final Drawable drawable) {
        if (DEBUG) {
            Timber.i("onDrawableChanged");
            Timber.v("scale: " + getScale() + ", minScale: " + getMinScale());
        }
        fireOnDrawableChangeListener(drawable);
    }

    protected void fireOnLayoutChangeListener(int left, int top, int right, int bottom) {
        if (null != mOnLayoutChangeListener) {
            mOnLayoutChangeListener.onLayoutChanged(true, left, top, right, bottom);
        }
    }

    protected void fireOnDrawableChangeListener(Drawable drawable) {
        if (null != mDrawableChangeListener) {
            mDrawableChangeListener.onDrawableChanged(drawable);
        }
    }

    /**
     * Called just after {@link #onLayout(boolean, int, int, int, int)}
     * if the view's bounds has changed or a new Drawable has been set
     * or the {@link DisplayType} has been modified
     *
     * @param left
     * @param top
     * @param right
     * @param bottom
     */
    protected void onLayoutChanged(int left, int top, int right, int bottom) {
        if (DEBUG) {
            Timber.i("onLayoutChanged");
        }
        fireOnLayoutChangeListener(left, top, right, bottom);
    }

    protected float computeMaxZoom() {
        final Drawable drawable = getDrawable();
        if (drawable == null) {
            return 1f;
        }
        float fw = mBitmapRect.width() / mViewPort.width();
        float fh = mBitmapRect.height() / mViewPort.height();
        float scale = Math.max(fw, fh) * 4;

        if (DEBUG) {
            Timber.i("computeMaxZoom: " + scale);
        }
        return scale;
    }

    protected float computeMinZoom() {
        if (DEBUG) {
            Timber.i("computeMinZoom");
        }

        final Drawable drawable = getDrawable();
        if (drawable == null) {
            return 1f;
        }

        float scale = getScale(mBaseMatrix);

        scale = Math.min(1f, 1f / scale);
        if (DEBUG) {
            Timber.i("computeMinZoom: " + scale);
        }

        return scale;
    }

    /**
     * Returns the current maximum allowed image scale
     *
     * @return
     */
    public float getMaxScale() {
        if (mMaxZoom == ZOOM_INVALID) {
            mMaxZoom = computeMaxZoom();
        }
        return mMaxZoom;
    }

    /**
     * Returns the current minimum allowed image scale
     *
     * @return
     */
    public float getMinScale() {
        if (DEBUG) {
            Timber.i("getMinScale, mMinZoom: " + mMinZoom);
        }

        if (mMinZoom == ZOOM_INVALID) {
            mMinZoom = computeMinZoom();
        }

        if (DEBUG) {
            Timber.v("mMinZoom: " + mMinZoom);
        }

        return mMinZoom;
    }

    /**
     * Returns the current view matrix
     *
     * @return
     */
    public Matrix getImageViewMatrix() {
        return getImageViewMatrix(mSuppMatrix);
    }

    public Matrix getImageViewMatrix(Matrix supportMatrix) {
        mDisplayMatrix.set(mBaseMatrix);
        mDisplayMatrix.postConcat(supportMatrix);
        return mDisplayMatrix;
    }

    @Override
    public void setImageMatrix(Matrix matrix) {
        Matrix current = getImageMatrix();
        boolean needUpdate = false;

        if (matrix == null && !current.isIdentity() || matrix != null && !current.equals(matrix)) {
            needUpdate = true;
        }

        super.setImageMatrix(matrix);
        if (needUpdate) {
            onImageMatrixChanged();
        }
    }

    /**
     * Called just after a new Matrix has been assigned.
     *
     * @see {@link #setImageMatrix(Matrix)}
     */
    protected void onImageMatrixChanged() {
    }

    /**
     * Returns the current image display matrix.<br />
     * This matrix can be used in the next call to the {@link #setImageDrawable(Drawable, Matrix, float, float)} to restore the same
     * view state of the previous {@link Bitmap}.<br />
     * Example:
     * <p/>
     * <pre>
     * Matrix currentMatrix = mImageView.getDisplayMatrix();
     * mImageView.setImageBitmap( newBitmap, currentMatrix, ZOOM_INVALID, ZOOM_INVALID );
     * </pre>
     *
     * @return the current support matrix
     */
    public Matrix getDisplayMatrix() {
        return new Matrix(mSuppMatrix);
    }

    protected void getProperBaseMatrix(Drawable drawable, Matrix matrix, RectF rect) {
        float w = mBitmapRect.width();
        float h = mBitmapRect.height();
        float widthScale, heightScale;

        matrix.reset();

        widthScale = rect.width() / w;
        heightScale = rect.height() / h;
        float scale = Math.min(widthScale, heightScale);
        matrix.postScale(scale, scale);
        matrix.postTranslate(rect.left, rect.top);

        float tw = (rect.width() - w * scale) / 2.0f;
        float th = (rect.height() - h * scale) / 2.0f;
        matrix.postTranslate(tw, th);
        printMatrix(matrix);
    }

    protected float getValue(Matrix matrix, int whichValue) {
        matrix.getValues(mMatrixValues);
        return mMatrixValues[whichValue];
    }

    public void printMatrix(Matrix matrix) {
        float scalex = getValue(matrix, Matrix.MSCALE_X);
        float scaley = getValue(matrix, Matrix.MSCALE_Y);
        float tx = getValue(matrix, Matrix.MTRANS_X);
        float ty = getValue(matrix, Matrix.MTRANS_Y);
        Timber.d("matrix: { x: " + tx + ", y: " + ty + ", scalex: " + scalex + ", scaley: " + scaley + " }");
    }

    public RectF getBitmapRect() {
        return getBitmapRect(mSuppMatrix);
    }

    protected RectF getBitmapRect(Matrix supportMatrix) {
        Matrix m = getImageViewMatrix(supportMatrix);
        m.mapRect(mBitmapRectTmp, mBitmapRect);
        return mBitmapRectTmp;
    }

    protected float getScale(Matrix matrix) {
        return getValue(matrix, Matrix.MSCALE_X);
    }

    @SuppressLint("Override")
    public float getRotation() {
        return 0;
    }

    /**
     * Returns the current image scale
     *
     * @return
     */
    public float getScale() {
        return getScale(mSuppMatrix);
    }

    public float getBaseScale() {
        return getScale(mBaseMatrix);
    }

    protected void center(boolean horizontal, boolean vertical) {
        final Drawable drawable = getDrawable();
        if (drawable == null) {
            return;
        }

        RectF rect = getCenter(mSuppMatrix, horizontal, vertical);

        if (rect.left != 0 || rect.top != 0) {
            postTranslate(rect.left, rect.top);
        }
    }

    protected RectF getCenter(Matrix supportMatrix, boolean horizontal, boolean vertical) {
        final Drawable drawable = getDrawable();

        if (drawable == null) {
            return new RectF(0, 0, 0, 0);
        }

        mCenterRect.set(0, 0, 0, 0);
        RectF rect = getBitmapRect(supportMatrix);
        float height = rect.height();
        float width = rect.width();
        float deltaX = 0, deltaY = 0;
        if (vertical) {
            if (height < mViewPort.height()) {
                deltaY = (mViewPort.height() - height) / 2 - (rect.top - mViewPort.top);
            } else if (rect.top > mViewPort.top) {
                deltaY = -(rect.top - mViewPort.top);
            } else if (rect.bottom < mViewPort.bottom) {
                deltaY = mViewPort.bottom - rect.bottom;
            }
        }
        if (horizontal) {
            if (width < mViewPort.width()) {
                deltaX = (mViewPort.width() - width) / 2 - (rect.left - mViewPort.left);
            } else if (rect.left > mViewPort.left) {
                deltaX = -(rect.left - mViewPort.left);
            } else if (rect.right < mViewPort.right) {
                deltaX = mViewPort.right - rect.right;
            }
        }
        mCenterRect.set(deltaX, deltaY, 0, 0);
        return mCenterRect;
    }

    protected void postTranslate(float deltaX, float deltaY) {
        if (deltaX != 0 || deltaY != 0) {
            mSuppMatrix.postTranslate(deltaX, deltaY);
            setImageMatrix(getImageViewMatrix());
        }
    }

    protected void postScale(float scale, float centerX, float centerY) {
        mSuppMatrix.postScale(scale, scale, centerX, centerY);
        setImageMatrix(getImageViewMatrix());
    }

    protected PointF getCenter() {
        return mCenter;
    }

    protected void zoomTo(float scale) {
        if (DEBUG) {
            Timber.i("zoomTo: " + scale);
        }

        if (scale > getMaxScale()) {
            scale = getMaxScale();
        }
        if (scale < getMinScale()) {
            scale = getMinScale();
        }

        if (DEBUG) {
            Timber.d("sanitized scale: " + scale);
        }

        PointF center = getCenter();
        zoomTo(scale, center.x, center.y);
    }

    /**
     * Scale to the target scale
     *
     * @param scale      the target zoom
     * @param durationMs the animation duration
     */
    public void zoomTo(float scale, long durationMs) {
        PointF center = getCenter();
        zoomTo(scale, center.x, center.y, durationMs);
    }

    protected void zoomTo(float scale, float centerX, float centerY) {
        if (scale > getMaxScale()) {
            scale = getMaxScale();
        }

        float oldScale = getScale();
        float deltaScale = scale / oldScale;
        postScale(deltaScale, centerX, centerY);
        onZoom(scale);
        center(true, true);
        broadcastToScaleListener(scale);
    }

    @SuppressWarnings("unused")
    protected void onZoom(float scale) {
    }

    @SuppressWarnings("unused")
    protected void onZoomAnimationCompleted(float scale) {
    }

    /**
     * Scrolls the view by the x and y amount
     *
     * @param x
     * @param y
     */
    public void scrollBy(float x, float y) {
        panBy(x, y);
    }

    private void internalPanBy(double dx, double dy) {
        RectF rect = getBitmapRect();
        mScrollPoint.set((float) dx, (float) dy);
        updateRect(rect, mScrollPoint);

        if (mScrollPoint.x != 0 || mScrollPoint.y != 0) {
            postTranslate(mScrollPoint.x, mScrollPoint.y);
        }
    }
    protected void panBy(double dx, double dy) {
        internalPanBy(dx, dy);
        center(true, true);
    }

    protected void panByNoCenter(double dx, double dy) {
        internalPanBy(dx, dy);
    }

    protected void updateRect(RectF bitmapRect, PointF scrollRect) {
        if (bitmapRect == null) {
            return;
        }
    }

    protected void stopAllAnimations() {
        if (null != mCurrentAnimation) {
            mCurrentAnimation.cancel();
            mCurrentAnimation = null;
        }
    }

    protected void snapBack(float distanceX, float distanceY, final long durationMs) {
        final ValueAnimator anim2 = ValueAnimator.ofFloat(-distanceY, 0).setDuration(durationMs);
         stopAllAnimations();

        mCurrentAnimation = new AnimatorSet();
        ((AnimatorSet) mCurrentAnimation).playTogether(
               anim2
        );

        mCurrentAnimation.setDuration(durationMs);
        mCurrentAnimation.setInterpolator(new DecelerateInterpolator());
        mCurrentAnimation.start();

        anim2.addUpdateListener(
                new ValueAnimator.AnimatorUpdateListener() {
                    float oldValueY = -distanceY;

                    @Override
                    public void onAnimationUpdate(final ValueAnimator animation) {
                        float valueY = (Float) anim2.getAnimatedValue();
                        updateBackgroundAlpha(valueY);
                        panByNoCenter(0, valueY - oldValueY);
                        oldValueY = valueY;
                        postInvalidateOnAnimation();
                    }
                }
        );

        mCurrentAnimation.addListener(
                new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(final Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(final Animator animation) {
                        sliding = false;
                    }

                    @Override
                    public void onAnimationCancel(final Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(final Animator animation) {

                    }
                }
        );
    }

    protected void updateBackgroundAlpha(float y) {
        float alpha = y > 0 ? 1 - y / scrollLimit : 1 + (y / scrollLimit);
        alphaRelay.accept(alpha);
    }

    protected void scrollBy(float distanceX, float distanceY, final long durationMs) {
        final ValueAnimator anim1 = ValueAnimator.ofFloat(0, distanceX).setDuration(durationMs);
        final ValueAnimator anim2 = ValueAnimator.ofFloat(0, distanceY).setDuration(durationMs);

        stopAllAnimations();

        mCurrentAnimation = new AnimatorSet();
        ((AnimatorSet) mCurrentAnimation).playTogether(
                anim1, anim2
        );

        mCurrentAnimation.setDuration(durationMs);
        mCurrentAnimation.setInterpolator(new DecelerateInterpolator());
        mCurrentAnimation.start();

        anim2.addUpdateListener(
                new ValueAnimator.AnimatorUpdateListener() {
                    float oldValueX = 0;
                    float oldValueY = 0;

                    @Override
                    public void onAnimationUpdate(final ValueAnimator animation) {
                        float valueX = (Float) anim1.getAnimatedValue();
                        float valueY = (Float) anim2.getAnimatedValue();
                        panBy(valueX - oldValueX, valueY - oldValueY);
                        oldValueX = valueX;
                        oldValueY = valueY;
                        postInvalidateOnAnimation();
                    }
                }
        );

        mCurrentAnimation.addListener(
                new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(final Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(final Animator animation) {
                        RectF centerRect = getCenter(mSuppMatrix, true, true);
                        if (centerRect.left != 0 || centerRect.top != 0) {
                            scrollBy(centerRect.left, centerRect.top);
                        }
                    }

                    @Override
                    public void onAnimationCancel(final Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(final Animator animation) {

                    }
                }
        );
    }

    List<ScaleListener> scaleListeners = new ArrayList<>();

    protected void zoomTo(float scale, float centerX, float centerY, final long durationMs) {
        if (scale > getMaxScale()) {
            scale = getMaxScale();
        }

        final float oldScale = getScale();

        Matrix m = new Matrix(mSuppMatrix);
        m.postScale(scale, scale, centerX, centerY);
        RectF rect = getCenter(m, true, true);

        final float finalScale = scale;
        final float destX = centerX + rect.left * scale;
        final float destY = centerY + rect.top * scale;

        stopAllAnimations();

        ValueAnimator animation = ValueAnimator.ofFloat(oldScale, finalScale);
        animation.setDuration(durationMs);
        animation.setInterpolator(new DecelerateInterpolator(1.0f));
        animation.addUpdateListener(
                animation1 -> {
                    float value = (Float) animation1.getAnimatedValue();
                    zoomTo(value, destX, destY);
                    postInvalidateOnAnimation();
                }
        );
        animation.start();
    }

    private void broadcastToScaleListener(float scale) {
        for (ScaleListener scaleListener : scaleListeners) {
            scaleListener.scaleChanged(scale);
        }
    }

    public void addScaleListener(@NotNull ScaleListener listener) {
        scaleListeners.add(listener);
    }

    public void removeScaleListener(@NotNull ScaleListener listener) {
        scaleListeners.remove(listener);
    }

    @Override
    public void dispose() {
        clear();
    }

    @Override
    protected void onDraw(final Canvas canvas) {

        if (getScaleType() == ScaleType.FIT_XY) {
            final Drawable drawable = getDrawable();
            if (null != drawable) {
                drawable.draw(canvas);
            }
        } else {
            super.onDraw(canvas);
        }
    }

    public interface ScaleListener {
        void scaleChanged(float scale);
    }
}
