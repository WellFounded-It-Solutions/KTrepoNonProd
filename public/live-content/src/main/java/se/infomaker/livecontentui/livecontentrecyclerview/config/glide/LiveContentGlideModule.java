package se.infomaker.livecontentui.livecontentrecyclerview.config.glide;

import android.content.Context;

import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.Registry;
import com.bumptech.glide.load.engine.cache.DiskLruCacheWrapper;
import com.bumptech.glide.load.engine.cache.LruResourceCache;
import com.bumptech.glide.module.GlideModule;

import se.infomaker.livecontentui.livecontentrecyclerview.config.ConfigConstants;

public class LiveContentGlideModule implements GlideModule {

    @Override
    public void applyOptions(final Context context, GlideBuilder builder) {
        // Apply options to the builder here.
        builder.setDiskCache(() -> DiskLruCacheWrapper.get(
                        Glide.getPhotoCacheDir(context),
                        ConfigConstants.MAX_DISK_CACHE_SIZE));
        builder.setMemoryCache(new LruResourceCache(ConfigConstants.MAX_MEMORY_CACHE_SIZE));
    }


    @Override
    public void registerComponents(Context context, Glide glide, Registry registry) {

    }
}
