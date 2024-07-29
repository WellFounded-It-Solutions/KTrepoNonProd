package se.infomaker.iap.provisioning.config

data class ProvisioningConfig(
    val activateSubscriptionUrl: String?,
    val forgotPasswordUrl: String?,
    val sso: SingleSignOnConfig?,
    val customerServiceUrl: String?,
)