package com.shrijay.image.manager;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by dinesh.k.masthaiah on 21-09-2016.
 */
public class LoaderAsyncTask extends AsyncTask<LoaderAsyncTask.Request, Integer, LoaderAsyncTask.Response> {
    public static final int REQUEST_TYPE_DOWNLOAD_FROM_NET = 1;
    public static final int REQUEST_TYPE_LOAD_FROM_DISK = 2;
    public static final int REQUEST_TYPE_SAVE_TO_DISK = 3;
    private boolean mIsForeground;
    private boolean mIsCircular;
    private Context mContext;
    private Request mRequest;

    public LoaderAsyncTask(Context context) {
mContext = context;
    }

    @Override
    protected Response doInBackground(Request... params) {
        mRequest = params[0];
        Bitmap bitmap;
        Response response = new Response();
        response.id = mRequest.id;
        response.key = mRequest.key;
        response.type = mRequest.type;
        response.isSuccess = true;
        if (mRequest.type == REQUEST_TYPE_LOAD_FROM_DISK) {
            Thread.currentThread().setName("ImageDownloadAsyncTask");
            bitmap = DiskCacheManager.getInstance(mContext).get(getHash(mRequest.key));
            response.bitmap = bitmap;
        } else if (mRequest.type == REQUEST_TYPE_SAVE_TO_DISK) {
            if (mRequest.bitmap != null) {
                DiskCacheManager.getInstance(mContext).put(getHash(mRequest.key), mRequest.bitmap);
                response.bitmap = mRequest.bitmap;
            } else {
                response.isSuccess = false;
                response.error = new ImageLoadError(ImageLoadError.INVALID_REQUEST_ARGUMENTS);
            }
        } else if (mRequest.type == REQUEST_TYPE_DOWNLOAD_FROM_NET) {
            bitmap = new NetworkImageLoader().download(mRequest.key, mRequest.dimensions[0], mRequest.dimensions[1]);
            response.bitmap = bitmap;
            return response;
        } else {
            response.isSuccess = false;
            response.error = new ImageLoadError(ImageLoadError.UNSUPPORTED_REQUEST_TYPE);
        }
        return response;
    }

    @Override
    protected void onPostExecute(Response response) {
        super.onPostExecute(response);
        mRequest.callback.onLoaded(response);
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

    static class Request {
        int type;
        int id;
        String key; // or url
        int[] dimensions;
        Bitmap bitmap;
        LoadCompletionListener callback;
    }

    static class Response {
        int type;
        int id;
        boolean isSuccess;
        String key;
        Bitmap bitmap;
        ImageLoadError error;
    }

    public interface LoadCompletionListener {
        void onLoaded(Response response);
    }
}
