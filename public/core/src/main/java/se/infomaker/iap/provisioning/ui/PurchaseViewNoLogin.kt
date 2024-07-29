package se.infomaker.iap.provisioning.ui

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ScrollView
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.billingclient.api.SkuDetails
import com.navigaglobal.mobile.R
import se.infomaker.iap.theme.ThemeManager

class PurchaseViewNoLogin  @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : ScrollView(context, attrs, defStyleAttr) {

    var onSkuSelectedListener: ((SkuDetails) -> Unit)? = null
    private var adapter : ProductItemAdapter? = null

    init {
        LayoutInflater.from(context).inflate(R.layout.purchase_view_no_login_contents, this)

        val recyclerView = findViewById<RecyclerView>(R.id.productList)

        recyclerView.layoutManager = LinearLayoutManager(context)
        ThemeManager.getInstance(context).appTheme.getColor("purchaseSubscriptionDivider", null)?.let { color ->
            val dividerItemDecoration = DividerItemDecoration(recyclerView.context, DividerItemDecoration.VERTICAL)
            dividerItemDecoration.setDrawable(ColorDrawable(color.get()))
            recyclerView.addItemDecoration(dividerItemDecoration)
        }

        adapter = ProductItemAdapter(ThemeManager.getInstance(context).appTheme) { skuDetails ->
            onSkuSelectedListener?.invoke(skuDetails)
        }.also {
            recyclerView.adapter = it
        }
    }

    fun updateAdapter(list: List<SkuDetails>)  {
        adapter?.update(list)
    }
}