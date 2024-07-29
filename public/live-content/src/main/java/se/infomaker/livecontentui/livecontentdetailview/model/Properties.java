package se.infomaker.livecontentui.livecontentdetailview.model;

import com.google.gson.annotations.SerializedName;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class Properties {

    @SerializedName("AuthorImageUuid")
    private ArrayList<String> authorImageUuid;
    @SerializedName("ImageUuid")
    private ArrayList<String> imageUuid;
    @SerializedName("Pubdate")
    private ArrayList<String> pubdate;
    @SerializedName("Product")
    private ArrayList<String> product;
    private ArrayList<String> updated;
    @SerializedName("Text")
    private ArrayList<String> text;
    @SerializedName("TeaserLeadin")
    private ArrayList<String> teaserLeadin;
    @SerializedName("Author")
    private ArrayList<String> author;
    @SerializedName("TeaserHeadline")
    private ArrayList<String> headline;
    @SerializedName("ArticleDateline")
    private ArrayList<String> articleDateline;
    @SerializedName("Location")
    private ArrayList<String> location;


    public Properties() {
    }

    public Properties(JSONObject json) {

        this.authorImageUuid = new ArrayList<String>();
        JSONArray arrayAuthorImageUuid = json.optJSONArray("AuthorImageUuid");
        if (null != arrayAuthorImageUuid) {
            int authorImageUuidLength = arrayAuthorImageUuid.length();
            for (int i = 0; i < authorImageUuidLength; i++) {
                String item = arrayAuthorImageUuid.optString(i);
                if (null != item) {
                    this.authorImageUuid.add(item);
                }
            }
        } else {
            String item = json.optString("AuthorImageUuid");
            if (null != item) {
                this.authorImageUuid.add(item);
            }
        }

        this.imageUuid = new ArrayList<String>();
        JSONArray arrayImageUuid = json.optJSONArray("ImageUuid");
        if (null != arrayImageUuid) {
            int imageUuidLength = arrayImageUuid.length();
            for (int i = 0; i < imageUuidLength; i++) {
                String item = arrayImageUuid.optString(i);
                if (null != item) {
                    this.imageUuid.add(item);
                }
            }
        } else {
            String item = json.optString("ImageUuid");
            if (null != item) {
                this.imageUuid.add(item);
            }
        }

        this.pubdate = new ArrayList<String>();
        JSONArray arrayPubdate = json.optJSONArray("Pubdate");
        if (null != arrayPubdate) {
            int pubdateLength = arrayPubdate.length();
            for (int i = 0; i < pubdateLength; i++) {
                String item = arrayPubdate.optString(i);
                if (null != item) {
                    this.pubdate.add(item);
                }
            }
        } else {
            String item = json.optString("Pubdate");
            if (null != item) {
                this.pubdate.add(item);
            }
        }


        this.product = new ArrayList<String>();
        JSONArray arrayProduct = json.optJSONArray("Product");
        if (null != arrayProduct) {
            int productLength = arrayProduct.length();
            for (int i = 0; i < productLength; i++) {
                String item = arrayProduct.optString(i);
                if (null != item) {
                    this.product.add(item);
                }
            }
        } else {
            String item = json.optString("Product");
            if (null != item) {
                this.product.add(item);
            }
        }


        this.updated = new ArrayList<String>();
        JSONArray arrayUpdated = json.optJSONArray("updated");
        if (null != arrayUpdated) {
            int updatedLength = arrayUpdated.length();
            for (int i = 0; i < updatedLength; i++) {
                String item = arrayUpdated.optString(i);
                if (null != item) {
                    this.updated.add(item);
                }
            }
        } else {
            String item = json.optString("updated");
            if (null != item) {
                this.updated.add(item);
            }
        }


        this.text = new ArrayList<String>();
        JSONArray arrayText = json.optJSONArray("Text");
        if (null != arrayText) {
            int textLength = arrayText.length();
            for (int i = 0; i < textLength; i++) {
                String item = arrayText.optString(i);
                if (null != item) {
                    this.text.add(item);
                }
            }
        } else {
            String item = json.optString("Text");
            if (null != item) {
                this.text.add(item);
            }
        }

        this.teaserLeadin = new ArrayList<String>();
        JSONArray arrayTeaserLeadin = json.optJSONArray("TeaserLeadin");
        if (null != arrayTeaserLeadin) {
            int teaserLeadinLength = arrayTeaserLeadin.length();
            for (int i = 0; i < teaserLeadinLength; i++) {
                String item = arrayTeaserLeadin.optString(i);
                if (null != item) {
                    this.teaserLeadin.add(item);
                }
            }
        } else {
            String item = json.optString("TeaserLeadin");
            if (null != item) {
                this.teaserLeadin.add(item);
            }
        }

        this.author = new ArrayList<String>();
        JSONArray arrayAuthor = json.optJSONArray("Author");
        if (null != arrayAuthor) {
            int authorLength = arrayAuthor.length();
            for (int i = 0; i < authorLength; i++) {
                String item = arrayAuthor.optString(i);
                if (null != item) {
                    this.author.add(item);
                }
            }
        } else {
            String item = json.optString("Author");
            if (null != item) {
                this.author.add(item);
            }
        }


        this.headline = new ArrayList<String>();
        JSONArray arrayHeadline = json.optJSONArray("TeaserHeadline");
        if (null != arrayHeadline) {
            int headlineLength = arrayHeadline.length();
            for (int i = 0; i < headlineLength; i++) {
                String item = arrayHeadline.optString(i);
                if (null != item) {
                    this.headline.add(item);
                }
            }
        } else {
            String item = json.optString("TeaserHeadline");
            if (null != item) {
                this.headline.add(item);
            }
        }

        this.articleDateline = new ArrayList<String>();
        JSONArray arrayArticleDateline = json.optJSONArray("ArticleDateline");
        if (null != arrayArticleDateline) {
            int arrayArticleDatelineLength = arrayArticleDateline.length();
            for (int i = 0; i < arrayArticleDatelineLength; i++) {
                String item = arrayArticleDateline.optString(i);
                if (null != item) {
                    this.articleDateline.add(item);
                }
            }
        } else {
            String item = json.optString("ArticleDateline");
            if (null != item) {
                this.articleDateline.add(item);
            }
        }

        this.location = new ArrayList<String>();
        JSONArray arrayLocation = json.optJSONArray("Location");
        if (null != arrayLocation) {
            int arrayArticleDatelineLength = arrayLocation.length();
            for (int i = 0; i < arrayArticleDatelineLength; i++) {
                String item = arrayLocation.optString(i);
                if (null != item) {
                    this.articleDateline.add(item);
                }
            }
        } else {
            String item = json.optString("Location");
            if (null != item) {
                this.articleDateline.add(item);
            }
        }
    }

    public ArrayList<String> getAuthorImageUuid() {
        return this.authorImageUuid;
    }

    public void setAuthorImageUuid(ArrayList<String> authorImageUuid) {
        this.authorImageUuid = authorImageUuid;
    }

    public ArrayList<String> getImageUuid() {
        return this.imageUuid;
    }

    public void setImageUuid(ArrayList<String> imageUuid) {
        this.imageUuid = imageUuid;
    }

    public ArrayList<String> getPubdate() {
        return this.pubdate;
    }

    public void setPubdate(ArrayList<String> pubdate) {
        this.pubdate = pubdate;
    }

    public ArrayList<String> getProduct() {
        return this.product;
    }

    public void setProduct(ArrayList<String> product) {
        this.product = product;
    }

    public ArrayList<String> getUpdated() {
        return this.updated;
    }

    public void setUpdated(ArrayList<String> updated) {
        this.updated = updated;
    }

    public ArrayList<String> getText() {
        return this.text;
    }

    public void setText(ArrayList<String> text) {
        this.text = text;
    }

    public ArrayList<String> getTeaserLeadin() {
        return this.teaserLeadin;
    }

    public void setgetTeaserLeadin(ArrayList<String> teaserLeadin) {
        this.teaserLeadin = teaserLeadin;
    }

    public ArrayList<String> getAuthor() {
        return this.author;
    }

    public void setAuthor(ArrayList<String> author) {
        this.author = author;
    }

    public ArrayList<String> getHeadline() {
        return this.headline;
    }

    public void setHeadline(ArrayList<String> headline) {
        this.headline = headline;
    }

    public ArrayList<String> getLocation() {
        return this.location;
    }

    public void setLocation(ArrayList<String> location) {
        this.location = location;
    }

    public ArrayList<String> getArticleDateline() {
        return this.articleDateline;
    }

    public void setArticleDateline(ArrayList<String> articleDateline) {
        this.articleDateline = articleDateline;
    }

}
