package se.infomaker.livecontentmanager.query;

import androidx.annotation.NonNull;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class CatchupFilter implements QueryFilter {
    private final String lastActive;
    private final String pubdateKey;
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:SS'Z'", Locale.ENGLISH);
    static
    {
        dateFormat.setTimeZone(TimeZone.getTimeZone("Zulu"));
    }

    public CatchupFilter(Date lastActive, String pubdateKey) {
        this.lastActive = dateFormat.format(lastActive);
        this.pubdateKey = pubdateKey;
    }

    @NonNull
    @Override
    public String identifier() {
        return "catchup" + lastActive;
    }

    @NonNull
    @Override
    public JSONObject createStreamFilter() {
        throw new RuntimeException("Unsupported feature");
    }

    @NonNull
    @Override
    public String createSearchQuery(@NonNull String baseQuery) {
        return pubdateKey + ":[" + lastActive + " TO NOW] AND (" + baseQuery + ")" ;
    }
}
