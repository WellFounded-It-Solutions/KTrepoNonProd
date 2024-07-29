package se.infomaker.datastore;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

@Database(entities = {Article.class, FrequencyRecord.class, Bookmark.class}, version = 6)
@TypeConverters({Converters.class})
public abstract class AppDatabase extends RoomDatabase {
    public abstract ArticleLastViewDao userLastViewDao();
    public abstract FrequencyDao frequencyDao();
    public abstract BookmarkDao bookmarkDao();
}