package se.infomaker.iap.articleview.transformer

class ResourceHelper {
    companion object {
        fun getResourceString(resource: String) = javaClass.classLoader.getResourceAsStream(resource).bufferedReader().use { it.readText() }

        fun createProperties(key: String, resource: String) : Map<String, List<String>> {
            val newsML = ResourceHelper.getResourceString(resource)
            return hashMapOf(key to listOf(newsML))
        }
    }
}