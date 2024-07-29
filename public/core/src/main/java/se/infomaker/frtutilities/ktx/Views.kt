@file:JvmName("ViewUtils")

package se.infomaker.frtutilities.ktx

import android.app.Activity
import android.view.View

fun View.findActivity(): Activity? =
    context.findActivity()

fun View.requireActivity(): Activity =
    context.requireActivity()