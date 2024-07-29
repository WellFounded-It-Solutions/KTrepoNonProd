package se.infomaker.frt.statistics;

/**
 * Allow intercepting and optionally modifying stats events
 */
public interface StatisticsEventInterceptor {
    // Called before the event is passed on to statistics services
    StatisticsEvent onEvent(StatisticsEvent event);
}
