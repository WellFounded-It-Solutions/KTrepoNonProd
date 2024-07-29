package se.infomaker.streamviewer.action;

import android.app.Activity;
import android.content.Context;
import android.view.ViewGroup;

import com.google.android.material.snackbar.Snackbar;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import se.infomaker.frtutilities.ktx.ContextUtils;
import se.infomaker.storagemodule.Storage;
import se.infomaker.storagemodule.model.Subscription;
import com.navigaglobal.mobile.livecontent.R;

/**
 * Can be used to create a subscription
 */
public class SubscriptionDescription implements Serializable {
    private String name;
    private String type;
    private Map<String, String> parameters;

    private SubscriptionDescription(String name, String type, Map<String, String> parameters) {
        this.name = name;
        this.type = type;
        this.parameters = parameters;
    }

    public void save(Context context, boolean enablePushOnSubscription, Function1<Subscription, Unit> onComplete) {
        final ViewGroup viewGroup = (ViewGroup) ((ViewGroup) (ContextUtils.requireActivity(context)).findViewById(android.R.id.content)).getChildAt(0);
        String message = String.format(context.getString(R.string.concept_added), name);
        Snackbar.make(viewGroup, message, Snackbar.LENGTH_LONG).show();
        Storage.addOrUpdateSubscription(UUID.randomUUID().toString(), name, type, parameters, enablePushOnSubscription, onComplete);
    }

    public static class Builder {
        private String name;
        private String type;
        private Map<String, String> parameters = new HashMap<>();

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public Builder setType(String type) {
            this.type = type;
            return this;
        }

        public Builder putParameters(Map<String, String> parameters) {
            this.parameters.putAll(parameters);
            return this;
        }

        public Builder putParameter(String key, String value) {
            this.parameters.put(key, value);
            return this;
        }

        public SubscriptionDescription create() {
            return new SubscriptionDescription(name, type, parameters);
        }
    }
}
