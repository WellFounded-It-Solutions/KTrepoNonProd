package se.infomaker.frtutilities;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import org.jetbrains.annotations.NotNull;

public abstract class AbstractInitContentProvider extends ContentProvider {
    @Override
    public boolean onCreate() {
        if (getContext() != null) {
            init(getContext().getApplicationContext());
            return true;
        }

        return false;
    }

    /**
     * Called once per application load
     * @param context
     */
    public abstract void init(@NotNull Context context);

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        return null;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }
}
