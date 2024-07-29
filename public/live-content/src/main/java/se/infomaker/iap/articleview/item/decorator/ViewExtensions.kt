package se.infomaker.iap.articleview.item.decorator

import android.view.View
import android.view.ViewGroup
import com.navigaglobal.mobile.livecontent.R

internal fun View.originalMargin(marginParams: ViewGroup.MarginLayoutParams): DimensionHolder {
    return getTag(R.id.original_margin_holder) as? DimensionHolder
            ?: DimensionHolder(marginParams.leftMargin, marginParams.topMargin, marginParams.rightMargin, marginParams.bottomMargin).also {
                setTag(R.id.original_margin_holder, it)
            }
}

internal fun View.originalPadding(): DimensionHolder {
    return getTag(R.id.original_padding_holder) as? DimensionHolder
            ?: DimensionHolder(paddingLeft, paddingTop, paddingRight, paddingBottom).also {
                setTag(R.id.original_padding_holder, it)
            }
}