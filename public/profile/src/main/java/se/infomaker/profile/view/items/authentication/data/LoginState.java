package se.infomaker.profile.view.items.authentication.data;

public interface LoginState {
    /**
     *
     * @return a display name for the current signed in user
     */
    String getDisplayName();

    boolean isLoggedIn();

    boolean isLoading();

    boolean isLoginEnabled();
}
