package se.infomaker.iap.provisioning.ui

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ScrollView
import androidx.constraintlayout.widget.ConstraintSet
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.billingclient.api.SkuDetails
import com.navigaglobal.mobile.R
import com.navigaglobal.mobile.databinding.PurchaseViewContentsBinding
import se.infomaker.iap.theme.ThemeManager
import se.infomaker.iap.theme.util.UI


class InlinePurchaseView  @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : ScrollView(context, attrs, defStyleAttr) {
    private val binding = PurchaseViewContentsBinding.inflate(LayoutInflater.from(context), this, true)

    var onSkuSelectedListener: ((SkuDetails) -> Unit)? = null
    private var adapter : ProductItemAdapter? = null

    var isLoggedIn: Boolean? = null
        set(value) {
            if (field == value) {
                return
            }
            field = value
            val state = field ?: false

            val set = ConstraintSet()
            set.clone(binding.container)
            set.clear(R.id.or, ConstraintSet.TOP)

            val target = if(state) R.id.productList else R.id.showLogin

            set.connect(R.id.or, ConstraintSet.TOP, target, ConstraintSet.BOTTOM, UI.dp2px(24F).toInt())
            binding.purchaseBarrier.referencedIds = intArrayOf(if(state) R.id.subscriptionExpired else R.id.or)

            set.clear(R.id.productList, ConstraintSet.TOP)
            val aboveProductList = if(state) R.id.subscriptionExpired else R.id.or
            set.connect(R.id.productList, ConstraintSet.TOP, aboveProductList, ConstraintSet.BOTTOM, 0)

            set.applyTo(binding.container)

            val loginVisible = if (!state) View.VISIBLE else View.GONE
            binding.loggedOutGroup.visibility = loginVisible

            val logoutVisible = if (state) View.VISIBLE else View.GONE
            binding.loggedInGroup.visibility = logoutVisible
    }

    init {
        LayoutInflater.from(context).inflate(R.layout.purchase_view_contents, this)

        binding.orGroup.visibility = View.INVISIBLE

        binding.productList.layoutManager = LinearLayoutManager(context)
        ThemeManager.getInstance(context).appTheme.getColor("purchaseSubscriptionDivider", null)?.let { color ->
            val dividerItemDecoration = DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
            dividerItemDecoration.setDrawable(ColorDrawable(color.get()))
            binding.productList.addItemDecoration(dividerItemDecoration)
        }

        adapter = ProductItemAdapter(ThemeManager.getInstance(context).appTheme) { skuDetails ->
            onSkuSelectedListener?.invoke(skuDetails)
        }.also {
            binding.productList.adapter = it
        }
    }

    fun setOnLoginClickListener(onClick: ((View) -> Unit)) = binding.showLogin.setOnClickListener(onClick)

    fun setOnLogoutClickListener(onClick: ((View) -> Unit)) = binding.showLogin.setOnClickListener(onClick)

    fun updateAdapter(list: List<SkuDetails>)  {
        if (list.isNotEmpty()) {
            binding.orGroup.visibility = View.VISIBLE
        }
        else {
            binding.orGroup.visibility = View.INVISIBLE
        }
        adapter?.update(list)
    }
}