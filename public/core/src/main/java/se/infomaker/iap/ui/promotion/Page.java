package se.infomaker.iap.ui.promotion;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import se.infomaker.iap.ui.content.Content;
import se.infomaker.iap.ui.value.KeyPathValueExtractor;
import se.infomaker.iap.ui.value.NoValueException;
import timber.log.Timber;

public class Page extends JSONObject implements Content, Parcelable {

    private static final String PAGE_DEFAULT = "default_promotion_slide";
    private static final String THEME = "theme";
    private static final String VIEW = "view";

    public Page(){
        super();
    }

    public Page(String fromString) throws JSONException {
        super(fromString);
    }

    public JSONObject getTheme() {
        return optJSONObject(THEME);
    }

    public String getViewName() {
        return optString(VIEW, PAGE_DEFAULT);
    }

    @Override
    public Object getValue(String key) throws NoValueException {
        try {
            return get(key);
        } catch (JSONException e) {
            throw new NoValueException(e);
        }
    }

    @Override
    public Object optValue(String key) {
        return opt(key);
    }

    /**
     * Create bindings to match available content
     * @return list of bindings extrapolated from content
     */
    public List<Binding> createBindings() {
        ArrayList<Binding> bindings = new ArrayList<>();
        try {
            JSONObject object = new JSONObject(toString());
            object.remove(THEME);
            object.remove(VIEW);
            Iterator<String> keys = object.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                KeyPathValueExtractor keypathValueExtractor = new KeyPathValueExtractor(key);
                bindings.add(new Binding(key, keypathValueExtractor));

            }
        } catch (JSONException e) {
            Timber.e(e, "Could not create binding");
        }
        return bindings;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.toString());
    }

    protected Page(Parcel in) throws JSONException {
        this(in.readString());
    }

    public static final Parcelable.Creator<Page> CREATOR = new Parcelable.Creator<Page>() {
        @Override
        public Page createFromParcel(Parcel source) {
            try {
                return new Page(source);
            } catch (JSONException e) {
                Timber.e(e, "Could not create page from parcelable");
            }
            return new Page();
        }

        @Override
        public Page[] newArray(int size) {
            return new Page[size];
        }
    };
}
