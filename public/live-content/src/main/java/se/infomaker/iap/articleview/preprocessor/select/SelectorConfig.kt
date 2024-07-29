package se.infomaker.iap.articleview.preprocessor.select

class SelectorConfig(var type: String? = null,
                     var matching: Map<String, String> = mapOf(),
                     var subset: Any? = null) {

    fun getSubset(fallback: List<Int>? = null): List<Int> {
        return subset?.let { subset ->
            when (subset) {
                is Int -> listOf(subset)
                is String -> {
                    return subset.split("|").mapNotNull { it.getSubsetValue() }
                }
                else -> null
            }
        } ?: fallback ?: throw RuntimeException("Subset $subset is invalid, check it again.")
    }

    private fun String.getSubsetValue(): Int? = when (this) {
        "first" -> 0
        "last" -> -1
        else -> this.toIntOrNull()
    }
}