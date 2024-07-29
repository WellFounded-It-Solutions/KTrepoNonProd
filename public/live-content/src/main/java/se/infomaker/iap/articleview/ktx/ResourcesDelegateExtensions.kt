package se.infomaker.iap.articleview.ktx

import androidx.lifecycle.AndroidViewModel
import se.infomaker.frtutilities.ktx.ResourcesDelegate

fun AndroidViewModel.resources(
    moduleIdProducer: () -> String? = { null }
): ResourcesDelegate {
    return ResourcesDelegate(getApplication(), moduleIdProducer = moduleIdProducer)
}