package se.infomaker.streamviewer.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.media.Ringtone;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.animation.CycleInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;

import timber.log.Timber;

/**
 * Provide simple animations
 */
public class AnimatorUtil {
    public static final int SUB_ANIMATION_DURATION = 400;
    public static final int VIBRATION_ANIMATION_DURATION = 250;
    private static final String TAG = AnimatorUtil.class.getSimpleName();

    public static Animator scaleInAlphaIn(View view)
    {
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.play(ObjectAnimator.ofFloat(view, View.SCALE_X, 0.4f, 1.0f))
                .with(ObjectAnimator.ofFloat(view, View.SCALE_Y, 0.4f, 1.0f))
                .with(ObjectAnimator.ofFloat(view, View.ALPHA, 0.0f, 1.0f));
        animatorSet.setDuration(SUB_ANIMATION_DURATION);
        animatorSet.setInterpolator(new OvershootInterpolator());
        return animatorSet;
    }

    public static Animator alphaIn(View view)
    {
        ObjectAnimator animator = ObjectAnimator.ofFloat(view, View.ALPHA, 0f, 1f);
        animator.setDuration(SUB_ANIMATION_DURATION);
        animator.setInterpolator(new DecelerateInterpolator());
        return animator;
    }

    public static Animator imageAlphaIn(final ImageView view)
    {
        final ValueAnimator animator = ValueAnimator.ofInt(0, 255);
        animator.setDuration(SUB_ANIMATION_DURATION);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                view.setImageAlpha((Integer) animator.getAnimatedValue());
            }
        });
        return animator;
    }

    public static Animator bottomIn(View view)
    {
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.play(ObjectAnimator.ofFloat(view, View.SCALE_X, 0.4f, 1.0f))
                .with(ObjectAnimator.ofFloat(view, View.SCALE_Y, 0.4f, 1.0f))
                .with(ObjectAnimator.ofFloat(view, View.TRANSLATION_Y, view.getHeight(), 0));
        animatorSet.setDuration(SUB_ANIMATION_DURATION);
        animatorSet.setInterpolator(new OvershootInterpolator());
        return animatorSet;
    }

    public static Animator vibrate(final View view, final boolean vibrate, final Ringtone ringtone)
    {
        int offset = dpToPx(view.getContext(), 1);
        ObjectAnimator animator = ObjectAnimator.ofFloat(view, View.TRANSLATION_X, 0, offset, 0, -offset);
        animator.setInterpolator(new CycleInterpolator(5));
        animator.setDuration(VIBRATION_ANIMATION_DURATION);
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                if (vibrate) {
                    Vibrator vibrator = (Vibrator) view.getContext().getSystemService(Context.VIBRATOR_SERVICE);
                    vibrateCompat(vibrator);
                }
                if (ringtone != null) {
                    try {
                        ringtone.play();
                    } catch (Exception e) {
                        Timber.e(e, "Failed to play ringtone ");
                    }
                }
            }
        });
        return animator;
    }

    @SuppressWarnings("deprecation")
    private static void vibrateCompat(Vibrator vibrator) {
        if (Build.VERSION.SDK_INT >= 26) {
            vibrator.vibrate(VibrationEffect.createOneShot(VIBRATION_ANIMATION_DURATION, VibrationEffect.DEFAULT_AMPLITUDE));
        }
        else {
            vibrator.vibrate(VIBRATION_ANIMATION_DURATION);
        }
    }

    public static Animator sequence(Animator... animators)
    {
        AnimatorSet set = new AnimatorSet();
        set.playSequentially(animators);
        return set;
    }

    public static int dpToPx(Context context, int dp) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    public static Animator together(Animator... animators) {
        AnimatorSet set = new AnimatorSet();
        set.playTogether(animators);
        return set;
    }
}
