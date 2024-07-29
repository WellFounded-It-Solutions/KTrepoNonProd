package se.infomaker.livecontentui.livecontentrecyclerview.binder;

import java.util.List;

import se.infomaker.livecontentmanager.parser.PropertyObject;

public class FieldValidator {

    public static boolean validateRequiredFields(List<String> requiredFields, PropertyObject properties) {
        if (requiredFields != null) {
            int templateRequiredFieldsFound = 0;
            for (String requiredField : requiredFields) {
                String value = properties.optString(requiredField);
                if (value != null && !value.trim().isEmpty()) {
                    templateRequiredFieldsFound++;
                    break;
                }
            }
            if (requiredFields.size() <= templateRequiredFieldsFound) {
                return true;
            }
            return false;
        }
        return true;
    }
}
