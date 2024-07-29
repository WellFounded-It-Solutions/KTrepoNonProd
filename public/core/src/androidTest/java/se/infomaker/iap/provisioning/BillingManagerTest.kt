package se.infomaker.iap.provisioning

import androidx.test.InstrumentationRegistry
import androidx.test.filters.LargeTest
import androidx.test.runner.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith
import se.infomaker.iap.provisioning.backend.BackendProvider
import androidx.test.rule.ActivityTestRule
import com.android.billingclient.api.SkuDetails
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.junit.Assert
import org.junit.Rule
import se.infomaker.iap.provisioning.billing.BillingManager
import se.infomaker.iap.provisioning.store.InMemoryStore
import se.infomaker.iap.provisioning.store.KeyValueStore
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit


@RunWith(AndroidJUnit4::class)
class BillingManagerTest {

    @Rule  @JvmField
    var activityRule: ActivityTestRule<UIActivity> = ActivityTestRule(UIActivity::class.java)


    @Test
    @LargeTest
    fun getSkuDetails() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getTargetContext()
        val backend = BackendProvider.get("https://us-central1-in-app-purchase-96a64.cloudfunctions.net");
        var billingManager: BillingManager? = null
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            billingManager = BillingManager(appContext, InMemoryStore(), backend)
            billingManager?.start()
        }
        val latch = CountDownLatch(1)
        var list: List<SkuDetails>? = null

        billingManager?.skuDetails()?.subscribeOn(Schedulers.computation())?.filter { t -> t.isNotEmpty() }?.firstOrError()?.observeOn(AndroidSchedulers.mainThread())?.subscribe { blah ->
            latch.countDown()
            list = blah
        }
        latch.await(5, TimeUnit.SECONDS)
        Assert.assertNotNull(list)
    }
}