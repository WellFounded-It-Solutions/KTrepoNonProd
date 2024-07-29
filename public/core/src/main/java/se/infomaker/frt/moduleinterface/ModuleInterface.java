package se.infomaker.frt.moduleinterface;

public interface ModuleInterface {
    /**
     * If Fragment wants to draw it's own toolbar, return false
     * @return
     */
    boolean shouldDisplayToolbar();

    /**
     * This method is called when back button is pressed, return true if handled
     * @return
     */
    boolean onBackPressed();

    /**
     * This method is called when the Activity's toolbar is pressed
     */
    void onAppBarPressed();
}
