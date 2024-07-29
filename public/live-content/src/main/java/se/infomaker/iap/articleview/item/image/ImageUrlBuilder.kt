package se.infomaker.iap.articleview.item.image

import android.net.Uri

interface ImageUrlBuilder {
    fun getBaseUri(): Uri?
    fun getUri(width: Int, height: Int): Uri?
    fun getFullUri(): Uri?
    fun getUriForCrop(cropData: CropData): Uri?
}