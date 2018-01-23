package com.example.vmac.WatBot;

import android.app.LauncherActivity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class News extends AppCompatActivity {

    NewsModel model;
    ListView newsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news);

        model = new NewsModel();

        new Test().execute("https://newsapi.org/v2/top-headlines?sources=the-irish-times&apiKey=b19ec501ac21494bb97c484975b6a764");

    }

    public void PopulateView(){

        newsList = (ListView) findViewById(R.id.newsTitleList);

        ArrayAdapter<Article> articleArrayAdapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_list_item_1, model.getNewsList());

        newsList.setAdapter(articleArrayAdapter);

        newsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

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
            } catch (ClientProtocolException e) {
                //TODO Handle problems..
            } catch (IOException e) {
                //TODO Handle problems..
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
