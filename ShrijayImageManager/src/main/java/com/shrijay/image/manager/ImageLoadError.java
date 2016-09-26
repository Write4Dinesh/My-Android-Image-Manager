package com.shrijay.image.manager;

/**
 * Created by dinesh.k.masthaiah on 21-09-2016.
 */
public class ImageLoadError {
    public static final int INVALID_REQUEST_ARGUMENTS = 100;
    public static final int UNSUPPORTED_REQUEST_TYPE = 101;
    public static final int FAILED_TO_DOWNLOAD_IMAGE = 102;
    public int mErrorCode;

    public ImageLoadError() {
    }

    public ImageLoadError(int errorCode) {
        mErrorCode = errorCode;
    }

    public String getDescription(int errorCode) {
        switch (errorCode) {
            case INVALID_REQUEST_ARGUMENTS:
                return "The Arguments passed are invalid";
            case FAILED_TO_DOWNLOAD_IMAGE:
                return "ImageManager could not able to download the image from the server. try again later";
            case UNSUPPORTED_REQUEST_TYPE:
                return "unknown request";
        }
        return "Un-known error code";
    }
}
