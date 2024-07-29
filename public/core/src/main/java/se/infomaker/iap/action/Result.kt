package se.infomaker.iap.action

import se.infomaker.frtutilities.meta.ValueProvider

data class Result(val success: Boolean, val value: ValueProvider? = null, val errorMessage: String? = null)