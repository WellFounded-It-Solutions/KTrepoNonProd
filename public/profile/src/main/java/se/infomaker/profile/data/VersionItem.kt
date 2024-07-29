package se.infomaker.profile.data

import android.content.Context
import android.content.pm.PackageManager
import se.infomaker.frtutilities.ResourceManager
import se.infomaker.iap.theme.Theme
import timber.log.Timber

class VersionItem private constructor(
    val config: VersionItemConfig,
    sectionPosition: SectionPosition,
    sectionIdentifier: String,
    override val moduleIdentifier: String?,
    theme: Theme,
    resourceManager: ResourceManager,
) : BaseItem(sectionPosition, sectionIdentifier, theme, resourceManager), ProfileItem {

    override val name: String
        get() = "version"

    override val text: String
        get() = config.parameters?.text ?: run {
            resources.getString("profile_versions", "Versions")
        }.orEmpty()

    override val image: Int
        get() = config.parameters?.image?.let {
            theme.getImage(null, it)?.resourceId ?: resources.getDrawableIdentifier(it)
        } ?: -1

    init {
        super.configure(config)
    }

    fun version(context: Context): String {
        val packageInfo = try {
            context.packageManager.getPackageInfo(context.packageName, 0)
        } catch (e: PackageManager.NameNotFoundException) {
            Timber.e(e, "Failed to resolve version")
            return ""
        }
        val versionCode =
            if (android.os.Build.VERSION.SDK_INT < 28) packageInfo.versionCode else packageInfo.longVersionCode
        return "${packageInfo.versionName} ($versionCode)"
    }

    companion object {
        operator fun invoke(
            config: VersionItemConfig,
            sectionPosition: SectionPosition,
            sectionIdentifier: String,
            moduleIdentifier: String?,
            theme: Theme,
            resourceManager: ResourceManager,
        ): VersionItem = VersionItem(
            config,
            sectionPosition,
            sectionIdentifier,
            moduleIdentifier,
            theme,
            resourceManager
        )
    }
}