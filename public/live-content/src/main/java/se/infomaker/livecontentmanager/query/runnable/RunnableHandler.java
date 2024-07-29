package se.infomaker.livecontentmanager.query.runnable;

/**
 * Runs a runnable at a fixed delay
 */
public interface RunnableHandler {

    /**
     * Schedule runnable at delay
     * @param r
     * @param delayMillis
     * @return
     */
    boolean postDelayed(Runnable r, long delayMillis);

    /**
     * Remove runnable from schedule
     * @param r
     */
    void removeCallbacks(Runnable r);
}
