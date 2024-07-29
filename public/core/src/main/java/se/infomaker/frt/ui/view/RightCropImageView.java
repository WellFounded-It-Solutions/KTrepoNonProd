package se.infomaker.frt.ui.view;

import android.content.Context;
import android.graphics.Matrix;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatImageView;

/**
 * Created by Magnus Ekström on 25/11/15.
 */
public class RightCropImageView extends AppCompatImageView {

    public RightCropImageView(Context context) {
        super(context);
        setScaleType(ScaleType.MATRIX);
    }

    public RightCropImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RightCropImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        recomputeImgMatrix(left, top, right, bottom);
    }

    @Override
    protected boolean setFrame(int l, int t, int r, int b) {
        recomputeImgMatrix(l, t, r, b);
        return super.setFrame(l, t, r, b);
    }

    private void recomputeImgMatrix(int l, int t, int r, int b) {
        if (getDrawable() != null) {
            float frameWidth = r - l;
            float frameHeight = b - t;

            float originalImageWidth = (float) getDrawable().getIntrinsicWidth();
            float originalImageHeight = (float) getDrawable().getIntrinsicHeight();

            float usedScaleFactor = 1;

            float fitHorizontallyScaleFactor = frameWidth / originalImageWidth;
            float fitVerticallyScaleFactor = frameHeight / originalImageHeight;
            usedScaleFactor = Math.max(fitHorizontallyScaleFactor, fitVerticallyScaleFactor);

            float newImageWidth = originalImageWidth * usedScaleFactor;
            float newImageHeight = originalImageHeight * usedScaleFactor;

            Matrix matrix = getImageMatrix();
            matrix.setScale(usedScaleFactor, usedScaleFactor, 0, 0);
            matrix.postTranslate(frameWidth - newImageWidth, (frameHeight - newImageHeight) / 2);
            setImageMatrix(matrix);
        }
    }
}