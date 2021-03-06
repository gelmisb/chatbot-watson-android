package com.example.vmac.WatBot;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class News extends AppCompatActivity {

    // Class references
    NewsModel model;
    ListView newsList;

    // UI components
    private static CustomListAdapter adapter;
    private Button backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_news);
        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);     //  Fixed Portrait orientation

        model = new NewsModel();

        backButton = (Button)findViewById(R.id.backBtn);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // stop talking when the application is closed.
                finish();

            }
        });
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        new Test().execute("https://newsapi.org/v2/everything?sources=the-irish-times&apiKey=b19ec501ac21494bb97c484975b6a764");

    }

    public void PopulateView(){

        newsList = (ListView) findViewById(R.id.newsList);

        adapter = new CustomListAdapter(model.getNewsList(),getBaseContext());

        newsList.setAdapter(adapter);

        newsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Vibrator vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                MediaPlayer mp = MediaPlayer.create(getApplicationContext(), R.raw.whoop);
                mp.start();
                if (vibe != null) {
                    vibe.vibrate(100);
                }


                Intent intent = new Intent(News.this,SingleNewsActivity.class);
                intent.putExtra("Article", model.getArticleAt((int)l));
                startActivity(intent);
            }
        });

    }

    private class Test extends AsyncTask<String,String,String>{


        @Override
        protected String doInBackground(String... uri) {
            HttpClient httpclient = new DefaultHttpClient();
            HttpResponse response;
            String responseString = null;
            try {
                response = httpclient.execute(new HttpGet(uri[0]));
                StatusLine statusLine = response.getStatusLine();
                if(statusLine.getStatusCode() == HttpStatus.SC_OK){
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    response.getEntity().writeTo(out);
                    responseString = out.toString("UTF-8");
                    out.close();
                } else{
                    //Closes the connection.
                    response.getEntity().getContent().close();
                    throw new IOException(statusLine.getReasonPhrase());
                }
            } catch (IOException ese) {
                ese.printStackTrace();

            }
            return responseString;

        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            try{
                JSONObject topHeadlines = new JSONObject(result);
                JSONArray metadata = topHeadlines.getJSONArray("articles");

                int count = metadata.length();

                for(int i = 0; i < count; i++){

                    JSONObject article = metadata.getJSONObject(i);

                    model.addArticle(new Article(article.getString("title"),article.getString("description"),article.getString("author"),article.getString("url"),article.getString("urlToImage"),article.getString("publishedAt")));

                }

                PopulateView();

            } catch (JSONException e){
                Log.d("App", e.toString());
            }

        }
    }
}
