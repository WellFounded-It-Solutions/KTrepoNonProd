package se.infomaker.iap.theme;

import android.graphics.Color;

import com.googlecode.zohhak.api.TestWith;
import com.googlecode.zohhak.api.runners.ZohhakRunner;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.runner.RunWith;

import se.infomaker.iap.theme.attribute.AttributeParseException;
import se.infomaker.iap.theme.color.ThemeColor;
import se.infomaker.iap.theme.color.ThemeColorParser;

@RunWith(ZohhakRunner.class)
public class ThemeColorParserTest {
    private enum InternalColor {
        BLACK(Color.BLACK, "#FF000000"),
        DKGRAY(Color.DKGRAY, "#FF444444"),
        GRAY(Color.GRAY, "#FF888888"),
        LTGRAY(Color.LTGRAY, "#FFCCCCCC"),
        RED(Color.RED, "#FFFF0000"),
        GREEN(Color.GREEN, "#FF00FF00"),
        BLUE(Color.BLUE, "#FF0000FF"),
        YELLOW(Color.YELLOW, "#FFFFFF00"),
        CYAN(Color.CYAN, "#FF00FFFF"),
        MAGENTA(Color.MAGENTA, "#FFFF00FF"),
        TRANSPARENT(Color.TRANSPARENT, "#00000000");

        private final int color;
        private final String hex;

        InternalColor(int color, String hex) {
            this.color = color;
            this.hex = hex;
        }
    }

    private ThemeColorParser parser;

    @Before
    public void setup(){
        parser = new ThemeColorParser();
    }

    @TestWith({
            "null, false",
            "#ffffff, true",
            "#ffffffff, true",
            "#00ffffff, true",
            "#ccffff, true",
            "#ffffff, true",
            "#ffffff, true",
            "#fffff, false",
            "#invalid, false",
            "björn, false",
            "foo, false",
            "bar, false",
            "#ffff, false"
    })
    public void isValueObject(String input, boolean result) {
        Assert.assertEquals(result, parser.isValueObject(input));
    }
    @TestWith({
            "BLACK",
            "DKGRAY",
            "GRAY",
            "LTGRAY",
            "RED",
            "GREEN",
            "BLUE",
            "YELLOW",
            "CYAN",
            "MAGENTA",
            "TRANSPARENT"
    })
    public void colorOutput(InternalColor color) throws AttributeParseException {
        ThemeColor result = parser.parseObject(color.hex);
        Assert.assertEquals(color.color, result.get());
    }
}
