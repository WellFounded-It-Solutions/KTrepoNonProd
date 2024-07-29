package se.infomaker.iap.action.display.flow

import io.reactivex.Single
import org.json.JSONObject
import se.infomaker.frtutilities.meta.ValueProvider

interface FlowStepHandler {

    fun getModuleId(): String

    /**
     * Extracts all current values from value providing views
     *
     * @return all current values
     */
    fun getValues(): JSONObject

    /**
     * Gets the current ValueProvider
     *
     * @return ValueProvider for the current FlowStep
     */
    fun getValueProvider(): ValueProvider

    /**
     * Validate all views, views will display their own error message if needed
     *
     * @return true if all views are valid
     */
    fun validateViews(): Boolean

    /**
     * Validate the combined input, if validation fails an error message is displayed to the user.
     *
     * @return true if all validators are successful
     */
    fun validate(): Single<ValidationResult>

    /**
     * Returns the id of the current view or null if id is not set
     */
    fun currentView(): String?
}