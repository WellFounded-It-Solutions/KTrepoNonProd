package se.infomaker.iap.update

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Update(val type: UpdateType) : Parcelable