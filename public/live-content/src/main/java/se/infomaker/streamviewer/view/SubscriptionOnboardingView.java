package se.infomaker.streamviewer.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.navigaglobal.mobile.livecontent.R;

public class SubscriptionOnboardingView extends FrameLayout {

    private TextView title;
    private TextView subtitle;
    private Animator currentAnimator;
    private ImageView arrowImage;
    private View promotionButton;

    public SubscriptionOnboardingView(Context context) {
        super(context);
        init();
    }

    public SubscriptionOnboardingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SubscriptionOnboardingView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public SubscriptionOnboardingView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.subscription_on_boarding, this);
        title = (TextView) findViewById(R.id.title);
        subtitle = (TextView) findViewById(R.id.subtitle);
        arrowImage = (ImageView) findViewById(R.id.arrow);
        promotionButton = findViewById(R.id.promotion_button);

        loadImage(arrowImage, "android_arrow");
    }

    public void setPromotionButtonClickListener(OnClickListener listener) {
        promotionButton.setOnClickListener(listener);
    }

    private void loadImage(ImageView view, String name) {
        int iconId = getResources().getIdentifier(name, "drawable", getContext().getPackageName());
        if (iconId > 0) {
            view.setImageResource(iconId);
        }
    }

    public void show() {
        // Make sure views are measured before we try to setup our animations
        getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                getViewTreeObserver().removeOnPreDrawListener(this);
                startShowAnimation();
                return false;
            }
        });

    }

    private void startShowAnimation() {
        if (currentAnimator != null) {
            currentAnimator.cancel();
        }
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            Animator reveal = ViewAnimationUtils.createCircularReveal(arrowImage, 0, 0, 0, Math.max(arrowImage.getWidth(), arrowImage.getHeight()));
            reveal.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationCancel(Animator animation) {
                    currentAnimator = null;
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    currentAnimator = null;
                }
            });
            currentAnimator = reveal;
            reveal.start();
        }
    }

    private Ringtone getRingtone() {
        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        return RingtoneManager.getRingtone(getContext(), notification);
    }
}
