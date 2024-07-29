package se.infomaker.datastore;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Date;

@Entity
public class Article {
    @PrimaryKey
    @NonNull
    private String uuid;
    @NonNull
    private String name;
    @ColumnInfo(name = "last_viewed")
    private Date lastViewed;

    public Article(@NonNull String uuid, String name, Date lastViewed) {
        this.uuid = uuid;
        this.name = name;
        this.lastViewed = lastViewed;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getLastViewed() {
        return lastViewed;
    }

    public void setLastViewed(Date lastViewed) {
        this.lastViewed = lastViewed;
    }

    @Override
    public String toString() {
        return uuid + " : " + name + " : " + lastViewed.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Article article = (Article) o;

        return getUuid().equals(article.getUuid());
    }

    @Override
    public int hashCode() {
        return getUuid().hashCode();
    }
}
