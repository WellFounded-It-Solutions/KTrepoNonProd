package se.infomaker.livecontentui;

import java.util.ArrayList;
import java.util.List;

import se.infomaker.livecontentmanager.config.PropertyConfig;
import se.infomaker.livecontentmanager.parser.PropertyObject;
import se.infomaker.livecontentui.config.FeatureConfiguration;
import se.infomaker.livecontentui.config.LiveContentUIConfig;
import se.infomaker.livecontentui.config.Permission;

public class Premium {

    public static final String READ_ARTICLE = "readArticle";

    /**
     * @return Key mapping to userPermissions in readArticle from featureConfiguration
     * in config
     */
    public static List<Permission> getPermissions(LiveContentUIConfig config) {
        FeatureConfiguration featureConfiguration = config.getFeatureConfiguration().get(READ_ARTICLE);
        if (featureConfiguration != null) {
            return featureConfiguration.getPermissions();
        }
        return null;
    }

    public static List<String> getPermissionAsString(LiveContentUIConfig config) {
        List<String> permissions = new ArrayList<>();
        FeatureConfiguration featureConfiguration = config.getFeatureConfiguration().get(READ_ARTICLE);
        if (featureConfiguration != null) {
            List<Permission> listOfPermissions = featureConfiguration.getPermissions();
            for (Permission permission: listOfPermissions) {
                permissions.add(permission.getPermission());
            }
        }
        return permissions;
    }

    /**
     * @return whether the article is closed under premium
     */
    public static boolean needsPremium(LiveContentUIConfig config, PropertyObject propertyObject) {
        PropertyConfig premium = config.getLiveContent().getTypePropertyMap().get(config.getLiveContent().getDefaultPropertyMap()).get("isPremium");
        return premium != null && getPermissions(config) != null && Boolean.parseBoolean(propertyObject.optString(premium.getName()));
    }
}

