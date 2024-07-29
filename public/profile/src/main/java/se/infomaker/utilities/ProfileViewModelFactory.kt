package se.infomaker.utilities

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.FlowPreview
import se.infomaker.dependencies.data.DependencyViewModel
import se.infomaker.profile.data.ProfileViewModel
import se.infomaker.profile.view.items.authentication.data.AuthenticationItemViewModel

@FlowPreview
class ProfileViewModelFactory (
    private val app: Application,
    private val moduleIdentifier: String?)
    : ViewModelProvider.AndroidViewModelFactory(app) {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(DependencyViewModel::class.java) -> DependencyViewModel(app, moduleIdentifier) as T
            modelClass.isAssignableFrom(ProfileViewModel::class.java) -> ProfileViewModel(app, moduleIdentifier) as T
            modelClass.isAssignableFrom(AuthenticationItemViewModel::class.java) -> AuthenticationItemViewModel(app) as T
            else -> throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}