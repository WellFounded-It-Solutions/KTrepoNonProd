package se.infomaker.iap.articleview.view

/**
 * Used to signal a [FocusAware] component the current visibility state of the hosting UI-component
 * (Fragment/Activity) or any blocking views (such as a paywall) on top of it.
 *
 * [IN_FOCUS] -> It is safe to render content that should only be rendered when the user can see it.
 * [BLOCKED] -> Something is blocking the user from seeing the content, i.e. paywall.
 * [OUT_OF_FOCUS] -> The UI component is not visible to the user, i.e. not RESUMED.
 */
enum class FocusState {
    IN_FOCUS, BLOCKED, OUT_OF_FOCUS,
}
