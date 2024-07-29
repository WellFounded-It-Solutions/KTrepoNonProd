package se.infomaker.iap.articleview.view.modifier

import android.content.Context

object TextSizeMultiplierProvider {

    private val modifiers = mutableMapOf<List<Float>, TextSizeMultiplier>()

    fun provide(context: Context, textSizeSteps: List<Float>?): TextSizeMultiplier {

        val steps = textSizeSteps ?: listOf(1.0f, 1.2f, 1.5f)

        modifiers[steps]?.let {
            return it
        }

        return TextSizeMultiplier(context, steps).also {
            modifiers[steps] = it
        }
    }
}