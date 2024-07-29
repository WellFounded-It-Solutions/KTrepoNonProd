package se.infomaker.livecontentui.livecontentdetailview.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.Map;

/**
 * Created by Magnus Ekström on 04/12/15.
 */
public class Article {
    @SerializedName("properties")
    ArrayList<ArticleProperty> properties;

    public Article() {
    }

    public Article(Map<String, ArrayList<String>> articleProperties) {
        properties = new ArrayList<>();
        for (Map.Entry<String, ArrayList<String>> pair : articleProperties.entrySet()) {
            if (pair.getValue() != null) {
                properties.add(new ArticleProperty(pair.getKey(), pair.getValue()));
            }
        }
    }

    public ArrayList<ArticleProperty> getProperties() {
        return properties;
    }

    public void setProperties(ArrayList<ArticleProperty> properties) {
        this.properties = properties;
    }
}
