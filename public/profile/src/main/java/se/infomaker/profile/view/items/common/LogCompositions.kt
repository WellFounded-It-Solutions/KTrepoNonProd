package se.infomaker.profile.view.items.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import timber.log.Timber

class Ref(var value: Int)

// Note the inline function below which ensures that this function is essentially
// copied at the call site to ensure that its logging only recompositions from the
// original call site.
@Composable
fun LogCompositions(tag: String) {
    val ref = remember { Ref(0) }
    SideEffect { ref.value++ }
    Timber.d("$tag, Compositions: ${ref.value}")
}