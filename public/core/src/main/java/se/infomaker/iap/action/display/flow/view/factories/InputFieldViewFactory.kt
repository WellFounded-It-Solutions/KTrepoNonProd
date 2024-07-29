package se.infomaker.iap.action.display.flow.view.factories

import android.content.Context
import androidx.appcompat.widget.AppCompatEditText
import android.text.InputType
import android.text.method.DigitsKeyListener
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.LinearLayout
import android.widget.TextView
import com.google.android.material.textfield.TextInputLayout
import com.redmadrobot.inputmask.MaskedTextChangedListener
import org.json.JSONObject
import se.infomaker.frtutilities.ResourceManager
import se.infomaker.frtutilities.meta.ValueProvider
import se.infomaker.iap.action.Operation
import se.infomaker.iap.action.createOperation
import se.infomaker.iap.action.display.flow.FlowStepHandler
import se.infomaker.iap.action.display.flow.condition.view.ConditionalAppCompatEditText
import se.infomaker.iap.action.display.flow.mustachify
import se.infomaker.iap.action.display.flow.validator.validate
import se.infomaker.iap.action.display.flow.view.FlowViewFactory
import se.infomaker.iap.action.display.flow.view.ValueView
import se.infomaker.iap.theme.Theme
import se.infomaker.iap.theme.view.ThemeableTextInputLayout

object InputFieldViewFactory : FlowViewFactory {
    override fun create(context: Context, definition: JSONObject, valueHandler: FlowStepHandler, resourceManager: ResourceManager, theme: Theme): View? {
        return InputField(context, definition.mustachify(valueHandler.getValueProvider()), valueHandler)
    }

    private class InputField(context: Context, val definition: JSONObject, valueHandler: FlowStepHandler) : ThemeableTextInputLayout(context), ValueView {
        val editText: AppCompatEditText
        var formattedValue: String? = null

        init {
            editText = ConditionalAppCompatEditText(context).apply {
                boxBackgroundMode = TextInputLayout.BOX_BACKGROUND_NONE
                layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)

                definition.hint()?.let { hint ->
                    this.hint = hint
                }

                inputType = definition.inputType()

                definition.groupFormatting()?.let {
                    val listener = MaskedTextChangedListener(
                            it,
                            true,
                            this,
                            null,
                            object : MaskedTextChangedListener.ValueListener {
                                override fun onTextChanged(maskFilled: Boolean, extractedValue: String, preFormattedValue: String) {
                                    formattedValue = extractedValue
                                }
                            }
                    )
                    addTextChangedListener(listener)
                    onFocusChangeListener = listener
                    if (inputType and InputType.TYPE_CLASS_NUMBER != 0) {
                        keyListener = DigitsKeyListener.getInstance("0123456789 -.")
                    }
                }

                if (inputType and InputType.TYPE_TEXT_VARIATION_PASSWORD != 0) {
                    isPasswordVisibilityToggleEnabled = true
                }

                definition.optString("themeColor", null)?.let { themeColor ->
                    setThemeColor(themeColor)
                }

                definition.optString("themeErrorColor", null)?.let { themeErrorColor ->
                    setThemeErrorColor(themeErrorColor)
                }

                definition.optString("themeKey", null)?.let { themeKey ->
                    setThemeKey(themeKey)
                }
            }
            definition.optString("value", null)?.let {
                setValue(it)
            }
            definition.optString("actionType", null)?.let {
                editText.imeOptions = when (it) {
                    "done" -> EditorInfo.IME_ACTION_DONE
                    "go" -> EditorInfo.IME_ACTION_GO
                    "next" -> EditorInfo.IME_ACTION_NEXT
                    "search" -> EditorInfo.IME_ACTION_SEARCH
                    "send" -> EditorInfo.IME_ACTION_SEND
                    else -> {
                        editText.imeOptions
                    }
                }
            }
            definition.optJSONObject("action")?.let { action ->
                editText.setOnEditorActionListener { textView, actionId, keyEvent: KeyEvent? ->
                    val isAction = actionId >= EditorInfo.IME_ACTION_DONE ||
                            actionId == EditorInfo.IME_ACTION_GO ||
                            actionId == EditorInfo.IME_ACTION_NEXT ||
                            actionId == EditorInfo.IME_ACTION_SEARCH ||
                            actionId == EditorInfo.IME_ACTION_SEND ||
                            actionId == EditorInfo.IME_ACTION_UNSPECIFIED
                    if ((keyEvent == null || keyEvent.action == KeyEvent.ACTION_UP) &&
                            isAction){
                        val operation = action.createOperation(valueHandler)
                        operation.perform(context) {}
                    }
                    return@setOnEditorActionListener false
                }
            }
            addView(editText)
        }

        override fun setValue(values: String) {
            editText.setText(values, TextView.BufferType.EDITABLE)
        }

        override fun getValue(): String = formattedValue ?: editText.text.toString()

        override fun validate(valueProvider: ValueProvider): Boolean {
            error = validate(valueProvider, definition.optJSONArray(("validators")))
            return error == null
        }
    }
}

private fun JSONObject.groupFormatting(): String? = optString("groupFormatting", null)

private fun JSONObject.hint() = optString("hint", null)
private fun JSONObject.inputType() = when (optString("inputType", null)) {
    "password" -> InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
    "email" -> InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
    "phone" -> InputType.TYPE_CLASS_PHONE
    "number" -> InputType.TYPE_CLASS_NUMBER
    else -> InputType.TYPE_TEXT_VARIATION_NORMAL
}
