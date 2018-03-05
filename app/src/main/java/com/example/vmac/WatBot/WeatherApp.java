package com.example.vmac.WatBot;

import android.*;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.location.Geocoder;
import android.location.Location;
import android.os.Handler;
import android.os.ResultReceiver;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.transition.Explode;
import android.transition.Fade;
import android.transition.Transition;
import android.transition.TransitionInflater;
import android.transition.TransitionManager;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.ibm.watson.developer_cloud.android.library.audio.StreamPlayer;
import com.ibm.watson.developer_cloud.text_to_speech.v1.TextToSpeech;
import com.ibm.watson.developer_cloud.text_to_speech.v1.model.Voice;

import static java.lang.Math.round;


public class WeatherApp extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private FusedLocationProviderClient mFusedLocationClient;
    protected Location mLastLocation;
    private AddressResultReceiver mResultReceiver = new AddressResultReceiver(new Handler());
    StreamPlayer streamPlayer;
    private TextToSpeech textToSpeech;
    private String TTS_username;
    private String TTS_password;
    private String mAddressOutput;
    private  TextView weather, realF, dow;
    private Button backButton;
    private boolean isPlaying;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);     //  Fixed Portrait orientation

        setContentView(R.layout.activity_weather_app);


        TTS_username = "c0e182f0-b270-4da3-8b29-3688aa598322";
        TTS_password = "sarujZ1gbc40";
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);


        textToSpeech = new TextToSpeech();
        streamPlayer = new StreamPlayer();
        backButton = (Button)findViewById(R.id.backBtn);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // stop talking when the application is closed.
                streamPlayer.interrupt();
                finish();

            }
        });


        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION)) {
            } else {

                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
        }

        getLastKnownLocation();

        //Watson Text-to-Speech Service on Bluemix
        textToSpeech.setUsernameAndPassword(TTS_username, TTS_password);
    }


    public void getLastKnownLocation() {
        Log.i("Button called", "getLastKnownLocation");

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            Log.i("Button called", "mFusedLocationClient");

            return;
        }

        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {

                        mLastLocation = location;

                        // In some rare cases the location returned can be null
                        if (mLastLocation == null) {
                            return;
                        }

                        if (!Geocoder.isPresent()) {
                            Toast.makeText(getApplicationContext(), R.string.no_geocoder_available, Toast.LENGTH_LONG).show();
                            return;
                        }

                        // Start service and update UI to reflect new location
                        startIntentService();
                        updateUI();

                    }
                });
    }


    public void speak(final String outMessage) throws InterruptedException {

        Thread thread = new Thread(new Runnable() {
            public void run() {

                try {
                    streamPlayer.playStream(textToSpeech.synthesize(outMessage, Voice.EN_ALLISON).execute());

                    isPlaying = true;


                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();

        if(!isPlaying){
        }
    }


    private void updateUI(){

        String url = "https://09ddab72-92bc-4c73-8eeb-994f8c7b9e64:LoR8rebnJL@twcservice.eu-gb.mybluemix.net/api/weather/v1/geocode/" + mLastLocation.getLatitude() + "/" + mLastLocation.getLongitude() + "/forecast/hourly/48hour.json";

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {

                try{
                    JSONObject weatherObject = new JSONObject(response);
                    JSONArray metadata = weatherObject.getJSONArray("forecasts");
                    JSONObject fd = metadata.getJSONObject(0);
                    int temp = fd.getInt("temp");

                    Log.d("Forecast", "IS the damn response: " + fd);

                    int thisTime = temp -32;
                    double nearly = Math.round (thisTime / 1.8);

                    int fullMath = fd.getInt("feels_like");
                    int total = fullMath - 32;
                    double nearly2 = Math.round (total / 1.8);

                    String currentWeather = fd.getString("phrase_22char") + ", " + nearly  + "°C";


                    String imageloc= "icon" + fd.getString("icon_code");


                    ImageView imageView = (ImageView) findViewById(R.id.imageView);


                    int id  = getBaseContext().getResources().getIdentifier(imageloc, "drawable", getBaseContext().getPackageName());
                    imageView.setImageResource(id);

                    weather = (TextView) findViewById(R.id.weather);
                    weather.setText("Current temperature: " + currentWeather);

                    realF = (TextView) findViewById(R.id.realTemp);
                    realF.setText("Real Feel: " + nearly2 + "°C");

                    dow = (TextView)findViewById(R.id.dow);
                    dow.setText(fd.getString("dow"));

                    String theSpokenString = "In your area - currently, it's, " + nearly  + "°C " + fd.getString("phrase_22char") +  " Where it's "  +fd.getString("clds") + "% are cloud coverage and " +fd.getString("rh") + "% humidity - it currently feels like "  + nearly2 + "°C";

                    if(fd.getInt("pop") == 0 ){
                        speak(theSpokenString);
                    }

                    if(fd.getInt("pop") > 0 ){
                        speak(theSpokenString + "There is a probability of " + fd.getString("precip_type") + " of " + fd.getInt("pop") + " percent - - Maybe you'd like to grab a coat just in case?");
                    }

                } catch (JSONException e){
                    Log.d("App", e.toString());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("App", "Error: " + error.getMessage());
            }
        })
        {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> params = new HashMap<String, String>();
                params.put("Content-Type", "application/json");
                String creds = String.format("%s:%s","09ddab72-92bc-4c73-8eeb-994f8c7b9e64","LoR8rebnJL");
                String auth = "Basic " + Base64.encodeToString(creds.getBytes(), Base64.DEFAULT);
                params.put("Authorization", auth);
                return params;

            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        requestQueue.add(stringRequest);


    }

    protected void startIntentService() {
        Intent intent = new Intent(this, FetchAddressIntentService.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra(Constants.RECEIVER, mResultReceiver);
        intent.putExtra(Constants.LOCATION_DATA_EXTRA, mLastLocation);
        startService(intent);
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d("App", "onConnectionSuspended");
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d("App", "onConnected");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d("App", "onConnectionFailed");
    }

    class AddressResultReceiver extends ResultReceiver {
        public AddressResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {

            // Display the address string
            // or an error message sent from the intent service.
            mAddressOutput = resultData.getString(Constants.RESULT_DATA_KEY);
            //displayAddressOutput();
            TextView direccion = (TextView) findViewById(R.id.direccion);
            direccion.setText(mAddressOutput);
            Log.d("App", mAddressOutput);

            // Show a toast message if an address was found.
            if (resultCode == Constants.SUCCESS_RESULT) {
                Log.d("App", getString(R.string.address_found)) ;
            }

        }
    }

}
