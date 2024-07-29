package se.infomaker.livecontentui.section

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ExpandableListTracker(val expandedLists: MutableSet<String> = mutableSetOf()) : Parcelable