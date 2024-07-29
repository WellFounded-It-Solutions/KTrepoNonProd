package se.infomaker.iap.provisioning.backend

import com.google.firebase.functions.HttpsCallableResult

data class FunctionResult<T>(val body: T?, val result: HttpsCallableResult)