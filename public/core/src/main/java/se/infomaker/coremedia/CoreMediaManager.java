package se.infomaker.coremedia;

import android.content.Context;
import android.net.Uri;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import timber.log.Timber;

/**
 * Created by Magnus Ekström on 22/02/16.
 */
public class CoreMediaManager {
    private static final String TAG = "CoreMediaManager";
    private static CoreMediaManager mInstance = null;

    private List<Class<? extends CoreMediaPlayer>> mCoreMediaPlayers;
    private int mColor;

    public static CoreMediaManager getInstance() {
        if (mInstance == null) {
            mInstance = new CoreMediaManager();
        }

        return mInstance;
    }

    public void init(List<Class<? extends CoreMediaPlayer>> coreMediaPlayers, int color) {
        mCoreMediaPlayers = coreMediaPlayers;
        mColor = color;
    }

    public CoreMediaObject parseShortcode(String shortCode) {
        Pattern pattern = Pattern.compile(getShortcodeRegex());
        Matcher matcher = pattern.matcher(shortCode);

        String name = null;
        String attributesString = null;
        if (matcher.find()) {
            name = matcher.group(2);
            attributesString = matcher.group(3);
        }

        if (name == null || attributesString == null) {
            Timber.w("Error parsing shortcode");
            return null;
        }

        Map<String, Object> attributesMap = parseShortcodeAttributes(attributesString);

        CoreMediaObject coreMediaObject = new CoreMediaObject();
        coreMediaObject.setName(name);
        coreMediaObject.setAttributes(attributesMap);
        return coreMediaObject;
    }

    public CoreMediaObject parseUri(Uri uri) {
        String name = uri.getAuthority();

        Map<String, Object> attributes = new HashMap<>();
        for (String key : uri.getQueryParameterNames()) {
            attributes.put(key, uri.getQueryParameter(key));
        }

        CoreMediaObject mediaObject = new CoreMediaObject();
        mediaObject.setName(name);
        mediaObject.setAttributes(attributes);

        return mediaObject;
    }

    private Map<String, Object> parseShortcodeAttributes(String attrubutes) {
        Pattern pattern = Pattern.compile(getShortcodeAttributeRegex());
        attrubutes = attrubutes.replace("\"", "");
        attrubutes = attrubutes.replace("/[x{00a0}x{200b}]+/u", " ");
        Matcher matcher = pattern.matcher(attrubutes);

        Map<String, Object> attributes = new HashMap<>();
        while (matcher.find()) {
            if (matcher.group(1) != null && !matcher.group(1).isEmpty()) {
                attributes.put(formatAttributeKey(matcher.group(1)), formatAttributeValue(matcher.group(2)));
            } else if (matcher.group(3) != null && !matcher.group(3).isEmpty()) {
                attributes.put(formatAttributeKey(matcher.group(3)), formatAttributeValue(matcher.group(4)));
            } else if (matcher.group(5) != null && !matcher.group(5).isEmpty()) {
                attributes.put(formatAttributeKey(matcher.group(5)), formatAttributeValue(matcher.group(6)));
            } else {
                Timber.w("Error parsing attribute");
            }
        }
        return attributes;
    }

    public void play(Context context, CoreMediaObject coreMediaObject, Map<String, Object> config) {
        CoreMediaPlayer coreMediaPlayer = getMediaPlayer(coreMediaObject, config);
        if (coreMediaPlayer != null) {
            coreMediaPlayer.start(context);
        }
    }

    public void play(Context context, Uri uri, Map<String, Object> config) {
        play(context, parseUri(uri), config);
    }

    public void play(Context context, Uri uri) {
        play(context, parseUri(uri), new HashMap<String, Object>());
    }

    private CoreMediaPlayer getMediaPlayer(CoreMediaObject coreMediaObject, Map<String, Object> config) {
        if (coreMediaObject == null) {
            Timber.w("Core media object was null");
            return null;
        }

        if (mCoreMediaPlayers != null && !mCoreMediaPlayers.isEmpty()) {
            for (Class<? extends CoreMediaPlayer> coreMediaPlayer : mCoreMediaPlayers) {
                try {
                    CoreMediaPlayer mediaPlayer = coreMediaPlayer.newInstance();
                    if (mediaPlayer.getName().equals(coreMediaObject.getName())) {
                        mediaPlayer.init(coreMediaObject, mColor, config);
                        return mediaPlayer;
                    }
                } catch (InstantiationException e) {
                    Timber.e(e, null);
                } catch (IllegalAccessException e) {
                    Timber.e(e, null);
                }
            }
        }

        Timber.w("No matching media player was found for " + coreMediaObject.getName());
        return null;
    }

    private String getShortcodeRegex() {
        String tagregexp = "[a-z]*";

        return "\\["// Opening bracket
                + "(\\[?)"                      // 1: Optional second opening bracket for escaping shortcodes: [[tag]]
                + "(" + tagregexp + ")"         // 2: Shortcode name
                + "(?![\\w-])"                  // Not followed by word character or hyphen
                + "("                           // 3: Unroll the loop: Inside the opening shortcode tag
                + "[^\\]\\/]*"                  // Not a closing bracket or forward slash
                + "(?:" + "\\/(?!\\])"          // A forward slash not followed by a closing bracket
                + "[^\\]\\/]*"                  // Not a closing bracket or forward slash
                + ")*?" + ")" + "(?:" + "(\\/)" // 4: Self closing tag ...
                + "\\]"                         // ... and closing bracket
                + "|" + "\\]"                   // Closing bracket
                + "(?:" + "("                   // 5: Unroll the loop: Optionally, anything between the opening and closing shortcode tags
                + "[^\\[]*+"                    // Not an opening bracket
                + "(?:" + "\\[(?!\\/\\2\\])"    // An opening bracket not followed by the closing shortcode tag
                + "[^\\[]*+"                    // Not an opening bracket
                + ")*+" + ")" + "\\[\\/\\2\\]"  // Closing shortcode tag
                + ")?" + ")" + "(\\]?)";        // 6: Optional second closing brocket for escaping shortcodes: [[tag]]
    }

    private String getShortcodeAttributeRegex() {
        return "/(\\w+)\\s*=\\s*\"([^\"]*)\"(?:\\s|$)|(\\w+)\\s*=\\s*\\'([^\\']*)\\'(?:\\s|$)|(\\w+)\\s*=\\s*([^\\s\\'\"]+)(?:\\s|$)|\"([^\"]*)\"(?:\\s|$)|(\\S+)(?:\\s|$)/";
    }

    private String formatAttributeKey(String s) {
        return s.toLowerCase();
    }

    private String formatAttributeValue(String s) {
        // Remove surrounding quotes
        if (s.startsWith("\"")) {
            s = s.substring(1, s.length());
        }
        if (s.endsWith("\"")) {
            s = s.substring(0, s.length() - 1);
        }

        return Uri.decode(s.replace("+", "%20"));
    }
}
