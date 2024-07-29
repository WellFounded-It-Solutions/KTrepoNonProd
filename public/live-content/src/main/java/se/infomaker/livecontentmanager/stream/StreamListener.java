package se.infomaker.livecontentmanager.stream;

import java.util.List;

public interface StreamListener<T> {
    void onItemsAdded(int index, List<T> items);
    void onItemsRemoved(List<T> items);
    void onItemsChanged(List<T> items);
    void onEndReached();
    void onReset();
    void onError(Exception exception);
}
