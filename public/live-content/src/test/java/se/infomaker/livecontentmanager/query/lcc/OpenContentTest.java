package se.infomaker.livecontentmanager.query.lcc;

import com.google.gson.JsonObject;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.CountDownLatch;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import se.infomaker.livecontentmanager.JUnitTree;
import com.navigaglobal.mobile.auth.BasicAuthAuthorizationProvider;
import com.navigaglobal.mobile.auth.ClientCredentialsAuthorizationProvider;
import com.navigaglobal.mobile.auth.TokenService;
import se.infomaker.livecontentmanager.query.lcc.opencontent.OpenContentBuilder;
import se.infomaker.livecontentmanager.query.lcc.opencontent.OpenContentService;
import timber.log.Timber;

public class OpenContentTest {

    @Before
    public void setup() {
        Timber.uprootAll();
        Timber.plant(new JUnitTree());
    }

    @Test
    public void testSearch() throws InterruptedException {
        OpenContentService openContent = new OpenContentBuilder().setBaseUrl(TestURLS.OPEN_CONTENT_URL).setLog(true)
                .setAuthorizationProvider(new BasicAuthAuthorizationProvider("bt", "daic6Nid")).build();

        testSearch(openContent);
    }

    @Test
    public void testClientCredentials() throws InterruptedException {
        TokenService tokenService = new Retrofit.Builder()
                .client(new OkHttpClient.Builder().build())
                .baseUrl(TestURLS.TOKEN_SERVICE)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(TokenService.class);
        OpenContentService openContent = new OpenContentBuilder()
                .setBaseUrl(TestURLS.OPEN_CONTENT_STAGE_URL)
                .setAuthorizationProvider(new ClientCredentialsAuthorizationProvider("b07780fc-74ad-4255-80bb-6ff0d051b372", "KgJ2PmHwny7K3WfAR6Nr8vm8I1GDMyZTGbV3KI4ztnX6dFORFT6z81OMQJ06i93V", tokenService))
                .build();
        testSearch(openContent);
    }

    private void testSearch(OpenContentService openContent) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        JsonObject[] result = new JsonObject[1];
        result[0] = null;
        openContent.search(0, 50, "uuid", "*:*", "Article", new HashMap<>()).subscribe(response -> {
                    result[0] = response.body();
                    latch.countDown();
                }, throwable -> {
                    Timber.e(throwable);
                    latch.countDown();
                }

        );
        latch.await();
        Assert.assertNotNull(result[0]);
    }

    @Test
    public void testQueryParamOrder() {
        String expected = TestURLS.OPEN_CONTENT_URL +"/opencontent/search?contenttype=Article&limit=50&properties=ArticleBodyRaw,ArticleContent,ArticleContentType,ArticleDateline,ArticleHeadline,AuthorConcepts%5BConceptName,Email,contenttype,uuid%5D,Categories%5BConceptName,uuid%5D,contenttype,Geometries%5BConceptName,uuid%5D,ImageHeights,ImageUuids,ImageWidths,NewsValue,Pubdate,RelatedLinks,ShowContentExplanation,Stories%5BConceptName,uuid%5D,TagConcepts%5BConceptName,uuid%5D,TeaserHeadline,TeaserText,TeaserText,uuid&q=PubStatus:usable%20AND%20-Tags:foobar%20AND%20ConceptCategoryUuids:95d3e986-26b7-4b14-b436-8c79c8645412&sort.name=Publiceringsdag&start=0";
        HashMap<String, String> parameters = new HashMap<>();
        
        parameters.put("q", "PubStatus:usable%20AND%20-Tags:foobar%20AND%20ConceptCategoryUuids:95d3e986-26b7-4b14-b436-8c79c8645412");
        parameters.put("start", "0");
        parameters.put("limit", "50");
        parameters.put("contenttype", "Article");
        parameters.put("properties", "ArticleBodyRaw,ArticleContent,ArticleContentType,ArticleDateline,ArticleHeadline,AuthorConcepts%5BConceptName,Email,contenttype,uuid%5D,Categories%5BConceptName,uuid%5D,contenttype,Geometries%5BConceptName,uuid%5D,ImageHeights,ImageUuids,ImageWidths,NewsValue,Pubdate,RelatedLinks,ShowContentExplanation,Stories%5BConceptName,uuid%5D,TagConcepts%5BConceptName,uuid%5D,TeaserHeadline,TeaserText,TeaserText,uuid");
        parameters.put("sort.name", "Publiceringsdag");

        OpenContentService openContent = new OpenContentBuilder().setBaseUrl(TestURLS.OPEN_CONTENT_URL)
                .addInterceptor(chain -> {
                    HttpUrl url = chain.request().url();
                    Assert.assertEquals(expected, url.toString());
                    throw new HappyFlowException();
                }).build();
        try {
            openContent.search(parameters).blockingGet();
            throw new RuntimeException("You should not be here!");
        } catch (Exception e) {
            if (e.getCause() instanceof HappyFlowException) {
                // Happy
            } else {
                Assert.fail();
            }
        }
    }

    private class HappyFlowException extends IOException {
    }


}
