package se.infomaker.iap.theme;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;

import androidx.test.InstrumentationRegistry;
import androidx.test.runner.AndroidJUnit4;

import junit.framework.Assert;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import se.infomaker.frtutilities.ResourceManager;
import se.infomaker.iap.theme.font.FontLoader;

@RunWith(AndroidJUnit4.class)
public class MultipleLayeredThemeTest {
    private static final String BASE_JSON = "{" +
            "    \"color\": {" +
            "        \"packageBackground\": \"#ffffff\"," +
            "        \"packageIcon\": \"#000000\"," +
            "        \"packageText\": \"#212121\"," +
            "        \"primaryColor\": \"#7CB342\"," +
            "        \"primaryText\": \"#212121\"," +
            "        \"statusbarColor\": \"#000000\"," +
            "        \"lightText\": \"#ffffff\"," +
            "        \"lightTransparentText\": \"#99ffffff\"," +
            "        \"pureWhite\": \"#FFFFFF\"," +
            "        \"secondaryText\": \"#727272\"," +
            "        \"author\": \"#727272\"," +
            "        \"factBody\": \"#727272\"," +
            "        \"appBackground\": \"#EEEEEE\"," +
            "        \"borders\": \"#FAFAFA\"," +
            "        \"activeSelected\": \"#4C4C4C\"," +
            "        \"activeUnselected\": \"secondaryText\"," +
            "        \"inactive\": \"#939393\"," +
            "        \"primaryAccentColor\": \"#7CB342\"," +
            "        \"secondaryDarkAccentColor\": \"#558B2F\"," +
            "        \"secondaryLightAccentColor\": \"#F1F8E9\"," +
            "        \"disabledLink\": \"#8a8a8a\"," +
            "        \"textError\": \"#ff0000\"," +
            "        \"background\": \"#ffffff\"," +
            "        \"factBackground\": \"secondaryLightAccentColor\"," +
            "        \"interaction\": \"secondaryLightAccentColor\"," +
            "        \"disabledInteraction\": \"appBackground\"," +
            "        \"link\": \"primaryAccentColor\"," +
            "        \"followActive\": \"#c6c6c6\"," +
            "        \"followInactive\": \"#222222\"," +
            "        \"linkItem\": \"#000000\"," +
            "        \"disabledLinkItem\": \"disabledLink\"," +
            "        \"red\": \"#ff0000\"," +
            "        \"justNuColor\": \"red\"," +
            "        \"separator\": \"appBackground\"," +
            "        \"adBackground\": \"appBackground\"," +
            "        \"tabBackground\": \"#ffffff\",        " +
            "        \"tabInactive\": \"#727272\"," +
            "        \"numberBackgroundOutlineColor\": \"primaryColor\"," +
            "        \"numberBackgroundColorRead\": \"primaryColor\"," +
            "        \"numberBackgroundColorUnread\": \"pureWhite\"," +
            "        \"numberBackgroundColor\": \"numberBackgroundColorUnread\"," +
            "        \"packageIndicatorBackground\": \"#77000000\"," +
            "        \"packageIndicator\": \"#CCFFFFFF\"," +
            "        \"indicatorSelected\": \"#ffffff\"," +
            "        \"indicatorUnselected\": \"#99ffffff\"," +
            "        \"touchfeedback\": \"secondaryLightAccentColor\"," +
            "        \"packageIndexRead\": \"pureWhite\"," +
            "        \"packageIndexUnread\": \"primaryColor\"," +
            "        \"packageIndex\": \"packageIndexUnread\"" +
            "    }," +
            "    \"size\" : {" +
            "        \"mini\": 12," +
            "        \"image-small\": 13," +
            "        \"x-small\": 14," +
            "        \"small\": 16," +
            "        \"medium\": 18," +
            "        \"medium-large\": 20," +
            "        \"large\": 30," +
            "        \"textPaddingHorizontal\": 16," +
            "        \"article_coverTemplateMarginBottom\": 8," +
            "        \"authorTextPaddingHorizontal\": 30," +
            "        \"authorPaddingHorizontal\": 16," +
            "        \"authorPaddingVertical\": 8," +
            "        \"elementPaddingHorizontal\": \"textPaddingHorizontal\"," +
            "        \"elementPaddingVertical\": 4," +
            "        \"headlinePaddingHorizontal\": \"textPaddingHorizontal\"," +
            "        \"headlinePaddingVertical\": 8," +
            "        \"preamblePaddingVertical\": 2," +
            "        \"bodyPaddingHorizontal\": \"textPaddingHorizontal\"," +
            "        \"bodyPaddingVertical\": 12," +
            "        \"separatorMargin\": 10," +
            "        \"linkSeparatorInset\": \"textPaddingHorizontal\"," +
            "        \"packageSeparatorInset\": 16," +
            "        \"datelinePaddingVertical\": 10," +
            "        \"linkItemPaddingVertical\": 8," +
            "        \"linkItemPaddingHorizontal\": \"textPaddingHorizontal\"," +
            "        \"linkItemImagePaddingTop\": 0," +
            "        \"imageTextPaddingVertical\": 8," +
            "        \"factTitlePaddingVertical\": 2," +
            "        \"factSubjectPaddingVertical\": 2," +
            "        \"factBodyPaddingVertical\": 8," +
            "        \"imageTextPaddingHorizontal\": \"textPaddingHorizontal\"," +
            "        \"contentPartMarginVertical\": 0," +
            "        \"contentPartMarginHorizontal\": 0," +
            "        \"factContentPartPaddingVertical\": 8," +
            "        \"factContentPartMarginVertical\": 8," +
            "        \"webfallbackTextPaddingHorizontal\": \"textPaddingHorizontal\"," +
            "        \"webfallbackTextPaddingVertical\": 6," +
            "        \"packageHeadline\": 72," +
            "        \"packageSubtitle\": 16," +
            "        \"adPaddingVertical\": 8," +
            "        \"adPaddingHorizontal\": 30" +
            "    }," +
            "    \"linespacing\": {" +
            "        \"normal\": 1.2," +
            "        \"image\": 1.25," +
            "        \"big\": 1.33," +
            "        \"headline\": {" +
            "            \"multiplier\": 1.0," +
            "            \"extra\": 10" +
            "        }" +
            "    }," +
            "    \"font\": {" +
            "        \"android\": {" +
            "            \"regular\": \"Graphik-Regular-App.ttf\"," +
            "            \"bold\": \"Tiempos Headline-Bold.ttf\"," +
            "            \"semibold\": \"Graphik-Semibold-App.ttf\"," +
            "            \"detail\": \"Lato-Regular.ttf\"," +
            "            \"detailLight\": \"Produkt-Light-App.ttf\"," +
            "            \"detailBold\": \"Lato-Bold.ttf\"," +
            "            \"detailMedium\": \"Produkt-Medium-App.ttf\"," +
            "            \"headlineBold\": \"Tiempos Headline-Bold.ttf\"," +
            "            \"headlineBoldItalic\": \"Tiempos Headline-BoldItalic.ttf\"" +
            "        }," +
            "        \"ios\": {" +
            "            \"regular\": \"GraphikApp-Regular\"," +
            "            \"bold\": \"TiemposHeadline-Bold\"," +
            "            \"semibold\" : \"GraphikApp-Semibold\"," +
            "            \"detailLight\" : \"ProduktApp-light\"," +
            "            \"detail\" : \"Lato-Regular\"," +
            "            \"detailItalic\" : \"Lato-Italic\"," +
            "            \"detailBold\": \"Lato-Bold\"," +
            "            \"detailMedium\": \"ProduktApp-Medium\"," +
            "            \"headlineBold\": \"TiemposHeadline-Bold\"," +
            "            \"headlineBoldItalic\": \"TiemposHeadline-BoldItalic\"" +
            "        }" +
            "    }," +
            "    \"text\" : {" +
            "        \"packageIndex\": {" +
            "            \"size\": \"medium-large\"," +
            "            \"font\": \"headlineBold\"," +
            "            \"color\": \"packageIndex\"" +
            "        }," +
            "        \"nextPackageHeadline\": {" +
            "            \"size\": \"packageHeadline\"," +
            "            \"font\": \"detail\"," +
            "            \"color\": \"lightText\"" +
            "        }," +
            "        \"nextPackageSubtitle\": {" +
            "            \"size\": \"packageSubtitle\"," +
            "            \"font\": \"detailBold\"," +
            "            \"color\": \"lightTransparentText\"" +
            "        }," +
            "        \"justnuPrefix\": {" +
            "            \"size\": \"small\"," +
            "            \"font\": \"detailBold\"," +
            "            \"color\": \"red\"," +
            "            \"linespacing\" : \"normal\"" +
            "        }," +
            "        \"justnuTitle\": {" +
            "            \"size\": \"small\"," +
            "            \"font\": \"detailBold\"," +
            "            \"color\": \"primaryText\"," +
            "            \"linespacing\" : \"normal\"" +
            "        }," +
            "        \"coverHeadline\" : {" +
            "            \"size\": \"large\"," +
            "            \"font\": \"bold\"," +
            "            \"color\": \"pureWhite\"," +
            "            \"linespacing\" : \"big\"" +
            "        }," +
            "        \"coverDate\" : {" +
            "            \"size\": \"x-small\"," +
            "            \"font\": \"detail\"," +
            "            \"color\": \"lightTransparentText\"," +
            "            \"linespacing\" : \"big\"" +
            "        }," +
            "        \"coverDateline\" : {" +
            "            \"size\": \"x-small\"," +
            "            \"font\": \"detailBold\"," +
            "            \"color\": \"lightText\"," +
            "            \"transforms\": [\"uppercase\"]" +
            "        }," +
            "        \"date\" : {" +
            "            \"size\": \"mini\"," +
            "            \"font\": \"detail\"," +
            "            \"color\": \"secondaryText\"" +
            "        }," +
            "        \"webfallbackText\": \"preamble\"," +
            "        \"disabledLink\": {" +
            "            \"size\": \"small\"," +
            "            \"font\": \"detailBold\"," +
            "            \"color\": \"disabledLink\"" +
            "        }," +
            "        \"link\": {" +
            "            \"size\": \"small\"," +
            "            \"font\": \"bold\"," +
            "            \"color\": \"primaryText\"" +
            "        }," +
            "        \"linkItem\": {" +
            "            \"size\": \"small\"," +
            "            \"font\": \"bold\"," +
            "            \"color\": \"primaryText\"," +
            "            \"linespacing\" : \"big\"" +
            "        }," +
            "        \"disabledLinkItem\": {" +
            "            \"size\": \"small\"," +
            "            \"font\": \"bold\"," +
            "            \"color\": \"disabledLinkItem\"," +
            "            \"linespacing\" : \"big\"" +
            "        }," +
            "        \"headline\": {" +
            "            \"size\": \"large\"," +
            "            \"font\": \"bold\"," +
            "            \"color\": \"primaryText\"," +
            "            \"linespacing\" : \"headline\"" +
            "        }," +
            "        \"dateline\": {" +
            "            \"size\": \"mini\"," +
            "            \"font\": \"detailBold\"," +
            "            \"color\": \"primaryAccentColor\"," +
            "            \"transforms\": [" +
            "              \"uppercase\"" +
            "            ]" +
            "        }," +
            "        \"error\": {" +
            "            \"size\": \"medium\"," +
            "            \"font\": \"bold\"," +
            "            \"color\": \"textError\"" +
            "        }," +
            "        \"preamble\": {" +
            "            \"size\": \"small\"," +
            "            \"font\": \"detailBold\"," +
            "            \"color\": \"primaryText\"," +
            "            \"linespacing\" : \"big\"" +
            "        }," +
            "        \"body\": {" +
            "            \"size\": \"small\"," +
            "            \"font\": \"detail\"," +
            "            \"color\": \"primaryText\"," +
            "            \"linespacing\" : \"big\"" +
            "        }," +
            "        \"factTitle\": {" +
            "            \"size\": \"x-small\"," +
            "            \"font\": \"detailBold\"," +
            "            \"color\": \"primaryAccentColor\"," +
            "            \"linespacing\" : \"big\"," +
            "            \"transforms\": [" +
            "               \"uppercase\"" +
            "            ]" +
            "        }," +
            "        \"adText\": {" +
            "            \"size\": \"x-small\"," +
            "            \"font\": \"detail\"," +
            "            \"color\": \"primaryText\"," +
            "            \"transforms\": [" +
            "              \"uppercase\"" +
            "            ]" +
            "        }," +
            "        \"factSubject\": {" +
            "            \"size\": \"medium-large\"," +
            "            \"font\": \"detailBold\"," +
            "            \"color\": \"primaryText\"," +
            "            \"linespacing\" : \"big\"" +
            "        }," +
            "        \"factBody\": {" +
            "            \"size\": \"small\"," +
            "            \"font\": \"detail\"," +
            "            \"color\": \"factBody\"," +
            "            \"linespacing\" : \"big\"" +
            "        }," +
            "        \"imageText\": {" +
            "            \"size\": \"image-small\"," +
            "            \"font\": \"detailBold\"," +
            "            \"color\": \"secondaryText\"," +
            "            \"linespacing\" : \"image\"" +
            "        }," +
            "        \"authorHeader\": {" +
            "            \"size\": \"x-small\"," +
            "            \"font\": \"detailBold\"," +
            "            \"color\": \"primaryText\"" +
            "        }," +
            "        \"author\": {" +
            "            \"size\": \"mini\"," +
            "            \"font\": \"detailItalic\"," +
            "            \"color\": \"author\"" +
            "        }," +
            "        \"followButtonActive\": {" +
            "            \"size\": \"mini\"," +
            "            \"font\": \"detailBold\"," +
            "            \"color\": \"followActive\"," +
            "            \"transforms\": [\"uppercase\"]" +
            "        }," +
            "        \"followButtonInactive\": {" +
            "            \"size\": \"mini\"," +
            "            \"font\": \"detailBold\"," +
            "            \"color\": \"followInactive\"," +
            "            \"transforms\": [\"uppercase\"]" +
            "        }," +
            "        \"searchLeadin\": \"listLeadin\"," +
            "        \"listLeadin\": {" +
            "            \"size\": 16," +
            "            \"font\": \"detail\"," +
            "            \"color\": \"packageText\"," +
            "            \"linespacing\": \"normal\"" +
            "        }," +
            "        \"searchHeadline\": \"listHeadline\"," +
            "        \"packageHeadline\": { " +
            "            \"size\": 24, " +
            "            \"font\": \"headlineBold\", " +
            "            \"color\": \"packageText\", " +
            "            \"linespacing\": \"big\" " +
            "        }, " +
            "        \"listHeadline\": {" +
            "            \"size\": 24," +
            "            \"font\": \"headlineBold\"," +
            "            \"color\": \"primaryText\"," +
            "            \"linespacing\": \"headline\"" +
            "        }," +
            "        \"packageBody\": \"listBody\"," +
            "        \"searchBody\": \"listBody\"," +
            "        \"listBody\": {" +
            "            \"size\": \"small\"," +
            "            \"font\": \"detail\"," +
            "            \"color\": \"primaryText\"," +
            "            \"linespacing\": \"big\"" +
            "        }," +
            "        \"searchDateline\" : \"listDateline\"," +
            "        \"packageDateline\" : \"listDateline\"," +
            "        \"listDateline\": {" +
            "            \"size\": \"x-small\"," +
            "            \"font\": \"detailBold\"," +
            "            \"color\": \"#8dc154\"," +
            "            \"transforms\": [\"uppercase\"]" +
            "        }," +
            "        \"searchDate\": \"listDate\"," +
            "        \"packageDate\": \"listDate\"," +
            "        \"listDate\": {" +
            "            \"size\": \"x-small\"," +
            "            \"font\": \"detail\"," +
            "            \"color\": \"secondaryText\"," +
            "            \"transforms\": [\"lowercase\"]" +
            "        }," +
            "        \"packageHeadlineHeadline\": {" +
            "            \"size\": 20," +
            "            \"font\": \"headlineBoldItalic\"," +
            "            \"linespacing\": 1.8," +
            "            \"color\": \"#ffffff\"" +
            "        }," +
            "        \"packageHeadlineBody\": {" +
            "            \"size\": \"small\"," +
            "            \"font\": \"detailBold\"," +
            "            \"linespacing\": \"big\"," +
            "            \"color\": \"#eeeeee\"" +
            "        }," +
            "        \"packageHeadlineDescriptor\": {" +
            "            \"size\": 14," +
            "            \"font\": \"detailBold\"," +
            "            \"color\": \"#ffffff\"," +
            "            \"transforms\": [\"uppercase\"]" +
            "        }," +
            "        \"errorHeadline\": {" +
            "            \"size\": 20," +
            "            \"font\": \"headlineBoldItalic\"," +
            "            \"color\": \"#ff0000\"" +
            "        }," +
            "        \"errorMessage\": {" +
            "            \"size\": 20," +
            "            \"font\": \"detail\"," +
            "            \"color\": \"#333333\"" +
            "        }" +
            "    }," +
            "    \"image\" : {" +
            "        \"numberBackgroundSeen\": \"circle_drawable_filled\"," +
            "        \"numberBackgroundUnseen\": \"circle_drawable\"," +
            "        \"numberBackground\": \"numberBackgroundUnseen\"," +
            "        \"packageHeadlineDescriptorIcon\": \"sun\", " +
            "        \"logo\": \"logo\"," +
            "        \"linkItem\": \"link\"" +
            "    }" +
            "}";

    private static final String SEEN_THEME = "{" +
            "  \"color\": {" +
            "      \"numberBackgroundColor\": \"numberBackgroundColorRead\"," +
            "      \"packageIndex\": \"packageIndexRead\"" +
            "  }," +
            "  \"image\": {" +
            "    \"numberBackground\": \"numberBackgroundSeen\"" +
            "  }" +
            "}";

    private static final String EVENING_THEME = "{" +
            "  \"color\": {" +
            "    \"packageBackground\": \"#000000\"," +
            "    \"packageIcon\": \"#ffffff\"," +
            "    \"packageText\": \"#ffffff\"," +
            "    \"packageIndexRead\": \"#0000ff\"," +
            "    \"packageIndexUnread\": \"#ff0000\"," +
            "    \"numberBackgroundColorRead\": \"#ffffff\"," +
            "    \"numberBackgroundColorUnread\": \"#000000\"," +
            "    \"numberBackgroundOutlineColor\": \"#ffffff\"," +
            "    \"secondaryText\": \"packageText\"" +
            "  }," +
            "  \"image\": {" +
            "    \"numberBackgroundSeen\": \"circle_drawable_filled_night\"," +
            "    \"numberBackgroundUnseen\": \"circle_drawable_night\"," +
            "    \"packageHeadlineDescriptorIcon\": \"moon\"," +
            "    \"logo\": \"logo\"," +
            "    \"link\": \"link\"" +
            "  }," +
            "    \"text\" : {" +
            "        \"packageBody\": {" +
            "            \"size\": \"small\"," +
            "            \"font\": \"detail\"," +
            "            \"color\": \"packageText\"," +
            "            \"linespacing\": \"big\"" +
            "        }" +
            "    }" +
            "}";

    @Before
    public void runBefore() throws JSONException {
        FontLoader fontLoader = new FontLoader() {
            @Override
            public Typeface getTypeFace(String fontFileName) throws ThemeException {
                return null;
            }
        };

        context = InstrumentationRegistry.getContext();
        resourceManager = new ResourceManager(context, null);

        JSONObject baseDefinition = new JSONObject(BASE_JSON);
        baseTheme = new LayeredThemeBuilder().setDefinition(baseDefinition).build(resourceManager, fontLoader);

        JSONObject seenDefinition = new JSONObject(SEEN_THEME);
        seenTheme = new LayeredThemeBuilder().setDefinition(seenDefinition).setParent(baseTheme).build(resourceManager, fontLoader);

        JSONObject eveningDefinition = new JSONObject(EVENING_THEME);
        eveningTheme = new LayeredThemeBuilder().setDefinition(eveningDefinition).setParent(baseTheme).build(resourceManager, fontLoader);

        eveningThemeSeen = new LayeredThemeBuilder().setDefinition(seenDefinition).setParent(eveningTheme).build(resourceManager, fontLoader);
        seenThemeEvening = new LayeredThemeBuilder().setDefinition(eveningDefinition).setParent(seenTheme).build(resourceManager, fontLoader);
    }

    private ResourceManager resourceManager;
    private Context context;
    private LayeredTheme baseTheme;
    private LayeredTheme seenTheme;
    private LayeredTheme eveningTheme;
    private LayeredTheme eveningThemeSeen;
    private LayeredTheme seenThemeEvening;

    @Test
    public void testColor() throws JSONException {
        Assert.assertEquals(Color.parseColor("#FFFFFF"), seenTheme.getColor("packageIndex", null).get());
        Assert.assertEquals(Color.parseColor("#0000ff"), eveningThemeSeen.getColor("packageIndex", null).get());
        Assert.assertEquals(Color.parseColor("#0000ff"), seenThemeEvening.getColor("packageIndex", null).get());
        Assert.assertEquals(Color.parseColor("#ff0000"), eveningTheme.getColor("packageIndex", null).get());
    }

    @Test
    public void testDrawable() {
        int circle_drawable = resourceManager.getDrawableIdentifier("circle_drawable");
        int circle_drawable_filled = resourceManager.getDrawableIdentifier("circle_drawable_filled");
        int circle_drawable_night = resourceManager.getDrawableIdentifier("circle_drawable_night");
        int circle_drawable_filled_night = resourceManager.getDrawableIdentifier("circle_drawable_filled_night");

        if (circle_drawable == 0 ||
                circle_drawable_filled == 0 ||
                circle_drawable_night == 0 ||
                circle_drawable_filled_night == 0) {
            Assert.fail();
        }

        Assert.assertEquals(circle_drawable, baseTheme.getImage("numberBackground", null).getResourceId());
        Assert.assertEquals(circle_drawable_filled, seenTheme.getImage("numberBackground", null).getResourceId());
        Assert.assertEquals(circle_drawable_night, eveningTheme.getImage("numberBackground", null).getResourceId());
        Assert.assertEquals(circle_drawable_filled_night, eveningThemeSeen.getImage("numberBackground", null).getResourceId());
        Assert.assertEquals(circle_drawable_filled_night, seenThemeEvening.getImage("numberBackground", null).getResourceId());

        Assert.assertEquals(context.getDrawable(circle_drawable).getConstantState(), baseTheme.getImage("numberBackground", null).getImage(context).getConstantState());
        Assert.assertEquals(context.getDrawable(circle_drawable_filled).getConstantState(), seenTheme.getImage("numberBackground", null).getImage(context).getConstantState());
        Assert.assertEquals(context.getDrawable(circle_drawable_night).getConstantState(), eveningTheme.getImage("numberBackground", null).getImage(context).getConstantState());
        Assert.assertEquals(context.getDrawable(circle_drawable_filled_night).getConstantState(), eveningThemeSeen.getImage("numberBackground", null).getImage(context).getConstantState());
        Assert.assertEquals(context.getDrawable(circle_drawable_filled_night).getConstantState(), seenThemeEvening.getImage("numberBackground", null).getImage(context).getConstantState());
    }
}
