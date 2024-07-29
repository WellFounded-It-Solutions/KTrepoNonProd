package se.infomaker.livecontentui.section.configuration;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

import se.infomaker.frtutilities.JSONUtil;
import se.infomaker.livecontentui.section.datasource.newspackage.PackageSectionConfig;

public class SectionConfigWrapper {
    public static final Gson GSON = new Gson();
    private String type;
    private String layout;
    private JsonObject configuration;
    private AdsConfiguration ads;
    private String sectionIdentifier;
    private JsonObject context;
    private ExtraContent extra;

    public String getType() {
        return type;
    }

    public Orientation getLayout() {
        return "horizontal".equals(layout) ? Orientation.HORIZONTAL : Orientation.VERTICAL;
    }

    public JsonObject getConfiguration() {
        return configuration;
    }

    public <T> T getConfiguration(Class<T> classOfT) {
        return GSON.fromJson(configuration, classOfT);
    }

    public void updateConfiguration(Object configuration) {
        this.configuration = new Gson().toJsonTree(configuration).getAsJsonObject();
    }

    public AdsConfiguration getAds() {
        return ads;
    }

    public String getSectionIdentifier() {
        if (sectionIdentifier == null) {
            // Calculate a hash for the config to still get a "unique" but repeatable section identifier
            sectionIdentifier = md5(configuration.toString());
        }
        return sectionIdentifier;
    }

    private static String md5(String string) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("md5");
            byte[] array = md.digest(string.getBytes());
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < array.length; ++i) {
                sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100).substring(1, 3));
            }
            return sb.toString();
        } catch (java.security.NoSuchAlgorithmException e) {
        }
        return null;
    }

    public JSONObject getContext() {
        if (context != null) {
            try {
                return JSONUtil.toJSONObject(context);
            } catch (JSONException e) {
                // ignore
            }
        }
        return null;
    }

    public ExtraContent getExtra() {
        return extra;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SectionConfigWrapper that = (SectionConfigWrapper) o;

        if (getType() != null ? !getType().equals(that.getType()) : that.getType() != null)
            return false;
        if (getConfiguration() != null ? !getConfiguration().equals(that.getConfiguration()) : that.getConfiguration() != null)
            return false;
        return getAds() != null ? getAds().equals(that.getAds()) : that.getAds() == null;
    }

    @Override
    public int hashCode() {
        int result = getType() != null ? getType().hashCode() : 0;
        result = 31 * result + (getConfiguration() != null ? getConfiguration().hashCode() : 0);
        result = 31 * result + (getAds() != null ? getAds().hashCode() : 0);
        return result;
    }

    public SectionConfigWrapper() {
    }

    public SectionConfigWrapper(PackageSectionConfig packageSectionConfig) {
        this.type = "package";
        this.configuration = new Gson().toJsonTree(packageSectionConfig).getAsJsonObject();
        this.ads = ads;
        this.sectionIdentifier = sectionIdentifier;
    }

    public static SectionConfigWrapper.Key createKey(SectionConfigWrapper config) {
        return new SectionConfigWrapper.Key(config);
    }
    public static SectionConfigWrapper.Key createKey(SectionConfigWrapper config, Orientation orientation) {
        return new SectionConfigWrapper.Key(config, orientation);
    }

    public static class Key {
        private final SectionConfigWrapper config;
        private final Orientation orientation;

        private Key(SectionConfigWrapper config) {
            this.config = config;
            this.orientation = Orientation.VERTICAL;
        }

        private Key(SectionConfigWrapper config, Orientation orientation) {
            this.config = config;
            this.orientation = orientation != null ? orientation : Orientation.VERTICAL;
        }

        @Override
        public int hashCode() {
            int configHashCode = Objects.hashCode(config);
            int orientationHasCode = Objects.hashCode(orientation);
            return configHashCode * 31 + orientationHasCode;
        }

        @Override
        public boolean equals(@Nullable Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof SectionConfigWrapper.Key)) return false;

            Key other = (Key) obj;
            return Objects.equals(config, other.config)
                    && Objects.equals(orientation, other.orientation);
        }

        @NonNull
        public String toString() {
            return "SectionConfigWrapper.Key(config=" + this.config + ", orientation=" + this.orientation + ")";
        }
    }
}
