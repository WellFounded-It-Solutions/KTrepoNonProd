package se.infomaker.iap.articleview.view.modifier

import android.content.Context
import androidx.core.content.edit
import com.jakewharton.rxrelay2.BehaviorRelay
import io.reactivex.Observable

class TextSizeMultiplier(context: Context, private val sizeSteps: List<Float>) {

    private val sharedPreferences = context.getSharedPreferences(SHARED_PREFERENCES_FILE_NAME, Context.MODE_PRIVATE)
    private val multiplierRelay = BehaviorRelay.create<Float>()
    private val key = sizeSteps.asKey()

    private var index = 0
        set(value) {
            field = value % sizeSteps.size
            sharedPreferences.edit { putInt(key, field) }
            val multiplier = sizeSteps[field]
            multiplierRelay.accept(multiplier)
        }

    init {
        index = sharedPreferences.getInt(key, 0)
    }

    fun next() {
        index++
    }

    fun observable(): Observable<Float> = multiplierRelay

    companion object {
        private const val SHARED_PREFERENCES_FILE_NAME = "text_size_multipliers"
    }
}

private fun List<Float>.asKey() = joinToString(separator = ",")