package se.infomaker.livecontentmanager.util;

import androidx.annotation.NonNull;

import junit.framework.Assert;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

import se.infomaker.frtutilities.JSONUtil;

public class JSONUtilTest {

    @Test
    public void constructionTest()
    {
        // While this test does not make any sense, it provides 100% coverage as only
        // static methods does not instantiate the class
        Assert.assertNotNull(new JSONUtil());
    }

    @Test
    public void testGetString() throws JSONException {
        String id = JSONUtil.getString(testObject(), "payload.contentProvider.id");
        Assert.assertEquals("framtidningen", id);
    }

    @Test
    public void testOptString()
    {
        String id = JSONUtil.optString(testObject(), "payload.contentProvider.id");
        Assert.assertEquals("framtidningen", id);
    }

    @Test
    public void testOptStringMissing()
    {
        String result = JSONUtil.optString(testObject(), "payload.no.way.boy");
        Assert.assertTrue("".equals(result));
    }

    @Test
    public void testGetJSONObject() throws JSONException {
        JSONObject object = JSONUtil.getJSONObject(testObject(), "payload.contentProvider");
        Assert.assertEquals("framtidningen", object.getString("id"));
    }

    @NonNull
    private JSONObject testObject() {
        try {
            return new JSONObject("{" +
                    "\"payload\": {" +
                    "\"contentProvider\": {" +
                    "\"id\": \"framtidningen\"" +
                    "}" +
                    "}" +
                    "}");
        } catch (JSONException e) {
            return null;
        }
    }


}
