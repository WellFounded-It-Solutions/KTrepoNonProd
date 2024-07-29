package se.infomaker.frtutilities;

import junit.framework.Assert;

import org.junit.Test;

import java.util.Map;

public class ShortCodeParserTest {

    @Test
    public void testParseOK() throws InvalidShortCodeException {
        String code = "[myshortstuff name=\"John Doe\"]";
        ShortCodeObject object = ShortCodeParser.parseShortcode(code);
        Assert.assertEquals(object.getName(), "myshortstuff");
        Map<String, Object> attributes = object.getAttributes();
        Assert.assertTrue(attributes.containsKey("name"));
        Assert.assertEquals("John Doe", attributes.get("name"));
    }

    @Test(expected = InvalidShortCodeException.class)
    public void testParseEmptyString() throws InvalidShortCodeException {
        ShortCodeParser.parseShortcode("");
        Assert.fail();
    }

    @Test(expected = InvalidShortCodeException.class)
    public void testParseNoName() throws InvalidShortCodeException {
        ShortCodeParser.parseShortcode("[name=\"tryggve\"]");
        Assert.fail();
    }

    @Test(expected = NullPointerException.class)
    public void testParseNull() throws InvalidShortCodeException {
        ShortCodeParser.parseShortcode(null);
        Assert.fail();
    }

    @Test
    public void testQuotesInAttributeValue() throws InvalidShortCodeException {
        String code = "[myshortstuff name='Emil \"bulten\" Gedda']";
        ShortCodeObject object = ShortCodeParser.parseShortcode(code);
        Assert.assertEquals("Emil \"bulten\" Gedda", object.getAttributes().get("name"));
    }

    @Test
    public void testSingleQuotesInAttributeValue() throws InvalidShortCodeException {
        String code = "[myshortstuff name=\"Emil 'bulten' Gedda\"]";
        ShortCodeObject object = ShortCodeParser.parseShortcode(code);
        Assert.assertEquals("Emil 'bulten' Gedda", object.getAttributes().get("name"));
    }

    @Test()
    public void testParseInvalidShortCode() throws InvalidShortCodeException {
        String code = "[myshortstuff name=\"John Doe]";
        ShortCodeObject object = ShortCodeParser.parseShortcode(code);
        Assert.assertEquals("myshortstuff", object.getName());
    }

    @Test()
    public void testOnlyName() throws InvalidShortCodeException {
        String code = "[myshortstuff]";
        ShortCodeObject object = ShortCodeParser.parseShortcode(code);
        Assert.assertEquals("myshortstuff", object.getName());
        Assert.assertTrue(object.getAttributes().isEmpty());
    }
}