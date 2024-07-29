package se.infomaker.iap.provisioning.config

import com.google.gson.Gson
import com.google.gson.JsonObject

data class ProvisioningProviderConfig(val provider: String?, val config: JsonObject?, val createAccountApproves: List<Approvable>?) {
    fun <T> getConfig(classOf: Class<T>): T {
        return Gson().fromJson(config, classOf)
    }
}
