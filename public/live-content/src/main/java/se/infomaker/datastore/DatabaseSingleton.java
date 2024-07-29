package se.infomaker.datastore;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Room;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

public class DatabaseSingleton {
    private static AppDatabase db;

    static final Migration MIGRATION_5_6 = new Migration(5, 6) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE Bookmark ADD COLUMN bookmarkedDate INTEGER");
        }
    };

    static AppDatabase initDatabaseInstance(Context context) {
        db = Room.databaseBuilder(context.getApplicationContext(), AppDatabase.class, "article-view")
                .addMigrations(MIGRATION_5_6)
                .fallbackToDestructiveMigration()
                .build();
        return db;
    }

    @NonNull
    public static AppDatabase getDatabaseInstance() {
        return db;
    }
}
