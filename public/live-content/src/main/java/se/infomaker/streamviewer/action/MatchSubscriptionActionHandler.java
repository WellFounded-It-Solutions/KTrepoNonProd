package se.infomaker.streamviewer.action;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.snackbar.Snackbar;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;

import javax.inject.Inject;

import se.infomaker.frt.moduleinterface.action.ActionHandler;
import se.infomaker.frtutilities.ConfigManager;
import se.infomaker.frtutilities.ktx.ContextUtils;
import se.infomaker.iap.action.Operation;
import se.infomaker.livecontentmanager.query.FilterHelper;
import se.infomaker.livecontentmanager.query.MatchFilter;
import se.infomaker.livecontentmanager.query.QueryFilter;
import se.infomaker.storagemodule.Storage;
import se.infomaker.storagemodule.model.MapBuilder;
import se.infomaker.storagemodule.model.Subscription;
import se.infomaker.streamviewer.FollowRecyclerViewActivity;
import com.navigaglobal.mobile.livecontent.R;
import se.infomaker.streamviewer.config.FollowConfig;
import se.infomaker.streamviewer.di.StreamNotificationSettingsHandlerFactory;
import se.infomaker.streamviewer.stream.SubscriptionUtil;
import timber.log.Timber;

public class MatchSubscriptionActionHandler implements ActionHandler {

    public static final String ADD_MATCH_SUBSCRIPTION_ACTION = "addMatchSubscription";
    public static final String REMOVE_MATCH_SUBSCRIPTION_ACTION = "removeMatchSubscription";
    public static final String SHOW_MATCH_SUBSCRIPTION_ACTION = "showMatchSubscription";
    public static final String VALUE = "value";
    public static final String NAME = "name";
    public static final String FIELD = "field";
    public static final String MODULE_ID = "moduleId";

    private final StreamNotificationSettingsHandlerFactory settingsHandlerFactory;

    @Inject
    public MatchSubscriptionActionHandler(StreamNotificationSettingsHandlerFactory settingsHandlerFactory) {
        this.settingsHandlerFactory = settingsHandlerFactory;
    }

    @Override
    public String perform(final Context context, final Operation operation) {
        // We need to modify the subscription on the main thread
        if (Looper.myLooper() != Looper.getMainLooper()) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    perform(context, operation);
                }
            });
            return "";
        }
        switch (operation.getAction()) {
            case ADD_MATCH_SUBSCRIPTION_ACTION: {
                final String name = operation.getParameter(NAME);
                final String value = operation.getParameter(VALUE);
                final String field = operation.getParameter(FIELD);
                final String moduleId = operation.getParameter(MODULE_ID);
                if (!hasValues(name, value, field)) {
                    Timber.w("Missing required values. name: " + name + " value: " + value + " field: " + field);
                    return "";
                }

                Subscription subscription = Storage.getMatchSubscription(value, field);
                if (subscription != null) {
                    Timber.w("Concept already exists");
                    return "";
                }
                Map<String, String> values = new MapBuilder()
                        .put(NAME, name)
                        .put(MODULE_ID, moduleId)
                        .put(VALUE, value)
                        .put(FIELD, field).create();

                boolean enablePushOnSubscription = ConfigManager.getInstance(context.getApplicationContext()).getConfig(moduleId, FollowConfig.class).getEnablePushOnSubscription();
                Storage.addOrUpdateSubscription(UUID.randomUUID().toString(), name, "match", values, enablePushOnSubscription, subscription1 -> {

                    settingsHandlerFactory.create(ContextUtils.requireActivity(context), moduleId, subscription1).initialActivate(context);

                    final ViewGroup viewGroup = (ViewGroup) ((ViewGroup) (ContextUtils.requireActivity(context)).findViewById(android.R.id.content)).getChildAt(0);
                    String message = String.format(context.getString(R.string.concept_added), name);
                    String action = context.getString(R.string.concept_added_action);
                    Snackbar.make(viewGroup, message, Snackbar.LENGTH_LONG)
                            .setAction(action, new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    JSONObject properties = new JSONObject();
                                    try {
                                        properties.put(NAME, name);
                                        properties.put(VALUE, value);
                                        properties.put(FIELD, field);
                                        properties.put(MODULE_ID, moduleId);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                    new Operation(SHOW_MATCH_SUBSCRIPTION_ACTION, "global", properties, null).perform(context, result -> null);
                                }
                            })
                            .show();
                    return null;
                });
                return "";
            }
            case REMOVE_MATCH_SUBSCRIPTION_ACTION: {
                String field = operation.getParameter(FIELD);
                String value = operation.getParameter(VALUE);
                String moduleId = operation.getParameter("moduleId");
                if (!hasValues(value, field)) {
                    Timber.w("Missing required values. value: " + value + " field: " + field);
                    return "";
                }
                Subscription subscription = Storage.getMatchSubscription(value, field);
                if (subscription != null) {
                    Storage.delete(subscription);
                }
                return "";
            }
            case SHOW_MATCH_SUBSCRIPTION_ACTION: {
                String field = operation.getParameter(FIELD);
                String value = operation.getParameter(VALUE);
                String moduleId = operation.getParameter("moduleId");
                String name = operation.getParameter("name");
                Intent intent;
                Bundle bundle = new Bundle();
                bundle.putString("moduleId", moduleId);
                intent = new Intent(context, FollowRecyclerViewActivity.class);
                if (SubscriptionUtil.hasMatchSubscription(field, value)) {
                    bundle.putString("subscriptionUUID", Storage.getMatchSubscription(value, field).getUuid());
                } else {
                    SubscriptionDescription description = new SubscriptionDescription.Builder()
                            .setName(name)
                            .setType("match")
                            .putParameter("name", name)
                            .putParameter(FIELD, field)
                            .putParameter("moduleId", moduleId)
                            .putParameter(VALUE, value).create();
                    bundle.putSerializable(FollowRecyclerViewActivity.SUBSCRIPTION_DESCRIPTION, description);
                }

                bundle.putString("title", name);

                ArrayList<QueryFilter> filters = new ArrayList<>();
                filters.add(new MatchFilter(field, value));
                FilterHelper.put(intent, filters);
                intent.putExtras(bundle);
                context.startActivity(intent);
                return "";
            }
            default:
                return null;
        }
    }

    private static boolean hasValues(String... values) {
        for (String value : values) {
            if (TextUtils.isEmpty(value)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean canPerform(Context context, Operation operation) {
        switch (operation.getAction()) {
            case ADD_MATCH_SUBSCRIPTION_ACTION: {
                String field = operation.getParameter(FIELD);
                String value = operation.getParameter(VALUE);
                return !SubscriptionUtil.hasMatchSubscription(field, value);
            }
            case REMOVE_MATCH_SUBSCRIPTION_ACTION: {
                String field = operation.getParameter(FIELD);
                String value = operation.getParameter(VALUE);
                return SubscriptionUtil.hasMatchSubscription(field, value);
            }
            case SHOW_MATCH_SUBSCRIPTION_ACTION: {
                return operation.getParameter(FIELD) != null
                        && operation.getParameter(VALUE) != null;
            }
            default:
                return false;
        }
    }
}
