package se.infomaker.frt;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.firebase.FirebaseApp;
import com.google.firebase.appcheck.FirebaseAppCheck;
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;
import se.infomaker.coremedia.CoreMediaManager;
import se.infomaker.coremedia.coveritlive.CoverItLiveCoreMediaPlayer;
import se.infomaker.coremedia.shootitlive.ShootItLiveCoreMediaPlayer;
import se.infomaker.coremedia.slideshow.SlideshowCoreMediaPlayer;
import se.infomaker.coremedia.solidtango.SolidTangoCoreMediaPlayer;
import se.infomaker.coremedia.youtube.YouTubeCoreMediaPlayer;
import se.infomaker.frt.logging.CrashlyticsLogTree;
import se.infomaker.frt.module.ModuleIntegrationProvider;
import se.infomaker.frt.moduleinterface.action.ActionHandlerRegister;
import se.infomaker.frt.moduleinterface.action.GlobalActionHandler;
import se.infomaker.frtutilities.ConfigManager;
import se.infomaker.frtutilities.ktx.ContextUtils;
import se.infomaker.iap.appreview.AppReviewConfig;
import se.infomaker.iap.appreview.AppReviewLifecycleObserver;
import se.infomaker.iap.theme.Theme;
import se.infomaker.iap.theme.ThemeManager;
import se.infomaker.iap.theme.color.ThemeColor;
import se.infomaker.iap.theme.ktx.ThemeUtils;
import se.infomaker.settings.SettingsHelper;
import timber.log.Timber;

public class CoreApp extends Application {
    private static final String OLD_DATA_CLEARED = "old_data_cleared";
    private static final String[] EMPTY_STRING_ARRAY = new String[0];

    @Override
    public void onCreate() {
        super.onCreate();
        LanguageManager.setup(this);

        setupAppReview();

        FirebaseApp.initializeApp(/*context=*/ this);
        FirebaseAppCheck firebaseAppCheck = FirebaseAppCheck.getInstance();
        firebaseAppCheck.installAppCheckProviderFactory(
                PlayIntegrityAppCheckProviderFactory.getInstance());

        try {
            clearOldData();
        } catch (Exception e) {
            Timber.e(e, "Failed to clear old data");
        }

        SettingsHelper.initializeSettings(getApplicationContext());
        setupMediaPlayer();
        setupGlobalActionHandler();
    }

    private void setupAppReview() {
        AppReviewConfig appReviewConfig = ConfigManager.getInstance(getApplicationContext()).getConfig("global", AppReviewConfig.class);
        if (appReviewConfig != null && appReviewConfig.getAppReview() != null && appReviewConfig.getAppReview().getEnabled() != null) {
            if (appReviewConfig.getAppReview().getEnabled()) {
                registerActivityLifecycleCallbacks(new AppReviewLifecycleObserver(getApplicationContext(), appReviewConfig));
                return;
            }
            return;
        }
        registerActivityLifecycleCallbacks(new AppReviewLifecycleObserver(getApplicationContext(), appReviewConfig));
    }

    private void setupGlobalActionHandler() {
        Observable.fromIterable(ModuleIntegrationProvider.getInstance(this).getIntegrationList())
                .distinct()
                .filter(moduleIntegration -> moduleIntegration instanceof ActionHandlerRegister)
                .doOnNext(actionHandlerRegister -> ((ActionHandlerRegister) actionHandlerRegister).registerActions(this, GlobalActionHandler.getInstance())).subscribe();
    }

    private void setupMediaPlayer() {
        Theme appTheme = ThemeManager.getInstance(this).getAppTheme();
        ThemeColor statusBarColor = ThemeUtils.getStatusBarColor(appTheme);
        CoreMediaManager.getInstance().init(new ArrayList<>(Arrays.asList(
                SolidTangoCoreMediaPlayer.class,
                CoverItLiveCoreMediaPlayer.class,
                ShootItLiveCoreMediaPlayer.class,
                YouTubeCoreMediaPlayer.class,
                SlideshowCoreMediaPlayer.class)), statusBarColor.get());
    }

    private void clearOldData() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if (prefs.getBoolean(OLD_DATA_CLEARED, false)) {
            return;
        }

        //Jump to io thread and delete cache and all old stuff
        Observable.just(new Object())
                .observeOn(Schedulers.io())
                .subscribe(o -> {
                    // Remove application data
                    deleteDir(getExternalFilesDir(null));
                    clearApplicationData();
                    // Clear shared preferences
                    prefs.edit().clear().apply();
                    prefs.edit().putBoolean(OLD_DATA_CLEARED, true).apply();
                });
    }

    private void clearApplicationData() {
        File cache = getExternalCacheDir();
        if (cache != null) {
            File appDir = new File(cache.getParent());
            if (appDir.exists()) {
                String[] children = appDir.list();
                for (String s : safe(children)) {
                    if (!s.equals("lib")) {
                        deleteDir(new File(appDir, s));
                        Timber.i("**************** File /data/data/APP_PACKAGE/%s DELETED *******************", s);
                    }
                }
            }
        }
    }

    private static String[] safe(String[] other) {
        return other == null ? EMPTY_STRING_ARRAY : other;
    }

    private boolean deleteDir(File file) {
        if (file != null) {
            if (file.isDirectory()) {
                String[] children = file.list();
                if (children == null) {
                    return false;
                }
                for (int i = 0; i < children.length; i++) {
                    boolean success = deleteDir(new File(file, children[i]));
                    if (!success) {
                        return false;
                    }
                }
            }
            return file.delete();
        }
        return false;
    }
}
