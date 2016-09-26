package com.shrijay.image.manager;

import android.graphics.Bitmap;
import android.util.LruCache;

/**
 * Created by dinesh.k.masthaiah on 12-04-2016.
 */
public class MemoryCacheManager {
    private static MemoryCacheManager diskCache;//Global object that will be tied to App Life cycle rather than any Activity
    private final LruCache<String, Bitmap> mMemoryCache;
    private int mCacheSize;
    private long mTotalVMmemory;
    private long mCurrentHeapSize;
    private long mAvailableHeapWithoutExpanding;

    public synchronized static MemoryCacheManager getInstance() {
        if (diskCache == null) {
            diskCache = new MemoryCacheManager();
        }
        return diskCache;
    }

    private MemoryCacheManager() {
        mCurrentHeapSize = Runtime.getRuntime().totalMemory();
        mTotalVMmemory = Runtime.getRuntime().maxMemory();
        mCacheSize = (int) mTotalVMmemory / 10;
        mMemoryCache = new LruCache<>(mCacheSize);

    }

    public void addToCache(String id, Bitmap bitmap) {
        mMemoryCache.put(id, bitmap);
    }

    public Bitmap getFromCache(String id) {
        return mMemoryCache.get(id);
    }

    public long getMaxCacheSize() {
        return mCacheSize;
    }

    public void clearAll() {
        mMemoryCache.evictAll();
    }

    public void trimToSize(int maxSize) {
        mMemoryCache.trimToSize(maxSize);
    }


}
