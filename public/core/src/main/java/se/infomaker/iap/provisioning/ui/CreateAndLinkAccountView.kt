package se.infomaker.iap.provisioning.ui

import android.content.Context
import android.content.res.Resources
import android.text.TextUtils
import android.util.AttributeSet
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import com.jakewharton.rxbinding3.widget.textChanges
import com.jakewharton.rxrelay2.BehaviorRelay
import com.navigaglobal.mobile.R
import com.navigaglobal.mobile.databinding.CreateAccountViewContentsBinding
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import se.infomaker.frtutilities.ConfigManager
import se.infomaker.frtutilities.ResourceManager
import se.infomaker.iap.provisioning.config.CoreProvisioningConfig

class CreateAndLinkAccountView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : ConstraintLayout(context, attrs, defStyleAttr) {
    private val binding = CreateAccountViewContentsBinding.inflate(LayoutInflater.from(context), this, true)
    private val approvables: List<ApprovableView>

    val disposable: Disposable

    var onCreateAccountListener: ((String, String) -> Unit)? = null
    var onOptOutListener: (() -> Unit)? = null

    init {
        setupTitle()
        setupDescription()

        approvables = createToggles()
        approvables.forEach { binding.toggleContainer.addView(it) }

        binding.repeatPasswordTextInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                hideKeyboard()
                if (approvables.none { !it.isApproved() }) {
                    notifyListener()
                }

                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false
        }
        binding.optOutButton.setOnClickListener {
            AlertDialog.Builder(context).setPositiveButton(android.R.string.ok) { _, _ ->
                onOptOutListener?.invoke()
            }
                    .setNegativeButton(android.R.string.cancel, null)
                    .setTitle(R.string.are_you_sure).setMessage(R.string.confirm_opt_out_message).show()
        }
        binding.createAccount.setOnClickListener {
            hideKeyboard()
            notifyListener()
        }
        val emailChanges = binding.emailTextInput.textChanges()
        val passwordChanges = binding.passwordTextInput.textChanges()
        val repeatChanges = binding.repeatPasswordTextInput.textChanges()
        val shouldValidateChanges = BehaviorRelay.createDefault(false)
        val approveChanges = approvables.map { it.observe()}
        val changeObservables = mutableListOf<Observable<*>>(emailChanges, passwordChanges, repeatChanges)
        changeObservables.addAll(approveChanges)
        changeObservables.add(shouldValidateChanges.distinctUntilChanged())
        disposable = Observable.combineLatest(changeObservables) {
            var isValid = true
            val email = binding.emailTextInput.text
            val password = binding.passwordTextInput.text
            val repeat = binding.repeatPasswordTextInput.text
            val shouldValidate = it[it.size - 1] as Boolean

            // Start validating when all fields have values
            if (!shouldValidate && !email.isNullOrEmpty() && !password.isNullOrEmpty() && !repeat.isNullOrEmpty()) {
                shouldValidateChanges.accept(true)
                return@combineLatest true
            }
            if (shouldValidate) {
                approvables.forEach {
                    if (!it.isApproved()) {
                        isValid = false
                    }
                }
            }

            if (shouldValidate && (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches())) {
                binding.emailTextInputLayout.error = context.getString(R.string.provide_valid_email)
                isValid = false
            } else {
                binding.emailTextInputLayout.error = null
            }
            if (shouldValidate && TextUtils.isEmpty(password)) {
                binding.passwordTextInputLayout.error = context.getString(R.string.need_to_provide_password)
                isValid = false
            } else {
                binding.passwordTextInputLayout.error = null
            }

            if (shouldValidate && password.toString() != repeat.toString()) {
                binding.repeatPasswordTextInputLayout.error = context.getString(R.string.passwords_does_not_match)
                isValid = false
            } else {
                binding.repeatPasswordTextInputLayout.error = null
            }
            return@combineLatest isValid && shouldValidate
        }.subscribe {
            binding.createAccount.isEnabled = it
        }
    }

    private fun setupDescription() {
        val value = ResourceManager(context, "shared").getString("createAccountDescription", null)
        when {
            value == null -> binding.description.setText(R.string.create_account_description)
            value.isEmpty() -> binding.description.visibility = View.GONE
            else -> binding.description.text = value
        }
    }

    private fun setupTitle() {
        val value = ResourceManager(context, "shared").getString("createAccountTitle", null)
        when {
            value == null -> binding.title.setText(R.string.create_account)
            value.isEmpty() -> binding.title.visibility = View.GONE
            else -> binding.title.text = value
        }
    }

    private fun createToggles() : List<ApprovableView> {
        val config = ConfigManager.getInstance(context).getConfig("core", CoreProvisioningConfig::class.java)
        val list = mutableListOf<ApprovableView>()
        config.provisioningProvider?.createAccountApproves?.forEach { approves ->
            ApprovableView(context).also {
                val layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

                layoutParams.setMargins(0, 8.dp2px(), 0, 8.dp2px())
                it.layoutParams = layoutParams
                it.setApprovable(approves)
                list.add(it)
            }
        }
        return list
    }

    fun setOptOutVisible(visible: Boolean) {
        val visibility = if (visible) View.VISIBLE else View.GONE
        binding.doNotCreateAccount.visibility = visibility
        binding.optOutButton.visibility = visibility
        binding.divider.visibility = visibility
    }



    private fun notifyListener() {
        onCreateAccountListener?.invoke(binding.emailTextInput.text.toString(), binding.passwordTextInput.text.toString())
    }

    fun clear() {
        binding.emailTextInput.setText("")
        binding.passwordTextInput.setText("")
        binding.repeatPasswordTextInput.setText("")
    }

    fun cleanUp() {
        disposable.dispose()
    }
}

fun Int.dp2px(): Int {
    val metrics = Resources.getSystem().displayMetrics
    val px = this.toFloat() * (metrics.densityDpi / 160f)
    return Math.round(px)
}