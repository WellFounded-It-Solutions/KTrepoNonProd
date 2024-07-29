package se.infomaker.iap.provisioning.dummy

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.reactivex.Observable
import io.reactivex.Single
import se.infomaker.frtutilities.ResourceManager
import se.infomaker.iap.provisioning.LoginManager
import se.infomaker.iap.provisioning.ProvisioningManager
import se.infomaker.iap.provisioning.backend.FunctionResult
import se.infomaker.iap.provisioning.backend.LinkAccountResponse
import se.infomaker.iap.provisioning.billing.BillingManager
import se.infomaker.iap.provisioning.firebase.containsAny
import se.infomaker.iap.provisioning.permission.PermissionManager
import se.infomaker.iap.provisioning.ui.PaywallFragment
import se.infomaker.iap.provisioning.ui.PaywallFragmentGota
import java.lang.reflect.Type

class DummyProvisioningManager(context: Context) : ProvisioningManager {
var mContext=context
    override fun loginEnabled(): Boolean {
        return true
    }

    override fun purchasesEnabled(): Boolean {
        return false
    }

    override fun canPassInlinePaywall(permission: String?, products: Collection<String>?): Boolean {
        return true
    }
    lateinit var preferences: SharedPreferences
    override fun availableProducts(): Observable<Set<String>>{
        preferences= PreferenceManager.getDefaultSharedPreferences(mContext)
        preferences = mContext.getSharedPreferences("accountInfo",Context.MODE_PRIVATE)

        var pref=  preferences!!.getString("gotaProducts",null)

      //  Log.d("GotaModule","!!!!--->>>inside  ProvisioningManagerProvider prefpref gotaProducts==> "+Gson().toJson(pref) )
        val type: Type = object : TypeToken<List<String?>?>() {}.type

        var productList= mutableListOf<String>()
         productList = Gson().fromJson<Any>(pref, type) as ArrayList<String>

       // Log.d( "GotaModule","!!!!--->>>inside  ProvisioningManagerProvider prefpref==> productList "+Gson().toJson(productList) )
        val products = mutableSetOf<String>()

      //  products.add("product_bt_premium")


        products.addAll(productList)
        return Observable.just(products) //Observable.just(products)
    }// = Observable.empty()

    override fun getAuthToken(): Single<String> = Single.just("")

    override fun canDisplayContentWithPermissions(permission: List<String>): Observable<Boolean> {
var        permissionManager = PermissionManager(ResourceManager(mContext, "shared"))

       val requiredProducts = permission.map {
            permissionManager.productsForPermission(it) ?: emptyList()
        }.flatten()


        return availableProducts().map { products ->
          //  Log.d("GotaModule","!!!!--->>>inside availableProducts==> "+Gson().toJson(products) +" con ta "+products.contains("product_bt_premium")+" contains Any "+ products.containsAny(requiredProducts))

            return@map products.containsAny(requiredProducts)
        }

     //   return Observable.just(true)
    }

    override fun canDisplayContentWithPermission(permission: String): Observable<Boolean> {
        return Observable.just(true)
    }

    override fun linkAccount(): Single<FunctionResult<LinkAccountResponse>> {
        return Single.error(RuntimeException("Unsupported operation"))
    }

    override var onAppStartPermissionRevokedListener: (() -> Unit)? = null

    override fun canStartAppWithProducts(products: Collection<String>?): Boolean = true

    override fun billingManager(): BillingManager? = null

    override fun loginManager(): LoginManager? = null

    override fun checkPermissionToPassPaywall(onResult: (Boolean) -> Unit, onError: (Throwable) -> Unit) { onResult.invoke(true) }

    override fun presentAppStartPaywall(from: Activity, onComplete: () -> Unit) {
        throw UnsupportedOperationException("You should not try to present paywall for the dummy")
    }

    override fun createPaywallFragment(from: Activity, headerLayout: Int?): Fragment {
        return PaywallFragmentGota().apply {
            headerLayout?.let {
                arguments = Bundle().apply {
                    putInt(PaywallFragment.HEADER_LAYOUT, it)
                }
            }
        }
    }
    // throw UnsupportedOperationException("You can't create a paywall with this one ;)")
    //}

    override fun hasAppStartPaywall() = false

}
