package se.infomaker.livecontentui.livecontentrecyclerview.view;

import android.view.View;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

public class LiveBinding implements View.OnAttachStateChangeListener {
    private final View view;
    private final int interval;
    private Disposable disposable;
    private final Runnable runnable;

    private LiveBinding(View view, Runnable runnable, int interval) {
        this.view = view;
        this.runnable = runnable;
        this.interval = interval;
    }

    public void recycle() {
        stop();
        view.removeOnAttachStateChangeListener(this);
    }

    public void stop() {
        if (disposable != null) {
            disposable.dispose();
            disposable = null;
        }
    }

    @Override
    public void onViewAttachedToWindow(View v) {
        start();
    }

    public void start() {
        if (disposable == null) {
            disposable = Observable.interval(interval, TimeUnit.SECONDS)
                    .observeOn(AndroidSchedulers.mainThread()).subscribe(aLong -> runnable.run());
        }
    }

    @Override
    public void onViewDetachedFromWindow(View v) {
        stop();
    }

    public static LiveBinding add(View view, Runnable runnable, int interval) {
        LiveBinding listener = new LiveBinding(view, runnable, interval);
        view.addOnAttachStateChangeListener(listener);
        return listener;
    }
}
