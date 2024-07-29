package se.infomaker.streamviewer.tabs

import android.app.Activity
import android.content.Context
import android.content.Intent
import com.google.gson.Gson
import se.infomaker.frt.statistics.StatisticsEvent
import se.infomaker.frt.statistics.StatisticsManager
import se.infomaker.frtutilities.ModuleInformationManager
import se.infomaker.streamviewer.StatsHelper
import se.infomaker.streamviewer.config.PickerConfig
import se.infomaker.streamviewer.config.TopicPickerConfig
import se.infomaker.streamviewer.topicpicker.TopicPickerActivity
import kotlin.collections.set

object PickerFactory {

    private val pickerIntents = mutableMapOf<String, PickerManager>()

    const val PICKER_REQUEST_CODE = 45688

    init {
        addPickerIntentFactory("topic", TopicPickerFactory())
    }


    fun addPickerIntentFactory(type: String, factory: PickerManager) {
        pickerIntents[type] = factory
    }

    /**
     * @return  An intent based on the configuration provided
     *
     */

    fun createIntent(context: Context, moduleId: String, pickerConfig: PickerConfig): Intent? {
        if (!pickerConfig.isTypeNull()) {
            return pickerIntents[pickerConfig.type]?.getIntent(context, moduleId, pickerConfig)
        }
        return null
    }

    /**
     * Call this from the onactivityresult method of the class which has initialized this factory with the [createIntent] method
     */
    fun handleReceivingResult(resultCode: Int, context: Context, moduleId: String, data: Intent?, requestCode: Int?, statisticLogCode: String) {
        if (requestCode == PICKER_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                data?.let { d ->
                    getPickerManagerIfValidData(d)?.handleIntentData(context, moduleId, d)
                }
            } else if (resultCode == Activity.RESULT_CANCELED) {
                logCanceledEvent(moduleId, statisticLogCode)
            }
        }
    }

    /**
     * Iterates throught the [PickerManager.canHandleIntent] methods on each pickermanager,
     * if the data returned by [PickerManager.canHandleIntent] if correct we proceed and enters that pickermanager handleIntentData
     */
    private fun getPickerManagerIfValidData(data: Intent): PickerManager? {
        data.extras.let {
            return pickerIntents.values.firstOrNull {
                it.canHandleIntent(data)
            }
        }
    }

    private fun logCanceledEvent(moduleId: String, statisticLogCode: String) {
        StatisticsManager.getInstance().logEvent(StatisticsEvent.Builder()
                .event(StatsHelper.VIEW_CANCEL_EVENT)
                .moduleId(moduleId)
                .moduleName(ModuleInformationManager.getInstance().getModuleName(moduleId))
                .moduleTitle(ModuleInformationManager.getInstance().getModuleTitle(moduleId))
                .viewName(statisticLogCode).build())
    }
}

/**
 * Processing TopicPicker is not necessary due to it being handled inside [TopicPickerActivity.save]
 */
class TopicPickerFactory : PickerManager {


    override fun canHandleIntent(data: Intent): Boolean {
        return false
    }

    override fun handleIntentData(context: Context, moduleId: String, data: Intent) {}


    override fun getIntent(context: Context, moduleId: String, pickerConfig: PickerConfig): Intent {
        val topicPickerConfig = Gson().fromJson(pickerConfig.config, TopicPickerConfig::class.java)
        return TopicPickerActivity.createIntent(context, moduleId, topicPickerConfig)
    }
}

interface PickerManager {

    /**
     * Determines if the intent data passed in matches the values given, if it matches we continue to [handleIntentData]
     * @return true if the pickermanager should handle the result
     */
    fun canHandleIntent(data: Intent): Boolean

    /**
     * Persist any added subscriptions passed through intent result
     */
    fun handleIntentData(context: Context, moduleId: String, data: Intent)

    /**
     * Creates an intent to launch a picker with configuration
     * @return Intent if picker is available or null otherwise
     */
    fun getIntent(context: Context, moduleId: String, pickerConfig: PickerConfig): Intent?
}