package se.infomaker.datastore;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.Date;
import java.util.List;

import io.reactivex.Flowable;

import static androidx.room.OnConflictStrategy.REPLACE;

@Dao
public interface ArticleLastViewDao {
    @Query("SELECT * FROM article")
    List<Article> getAll();

    @Query("SELECT * FROM article")
    LiveData<List<Article>> liveAll();

    @Query("SELECT * FROM article WHERE uuid IN (:uuids)")
    List<Article> getArticles(String[] uuids);

    @Query("SELECT * FROM article WHERE uuid IN (:uuids)")
    List<Article> getArticles(List<String> uuids);

    @Query("SELECT * FROM article")
    Flowable<List<Article>> subscribeAll();

    @Query("SELECT * FROM article WHERE uuid IN (:uuids)")
    Flowable<List<Article>> subscribeArticles(String[] uuids);

    @Query("SELECT * FROM article WHERE uuid IN (:uuids)")
    Flowable<List<Article>> subscribeArticles(List<String> uuids);

    @Insert(onConflict = REPLACE)
    void insertAll(Article... articles);

    @Insert(onConflict = REPLACE)
    void insertAll(List<Article> articles);

    @Insert(onConflict = REPLACE)
    void insert(Article article);

    @Query("DELETE FROM article WHERE last_viewed < :age OR last_viewed IS NULL")
    void cleanupOlderThan(Date age);

    @Delete
    void delete(Article article);

    @Delete
    void delete(Article... articles);
}