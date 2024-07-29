package se.infomaker.iap.provisioning.credentials

import android.graphics.drawable.Drawable

data class Credential(val appLabel: String, val username: String, val password: String, val icon: Drawable? = null)