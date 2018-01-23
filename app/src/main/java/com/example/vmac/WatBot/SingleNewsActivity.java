package com.example.vmac.WatBot;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import java.net.URL;

import static com.facebook.FacebookSdk.getApplicationContext;

public class SingleNewsActivity extends AppCompatActivity {


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
        ImageView iv = (ImageView)findViewById(R.id.image);

        aTitle.setText(article.getTitle());
        aDescription.setText(article.getContent());
        aAuthor.setText(article.getAuthor());
        aDate.setText(article.getDate());


//        iv.setImageBitmap(new ImageDownloader().execute(article.getImageURL()));

    }
}
