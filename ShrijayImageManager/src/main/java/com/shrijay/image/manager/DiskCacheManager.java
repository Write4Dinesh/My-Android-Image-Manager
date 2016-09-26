package com.shrijay.image.manager;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.jakewharton.disklrucache.DiskLruCache;
import com.jakewharton.disklrucache.DiskLruCache.Editor;
import com.jakewharton.disklrucache.DiskLruCache.Snapshot;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

public class DiskCacheManager {
    private File mDiskCacheFile;
    private DiskLruCache mDiskLruCache;
    private static DiskCacheManager mHorizonsDiskCacheManager = null;
    private static Context mContext;

    private DiskCacheManager() {
        if (mContext != null) {
            String cacheFilePath = String.format("%s/%s", mContext.getFilesDir(), "/horizons_event_app_cache");
            //ShrijayLogger.debugLog("HorizonsDiskCacheManager", "cache file path=" + cacheFilePath);
            mDiskCacheFile = new File(cacheFilePath);
        }
        mContext = null; // release context;
    }

    public synchronized static DiskCacheManager getInstance(Context context) {
        mContext = context;
        if (mHorizonsDiskCacheManager == null) {
            mHorizonsDiskCacheManager = new DiskCacheManager();
        }
        return mHorizonsDiskCacheManager;
    }

    private synchronized void openDiskCache() throws IOException {
        if (mDiskLruCache == null) {
            mDiskLruCache = DiskLruCache.open(mDiskCacheFile, 100, 1, ShrijayUtility.getDiskCacheSize());
        }
    }

    public Bitmap get(String key) {
        byte[] result = null;
        Snapshot current = null;
        try {
            openDiskCache();
            current = mDiskLruCache.get(key);
            if (current != null) {
                //ShrijayLogger.debugLog("Found Item in DiskCache with key=" + key);
                BufferedInputStream bufferedInputStream = new BufferedInputStream(current.getInputStream(0));
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                byte[] data = new byte[1024];
                int bytesRead;
                while ((bytesRead = bufferedInputStream.read(data)) != -1) {
                    byteArrayOutputStream.write(data, 0, bytesRead);
                }
                result = byteArrayOutputStream.toByteArray();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (current != null) {
                current.close();
                try {
                    if (current.getInputStream(0) != null) {
                        current.getInputStream(0).close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        if (result != null) {
            return BitmapFactory.decodeByteArray(result, 0, result.length);
        }
        return null;
    }

    public boolean put(String key, Bitmap bitmap) {
        boolean returnResult = false;
        byte[] result = null;
        synchronized (NetworkImageLoader.BITMAP_DECODE_LOCK) {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            result = stream.toByteArray();
        }
        Editor put = null;
        OutputStream out = null;
        try {
            openDiskCache();
            put = mDiskLruCache.edit(key);
            out = new BufferedOutputStream(put.newOutputStream(0), 3000);
            put.newOutputStream(0).write(result);
            put.commit();
            returnResult = true;

        } catch (Exception e) {
        } finally {
            try {
                if (out != null) {
                    out.close();
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
            if (put != null) {
                put.abortUnlessCommitted();
            }

        }

        return returnResult;

    }

    public void close() {
        try {
            if (mDiskLruCache != null) {
                mDiskLruCache.close();
                mDiskLruCache = null;
            }
        } catch (IOException e) {
        }
    }

    public void clear() {
        try {
            if (mDiskLruCache != null) {
                mDiskLruCache.delete();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
