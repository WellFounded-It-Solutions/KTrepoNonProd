package se.infomaker.livecontentui;

import android.os.Bundle;
import android.text.TextUtils;

import java.util.HashMap;
import java.util.Map;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import se.infomaker.frt.statistics.StatisticsEvent;
import se.infomaker.frt.statistics.StatisticsManager;
import se.infomaker.frtutilities.ModuleInformationManager;
import se.infomaker.livecontentmanager.parser.PropertyObject;
import se.infomaker.livecontentui.sharing.SharingResponse;
import timber.log.Timber;

public class StatsHelper {

    public static final SharingResponse NO_SHARING_RESPONSE = new SharingResponse("00000000-0000-0000-0000-000000000000", "");
    public static final String ARTICLE_URL = "articleUrl";

    private StatsHelper() {
    }

    public static void logArticleShowStatsEvent(PropertyObject propertyObject, String moduleId, String source, Single<Map<String, Object>> asyncAttributes, Single<SharingResponse> sharing) {
        StatisticsEvent.Builder builder = new StatisticsEvent.Builder();
        builder.viewShow();
        builder.moduleId(String.valueOf(moduleId));
        builder.moduleName(ModuleInformationManager.getInstance().getModuleName(moduleId));
        builder.moduleTitle(ModuleInformationManager.getInstance().getModuleTitle(moduleId));
        builder.attribute("article", new HashMap<>(propertyObject.describe()));
        builder.attribute("articleHeadline", propertyObject.optString("ArticleHeadline"));
        builder.attribute("articleUUID", propertyObject.getId());
        builder.attribute("isFrequency", propertyObject.optString("isFrequency", "false"));
        builder.attribute("isPremium", propertyObject.optString("isPremium", "false"));
        builder.viewName("article");
        if (!TextUtils.isEmpty(source)) {
            builder.attribute("source", source);
        }
        sharing = sharing != null ? sharing : Single.just(NO_SHARING_RESPONSE);
        Disposable disposable = Single.zip(sharing, asyncAttributes,
                (SharingResponse sharingResponse, Map<String, Object> attributes) ->
                {
                    if (!sharingResponse.equals(NO_SHARING_RESPONSE)) {
                        builder.attribute(ARTICLE_URL, sharingResponse.getUrl());
                    }
                    for (String key : attributes.keySet()) {
                        builder.attribute(key, attributes.get(key));
                    }
                    return builder.build();
                }).subscribeOn(AndroidSchedulers.mainThread()).subscribe(event -> StatisticsManager.getInstance().logEvent(event)
                , error -> Timber.e(error, "Failed to register stats event"));
    }

    public static void logArticleListShowStatsEvent(PropertyObject propertyObject, String moduleId) {
        logArticleListShowStatsEvent(propertyObject, moduleId, null);
    }

    public static void logArticleListShowStatsEvent(PropertyObject propertyObject, String moduleId, Bundle statsExtras) {
        StatisticsEvent.Builder builder = new StatisticsEvent.Builder();
        builder.event("articleListRowView");
        builder.moduleId(String.valueOf(moduleId));
        builder.moduleName(ModuleInformationManager.getInstance().getModuleName(moduleId));
        builder.moduleTitle(ModuleInformationManager.getInstance().getModuleTitle(moduleId));
        builder.attribute("article", new HashMap<>(propertyObject.describe()));
        builder.attribute("articleHeadline", propertyObject.optString("ArticleHeadline"));
        builder.attribute("articleUUID", propertyObject.getId());
        builder.attribute("isFrequency", propertyObject.optString("isFrequency", "false"));
        builder.attribute("isPremium", propertyObject.optString("isPremium", "false"));
        builder.attribute("userHasAccess", true);
        if (statsExtras != null) {
            for (String key : statsExtras.keySet()) {
                builder.attribute(key, statsExtras.get(key));
            }
        }
        StatisticsManager.getInstance().logEvent(builder.build());
    }

    public static void logArticleReadStatsEvent(PropertyObject propertyObject, String moduleId) {
        StatisticsEvent.Builder builder = new StatisticsEvent.Builder();
        builder.event("articleRead");
        builder.moduleId(String.valueOf(moduleId));
        builder.moduleName(ModuleInformationManager.getInstance().getModuleName(moduleId));
        builder.moduleTitle(ModuleInformationManager.getInstance().getModuleTitle(moduleId));
        builder.attribute("article", new HashMap<>(propertyObject.describe()));
        builder.attribute("articleHeadline", propertyObject.optString("ArticleHeadline"));
        builder.attribute("articleUUID", propertyObject.getId());
        builder.attribute("isFrequency", propertyObject.optString("isFrequency", "false"));
        builder.attribute("isPremium", propertyObject.optString("isPremium", "false"));
        builder.attribute("userHasAccess", true);
        StatisticsManager.getInstance().logEvent(builder.build());
    }

    public static void logBookmarkEvent(PropertyObject propertyObject, String moduleId, String bookmarkEventType, Single<Map<String, Object>> asyncAttributes) {
        StatisticsEvent.Builder builder = new StatisticsEvent.Builder();
        builder.event(bookmarkEventType);
        builder.moduleId(String.valueOf(moduleId));
        builder.moduleName(ModuleInformationManager.getInstance().getModuleName(moduleId));
        builder.moduleTitle(ModuleInformationManager.getInstance().getModuleTitle(moduleId));
        builder.attribute("article", new HashMap<>(propertyObject.describe()));
        builder.attribute("articleHeadline", propertyObject.optString("ArticleHeadline"));
        builder.attribute("articleUUID", propertyObject.getId());
        builder.attribute("isFrequency", propertyObject.optString("isFrequency", "false"));
        builder.attribute("isPremium", propertyObject.optString("isPremium", "false"));
        Disposable subscribe = asyncAttributes
                .subscribeOn(io.reactivex.android.schedulers.AndroidSchedulers.mainThread())
                .doOnError(error -> StatisticsManager.getInstance().logEvent(builder.build()))
                .subscribe(attributes -> {
                    for (String key : attributes.keySet()) {
                        builder.attribute(key, attributes.get(key));
                    }
                    StatisticsManager.getInstance().logEvent(builder.build());
                });
    }

    public static void logPackagePreviewShowStatsEvent(PropertyObject propertyObject, String moduleId, String source) {
        StatisticsEvent.Builder builder = new StatisticsEvent.Builder()
                .viewShow()
                .moduleId(String.valueOf(moduleId))
                .moduleName(ModuleInformationManager.getInstance().getModuleName(moduleId))
                .moduleTitle(ModuleInformationManager.getInstance().getModuleTitle(moduleId))
                .viewName("packagePreview");

        if (!TextUtils.isEmpty(source)) {
            builder.attribute("source", source);
        }
        StatisticsManager.getInstance().logEvent(builder.build());
    }

    public static void logShareEvent(PropertyObject propertyObject, String sharingUrl, String moduleId) {
        StatisticsEvent.Builder builder = new StatisticsEvent.Builder();
        builder.event("articleShared");
        builder.moduleId(moduleId);
        builder.moduleName(ModuleInformationManager.getInstance().getModuleName(moduleId));
        builder.moduleTitle(ModuleInformationManager.getInstance().getModuleTitle(moduleId));
        builder.attribute("article", new HashMap<>(propertyObject.describe()));
        builder.attribute("articleHeadline", propertyObject.optString("ArticleHeadline"));
        builder.attribute("articleUUID", propertyObject.getId());
        builder.attribute("isFrequency", propertyObject.optString("isFrequency", "false"));
        builder.attribute("isPremium", propertyObject.optString("isPremium", "false"));
        builder.attribute(ARTICLE_URL, sharingUrl);
        StatisticsManager.getInstance().logEvent(builder.build());
    }

    public static void logArticleViewEvent(PropertyObject propertyObject, String moduleId) {
        StatisticsEvent.Builder builder = new StatisticsEvent.Builder();
        builder.event("articleView");
        builder.moduleId(String.valueOf(moduleId));
        builder.moduleName(ModuleInformationManager.getInstance().getModuleName(moduleId));
        builder.moduleTitle(ModuleInformationManager.getInstance().getModuleTitle(moduleId));
        builder.attribute("article", new HashMap<>(propertyObject.describe()));
        builder.attribute("articleHeadline", propertyObject.optString("ArticleHeadline"));
        builder.attribute("articleUUID", propertyObject.getId());
        builder.attribute("isFrequency", propertyObject.optString("isFrequency", "false"));
        builder.attribute("isPremium", propertyObject.optString("isPremium", "false"));
        builder.attribute("userHasAccess", true);
        StatisticsManager.getInstance().logEvent(builder.build());
    }
}
