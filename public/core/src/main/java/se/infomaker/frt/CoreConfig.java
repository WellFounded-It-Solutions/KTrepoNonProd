package se.infomaker.frt;

import java.util.ArrayList;

/**
 * Created by magnusekstrom on 05/07/16.
 */

public class CoreConfig {
    private int productId;
    private String loginURL;
    private String pushApplication;
    private String pushTopic;
    private String pushRegisterURL;
    private ArrayList statisticsProviders;
    private String statisticsDisablerBaseUrl;

    public int getProductId() {
        return productId;
    }

    public String getLoginURL() {
        return loginURL;
    }

    public String getPushApplication() {
        return pushApplication;
    }

    public String getPushTopic() {
        return pushTopic;
    }

    public String getPushRegisterURL() {
        return pushRegisterURL;
    }

    public ArrayList getStatisticsProviders() {
        return statisticsProviders;
    }

    public String getStatisticsDisablerBaseUrl() {
        return statisticsDisablerBaseUrl;
    }
}
