package se.infomaker.livecontentmanager.query;

/**
 * Performs queries and routes responses to receivers
 */
public interface QueryManager {

    /**
     * Sends a query
     * @param query Query to send
     * @param listener to receive response
     */
    void addQuery(Query query, QueryResponseListener listener);

    /**
     * Cancel a query
     * @param query to cancel
     * @return true if the query was canceled
     */
    boolean removeQuery(Query query);
}
