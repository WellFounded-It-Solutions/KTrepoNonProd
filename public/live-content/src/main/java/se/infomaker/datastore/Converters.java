package se.infomaker.datastore;

import androidx.room.TypeConverter;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

import timber.log.Timber;

public class Converters {
    @TypeConverter
    public static Date fromTimestamp(Long value) {
        return value == null ? null : new Date(value);
    }

    @TypeConverter
    public static Long dateToTimestamp(Date date) {
        return date == null ? null : date.getTime();
    }

    @TypeConverter
    public static JSONObject stringToJSON(String json) {
        if (json != null) {
            try {
                return new JSONObject(json);
            }
            catch (JSONException e) {
                Timber.e("Could not convert stored string: %s to JSONObject.", json);
            }
        }
        return null;
    }

    @TypeConverter
    public static String stringFromJSON(JSONObject jsonObject) {
        return jsonObject != null ? jsonObject.toString() : null;
    }
}
