package se.infomaker.iap.action.display.flow.condition.view

import android.content.Context
import androidx.appcompat.widget.AppCompatEditText
import android.view.View
import se.infomaker.frtutilities.meta.ValueProvider
import se.infomaker.iap.action.display.flow.condition.Condition
import se.infomaker.iap.theme.view.ThemeableButton
import se.infomaker.iap.theme.view.ThemeableImageView
import se.infomaker.iap.theme.view.ThemeableTextView

interface ConditionalView {
    var condition: Condition?
}

class ConditionalThemeableButton @JvmOverloads constructor(context: Context, override var condition: Condition? = null) : ThemeableButton(context), ConditionalView

class ConditionalThemeableImageView @JvmOverloads constructor(context: Context, override var condition: Condition? = null) : ThemeableImageView(context), ConditionalView
class ConditionalAppCompatEditText @JvmOverloads constructor(context: Context, override var condition: Condition? = null) : AppCompatEditText(context), ConditionalView
class ConditionalThemeableTextView @JvmOverloads constructor(context: Context, override var condition: Condition? = null) : ThemeableTextView(context), ConditionalView

/**
 * Enforce showIf for condition
 */
fun View.showIf(valueProvider: ValueProvider, condition: Condition?) {
    visibility = if(condition != null) {
        if(condition.evaluate(valueProvider)) View.VISIBLE else View.GONE
    } else {
        View.VISIBLE
    }
}

fun shouldShowIf(valueProvider: ValueProvider, condition: Condition?): Boolean =
    condition?.evaluate(valueProvider)  ?: true
