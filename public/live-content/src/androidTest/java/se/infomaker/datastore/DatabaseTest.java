package se.infomaker.datastore;

import static org.junit.Assert.assertEquals;
import static se.infomaker.datastore.DatabaseSingleton.MIGRATION_5_6;

import android.content.Context;

import androidx.lifecycle.Observer;
import androidx.room.Room;
import androidx.room.testing.MigrationTestHelper;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import junit.framework.Assert;

import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

@RunWith(AndroidJUnit4.class)
public class DatabaseTest {

    private static final String TEST_DB = "migration-test";

    private AppDatabase db;
    private ArticleLastViewDao dao;

    @Rule
    public MigrationTestHelper helper;

    public DatabaseTest() {
        helper = new MigrationTestHelper(InstrumentationRegistry.getInstrumentation(),
                AppDatabase.class.getCanonicalName(),
                new FrameworkSQLiteOpenHelperFactory());
    }

    @Before
    public void createDB() {
        Context context = ApplicationProvider.getApplicationContext();
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase.class).build();
        dao = db.userLastViewDao();
    }

    @After
    public void closeDb() throws IOException {
        db.close();
    }

    @Test
    public void testCleanup() {
        Assert.assertEquals(0, dao.getAll().size());
        List<Article> articles = new ArrayList<>();
        articles.add(new Article(UUID.randomUUID().toString(), "My first article", new Date()));

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, -6);
        calendar.add(Calendar.DAY_OF_YEAR, -1);
        articles.add(new Article(UUID.randomUUID().toString(), "My second article", calendar.getTime()));

        dao.insertAll(articles);

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, -6);
        dao.cleanupOlderThan(cal.getTime());

        List<Article> actual = new ArrayList(articles);
        actual.remove(1);
        Assert.assertEquals(actual, dao.getAll());
    }

    @Test
    public void testUpdate() {
        Assert.assertEquals(0, dao.getAll().size());

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, -6);
        calendar.add(Calendar.DAY_OF_YEAR, -1);
        Article oldArticle = new Article(UUID.randomUUID().toString(), "My old article", calendar.getTime());
        dao.insert(oldArticle);

        Article newArticle = new Article(UUID.randomUUID().toString(), "My new article", new Date());
        dao.insert(newArticle);

        oldArticle.setLastViewed(new Date());
        dao.insert(oldArticle);

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, -6);
        dao.cleanupOlderThan(cal.getTime());

        List<Article> actual = new ArrayList<>();
        actual.add(newArticle);
        actual.add(oldArticle);
        Assert.assertEquals(actual, dao.getAll());
    }

    @Test
    public void migrate5To6() throws IOException, InterruptedException {
        SupportSQLiteDatabase db = helper.createDatabase(TEST_DB, 5);

        // db has schema version 5. insert some data using SQL queries.
        // You cannot use DAO classes because they expect the latest schema.
        String emptyJson = new JSONObject().toString();
        String firstUuid = "691bfff0-71fd-11ec-8737-3d7e7424d5c9";
        db.execSQL("INSERT INTO Bookmark VALUES (" +
                firstUuid + ", " +
                emptyJson + ", " +
                "toppnyheter, " +
                false + ", " +
                1641809488000L +
                ")");

        String secondUuid = "93e95227-6009-428c-b1e2-c615ae5ef075";
        db.execSQL("INSERT INTO Bookmark VALUES (" +
                secondUuid + ", " +
                emptyJson + ", " +
                "toppnyheter, " +
                false + ", " +
                1641806769000L +
                ")");

        // Prepare for the next version.
        db.close();

        // Re-open the database with version 6 and provide
        // MIGRATION_5_6 as the migration process.
        helper.runMigrationsAndValidate(TEST_DB, 6, true, MIGRATION_5_6);

        // MigrationTestHelper automatically verifies the schema changes,
        // but you need to validate that the data was migrated properly.
        BookmarkDao bookmarkDao = getMigratedRoomDatabase().bookmarkDao();
        CountDownLatch latch = new CountDownLatch(1);
        final Bookmark[] observedBookmarks = new Bookmark[2];
        bookmarkDao.get(firstUuid).observeForever(bookmark -> {
            observedBookmarks[0] = bookmark;
            latch.countDown();
        });
        bookmarkDao.get(secondUuid).observeForever(bookmark -> {
            observedBookmarks[1] = bookmark;
            latch.countDown();
        });
        latch.await();

        Bookmark firstBookmark = observedBookmarks[0];
        assertEquals(firstBookmark.getUuid(), firstUuid);

        Bookmark secondBookmark = observedBookmarks[1];
        assertEquals(secondBookmark.getUuid(), secondUuid);
    }

    private AppDatabase getMigratedRoomDatabase() {
        AppDatabase database = Room.databaseBuilder(ApplicationProvider.getApplicationContext(),
                AppDatabase.class, TEST_DB)
                .addMigrations(MIGRATION_5_6)
                .build();
        // close the database and release any stream resources when the test finishes
        helper.closeWhenFinished(database);
        return database;
    }
}
