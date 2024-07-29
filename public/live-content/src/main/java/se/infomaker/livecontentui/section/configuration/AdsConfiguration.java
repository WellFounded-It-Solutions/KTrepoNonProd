package se.infomaker.livecontentui.section.configuration;

import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

public class AdsConfiguration {
    private int width = 320;
    private int height = 320;
    private int distanceMin = 3;
    private int distanceMax = 7;
    private Integer startIndex = null;
    private List<JsonObject> providerConfiguration = new ArrayList<>();
    private String provider;

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getDistanceMin() {
        return distanceMin;
    }

    public int getDistanceMax() {
        return distanceMax;
    }

    public List<JsonObject> getProviderConfiguration() {
        return providerConfiguration;
    }

    public List<JSONObject> getProviderConfigurationAsJSONObject() {
        ArrayList<JSONObject> out = new ArrayList<>();
        for (JsonObject jsonObject : providerConfiguration) {
            try {
                out.add(new JSONObject(jsonObject.toString()));
            } catch (JSONException e) {
                Timber.e(e, "Failed to convert ad configuration");
            }
        }
        return out;
    }

    public int getStartIndex() {
        return startIndex != null && startIndex >= 0 ? startIndex : distanceMin;
    }

    public String getProvider() {
        return provider;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AdsConfiguration that = (AdsConfiguration) o;

        if (getWidth() != that.getWidth()) return false;
        if (getHeight() != that.getHeight()) return false;
        if (getDistanceMin() != that.getDistanceMin()) return false;
        if (getDistanceMax() != that.getDistanceMax()) return false;
        if (getStartIndex() != that.getStartIndex()) return false;
        if (getProviderConfiguration() != null ? !getProviderConfiguration().equals(that.getProviderConfiguration()) : that.getProviderConfiguration() != null)
            return false;
        return getProvider() != null ? getProvider().equals(that.getProvider()) : that.getProvider() == null;
    }

    @Override
    public int hashCode() {
        int result = getWidth();
        result = 31 * result + getHeight();
        result = 31 * result + getDistanceMin();
        result = 31 * result + getDistanceMax();
        result = 31 * result + getStartIndex();
        result = 31 * result + (getProviderConfiguration() != null ? getProviderConfiguration().hashCode() : 0);
        result = 31 * result + (getProvider() != null ? getProvider().hashCode() : 0);
        return result;
    }
}
