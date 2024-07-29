package se.infomaker.livecontentmanager.query;


import androidx.annotation.NonNull;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;


import org.junit.Before;
import org.junit.Test;

import java.net.URISyntaxException;
import java.util.HashMap;

import io.reactivex.Scheduler;
import io.reactivex.android.plugins.RxAndroidPlugins;
import io.reactivex.internal.schedulers.ExecutorScheduler;
import io.reactivex.schedulers.Schedulers;
import se.infomaker.livecontentmanager.JUnitTree;
import com.navigaglobal.mobile.auth.BasicAuthAuthorizationProvider;
import se.infomaker.livecontentmanager.config.PropertyConfig;
import se.infomaker.livecontentmanager.config.SearchConfig;
import se.infomaker.livecontentmanager.config.StreamConfig;
import se.infomaker.livecontentmanager.config.TransformSettingsConfig;
import se.infomaker.livecontentmanager.parser.DefaultPropertyObjectParser;
import se.infomaker.livecontentmanager.parser.PropertyObject;
import se.infomaker.livecontentmanager.query.lcc.LCCQueryManager;
import se.infomaker.livecontentmanager.query.lcc.TestURLS;
import se.infomaker.livecontentmanager.query.lcc.infocaster.InfocasterConnection;
import se.infomaker.livecontentmanager.query.lcc.opencontent.OpenContentBuilder;
import se.infomaker.livecontentmanager.query.lcc.opencontent.OpenContentService;
import se.infomaker.livecontentmanager.query.lcc.querystreamer.Meta;
import se.infomaker.livecontentmanager.query.lcc.querystreamer.QueryStreamerManager;
import se.infomaker.livecontentmanager.query.lcc.querystreamer.QueryStreamerService;
import se.infomaker.livecontentmanager.query.lcc.querystreamer.QueryStreamerServiceBuilder;
import timber.log.Timber;

import static com.google.common.truth.Truth.assertThat;

public class ObjectResolverTest {

    private LCCQueryManager queryManager;

    Scheduler immediate = new Scheduler() {
        @Override
        public Worker createWorker() {
            return new ExecutorScheduler.ExecutorWorker(Runnable::run, true);
        }
    };
    private StreamConfig streamConfig;

    @Before
    public void setup() throws URISyntaxException {
        Timber.uprootAll();
        Timber.plant(new JUnitTree());
        RxAndroidPlugins.setInitMainThreadSchedulerHandler(scheduler -> immediate);
        InfocasterConnection connection = new InfocasterConnection.Builder().setRunnableHandler(new TestRunnableHandler()).setUrl(TestURLS.INFOCASTER_URL).create();
        OpenContentService openContentService = new OpenContentBuilder().setAuthorizationProvider(new BasicAuthAuthorizationProvider("bt", "daic6Nid")).setBaseUrl(TestURLS.OPEN_CONTENT_URL).setLog(true).build();
        QueryStreamerService service = new QueryStreamerServiceBuilder().setBaseUrl(TestURLS.QUERYSTREAMER_URL)
                .setLog(true)
                .setId("gota-test")
                .setReadToken("407d2c1b-e43a-4f9c-9634-b4dbdbaf7c90").build();
        QueryStreamerManager streamerManager = new QueryStreamerManager(connection, service, new Meta("test", "1.0", "createStream"));
        queryManager = new LCCQueryManager(openContentService, streamerManager);

        JsonObject baseQuery = new JsonParser().parse("{\"must\": [{\"match\": {\"ArticleType\": \"article\"}}]}").getAsJsonObject();
        streamConfig = new StreamConfig(null, baseQuery);
    }

    @Test
    public void testResolveFromId() {
        SearchConfig config = createSearchConfig();
        HashMap<String, HashMap<String, PropertyConfig>> propertyMap = createPropertyTypeMap();

        DefaultPropertyObjectParser parser = new DefaultPropertyObjectParser(propertyMap, new HashMap<>(), new TransformSettingsConfig());
        ObjectResolver objectResolver = new ObjectResolver(config, getPropertiesList(), parser, queryManager, Schedulers.single());
        PropertyObject propertyObject = objectResolver.resolve("10a0004d-b8e5-4491-a226-b3ac5fb93597", "Article").blockingFirst();
        assertThat(propertyObject).isNotNull();
        assertThat(propertyObject.getId()).isEqualTo("10a0004d-b8e5-4491-a226-b3ac5fb93597");
    }

    @NonNull
    private HashMap<String, HashMap<String, PropertyConfig>> createPropertyTypeMap() {
        HashMap<String, HashMap<String, PropertyConfig>> typePropertyMap = new HashMap<>();
        HashMap<String, PropertyConfig> articlePropertyMap = new HashMap<>();
        typePropertyMap.put("Article", articlePropertyMap);

        PropertyConfig uuid = new PropertyConfig("uuid", null, null, null, null);
        articlePropertyMap.put("contentId", uuid);

        HashMap<String, PropertyConfig> concept = new HashMap<>();
        typePropertyMap.put("ConecptAuthor", concept);

        PropertyConfig email = new PropertyConfig("Email", null, null, null, null);
        concept.put("email", email);

        PropertyConfig conceptName = new PropertyConfig("ConceptName", null, null, null, null);
        concept.put("name", conceptName);

        PropertyConfig conceptAuthor = new PropertyConfig("AuthorConcepts", null, "ConceptAuthor", null, null);
        articlePropertyMap.put("authors", conceptAuthor);

        return typePropertyMap;
    }

    @NonNull
    private String getPropertiesList() {
        return "uuid,AuthorConcepts[Email,ConceptName]";
    }

    @NonNull
    private SearchConfig createSearchConfig() {
        SearchConfig config = new SearchConfig("PubStatus:usable", "Article", "Pubdate");
        config.setContentProvider("framtidningen");
        config.setSortIndex("Pubdate");
        return config;
    }
}
