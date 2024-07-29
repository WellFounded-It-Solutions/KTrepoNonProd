package se.infomaker.iap.theme.transforms;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

import se.infomaker.iap.theme.attribute.AttributeParseException;
import se.infomaker.iap.theme.attribute.ThemeAttributeParser;


public class ThemeTransformsParser extends ThemeAttributeParser<ThemeTransforms> {
    @Override
    public boolean isValueObject(Object value) {
        return (value instanceof String && ((String) value).startsWith("["));
    }

    @Override
    public ThemeTransforms parseObject(Object value) throws AttributeParseException {
        JSONArray transformsJson;
        try {
            transformsJson = new JSONArray((String) value);
            List<ThemeTransforms.Transforms> transforms = new ArrayList<>();
            String transformer;
            for (int i = 0; i < transformsJson.length(); i++) {
                transformer = transformsJson.getString(i);
                switch (transformer) {
                    case "uppercase":
                        transforms.add(ThemeTransforms.Transforms.UPPERCASE);
                        break;
                    case "capitalize":
                        transforms.add(ThemeTransforms.Transforms.CAPITALIZE);
                        break;
                    default:
                        throw new AttributeParseException("Transformer " + transformer + " is unknown. Check for spelling errors");
                }
            }
            return new ThemeTransforms(transforms);
        } catch (JSONException e) {
            throw new AttributeParseException("Could not parse", e);
        }
    }
}
