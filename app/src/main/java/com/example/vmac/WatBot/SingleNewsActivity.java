package com.example.vmac.WatBot;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.ComponentCallbacks2;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import org.apache.http.HttpEntity;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import static com.facebook.FacebookSdk.getApplicationContext;

public class SingleNewsActivity extends AppCompatActivity {

    Drawable d;
    ImageView iv;

    private ProgressDialog simpleWaitDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_news);

        final Article article = (Article)getIntent().getSerializableExtra("Article");

        Log.d("ARTICLE BITH", article.getTitle());

        TextView aTitle = (TextView)findViewById(R.id.title);
        TextView aDescription = (TextView)findViewById(R.id.description);
        TextView aAuthor = (TextView)findViewById(R.id.author);
        TextView aDate = (TextView)findViewById(R.id.date);
        Button aLink = (Button)findViewById(R.id.read);

        aTitle.setText(article.getTitle());
        aDescription.setText(article.getContent());
        aAuthor.setText(article.getAuthor());
        aDate.setText(article.getDate());

        aLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MediaPlayer mp = MediaPlayer.create(getApplicationContext(), R.raw.whoop);
                mp.start();
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(article.getURL()));
                startActivity(browserIntent);

            }
        });


        String imageUrl = article.getImageURL();
        new GetImage().execute(imageUrl,null,null);

        onTrimMemory(TRIM_MEMORY_RUNNING_MODERATE);
    }


    @SuppressLint("StaticFieldLeak")
    private class GetImage extends AsyncTask<String, String, String> {

        URL url;

        @Override
        protected String doInBackground(String... urls) {

            try{
                url = new URL(urls[0]);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }

            try
            {
                InputStream is = (InputStream) url.getContent();
                d = Drawable.createFromStream(is, "src name");
                return "";
            } catch (Exception e)
            {
                System.out.println("Exc=" + e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(String image){
            Animation myFadeInAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fadein);
            iv = (ImageView) findViewById(R.id.image);
            iv.setImageDrawable(d);
            iv.startAnimation(myFadeInAnimation); //Set animation to your ImageView

        }
    }


    /**
     * Release memory when the UI becomes hidden or when system resources become low.
     * @param level the memory-related event that was raised.
     */
    public void onTrimMemory(int level) {

        // Determine which lifecycle or system event was raised.
        switch (level) {

            case ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN:

                /*
                   Release any UI objects that currently hold memory.

                   The user interface has moved to the background.
                */

                break;

            case ComponentCallbacks2.TRIM_MEMORY_RUNNING_MODERATE:
            case ComponentCallbacks2.TRIM_MEMORY_RUNNING_LOW:
            case ComponentCallbacks2.TRIM_MEMORY_RUNNING_CRITICAL:

                /*
                   Release any memory that your app doesn't need to run.

                   The device is running low on memory while the app is running.
                   The event raised indicates the severity of the memory-related event.
                   If the event is TRIM_MEMORY_RUNNING_CRITICAL, then the system will
                   begin killing background processes.
                */

                break;

            case ComponentCallbacks2.TRIM_MEMORY_BACKGROUND:
            case ComponentCallbacks2.TRIM_MEMORY_MODERATE:
            case ComponentCallbacks2.TRIM_MEMORY_COMPLETE:

                /*
                   Release as much memory as the process can.

                   The app is on the LRU list and the system is running low on memory.
                   The event raised indicates where the app sits within the LRU list.
                   If the event is TRIM_MEMORY_COMPLETE, the process will be one of
                   the first to be terminated.
                */

                break;

            default:
                /*
                  Release any non-critical data structures.

                  The app received an unrecognized memory level value
                  from the system. Treat this as a generic low-memory message.
                */
                break;
        }
    }
}
