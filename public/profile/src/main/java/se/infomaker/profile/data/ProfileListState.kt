package se.infomaker.profile.data

data class ProfileListState(
    val isLoaded: Boolean = false,
    val itemConfigs: List<ProfileItemConfig> = emptyList()
)
