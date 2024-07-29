package se.infomaker.frtutilities;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ShortCodeParser {
    private static final String TAG = "ShortCodeParser";

    /**
     *
     * @param shortCode in string format
     * @return ShortCodeObject
     * @throws InvalidShortCodeException if unable to get
     */
    public static ShortCodeObject parseShortcode(String shortCode) throws InvalidShortCodeException {
        shortCode = shortCode.replaceAll("\\[|\\]", "").replaceAll("/[x{00a0}x{200b}]+/u", " ");
        Pattern pattern = Pattern.compile(getCorrectShortcodeRegex());
        Matcher matcher = pattern.matcher(shortCode);
        String name = null;
        HashMap<String, Object> attributes = new HashMap<>();
        while (matcher.find()) {
            if (!TextUtils.isEmpty(matcher.group(1))) {
                attributes.put(matcher.group(1).toLowerCase(), matcher.group(2));
            } else if (!TextUtils.isEmpty(matcher.group(3))) {
                attributes.put(matcher.group(3).toLowerCase(), matcher.group(4));
            } else if (!TextUtils.isEmpty(matcher.group(5))) {
                attributes.put(matcher.group(5).toLowerCase(), matcher.group(6));
            } else if (!TextUtils.isEmpty(matcher.group(7))) {
                if (name == null) {
                    name = matcher.group(7);
                }
                else {
                    attributes.put(matcher.group(7), null);
                }
            } else if (!TextUtils.isEmpty(matcher.group(8))) {
                if (name == null) {
                    name = matcher.group(8);
                }
                else {
                    attributes.put(matcher.group(8), null);
                }
            }
        }
        if (name == null) {
            throw new InvalidShortCodeException("Name is undefined: " + shortCode);
        }
        ShortCodeObject shortCodeObject = new ShortCodeObject();
        shortCodeObject.setName(name);
        shortCodeObject.setAttributes(attributes);
        return shortCodeObject;
    }

    /*
     * This regexp is originally defined in wordpress (@since 4.4.0)
     * -> wp-includes/shortcodes.php
     */
    private static String getCorrectShortcodeRegex() {
        return "([\\w-]+)\\s*=\\s*\"([^\"]*)\"(?:\\s|$)|([\\w-]+)\\s*=\\s*\\'([^\\']*)\\'(?:\\s|$)|([\\w-]+)\\s*=\\s*([^\\s\\'\"]+)(?:\\s|$)|\"([^\"]*)\"(?:\\s|$)|(\\S+)(?:\\s|$)";
    }
}
