package com.shrijay.image.manager;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.lang.ref.WeakReference;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by dinesh.k.masthaiah on 12-04-2016.
 */
public class ShrijayImageManager {
    public static String LOG_TAG = "ShrijayImageManager";
    private MemoryCacheManager mMemoryCache;
    public static final int NO_SCALING = -3;
    private static ShrijayImageManager mShrijayImageManager;
    private static Context mContext;
    private ImageRequest mImageRequest;
    private boolean mIsLoggingEnabled;

    private ShrijayImageManager() {
        mImageRequest = new ImageRequest();
        mImageRequest.mWidth = NO_SCALING;
        mImageRequest.mHeight = NO_SCALING;
        mMemoryCache = MemoryCacheManager.getInstance();
        mIsLoggingEnabled = false;
    }

    public static ShrijayImageManager with(Context context) {
        if (context == null) {
            return null;
        }
        mContext = context;
        mShrijayImageManager = new ShrijayImageManager();
        return mShrijayImageManager;
    }

    public ShrijayImageManager load(String url, View imageView, boolean isForeground) {
        mImageRequest.mView = new WeakReference<>(imageView);
        mImageRequest.mUrl = url;
        mImageRequest.mIsForeground = isForeground;
        return mShrijayImageManager;
    }

    public ShrijayImageManager makeItCircular() {
        mImageRequest.mIsCircular = true;
        return mShrijayImageManager;
    }

    public ShrijayImageManager setDimension(int width, int height) {
        mImageRequest.mWidth = getPixelsForDp(width);
        mImageRequest.mHeight = getPixelsForDp(height);
        return mShrijayImageManager;
    }

    public ShrijayImageManager enableLoggingForThisRequest(boolean isEnabled, String logTag) {
        if (logTag != null && !logTag.isEmpty()) {
            LOG_TAG = logTag;
        }
        return enableLoggingForThisRequest(isEnabled);
    }

    public ShrijayImageManager enableLoggingForThisRequest(boolean isEnabled) {
        mIsLoggingEnabled = isEnabled;
        return mShrijayImageManager;
    }

    public boolean isLoggingEnabled() {
        return mIsLoggingEnabled;
    }

    private int getPixelsForDp(int dp) {
        return (int) (dp * mContext.getResources().getDisplayMetrics().density);
    }

    public ShrijayImageManager onLoadedCallback(OnImageLoadedCallback onImageLoadedCallback) {
        mImageRequest.mOnImageLoadedCallback = onImageLoadedCallback;
        return mShrijayImageManager;
    }

    public ShrijayImageManager setProgressView(View progressView) {
        mImageRequest.mProgressView = progressView;
        return mShrijayImageManager;
    }

    public void submit() {
        if (mImageRequest.mWidth == NO_SCALING || mImageRequest.mHeight == NO_SCALING) {

            if (!isMatchParentOrWrapContent() && (mImageRequest.mView != null && mImageRequest.mView.get() != null)) {
                log("Dinesh", "Retreving the dimensions from view");
                mImageRequest.mWidth = getPixelsForDp(mImageRequest.mView.get().getLayoutParams().width);
                mImageRequest.mHeight = getPixelsForDp(mImageRequest.mView.get().getLayoutParams().height);
            }

        }
        if (mImageRequest.mUrl == null || mImageRequest.mView == null) {
            publishError(ImageLoadError.INVALID_REQUEST_ARGUMENTS);
            return;
        }
        loadBitmap();
    }

    private boolean isMatchParentOrWrapContent() {
        return (mImageRequest.mWidth == ViewGroup.LayoutParams.MATCH_PARENT || mImageRequest.mHeight == ViewGroup.LayoutParams.MATCH_PARENT || mImageRequest.mWidth == ViewGroup.LayoutParams.WRAP_CONTENT || mImageRequest.mHeight == ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    private void loadBitmap() {
        showProgress(true);
        Bitmap bitmap = mMemoryCache.getFromCache(String.valueOf(mImageRequest.mUrl));
        if (mImageRequest.mView.get() == null) {
            return;
        }
        if (bitmap != null) {
            if (mImageRequest.mIsForeground && mImageRequest.mView.get() instanceof ImageView) {
                if (mImageRequest.mIsCircular) {
                    ((ImageView) mImageRequest.mView.get()).setImageDrawable(makeCircular(bitmap));
                } else {
                    ((ImageView) mImageRequest.mView.get()).setImageBitmap(bitmap);
                }
            } else {
                mImageRequest.mView.get().setBackground(new BitmapDrawable(null, bitmap));
            }
            publishResult();
            showProgress(false);
            return;
        }
        loadFromDisk();

    }

    private void showProgress(boolean show) {
        if (mImageRequest.mProgressView != null) {
            if (show) {
                mImageRequest.mProgressView.setVisibility(View.VISIBLE);
            } else {
                mImageRequest.mProgressView.setVisibility(View.GONE);
            }
        }
    }

    private void publishResult() {
        if (mImageRequest.mOnImageLoadedCallback != null) {
            mImageRequest.mOnImageLoadedCallback.onImageLoadedSuccessful(mImageRequest.mUrl, mImageRequest.mView.get());
        }
    }

    private void publishError(int errorCode) {
        if (mImageRequest.mOnImageLoadedCallback != null) {
            ImageLoadError error = new ImageLoadError(errorCode);
            mImageRequest.mOnImageLoadedCallback.onImageLoadedError(error, mImageRequest.mUrl, mImageRequest.mView.get());
        }
    }


    private RoundedBitmapDrawable makeCircular(Bitmap bitmap) {
        if (bitmap == null || mImageRequest.mView.get() == null) {
            return null;
        }
        RoundedBitmapDrawable bitmapDrawable = RoundedBitmapDrawableFactory.create((mImageRequest.mView.get().getContext()).getResources(), bitmap);
        bitmapDrawable.setCircular(true);
        return bitmapDrawable;
    }

    private void downloadFromNet() {
        LoaderAsyncTask loaderTask = new LoaderAsyncTask(mContext);
        LoaderAsyncTask.Request request = new LoaderAsyncTask.Request();
        request.id = 123;
        request.type = LoaderAsyncTask.REQUEST_TYPE_DOWNLOAD_FROM_NET;
        request.dimensions = new int[2];
        request.dimensions[0] = mImageRequest.mWidth;
        request.dimensions[1] = mImageRequest.mHeight;
       log("Dinesh", "width=" + request.dimensions[0]);
        log("Dinesh", "height=" + request.dimensions[1]);
        request.key = mImageRequest.mUrl;
        request.callback = new LoaderAsyncTask.LoadCompletionListener() {

            @Override
            public void onLoaded(LoaderAsyncTask.Response response) {
                if (response.isSuccess && response.bitmap != null) {
                    handleResponse(response.bitmap);
                    showProgress(false);
                    saveToDisk(response.bitmap);
                } else {
                    publishError(ImageLoadError.FAILED_TO_DOWNLOAD_IMAGE);
                    showProgress(false);
                }
            }
        };

        loaderTask.executeOnExecutor(LoaderAsyncTask.THREAD_POOL_EXECUTOR, new LoaderAsyncTask.Request[]{request});
    }

    private void loadFromDisk() {
        LoaderAsyncTask loaderTask = new LoaderAsyncTask(mContext);
        LoaderAsyncTask.Request request = new LoaderAsyncTask.Request();
        request.id = 123;
        request.type = LoaderAsyncTask.REQUEST_TYPE_LOAD_FROM_DISK;
        request.dimensions = new int[2];
        request.key = getHash(mImageRequest.mUrl);
        request.callback = new LoaderAsyncTask.LoadCompletionListener() {

            @Override
            public void onLoaded(LoaderAsyncTask.Response response) {
                if (response.isSuccess && response.bitmap != null) {
                    handleResponse(response.bitmap);
                    showProgress(false);
                } else {
                    //in error case
                    downloadFromNet();
                }
            }
        };

        loaderTask.executeOnExecutor(LoaderAsyncTask.THREAD_POOL_EXECUTOR, new LoaderAsyncTask.Request[]{request});
    }

    private void saveToDisk(Bitmap bitmap) {
        LoaderAsyncTask loaderTask = new LoaderAsyncTask(mContext);
        LoaderAsyncTask.Request request = new LoaderAsyncTask.Request();
        request.id = 123;
        request.type = LoaderAsyncTask.REQUEST_TYPE_SAVE_TO_DISK;
        request.dimensions = new int[2];
        request.key = getHash(mImageRequest.mUrl);
        request.bitmap = bitmap;
        request.callback = new LoaderAsyncTask.LoadCompletionListener() {

            @Override
            public void onLoaded(LoaderAsyncTask.Response response) {
                if (response.isSuccess) {
                    //success case
                } else {
                    //error case
                }
            }
        };

        loaderTask.executeOnExecutor(LoaderAsyncTask.THREAD_POOL_EXECUTOR, new LoaderAsyncTask.Request[]{request});
    }

    private String getHash(String url) {
        //return String.valueOf(url);
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            messageDigest.update(url.getBytes());
            byte[] digested = messageDigest.digest();
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < digested.length; i++) {
                sb.append(Integer.toString((digested[i] & 0xff) + 0x100, 16).substring(1));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void handleResponse(Bitmap bitmap) {
        mMemoryCache.addToCache(String.valueOf(mImageRequest.mUrl), bitmap);
        if (mImageRequest.mView.get() == null) {
            return;
        }
        if (mImageRequest.mIsForeground) {
            if (mImageRequest.mIsCircular) {
                ((ImageView) mImageRequest.mView.get()).setImageDrawable(makeCircular(bitmap));
            } else {
                ((ImageView) mImageRequest.mView.get()).setImageBitmap(bitmap);
            }
        } else {
            BitmapDrawable drawable = new BitmapDrawable(null, bitmap);
            mImageRequest.mView.get().setBackground(drawable);
        }
        publishResult();
    }

    static class ImageRequest {
        private WeakReference<View> mView;
        private boolean mIsForeground;
        private boolean mIsCircular;
        private String mUrl;
        private OnImageLoadedCallback mOnImageLoadedCallback;
        private int mWidth;
        private int mHeight;
        private View mProgressView;
    }

    public void log(String module, String message) {
        if (isLoggingEnabled()) {
            Log.d(LOG_TAG, module + ":" + message);
        }
    }
}
