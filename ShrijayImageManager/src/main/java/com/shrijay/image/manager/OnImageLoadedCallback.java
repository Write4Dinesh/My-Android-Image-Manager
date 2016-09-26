package com.shrijay.image.manager;

import android.view.View;

/**
 * Created by dinesh.k.masthaiah on 21-09-2016.
 */
public interface OnImageLoadedCallback {
    public void onImageLoadedSuccessful(String Url, View view);

    public void onImageLoadedError(ImageLoadError imageLoadError, String Url, View view);
}
