package se.infomaker.dependencies.data

import android.app.Application
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import se.infomaker.frtutilities.ResourceManager
import timber.log.Timber
import java.io.IOException
import java.io.InputStreamReader
import android.content.ActivityNotFoundException

import android.content.Intent
import android.net.Uri


class DependencyViewModel(app: Application, val moduleIdentifier: String?) : AndroidViewModel(app) {

    companion object {
        const val DEPENDENCY_LICENSE_JSON = "index.json"
        const val INFOMAKER_PACKAGE_NAME = "se.infomaker"
        const val NAVIGA_PACKAGE_NAME = "com.naviga"
    }

    val state = mutableStateOf(DependencyListState(isLoading = true))

    private fun openLicense(url: String) {
        val intent =
            Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try {
            getApplication<Application>().startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Timber.e(e, "Failed to open license. ${e.message}")
        }
    }

    fun onTriggerEvent(event: DependencyListEvents) {
        when (event) {
            is DependencyListEvents.OpenLicense -> {
                openLicense(event.licenseUrl)
            }
        }
    }

    suspend fun getThirdPartyDependencies() {
        val resourceManager = ResourceManager(getApplication<Application>().applicationContext, moduleIdentifier)
        getDependencies()
            .filter(Dependency::hasLicense)
            .filterNot { dependency ->
                isNavigaDependency(dependency)
            }.apply {
                state.value =
                    state.value.copy(
                        isLoading = false,
                        dependencies = this,
                        showLicenses = true,
                        headerText = resourceManager.getString("profile_licenses_header", null)
                    )
            }
    }

    suspend fun getNavigaDependencies() {
        getDependencies()
            .filter { dependency ->
                isNavigaDependency(dependency)
            }.apply {
                state.value =
                    state.value.copy(isLoading = false, dependencies = this, showLicenses = false)
            }
    }

    private suspend fun getDependencies(): List<Dependency> =
        loadDependencies()?.dependencies?.sortedBy { it.artifactName } ?: run { return emptyList() }

    private fun isNavigaDependency(dependency: Dependency): Boolean =
        dependency.groupName.startsWith(INFOMAKER_PACKAGE_NAME) || dependency.groupName.startsWith(
            NAVIGA_PACKAGE_NAME
        )

    private suspend fun loadDependencies(): DependencyReport? {
        val resourceManager =
            ResourceManager(getApplication<Application>().applicationContext, moduleIdentifier)
        try {
            resourceManager.apply {
                return withContext(Dispatchers.IO) {
                    runCatching {
                        getAssetStream(DEPENDENCY_LICENSE_JSON).use { inputStream ->
                            val reader = InputStreamReader(inputStream, "UTF-8")
                            Gson().fromJson(reader, DependencyReportDTO::class.java).toDomain()
                        }
                    }.getOrNull()
                }
            }
        } catch (e: IOException) {
            Timber.e("Exception occurred reading dependencies. ${e.message}")
            return null
        }
    }
}