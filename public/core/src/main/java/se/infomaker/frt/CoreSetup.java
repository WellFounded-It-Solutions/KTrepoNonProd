package se.infomaker.frt;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.widget.Toast;

import com.navigaglobal.mobile.R;
import com.navigaglobal.mobile.di.AppName;
import com.navigaglobal.mobile.di.PackageName;
import com.navigaglobal.mobile.di.VersionCode;
import com.navigaglobal.mobile.di.VersionName;
import com.navigaglobal.mobile.migration.MigrationRunner;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import dagger.hilt.EntryPoint;
import dagger.hilt.InstallIn;
import dagger.hilt.android.EntryPointAccessors;
import dagger.hilt.components.SingletonComponent;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import kotlin.Unit;
import kotlin.jvm.functions.Function0;
import se.infomaker.frt.moduleinterface.deeplink.OpenAppUriTargetResolver;
import se.infomaker.frt.logging.CrashlyticsLogTree;
import se.infomaker.frt.moduleinterface.deeplink.DeepLinkUrlManager;
import se.infomaker.frt.prefetch.PrefetchManager;
import se.infomaker.frt.remotenotification.PushRegistrationManager;
import se.infomaker.frtutilities.AbstractInitContentProvider;
import se.infomaker.frtutilities.ConfigManager;
import se.infomaker.frtutilities.ForegroundDetector;
import se.infomaker.frtutilities.GlobalValueManager;
import se.infomaker.frtutilities.ResourceManager;
import se.infomaker.frtutilities.ktx.ContextUtils;
import se.infomaker.frtutilities.runtimeconfiguration.OnConfigChangeListener;
import se.infomaker.iap.Route;
import se.infomaker.iap.SpringBoardManager;
import se.infomaker.iap.StateRouter;
import se.infomaker.iap.provisioning.ForegroundTracker;
import se.infomaker.iap.provisioning.ProvisioningManager;
import se.infomaker.iap.provisioning.ProvisioningManagerProvider;
import se.infomaker.iap.provisioning.ui.LoginActivityKt;
import timber.log.Timber;


public class CoreSetup extends AbstractInitContentProvider implements OnConfigChangeListener {

    @EntryPoint
    @InstallIn(SingletonComponent.class)
    interface CoreEntryPoint {
        ConfigManager configManager();
        PushRegistrationManager pushRegistrationManager();
        @VersionName String versionName();
        @VersionCode Long versionCode();
        @PackageName String packageName();
        @AppName String appName();
        MigrationRunner migrationRunner();
    }

    private final CompositeDisposable garbage = new CompositeDisposable();
    private Function0<Unit> onForegroundAction = null;
    private PrefetchManager prefetchManager;

    @Override
    public void init(@NotNull Context context) {
        setupTimber(context);
        CoreEntryPoint entryPoint = EntryPointAccessors.fromApplication(context, CoreEntryPoint.class);
        try {
            JSONObject app = new JSONObject();
            app.put("versionName", entryPoint.versionName());
            app.put("versionCode", entryPoint.versionCode());
            app.put("packageName", entryPoint.packageName());
            app.put("name", entryPoint.appName());
            GlobalValueManager.INSTANCE.put("APP", app);
        } catch (JSONException e) {
            Timber.e(e, "Failed to create app value object");
        }
        entryPoint.pushRegistrationManager().ensureRegistered();
        entryPoint.configManager().addOnConfigChangeListener(this);

        SpringBoardManager.INSTANCE.setRouter(new StateRouter() {

            private CoreRoute currentRoute = null;

            @Override
            public void route(@NotNull Context routeContext, @NotNull Intent intent, @NotNull Function0<Unit> onComplete, @NotNull Function0<Unit> onCancel) {

                // Check if there is a foreground action to handle to avoid routing and instantly restarting.
                if (handleDeferredForegroundAction()) {
                    return;
                }

                Timber.d("Routing app...");
                ProvisioningManager provisioningManager = ProvisioningManagerProvider.INSTANCE.provide(context);

                currentRoute = new CoreRoute(provisioningManager, cleanUpRouteOnInvocation(onComplete), cleanUpRouteOnInvocation(onCancel));

                if (provisioningManager.hasAppStartPaywall()) {

                    Timber.d("Setting up OnAppStartPermissionRevokedListener");
                    provisioningManager.setOnAppStartPermissionRevokedListener(() -> {
                        String errorMessage = new ResourceManager(context, "shared").getString("no_access_to_content_toast", null);
                        if (!TextUtils.isEmpty(errorMessage)) {
                            runOnUiThread(() -> Toast.makeText(context, context.getString(R.string.no_access_to_content_toast), Toast.LENGTH_LONG).show());
                        }
                        Activity foregroundActivity = ForegroundTracker.INSTANCE.getForegroundActivity();
                        if (foregroundActivity != null) {
                            LoginActivityKt.restartApp(foregroundActivity);
                        } else {
                            Timber.d("App start permission has been revoked, deferring restart until next time app enters foreground.");
                            onForegroundAction = () -> {
                                Timber.d("Running deferred restart...");
                                SpringBoardManager.INSTANCE.restart(context);
                                return Unit.INSTANCE;
                            };
                        }
                        return Unit.INSTANCE;
                    });

                    currentRoute.handleAppStartPaywall(ContextUtils.requireActivity(routeContext), intent);
                } else {
                    currentRoute.startApp(ContextUtils.requireActivity(routeContext), intent);
                }
            }

            private Function0<Unit> cleanUpRouteOnInvocation(Function0<Unit> func) {
                return () -> {
                    func.invoke();
                    currentRoute = null;
                    return Unit.INSTANCE;
                };
            }

            @Nullable
            @Override
            public Route currentRoute() {
                return currentRoute;
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            setupPrefetching(context);
        } else {
            Timber.w("Content prefetching is not supported on this device.");
        }

        garbage.add(ForegroundDetector.observable()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(foreground -> {
                    if (foreground) {
                        onForeground();
                    }
                })
        );

        DeepLinkUrlManager.INSTANCE.register(new OpenAppUriTargetResolver());
    }

    private void onForeground() {
        handleDeferredForegroundAction();
        EntryPointAccessors.fromApplication(getContext(), CoreEntryPoint.class).migrationRunner().runMigrations();
    }

    private boolean handleDeferredForegroundAction() {
        if (onForegroundAction != null) {
            onForegroundAction.invoke();
            onForegroundAction = null;
            return true;
        }
        return false;
    }

    private void runOnUiThread(Runnable action) {
        if (Thread.currentThread() != Looper.getMainLooper().getThread()) {
            new Handler(Looper.getMainLooper()).post(action);
        } else {
            action.run();
        }
    }

    private void setupTimber(Context context) {
        if (ContextUtils.isDebuggable(context)) {
            Timber.plant(new Timber.DebugTree());
        } else {
            Timber.plant(new CrashlyticsLogTree());
        }
    }

    @Override
    public Set<String> onChange(List<String> updated, List<String> removed) {
        ArrayList<String> usedResources = new ArrayList<>();
        usedResources.add("shared/configuration/modules_config.json");
        for (String resource : usedResources) {
            if (updated.contains(resource) || removed.contains(resource)) {
                HashSet<String> list = new HashSet<>();
                list.addAll(updated);
                list.addAll(removed);
                SpringBoardManager.INSTANCE.restart(getContext());
                return list;
            }
        }
        return Collections.emptySet();
    }

    public static String getApplicationName(Context context) {
        ApplicationInfo applicationInfo = context.getApplicationInfo();
        int stringId = applicationInfo.labelRes;
        return stringId == 0 ? applicationInfo.nonLocalizedLabel.toString() : context.getString(stringId);
    }

    private void setupPrefetching(Context context) {
        prefetchManager = new PrefetchManager(context);
    }
}
