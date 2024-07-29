package se.infomaker.frt.moduleinterface.deeplink

data class UriTarget(val moduleId: String, val data: HashMap<String,String>?) {
    companion object {
        val NOT_FOUND = UriTarget("", null)
    }
}
