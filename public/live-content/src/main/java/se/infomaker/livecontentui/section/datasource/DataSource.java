package se.infomaker.livecontentui.section.datasource;

import java.util.Set;

import io.reactivex.Observable;

public interface DataSource {

    /**
     * Start providing data
     *
     * @return true if an update is triggered
     */
    boolean resume();

    /**
     * Force update content
     */
    void update();

    /**
     * Stop providing data
     */
    void pause();

    /**
     * Observable providing
     *
     * @return observable to stream responses
     */
    Observable<DataSourceResponse> observeResponse();


    Set<String> groupKeys();
}
