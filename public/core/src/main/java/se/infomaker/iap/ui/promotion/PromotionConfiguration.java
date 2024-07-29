package se.infomaker.iap.ui.promotion;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import se.infomaker.iap.ui.content.Content;
import se.infomaker.iap.ui.value.NoValueException;
import timber.log.Timber;

@SuppressWarnings("CanBeFinal")
public class PromotionConfiguration implements Content, Parcelable {


    private String id;
    private String doneButtonTitle;
    private String cancelButtonTitle;
    private String nextButtonTitle;
    private JSONObject doneAction;
    private JSONObject theme;
    private List<Page> pages;

    public PromotionConfiguration(String id, String doneButtonTitle,  String cancelButtonTitle, String nextButtonTitle, JSONObject doneAction, JSONObject theme, List<Page> pages) {
        this.id = id;
        this.doneButtonTitle = doneButtonTitle;
        this.nextButtonTitle = nextButtonTitle;
        this.cancelButtonTitle = cancelButtonTitle;
        this.doneAction = doneAction;
        this.theme = theme;
        this.pages = pages;
    }

    public String getId() {
        return id;
    }

    public String getDoneButtonTitle() {
        return doneButtonTitle;
    }

    public String getNextButtonTitle() {
        return nextButtonTitle;
    }

    public String getCancelButtonTitle() {
        return cancelButtonTitle;
    }

    public JSONObject getTheme() {
        return theme;
    }

    public List<Page> getPages() {
        return pages;
    }

    public JSONObject getDoneAction() {
        return doneAction;
    }

    @Override
    public Object getValue(String key) throws NoValueException {
        switch (key) {
            case "id": return id;
            case "doneButtonTitle" : return doneButtonTitle;
            case "nextButtonTitle" : return nextButtonTitle;
            default:
            {
                throw new NoValueException("No value for key" + key);
            }
        }
    }

    @Override
    public Object optValue(String key) {
        try {
            return getValue(key);
        } catch (NoValueException e) {
            return null;
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.id);

        writeNonNull(dest, this.doneButtonTitle);
        writeNonNull(dest, this.nextButtonTitle);
        writeNonNull(dest, this.cancelButtonTitle);

        writeNonNull(dest, this.theme != null ? this.theme.toString() : null);
        writeNonNull(dest, this.doneAction != null ? this.doneAction.toString() : null);
        dest.writeList(this.pages);
    }

    private void writeNonNull(Parcel dest, String value) {
        dest.writeByte((byte) (value != null ? 1 : 0));
        if (value != null) {
            dest.writeString(value);
        }
    }

    private String readNotNull(Parcel in){
        boolean themeIsPresent = in.readByte() == 1;
        return themeIsPresent ? in.readString() : null;
    }


    private PromotionConfiguration(Parcel in) throws JSONException {
        this.id = in.readString();
        this.doneButtonTitle = readNotNull(in);
        this.nextButtonTitle = readNotNull(in);
        this.cancelButtonTitle = readNotNull(in);
        String value = readNotNull(in);
        if (value != null) {
            theme = new JSONObject(value);
        }
        value = readNotNull(in);
        if (value != null) {
            doneAction = new JSONObject(value);
        }
        this.pages = new ArrayList<>();
        in.readList(this.pages, Page.class.getClassLoader());
    }



    public static final Parcelable.Creator<PromotionConfiguration> CREATOR = new Parcelable.Creator<PromotionConfiguration>() {
        @Override
        public PromotionConfiguration createFromParcel(Parcel source) {
            try {
                return new PromotionConfiguration(source);
            } catch (JSONException e) {
                Timber.e(e, "Could not create PromotionConfiguration from parcelable");
            }
            return null;
        }

        @Override
        public PromotionConfiguration[] newArray(int size) {
            return new PromotionConfiguration[size];
        }
    };
}
