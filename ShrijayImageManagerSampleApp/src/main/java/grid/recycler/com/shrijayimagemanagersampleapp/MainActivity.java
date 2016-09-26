package grid.recycler.com.shrijayimagemanagersampleapp;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.shrijay.image.manager.ImageLoadError;
import com.shrijay.image.manager.OnImageLoadedCallback;
import com.shrijay.image.manager.ShrijayImageManager;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ImageView imageView = (ImageView) findViewById(R.id.image1);
        ImageView imageView2 = (ImageView) findViewById(R.id.image2);
        ImageView imageView3 = (ImageView) findViewById(R.id.image3);
        View progressView = findViewById(R.id.progress_view);
        ShrijayImageManager
                .with(this)
                .load("http://www.dineshmasthi.biz.ly/images/meditation.jpg", imageView, true)
                .setProgressView(progressView)
                .setDimension(200, 200)
                .onLoadedCallback(new OnImageLoadedCallback() {
                    @Override
                    public void onImageLoadedSuccessful(String Url, View view) {
                        Toast.makeText(MainActivity.this, "Image Loaded", Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onImageLoadedError(ImageLoadError imageLoadError, String Url, View view) {

                    }
                }).submit();

        ShrijayImageManager
                .with(this)
                .load("http://www.dineshmasthi.biz.ly/images/my_recent_photo.jpg", imageView2, true)
                .setProgressView(progressView)
                .setDimension(200, 200)
                .makeItCircular()
                .onLoadedCallback(new OnImageLoadedCallback() {
                    @Override
                    public void onImageLoadedSuccessful(String Url, View view) {
                        Toast.makeText(MainActivity.this, "Image Loaded", Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onImageLoadedError(ImageLoadError imageLoadError, String Url, View view) {

                    }
                }).submit();

        ShrijayImageManager
                .with(this)
                .load("http://www.dineshmasthi.biz.ly/images/shwe.jpg", imageView3, true)
                .setProgressView(progressView)
                .makeItCircular()
                .onLoadedCallback(new OnImageLoadedCallback() {
                    @Override
                    public void onImageLoadedSuccessful(String Url, View view) {
                        Toast.makeText(MainActivity.this, "Image Loaded", Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onImageLoadedError(ImageLoadError imageLoadError, String Url, View view) {

                    }
                }).submit();
    }

}
