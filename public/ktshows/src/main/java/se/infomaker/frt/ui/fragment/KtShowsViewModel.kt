package se.infomaker.frt.ui.fragment
import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ktshows.data.remote.VideoData
import com.example.ktshows.domain.UseCase.GetKtShowsDataUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class KtShowsViewModel @Inject constructor(
    private val getVideoDataUseCase: GetKtShowsDataUseCase
) : ViewModel() {

    private val _videos = MutableStateFlow<UIResult<List<VideoData>>>(UIResult.Loading)
    val videos: MutableStateFlow<UIResult<List<VideoData>>> = _videos

    init {
        fetchVideos()
    }

    private fun fetchVideos() {
        viewModelScope.launch {
            getVideoDataUseCase.execute()
                .onEach { videosResult ->
                    _videos.value = UIResult.Success(videosResult)
                }
                .catch { exception ->
                    _videos.value = UIResult.Error(exception)
                }
                .collect()
        }
    }
}
