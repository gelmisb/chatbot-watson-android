package com.example.vmac.WatBot;

import android.content.Context;
import android.content.Intent;
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
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class MainScreenTime extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    public TextView welcomeUser;
    public Button weatherButton, botSpeak, news, media;

    private FusedLocationProviderClient mFusedLocationClient;
    protected Location mLastLocation;
    private AddressResultReceiver mResultReceiver = new AddressResultReceiver(new Handler());
    private String mAddressOutput;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_screen_time);


        weatherButton = (Button) findViewById(R.id.buttonWeather);
        botSpeak = (Button) findViewById(R.id.talkBtn);

        botSpeak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
            }
        });

        weatherButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Vibrator vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

                if (vibe != null) {
                    vibe.vibrate(250);
                }
                Intent intent = new Intent(getApplicationContext(), WeatherApp.class);
                startActivity(intent);
            }
        });


        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        getLastKnownLocation();

        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }

    }

    public void getLastKnownLocation() {
        Log.i("Button called", "getLastKnownLocation");

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
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

    public void updateLocation (View view){
        Vibrator vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (vibe != null) {
            vibe.vibrate(500);
        }
        getLastKnownLocation();
    }


    private void updateUI(){

        String url = "https://09ddab72-92bc-4c73-8eeb-994f8c7b9e64:LoR8rebnJL@twcservice.eu-gb.mybluemix.net/api/weather/v1/geocode/" + mLastLocation.getLatitude() + "/" + mLastLocation.getLongitude() + "/observations.json";
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d("App", "Response: " + response);


                try{
                    JSONObject weatherObject = new JSONObject(response);
                    JSONObject observation = weatherObject.getJSONObject("observation");

                    int tempo = Integer.parseInt(observation.getString("temp"));
                    int thisTime = tempo -32;
                    double nearly = Math.round (thisTime / 1.8);

                    String currentWeather = nearly  + "°C";
                    String weatherPhreasetext = observation.getString("wx_phrase");

                    String imageloc= "icon" + observation.getString("wx_icon");



                    ImageView imageView = (ImageView) findViewById(R.id.imageView);


                    int id  = getBaseContext().getResources().getIdentifier(imageloc, "drawable", getBaseContext().getPackageName());
                    imageView.setImageResource(id);

//                    imageView.setImageDrawable(Drawable.createFromPath(observation.getString("wx_icon")));
                    TextView weatherPhrase = (TextView) findViewById(R.id.weather);
                    weatherPhrase.setText(currentWeather);

                    TextView temperatureText = (TextView) findViewById(R.id.tempText);
                    temperatureText.setText(weatherPhreasetext);

                } catch (JSONException e){
                    Log.d("App", e.toString());
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
