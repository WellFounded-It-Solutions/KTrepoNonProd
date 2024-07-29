package se.infomaker.livecontentui.livecontentrecyclerview.adapter;

import androidx.annotation.NonNull;

import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import timber.log.Timber;

/**
 * Insert and purge ads ids from a list
 */
public class AdInsertHelper {
    private final int minDistance;
    private final int maxDistance;
    private final List<JsonObject> adConfigurations;
    private final String articleTypeId;
    private final int startIndex;
    private final List<String> adBlockers;
    private int mAdIdIndex;

    public AdInsertHelper(int startIndex, int minDistance, int maxDistance, List<JsonObject> adConfigurations, @NonNull String articleTypeId) {
        this(startIndex, minDistance, maxDistance, adConfigurations, articleTypeId, null);
    }

    public AdInsertHelper(int startIndex, int minDistance, int maxDistance, List<JsonObject> adConfigurations, @NonNull String articleTypeId, List<String> adBlockers) {
        this.startIndex = startIndex;
        this.minDistance = minDistance;
        this.maxDistance = maxDistance;
        this.adConfigurations = adConfigurations;
        this.articleTypeId = articleTypeId;
        this.adBlockers = adBlockers;
    }

    /**
     * Makes sure the article list contains ads at given min/max distances
     * @return List of indexes where ads have been inserted, note that the indexes encounter for
     * earlier inserted indexes and must therefor be read in order
     */
    public List<Integer> fillAds(List<Object> itemList) {
        List<Integer> added = new ArrayList<>();
        if (itemList.size() <= startIndex) {
            return added;
        }

        int actualStartIndex = findValidStartIndex(itemList, startIndex);

        // Sanity check if item at this start index is actually an article, since we might have already injected an ad at this index
        if (isArticle(itemList.get(actualStartIndex))) {
            if (!hasNearByAd(itemList, actualStartIndex, minDistance)) {
                addAd(itemList, added, actualStartIndex);
            }
        }

        int articleCount = 0;
        int adBlockerCount = 0;
        Random random = new Random();
        for (int i = actualStartIndex + 1; i < itemList.size(); i++) {
            Object item = itemList.get(i);
            if (isArticle(item)) {
                articleCount++;
                // We MUST insert an ad in the available space
                if (articleCount >= minDistance + maxDistance && articleCount > 0) {
                    int add = 0;
                    if (maxDistance - minDistance > 0) {
                        add = random.nextInt(maxDistance - minDistance);
                    }
                    int desiredIndex = i - minDistance - add - adBlockerCount;
                    int adIndex = findNextValidIndex(itemList, desiredIndex);
                    if (!hasNearByAd(itemList, adIndex, minDistance)) {
                        addAd(itemList, added, adIndex);
                        i = i + 1;
                        articleCount = i - adIndex;
                        adBlockerCount = 0;
                    }
                }
            }
            else if (isAdBlocker(item)) {
                adBlockerCount++;
            }
            else {
                // Check if we must insert an ad in the available space
                if (articleCount > maxDistance && articleCount > 2 * minDistance) {
                    int desiredIndex = minDistance + random.nextInt(articleCount - 2*minDistance);
                    int adIndex = findNextValidIndex(itemList, desiredIndex);
                    if (!hasNearByAd(itemList, adIndex, minDistance)) {
                        addAd(itemList, added, adIndex);
                        i = i + 1;
                        articleCount = i - adIndex;
                        adBlockerCount = 0;
                    }
                }
                else {
                    articleCount = 0;
                }
            }
        }
        // Make sure we try to ad an ad if allowed
        if (articleCount >= minDistance) {

            int nextInt = (maxDistance - minDistance) > 0 ? random.nextInt(maxDistance - minDistance) : 0;
            int possibleIndex = minDistance + itemList.size() - articleCount + nextInt;
            int adIndex = findNextValidIndex(itemList, possibleIndex);
            int lastPossibleIndex = itemList.size() - 1;
            if (adIndex <= lastPossibleIndex) {
                if (!hasNearByAd(itemList, adIndex, minDistance)) {
                    addAd(itemList, added, adIndex);
                }
            }
        }
        return added;
    }

    private int findValidStartIndex(List<Object> itemList, int desiredStartIndex) {
        int lastPossibleIndex = itemList.size() - 1;
        desiredStartIndex = Math.min(desiredStartIndex, lastPossibleIndex);
        int articlesPassed = 0;
        for (int i = 0; i < itemList.size(); i++) {
            Object item = itemList.get(i);
            if (isArticle(item)) {
                if (articlesPassed++ == desiredStartIndex) {
                    return i;
                }
            }
        }
        return lastPossibleIndex;
    }

    private int findNextValidIndex(List<Object> itemList, int desiredIndex) {
        int lastPossibleIndex = itemList.size() - 1;
        int nextPosition = Math.min(desiredIndex + 1, lastPossibleIndex);
        if (nextPosition > desiredIndex) {
            Object currentItem = itemList.get(desiredIndex);
            if (isAdBlocker(currentItem)) {
                return findNextValidIndex(itemList, nextPosition);
            }
        }
        return desiredIndex;
    }

    private boolean isAdBlocker(Object item) {
        if (adBlockers != null) {
            for (String type : adBlockers) {
                if (type != null && type.equals(item)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean hasNearByAd(List<Object> itemList, int start, int distance) {
        boolean isAd = itemList.size() > mAdIdIndex && itemList.get(start) != articleTypeId;
        boolean adBefore = hasNearByAdBefore(itemList, start, distance);
        boolean adAfter = hasNearByAdAfter(itemList, start, distance);

        return isAd || adBefore || adAfter;
    }

    private boolean hasNearByAdBefore(List<Object> itemList, int start, int distance) {
        boolean found = false;
        int offset = 1;
        while (!found) {
            int index = start - offset;
            if (index > 0) {
                Object before = itemList.get(index);
                boolean isAdBlocker = isAdBlocker(before);
                found |= !isArticle(before) && !isAdBlocker;
                if (isAdBlocker) {
                    distance++;
                }
            }
            offset++;
            if (offset > distance) {
                break;
            }
        }
        return found;
    }

    private boolean hasNearByAdAfter(List<Object> itemList, int start, int distance) {
        boolean found = false;
        int offset = 1;
        while (!found) {
            int index = start + offset;
            if (index < itemList.size()) {
                Object after = itemList.get(index);
                boolean isAdBlocker = isAdBlocker(after);
                found = !isArticle(after) && !isAdBlocker;
                if (isAdBlocker) {
                    distance++;
                }
            }
            offset++;
            if (offset > distance) {
                break;
            }
        }
        return found;
    }

    private boolean isArticle(Object item) {
        return articleTypeId.equals(item);
    }

    private void addAd(List<Object> itemList, List<Integer> added, int adIndex) {
        itemList.add(adIndex, AdPosition.create(getNextAdConfiguration()));
        added.add(adIndex);
        logInsert(adIndex);
    }

    private void logInsert(int adIndex) {
        Timber.d("Inserted ad at %s", adIndex);
    }

    public JSONObject getNextAdConfiguration() {
        if (adConfigurations == null || adConfigurations.size() == 0) {
            Timber.w("No ad ids available!");
            return new JSONObject();
        }
        JSONObject adConfiguration = null;
        try {
            adConfiguration = new JSONObject(adConfigurations.get(mAdIdIndex).toString());
        } catch (JSONException e) {
            Timber.e(e, "Failed to load ad configuration");
        }
        mAdIdIndex = (mAdIdIndex + 1) % adConfigurations.size();
        return adConfiguration;
    }

    /**
     * Resets the ad index
     */
    public void reset() {
        mAdIdIndex = 0;
    }
}
