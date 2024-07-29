package se.infomaker.library

import org.json.JSONObject

interface ModuleAdConfigResolver {
    /**
     * Resolve all ad configurations in the module for the provider
     */
    fun resolveConfigurations(provider: String) : List<JSONObject>
}