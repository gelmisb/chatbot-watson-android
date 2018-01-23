package com.example.vmac.WatBot;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
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

        Article article = (Article)getIntent().getSerializableExtra("Article");

        Log.d("ARTICLE BITH", article.getTitle());

        TextView aTitle = (TextView)findViewById(R.id.title);
        TextView aDescription = (TextView)findViewById(R.id.description);
        TextView aAuthor = (TextView)findViewById(R.id.author);
        TextView aDate = (TextView)findViewById(R.id.date);

        aTitle.setText(article.getTitle());
        aDescription.setText(article.getContent());
        aAuthor.setText(article.getAuthor());
        aDate.setText(article.getDate());


        String imageUrl = article.getImageURL();
        new GetImage().execute(imageUrl,imageUrl,imageUrl);

    }


    class GetImage extends AsyncTask<String, String, String> {

        URL url;

        @Override
        protected String doInBackground(String... urls) {

            try{
                System.out.println(urls[0] + "THIS IS THE URL WE'RE USING");
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
            iv = (ImageView) findViewById(R.id.image);
            iv.setImageDrawable(d);
        }
    }
}
