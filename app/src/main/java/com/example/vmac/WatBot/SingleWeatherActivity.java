package com.example.vmac.WatBot;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.ComponentCallbacks2;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

public class SingleWeatherActivity extends AppCompatActivity {


    Drawable d;
    ImageView iv;
    Button backButton;

    private String dow;
    private int temp;
    private int feelsLike;
    private int icon;
    private String phrase;
    private int pop;
    private String popType;
    private String clouds;
    private String rh;
    private String wspd;


    private ProgressDialog simpleWaitDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_single_weather);
        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);     //  Fixed Portrait orientation


        backButton = (Button)findViewById(R.id.backBtn);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // stop talking when the application is closed.
                finish();

            }
        });


        final Weatherw weatherw = (Weatherw)getIntent().getSerializableExtra("Weatherw");


        TextView dow = (TextView)findViewById(R.id.dow);
        TextView temp = (TextView)findViewById(R.id.temp);
        TextView feelsLike = (TextView)findViewById(R.id.feelslike);
        ImageView icon = (ImageView) findViewById(R.id.icon);
        TextView pop = (TextView)findViewById(R.id.pop);
        TextView popType = (TextView)findViewById(R.id.poptype);
        TextView clouds = (TextView)findViewById(R.id.clouds);
        TextView rh = (TextView)findViewById(R.id.rh);
        TextView wspd = (TextView)findViewById(R.id.wspd);
        TextView time = (TextView)findViewById(R.id.time);

        dow.setText(weatherw.getDow());
        temp.setText(weatherw.getTemp() + "°C "  + weatherw.getPhrase());
        feelsLike.setText("Feels like: " + weatherw.getFeelsLike() + "°C");

        icon.setImageResource(weatherw.getIcon());


        if(weatherw.getPop() != 0)
            pop.setText(weatherw.getPop());

        pop.setText("Probability of precipitation: 0");
        popType.setText("Precipitation type: " + weatherw.getPopType().toUpperCase());
        clouds.setText("Cloud coverage: " + weatherw.getClouds() + "%");
        rh.setText("Relative humidity " + weatherw.getRh() + "%");
        wspd.setText( "Wind : " + weatherw.getWspd() + "km/h");
        time.setText(weatherw.getTime());


        onTrimMemory(TRIM_MEMORY_RUNNING_MODERATE);
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
