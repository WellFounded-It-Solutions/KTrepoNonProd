package se.infomaker.iap.provisioning

import androidx.test.InstrumentationRegistry
import androidx.test.rule.ActivityTestRule
import androidx.test.runner.AndroidJUnit4
import com.google.firebase.FirebaseApp
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import se.infomaker.iap.provisioning.backend.BackendProvider
import com.google.firebase.FirebaseOptions
import org.junit.Before
import se.infomaker.iap.provisioning.backend.LoginType


@RunWith(AndroidJUnit4::class)
class BackendTest {

    @Rule
    @JvmField
    var activityRule: ActivityTestRule<UIActivity> = ActivityTestRule(UIActivity::class.java)

    @Before
    fun setup() {
        val options = FirebaseOptions.Builder().setApplicationId("se.infomaker.iap.subscriptionlab").setProjectId("in-app-purchase-96a64").setApiKey("AIzaSyCyxfmggXgCpf6a_VQYw0GPdxJbMYMFeoY ").build()
        FirebaseApp.initializeApp(InstrumentationRegistry.getTargetContext(), options)
    }

    @Test
    fun getProducts() {
        val productInfo = BackendProvider.get("subscriptionlab").products().blockingGet()
        Assert.assertEquals(1, productInfo.body?.products?.size)
    }

    @Test
    fun getLoginUrl() {
        val result = BackendProvider.get("subscriptionlab").loginUrl("test").blockingGet()
        Assert.assertEquals("https://connectid.no/user/oauth/authorize?client_id=no.medierogledelse.app&response_type=code&redirect_uri=test", result.body?.url)
    }


    /*

    This test requires a valid session cookie, it can be obtained from a browser

    @Test
    fun testLoginWithAuthCode() {
        // This cookie was obtained from a browser where we have signed in previously
        val browserReplacement = ConnectIdBrowserReplacement(mapOf("Cookie" to "SESSION=7771aabf-dad9-4415-8340-3890f589421c; PARTNER_3_REMEMBER_ME=TE5Qb3Z4a2hTeFZCNzZHOUhVTDJlQT09OlQ0TThKRWZQcjdub3dGSUNCdUd0MGc9PQ; CONNECTID_DEVICE_COOKIE=7bfd0052-3b79-4c59-ad31-89b42e5f838e; PARTNER_10_REMEMBER_ME=OWdHVk42SjNOODBRMnFJRGk0ZXVIUT09OjVBekdUTHB6YjZmRVdoaW9WS3QyWnc9PQ; _ga=GA1.2.1554924030.1542365720; km_ai=scHOK8ta2lRUG4fm4jteJb4XaQ8%3D; kvcd=1542365804377; km_lv=1542365804"))
        val authCode = browserReplacement.getAuthCode("test").blockingGet()
        Assert.assertTrue("Failed to get auth code from media connect", authCode.isNotEmpty())
        val result = BackendProvider.get("subscriptionlab").loginAuthCode(authCode, "test").blockingGet()
        Assert.assertTrue("Failed to get token from backend", result?.body?.token?.isNotEmpty() == true)
    }

    */

    @Test
    fun testLoginType() {
        val result = BackendProvider.get("subscriptionlab").loginType().blockingGet()
        Assert.assertEquals(LoginType.URL, result.body?.loginType)
    }
}