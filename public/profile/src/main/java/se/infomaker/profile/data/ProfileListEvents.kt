package se.infomaker.profile.data


sealed class ProfileListEvent {
    data class UpdateConfig(val config: MyProfileConfig) : ProfileListEvent()
}
