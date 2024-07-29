package se.infomaker.iap.provisioning.ui

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.android.billingclient.api.SkuDetails
import com.navigaglobal.mobile.R
import com.navigaglobal.mobile.databinding.ProductItemBinding
import se.infomaker.iap.theme.Theme
import java.time.Period

class ProductItemAdapter(val theme: Theme,val  onSkuDetailsSelected: (SkuDetails) -> Unit) : RecyclerView.Adapter<ProductViewHolder>() {
    var products: List<SkuDetails> = emptyList()

    fun update(list: List<SkuDetails>) {
        val old = products
        val incoming = list
        DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun areItemsTheSame(p0: Int, p1: Int): Boolean {
                val first = old[p0]
                val second = incoming[p1]
                return first.sku == second.sku
            }

            override fun getOldListSize(): Int {
                return old.size
            }

            override fun getNewListSize(): Int {
                return incoming.size
            }

            override fun areContentsTheSame(p0: Int, p1: Int): Boolean {
                val first = old[p0]
                val second = incoming[p1]
                return first == second
            }
        }).dispatchUpdatesTo(this)
        products = list
    }

    override fun onCreateViewHolder(parent: ViewGroup, type: Int): ProductViewHolder {
        val itemBinding = ProductItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        theme.apply(itemBinding.root)
        return ProductViewHolder(onSkuDetailsSelected, itemBinding)
    }

    override fun getItemCount(): Int = products.size

    override fun onBindViewHolder(vh: ProductViewHolder, index: Int) {
        vh.bind(products[index])
    }
}

class ProductViewHolder(private val onSkuDetailsSelected: (SkuDetails) -> Unit, private val itemBinding: ProductItemBinding) : RecyclerView.ViewHolder(itemBinding.root) {
    fun bind(product: SkuDetails) {
        itemBinding.title.text = product.title
        itemBinding.description.text = product.description
        itemBinding.purchaseButton.text = product.generateOfferText(itemView.context)
        itemBinding.purchaseButton.setOnClickListener {
            onSkuDetailsSelected.invoke(product)
        }
        itemBinding.footer.text= product.generateFootnote(itemView.context)
    }
}

private fun SkuDetails.generateOfferText(context: Context): CharSequence? {
    return if (freeTrialPeriod.isNotEmpty()) {
        val formattedTrial = formatTrial(context, freeTrialPeriod)
        "Prova gratis i $formattedTrial *"
    } else if (!introductoryPrice.isNullOrEmpty()) {
        val formatIntroductory = formatIntroductory(context)
        "$formatIntroductory *"
    } else {
        "$price ${formatSubscriptionPeriod(context, subscriptionPeriod)}"
    }
}

fun SkuDetails.formatIntroductory(context: Context): String {
    val period = Period.parse(introductoryPricePeriod)
    return if (introductoryPriceCycles == 1) {
        context.getString(R.string.introductoryPriceFormat, introductoryPrice, period.days)
    } else {
        val introductoryLength = period.multipliedBy(introductoryPriceCycles).days
        context.getString(R.string.introductoryPriceWithPeriodsFormat, introductoryPrice, period.days, introductoryLength)
    }
}


private fun SkuDetails.generateFootnote(context: Context): String {
    val builder = StringBuilder().append("*")
    if (!freeTrialPeriod.isNullOrEmpty() || !introductoryPrice.isNullOrEmpty()) {
        val formattedPeriod = formatSubscriptionPeriod(context, subscriptionPeriod)
        builder.append(context.getString(R.string.therafterFormat, price, formattedPeriod))
        builder.append(" ")
    }
    builder.append(context.getString(R.string.cancel_suscription_note))
    return builder.toString()
}

fun formatTrial(context: Context, subscriptionPeriod: String): String {
    val period = Period.parse(subscriptionPeriod)
    return context.resources.getQuantityString(R.plurals.days_format, period.days, period.days)
}

fun formatSubscriptionPeriod(context: Context, subscriptionPeriod: String): String {
    return when(subscriptionPeriod) {
        "P1M" -> context.getString(R.string.per_month)
        "P2M" -> context.getString(R.string.per_2_months)
        "P3M" -> context.getString(R.string.per_3_months)
        "P6M" -> context.getString(R.string.per_6_months)
        "P1Y" -> context.getString(R.string.per_year)
        else -> {
            val period = Period.parse(subscriptionPeriod)
            return "per ${period.days} dagar"
        }
    }
}
