package se.infomaker.iap.articleview.preprocessor.divider

import se.infomaker.iap.articleview.preprocessor.select.SelectorConfig

class DividerPreprocessorConfig(val select: SelectorConfig) {
    val themeKey: String? = null
        get() = field ?: "separator"
    val template: String? = null

    private val placement: String? = null

    fun getPlacement(): List<Placement> {
        return if (placement != null) {
            val placementList = mutableListOf<Placement>()
            placement.split("|")
                    .forEach {
                        when (it) {
                            "between" -> placementList.add(Placement.between)
                            "before" -> placementList.add(Placement.before)
                            "after" -> placementList.add(Placement.after)
                            "around" -> placementList.apply {
                                add(Placement.before)
                                add(Placement.after)
                            }
                        }
                    }
            placementList
        } else {
            listOf(Placement.between)
        }
    }

    enum class Placement {
        between,
        before,
        after,
    }
}