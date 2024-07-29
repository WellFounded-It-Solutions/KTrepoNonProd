package se.infomaker.frt.ui.fragment

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.ktshows.domain.UseCase.GetKtShowsDataUseCase
import kotlinx.coroutines.FlowPreview

@FlowPreview
class KtShowsViewModelFactory (
    private val app: Application,
    private val moduleIdentifier: String?,
    private val getKtShowsDataUseCase: GetKtShowsDataUseCase

)
    : ViewModelProvider.AndroidViewModelFactory(app) {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(KtShowsViewModel::class.java) -> KtShowsViewModel(
                getKtShowsDataUseCase
            ) as T

            else -> throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}