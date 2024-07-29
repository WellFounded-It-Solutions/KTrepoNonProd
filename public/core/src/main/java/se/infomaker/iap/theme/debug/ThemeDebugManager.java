package se.infomaker.iap.theme.debug;

import android.content.Context;
import android.content.pm.ApplicationInfo;

import io.reactivex.disposables.CompositeDisposable;
import se.infomaker.frtutilities.ForegroundDetector;

public class ThemeDebugManager {

    private final CompositeDisposable garbage = new CompositeDisposable();
    private ShakeDetector shakeDetector;
    private OnThemeDebugChange listener;
    private boolean isDebug = false;

    public ThemeDebugManager(Context context) {
        if (isDebug(context)) {
            shakeDetector = new ShakeDetector(context);
            shakeDetector.setListener(() -> {
                isDebug = !isDebug;
                listener.onThemeDebugChange(isDebug);
            });
            garbage.add(ForegroundDetector.INSTANCE.observable().subscribe((isInForeground) -> {
                if (isInForeground)  {
                    shakeDetector.start();
                }
                else {
                    shakeDetector.start();
                }
            }));
        }
    }

    public OnThemeDebugChange getListener() {
        return listener;
    }

    public void setListener(OnThemeDebugChange listener) {
        this.listener = listener;
    }

    private boolean isDebug(Context context) {
        return ( 0 != ( context.getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE ) );
    }
}
