package com.shrijay.image.manager;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by dinesh.k.masthaiah on 12-04-2016.
 */
public class NetworkImageLoader {
    private String mUrl;
    public static Object BITMAP_DECODE_LOCK = new Object();

    public Bitmap download(String url, int width, int height) {
        mUrl = url;
        long downloadSize = 0;
        //ShrijayLogger.debugDiagnosticLog("NetworkImageLoader", "Initiating an Image Download with URL=" + mUrl);
        //HorizonsStopWatch watch = new HorizonsStopWatch("NetworkImageLoader");
        //watch.start("download image from " + mUrl);
        BufferedInputStream bufferedInputStream = null;
        ByteArrayOutputStream byteArrayOutputStream = null;
        Bitmap bitmap = null;
        try {
            bufferedInputStream = new BufferedInputStream(new URL(mUrl).openStream());
            byteArrayOutputStream = new ByteArrayOutputStream();
            byte[] data = new byte[1024];//1KB at a time
            int bytesRead;
            while ((bytesRead = bufferedInputStream.read(data)) != -1) {
                byteArrayOutputStream.write(data, 0, bytesRead);
            }
            byte[] imageData = byteArrayOutputStream.toByteArray();
            downloadSize = imageData.length;
            Log.d("Dinesh", "bytes=" + downloadSize);
            synchronized (BITMAP_DECODE_LOCK) {
                if (height == ShrijayImageManager.NO_SCALING || width == ShrijayImageManager.NO_SCALING) {
                    bitmap = BitmapFactory.decodeByteArray(imageData, 0, imageData.length);
                } else {
                    bitmap = decodeBitmapWithScaling(imageData, width, height);
                }
            }
        } catch (MalformedURLException mue) {
            //ShrijayLogger.errorLog("NetworkImageLoader", "Url=" + mUrl, mue);
        } catch (IOException ioe) {
            // ShrijayLogger.errorLog("NetworkImageLoader", "Url=" + mUrl, ioe);
        } finally {
            try {
                if (bufferedInputStream != null) {
                    bufferedInputStream.close();
                }
                if (byteArrayOutputStream != null) {
                    byteArrayOutputStream.close();
                }
            } catch (Exception e) {
                //ShrijayLogger.errorLog("NetworkImageLoader", "Url=" + mUrl, e);
            }
        }
        // ShrijayLogger.debugDiagnosticLog("NetworkImageLoader", "Url=" + mUrl + ", downloadSize=" + HorizonsDiagnosticUtility.populateMemSize(downloadSize));
        //watch.stop();
        return bitmap;
    }

    private Bitmap decodeBitmapWithScaling(byte[] imageData, int reqWidth, int reqHeight) {
        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(imageData, 0, imageData.length, options);
        final int imageHeight = options.outHeight;
        final int imageWidth = options.outWidth;
        //String memoryRequired = HorizonsDiagnosticUtility.populateMemSize((imageHeight * imageWidth * 4));
        //ShrijayLogger.debugDiagnosticLog("NetworkImageLoader", "SCALING:ORIGINAL_IMAGE Size,width=" + imageWidth + ",height=" + imageHeight + ",memory" + memoryRequired + ":Url=" + mUrl);
        calculateInSampleSize(options, reqWidth, reqHeight, true);
        return BitmapFactory.decodeByteArray(imageData, 0, imageData.length, options);
    }

    private void calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int imageHeight = options.outHeight;
        final int imageWidth = options.outWidth;
        int inSampleSize = 1;
        if (imageHeight > reqHeight || imageWidth > reqWidth) {
            final int halfHeight = imageHeight / 2;
            final int halfWidth = imageWidth / 2;
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }
        options.inSampleSize = inSampleSize;
        options.inJustDecodeBounds = false;
    }

    private void calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight, boolean isCenterInside) {
        int sampleSize = 1;
        int width = options.outWidth;
        int height = options.outHeight;
        if (height > reqHeight || width > reqWidth) {
            final int heightRatio;
            final int widthRatio;
            if (reqHeight == 0) {
                sampleSize = (int) Math.floor((float) width / (float) reqWidth);
            } else if (reqWidth == 0) {
                sampleSize = (int) Math.floor((float) height / (float) reqHeight);
            } else {
                heightRatio = (int) Math.floor((float) height / (float) reqHeight);
                widthRatio = (int) Math.floor((float) width / (float) reqWidth);
                sampleSize = isCenterInside
                        ? Math.max(heightRatio, widthRatio)
                        : Math.min(heightRatio, widthRatio);
            }
        }
        options.inSampleSize = sampleSize;
        options.inJustDecodeBounds = false;
    }
}
