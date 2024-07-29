package se.infomaker.livecontentmanager.query;

import org.json.JSONException;
import org.json.JSONObject;

public class SimpleQuery implements Query {

    private final String query;

    public SimpleQuery()
    {
        query = "uuid:c71114a1-5f4a-4f93-8e05-baecf42c9c2a";
    }

    public SimpleQuery(String query)
    {
        this.query = query;
    }

    @Override
    public boolean finishedOnResponse() {
        return true;
    }

    @Override
    public JSONObject toJSONObject() {
        String request = "{" +
                "   \"payload\": {" +
                "       \"action\": \"search\"," +
                "       \"auth\": {}," +
                "       \"contentProvider\": {" +
                "           \"id\": \"framtidningen\"" +
                "       }," +
                "       \"data\": {" +
                "           \"query\": {" +
                "               \"q\": \"" + query + "\"," +
                "               \"start\": 0," +
                "               \"property\": [\"uuid\", \"Pubdate\", \"NewsValue\", \"TeaserHeadline\", \"TeaserLeadin\", \"ArticleDateline\", \"ArticleHeadline\", \"ArticleDrophead\", \"ArticleLeadin\", \"ArticleBody\", \"ArticleCaption\", \"FactHeadline\", \"factBody\", \"ImageUuid\", \"ImageWidth\", \"ImageHeight\", \"AuthorByline\", \"AuthorImageUuid\", \"PhotographerByline\", \"PhotographerImageUuid\", \"RelatedLinks\", \"GeoPoints\"]," +
                "               \"sort.name\": \"Publiceringsdag\"," +
                "               \"limit\": 50," +
                "               \"contenttype\": \"Article\"" +
                "           }" +
                "       }," +
                "       \"version\": 1" +
                "   }" +
                "}";
        try {
            return new JSONObject(request);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String toString() {
        return "SimpleQuery{" +
                "query='" + query + '\'' +
                '}';
    }
}
