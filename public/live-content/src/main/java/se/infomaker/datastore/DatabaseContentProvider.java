package se.infomaker.datastore;

import android.content.Context;

import org.jetbrains.annotations.NotNull;

import java.util.Calendar;

import se.infomaker.frtutilities.AbstractInitContentProvider;


public class DatabaseContentProvider extends AbstractInitContentProvider {
    @Override
    public void init(@NotNull Context context) {
        AppDatabase appDatabase = DatabaseSingleton.initDatabaseInstance(context);
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, -6);
        new Thread(() -> appDatabase.userLastViewDao().cleanupOlderThan(cal.getTime()));
        new Thread(appDatabase::frequencyDao);
        ArticleLastViewMemoryCache.INSTANCE.get();
    }
}
