package se.infomaker.streamviewer;

import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;

import com.navigaglobal.mobile.livecontent.R;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import io.reactivex.disposables.Disposable;
import io.realm.RealmChangeListener;
import io.realm.RealmModel;
import se.infomaker.frtutilities.ConfigManager;
import se.infomaker.iap.theme.Theme;
import se.infomaker.iap.theme.color.ThemeColor;
import se.infomaker.iap.ui.theme.OverlayThemeProvider;
import se.infomaker.livecontentui.livecontentrecyclerview.activity.LiveContentRecyclerviewActivity;
import se.infomaker.storagemodule.Storage;
import se.infomaker.storagemodule.model.Subscription;
import se.infomaker.streamviewer.action.SubscriptionDescription;
import se.infomaker.streamviewer.config.FollowConfig;
import se.infomaker.streamviewer.di.StreamNotificationSettingsHandlerFactory;
import se.infomaker.streamviewer.stream.SubscriptionMenuHandler;
import se.infomaker.streamviewer.stream.SubscriptionUtil;

@AndroidEntryPoint
public class FollowRecyclerViewActivity extends LiveContentRecyclerviewActivity implements RealmChangeListener<RealmModel> {

    public static final String VIEW_NAME = "subscriptionContentList";
    public static final String SUBSCRIPTION_UUID = "subscriptionUUID";
    public static final String SUBSCRIPTION_DESCRIPTION = "subscriptionDescription";
    private String identifier;
    private String subscriptionUUID;
    private SubscriptionMenuHandler streamMenuHandler;
    private Subscription subscription;
    private Disposable rxSubscription;
    private SubscriptionDescription description;

    @Inject StreamNotificationSettingsHandlerFactory streamNotificationSettingsHandlerFactory;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        identifier = getIntent().getStringExtra("moduleId");
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        Drawable overflowIcon = toolbar.getOverflowIcon();
        if (overflowIcon != null) {
            overflowIcon.mutate();
            Theme theme = OverlayThemeProvider.forModule(this, identifier).getTheme();
            overflowIcon.setColorFilter(theme.getColor("toolbarAction", ThemeColor.WHITE).get(), PorterDuff.Mode.SRC_ATOP);
            toolbar.setOverflowIcon(overflowIcon);
        }
        subscriptionUUID = getIntent().getStringExtra(SUBSCRIPTION_UUID);
        if (subscriptionUUID != null) {
            loadSubscription();
        } else {
            description = (SubscriptionDescription) getIntent().getSerializableExtra(SUBSCRIPTION_DESCRIPTION);
        }
    }

    private void loadSubscription() {
        subscription = Storage.getSubscription(subscriptionUUID);
        rxSubscription = Storage.getSubscriptions().asFlowable().subscribe(subscriptions -> {
            if (!subscription.isValid()) {
                finish();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (rxSubscription != null) {
            rxSubscription.dispose();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (subscriptionUUID != null) {
            SubscriptionUtil.inflateStreamMenu(this, subscription, menu);
            MenuItem item = menu.findItem(R.id.pushSettings);
            item.setChecked(subscription.getPushActivated());
            streamMenuHandler = SubscriptionUtil.menuHandlerWith(findViewById(R.id.app_bar_layout), this, identifier, subscription, item, null, VIEW_NAME, streamNotificationSettingsHandlerFactory);
        } else {
            getMenuInflater().inflate(R.menu.new_subscription, menu);
        }

        for(int i = 0; i < menu.size(); i++) {
            MenuItem menuItem = menu.getItem(i);
            Drawable itemIcon = menuItem.getIcon();
            if (itemIcon != null) {
                itemIcon.mutate();
                Theme theme = OverlayThemeProvider.forModule(this, identifier).getTheme();
                itemIcon.setColorFilter(theme.getColor("toolbarAction", ThemeColor.WHITE).get(), PorterDuff.Mode.SRC_ATOP);
            }
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (streamMenuHandler != null) {
            return streamMenuHandler.onMenuItemClick(item) || super.onOptionsItemSelected(item);
        } else if (item.getItemId() == R.id.follow) {
            boolean enablePushOnSubscription = ConfigManager.getInstance(getApplicationContext()).getConfig(identifier, FollowConfig.class).getEnablePushOnSubscription();
            description.save(this, enablePushOnSubscription, subscription -> {
                streamNotificationSettingsHandlerFactory.create(FollowRecyclerViewActivity.this, identifier, subscription).initialActivate(getApplicationContext());
                subscriptionUUID = subscription.getUuid();
                getIntent().putExtra(SUBSCRIPTION_UUID, subscriptionUUID);
                loadSubscription();
                invalidateOptionsMenu();
                description = null;
                return null;
            });
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onChange(RealmModel element) {
        // close activity if the subscription is deleted
        if (!((Subscription) element).isValid()) {
            finish();
        }
    }
}
