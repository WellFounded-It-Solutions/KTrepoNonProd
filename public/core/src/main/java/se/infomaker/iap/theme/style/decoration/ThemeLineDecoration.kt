package se.infomaker.iap.theme.style.decoration

enum class ThemeLineDecoration {
    SINGLE, NONE;

    companion object {
        fun from(value: String?) = values().find { it.name == value?.uppercase() } ?: NONE
    }
}