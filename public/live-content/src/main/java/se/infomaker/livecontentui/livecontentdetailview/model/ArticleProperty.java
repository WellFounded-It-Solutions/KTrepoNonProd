package se.infomaker.livecontentui.livecontentdetailview.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
 * Created by Magnus Ekström on 04/12/15.
 */
public class ArticleProperty {
    @SerializedName("name")
    String name;

    @SerializedName("values")
    ArrayList<String> values;

    public ArticleProperty(){
    }

    public ArticleProperty(String name, ArrayList<String> values){
        this.name = name;
        this.values = values;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<String> getValues() {
        return values;
    }

    public void setValues(ArrayList<String> values) {
        this.values = values;
    }
}
