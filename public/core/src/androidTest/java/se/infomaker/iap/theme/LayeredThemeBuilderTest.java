package se.infomaker.iap.theme;

import android.graphics.Color;

import androidx.test.runner.AndroidJUnit4;

import junit.framework.Assert;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;

import se.infomaker.iap.theme.color.ThemeColor;

@RunWith(AndroidJUnit4.class)
public class LayeredThemeBuilderTest {
    @Test
    public void testSimpleTheme() throws JSONException {
        JSONObject definition = new JSONObject();
        JSONObject colors = new JSONObject();
        colors.put("black", "#ff000000");
        definition.put("color", colors);
        LayeredTheme theme = new LayeredThemeBuilder().setDefinition(definition).build(null, null);
        ThemeColor black = theme.getColor("black", ThemeColor.TRANSPARENT);
        Assert.assertEquals(Color.BLACK, black.get());
    }
}
