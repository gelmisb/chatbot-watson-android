package com.example.vmac.WatBot;

import android.*;
import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Geocoder;
import android.location.Location;
import android.os.Handler;
import android.os.ResultReceiver;
import android.os.Vibrator;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
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
import com.ibm.mobilefirstplatform.clientsdk.android.analytics.api.Analytics;
import com.ibm.mobilefirstplatform.clientsdk.android.core.api.BMSClient;
import com.ibm.watson.developer_cloud.android.library.audio.MicrophoneHelper;
import com.ibm.watson.developer_cloud.android.library.audio.MicrophoneInputStream;
import com.ibm.watson.developer_cloud.android.library.audio.StreamPlayer;
import com.ibm.watson.developer_cloud.android.library.audio.utils.ContentType;
import com.ibm.watson.developer_cloud.conversation.v1.ConversationService;
import com.ibm.watson.developer_cloud.conversation.v1.model.MessageRequest;
import com.ibm.watson.developer_cloud.conversation.v1.model.MessageResponse;
import com.ibm.watson.developer_cloud.speech_to_text.v1.SpeechToText;
import com.ibm.watson.developer_cloud.speech_to_text.v1.model.RecognizeOptions;
import com.ibm.watson.developer_cloud.speech_to_text.v1.model.SpeechResults;
import com.ibm.watson.developer_cloud.speech_to_text.v1.websocket.RecognizeCallback;
import com.ibm.watson.developer_cloud.text_to_speech.v1.TextToSpeech;
import com.ibm.watson.developer_cloud.text_to_speech.v1.model.Voice;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainScreenTime extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    public TextView welcomeUser;
    public Button weatherButton, botSpeak, news, media;
    private ImageButton recordingButton;

    private RecyclerView recyclerView;
    private ChatAdapter mAdapter;

    private SpeechToText speechService;
    private MicrophoneHelper microphoneHelper;

    private boolean listening = false;
    private boolean initialRequest;
    private boolean permissionToRecordAccepted = false;
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private static String TAG = "MainActivity";
    private static final int RECORD_REQUEST_CODE = 101;

    StreamPlayer streamPlayer;

    private ImageButton btnSend;
    private ImageButton btnRecord;

    private MicrophoneInputStream capture;
    private EditText inputMessage;
    private Context mContext;
    private ComponentName cn;

    private String conversation_username;
    private String conversation_password;
    private String STT_username;
    private String STT_password;
    private String workspace_id;
    private String TTS_username;
    private String TTS_password;
    private String analytics_APIKEY;


    private TextToSpeech textToSpeech;


    private Map<String,Object> context = new HashMap<>();

    private FusedLocationProviderClient mFusedLocationClient;
    protected Location mLastLocation;
    private AddressResultReceiver mResultReceiver = new AddressResultReceiver(new Handler());
    private String mAddressOutput;
    private GoogleSignInTrial googleSignInTrial;
    private ArrayList messageArrayList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_screen_time);
        mContext = getApplicationContext();

        conversation_username = "98e3168d-7c24-4958-81f2-aaa303ab9775";
        conversation_password = "A3CCaGkbrBi5";
        STT_username = "3b09c22d-6681-4a51-b070-1b17a29859d7";
        STT_password = "CFJcDBkcbC4G";
        workspace_id = "66a84a01-8b2e-4ec3-8b9f-c80ceeb6d707";
        TTS_username = "c0e182f0-b270-4da3-8b29-3688aa598322";
        TTS_password = "sarujZ1gbc40";

        analytics_APIKEY = mContext.getString(R.string.mobileanalytics_apikey);


        //Bluemix Mobile Analytics
        BMSClient.getInstance().initialize(getApplicationContext(), BMSClient.REGION_US_SOUTH);

        Analytics.init(getApplication(), "Mia-Testing", analytics_APIKEY, false, Analytics.DeviceEvent.ALL);


        inputMessage = (EditText) findViewById(R.id.message);
        btnSend = (ImageButton) findViewById(R.id.btn_send);
        btnRecord= (ImageButton) findViewById(R.id.btn_record);
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);


        messageArrayList = new ArrayList<>();
        mAdapter = new ChatAdapter(messageArrayList);
        microphoneHelper = new MicrophoneHelper(this);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);
        this.inputMessage.setText("");
        this.initialRequest = true;

        sendMessage();

        int permission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            Log.i("Tagging", "Permission to record denied");
            makeRequest();
        }


        //Watson Text-to-Speech Service on Bluemix
        textToSpeech = new TextToSpeech();
        textToSpeech.setUsernameAndPassword(TTS_username, TTS_password);


        weatherButton = (Button) findViewById(R.id.buttonWeather);
        botSpeak = (Button) findViewById(R.id.talkBtn);
        news = (Button) findViewById(R.id.newsBtn);
        recordingButton = (ImageButton) findViewById(R.id.record_button);

        news.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Vibrator vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

                if (vibe != null) {
                    vibe.vibrate(150);
                }
                Intent intent = new Intent(getApplicationContext(), News.class);
                startActivity(intent);
            }
        });

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
                    vibe.vibrate(150);
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

            } else {

                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
        }

        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(getApplicationContext(), recyclerView, new ClickListener() {
            @Override
            public void onClick(View view, final int position) {
                Thread thread = new Thread(new Runnable() {
                    public void run() {
                        Message audioMessage;
                        try {
                            audioMessage =(Message) messageArrayList.get(position);
                            streamPlayer = new StreamPlayer();
                            if(audioMessage != null && !audioMessage.getMessage().isEmpty())
                                //Change the Voice format and choose from the available choices
                                streamPlayer.playStream(textToSpeech.synthesize(audioMessage.getMessage(), Voice.EN_LISA).execute());
                            else
                                streamPlayer.playStream(textToSpeech.synthesize("No Text Specified", Voice.EN_LISA).execute());

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
                thread.start();
            }

            @Override
            public void onLongClick(View view, int position) {

            }

        }));


        recordingButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {

                if(motionEvent.getAction() == MotionEvent.ACTION_DOWN){
                    try {
                        listening = true;
                        recordMessage();

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    Log.i("Key", "Key event down " + motionEvent);
                }

                if(motionEvent.getAction() == MotionEvent.ACTION_UP){
//                    listening = false;
                    try {
                        microphoneHelper.closeInputStream();
                        Toast.makeText(getApplicationContext(),"Stopped Listening....Click to Start", Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }


                    sendMessage();
                    Log.i("Key", "Key event up " + motionEvent);
                    Log.i("Message", "The " + inputMessage.getText());

                }

                return false;
            }
        });

    }



    // Speech-to-Text Record Audio permission
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case REQUEST_RECORD_AUDIO_PERMISSION:
                permissionToRecordAccepted  = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                break;
            case RECORD_REQUEST_CODE: {

                if (grantResults.length == 0
                        || grantResults[0] !=
                        PackageManager.PERMISSION_GRANTED) {

                    Log.i(TAG, "Permission has been denied by user");
                } else {
                    Log.i(TAG, "Permission has been granted by user");
                }
                return;
            }

            case MicrophoneHelper.REQUEST_PERMISSION: {
                if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Permission to record audio denied", Toast.LENGTH_SHORT).show();
                }
            }
        }
        if (!permissionToRecordAccepted ) finish();

    }

    protected void makeRequest() {
        ActivityCompat.requestPermissions(this,
                new String[]{android.Manifest.permission.RECORD_AUDIO},
                MicrophoneHelper.REQUEST_PERMISSION);
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

    public void updateLocation (View view){
        Vibrator vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (vibe != null) {
            vibe.vibrate(500);
        }
        getLastKnownLocation();
    }


    private void updateUI(){

        String url = "https://09ddab72-92bc-4c73-8eeb-994f8c7b9e64:LoR8rebnJL@twcservice.eu-gb.mybluemix.net/api/weather/v1/geocode/" + mLastLocation.getLatitude() + "/" + mLastLocation.getLongitude() + "/forecast/hourly/48hour.json";

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d("App", "Response: " + response);


                try{

                    JSONObject weatherObject = new JSONObject(response);

                    JSONArray metadata = weatherObject.getJSONArray("forecasts");

                    JSONObject fd = metadata.getJSONObject(0);



                    int temp = fd.getInt("temp");

                    int thisTime = temp -32;

                    double nearly = Math.round (thisTime / 1.8);



                    String currentWeather =   nearly  + "°C";

                    String weatherPhreasetext =fd.getString("phrase_22char");

                    String imageloc= "icon" + fd.getString("icon_code");



                    ImageView imageView = (ImageView) findViewById(R.id.imageView);

                    int id  = getBaseContext().getResources().getIdentifier(imageloc, "drawable", getBaseContext().getPackageName());

                    imageView.setImageResource(id);



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

    public void speak(final String outMessage){

        Thread thread = new Thread(new Runnable() {
            public void run() {

                try {
                    streamPlayer = new StreamPlayer();
                    streamPlayer.playStream(textToSpeech.synthesize(outMessage, Voice.EN_ALLISON).execute());
                    Thread.sleep(100);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }


    public void sendMessage()  {

        final String inputmessage = this.inputMessage.getText().toString().trim();

        if(inputMessage.getText().toString().equals("calendar") || inputMessage.getText().toString().equals("Calendar")){
            Intent intent = new Intent();

            //Froyo or greater (mind you I just tested this on CM7 and the less than froyo one worked so it depends on the phone...)
            cn = new ComponentName("com.google.android.calendar", "com.android.calendar.LaunchActivity");

            intent.setComponent(cn);
            startActivity(intent);
        }

        if(inputMessage.getText().toString().equals("call") || inputMessage.getText().toString().equals("Call") || inputMessage.getText().toString().equals("call ") || inputMessage.getText().toString().equals("Call ")){
            Intent intent = new Intent(Intent.ACTION_VIEW, ContactsContract.Contacts.CONTENT_URI);
            startActivity(intent);
        }

        if(inputMessage.getText().toString().equals("music") || inputMessage.getText().toString().equals("Music") || inputMessage.getText().toString().equals("music ") || inputMessage.getText().toString().equals("Music ")){
            Intent intent = Intent.makeMainSelectorActivity(Intent.ACTION_MAIN, Intent.CATEGORY_APP_MUSIC);
            startActivity(intent);
        }

        if(!this.initialRequest) {
            Message inputMessage = new Message();
            inputMessage.setMessage(inputmessage);
            inputMessage.setId("1");
            messageArrayList.add(inputMessage);
            Log.i("Sending message", "Sending a message to Watson Conversation Service");
        }

        else {
            Message inputMessage = new Message();
            inputMessage.setMessage(inputmessage);
            inputMessage.setId("100");
            this.initialRequest = false;
        }

        this.inputMessage.setText("");
        mAdapter.notifyDataSetChanged();


        final Thread thread = new Thread(new Runnable(){
            public void run() {
                try {

                    ConversationService service = new ConversationService(ConversationService.VERSION_DATE_2017_02_03);
                    service.setUsernameAndPassword(conversation_username, conversation_password);

                    MessageRequest newMessage = new MessageRequest.Builder().inputText(inputmessage).context(context).build();
                    MessageResponse response = service.message(workspace_id, newMessage).execute();

                    String name = getIntent().getStringExtra("name");

                    //Passing Context of last conversation
                    if(response.getContext() !=null)
                    {
                        context.clear();
                        context = response.getContext();
                        Log.i("context", "  "  + context + " ");
                    }

                    Message outMessage = new Message();

                    if(response!=null)
                    {

                        if(response.getOutput()!=null && response.getOutput().containsKey("text"))
                        {

                            ArrayList responseList = (ArrayList) response.getOutput().get("text");

                            if(null !=responseList && responseList.size()>0)
                            {

                                outMessage.setMessage((String)responseList.get(0));

                                outMessage.setId("2");

                                if(outMessage.getMessage().equals("Hello . How can I help you?"))
                                {
                                    outMessage.setMessage("Hello " + UserInfo.getUsername() + ", how could I assist you?");
                                }

                                Log.i(String.valueOf(outMessage), String.valueOf(outMessage.getMessage()));

                                speak(outMessage.getMessage());
                            }

                            messageArrayList.add(outMessage);
                        }

                        runOnUiThread(new Runnable() {
                            public void run() {
                                mAdapter.notifyDataSetChanged();
                                if (mAdapter.getItemCount() > 1) {
                                    recyclerView.getLayoutManager().smoothScrollToPosition(recyclerView, null, mAdapter.getItemCount()-1);

                                }
                            }
                        });

                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        thread.start();
    }





    //Record a message via Watson Speech to Text
    private void recordMessage() throws InterruptedException {
        speechService = new SpeechToText();
        speechService.setUsernameAndPassword(STT_username, STT_password);

        microphoneHelper = new MicrophoneHelper(this);


        if(listening) {
            capture = microphoneHelper.getInputStream(true);
            new Thread(new Runnable() {
                @Override public void run() {
                    try {
                        speechService.recognizeUsingWebSocket(capture, getRecognizeOptions(), new MicrophoneRecognizeDelegate());
                    } catch (Exception e) {
                        showError(e);
                    }
                }
            }).start();

            Toast.makeText(getApplicationContext(),"Listening....Click to Stop", Toast.LENGTH_SHORT).show();

        } else {
            try {
                microphoneHelper.closeInputStream();
                Toast.makeText(getApplicationContext(),"Stopped Listening....Click to Start", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    //Private Methods - Speech to Text
    private RecognizeOptions getRecognizeOptions() {
        return new RecognizeOptions.Builder()
                .continuous(true)
                .contentType(ContentType.OPUS.toString())
                //.model("en-UK_NarrowbandModel")
                .interimResults(true)
                .inactivityTimeout(2000)
                //TODO: Uncomment this to enable Speaker Diarization
                //.speakerLabels(true)
                .build();
    }

    //Watson Speech to Text Methods.
    private class MicrophoneRecognizeDelegate implements RecognizeCallback {
        @Override
        public void onTranscription(SpeechResults speechResults) {
            //TODO: Uncomment this to enable Speaker Diarization
            /*recoTokens = new SpeakerLabelsDiarization.RecoTokens();
            if(speechResults.getSpeakerLabels() !=null)
            {
                recoTokens.add(speechResults);
                Log.i("SPEECHRESULTS",speechResults.getSpeakerLabels().get(0).toString());

            }*/
            if(speechResults.getResults() != null && !speechResults.getResults().isEmpty()) {
                String text = speechResults.getResults().get(0).getAlternatives().get(0).getTranscript();
                showMicText(text);
            }
        }

        @Override public void onConnected() {

        }

        @Override public void onError(Exception e) {
            showError(e);
            enableMicButton();
        }

        @Override public void onDisconnected() {
            enableMicButton();
        }

        @Override
        public void onInactivityTimeout(RuntimeException runtimeException) {

        }

        @Override
        public void onListening() {

        }

        @Override
        public void onTranscriptionComplete() {

        }
    }

    private void showMicText(final String text) {
//        runOnUiThread(new Runnable() {
//            @Override public void run() {
//                //inputMessage.setText(text);
//                try {
//                    Thread.sleep(100);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            }
//        });
    }

    private void enableMicButton() {
        runOnUiThread(new Runnable() {
            @Override public void run() {
                recordingButton.setEnabled(true);
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void showError(final Exception e) {
        runOnUiThread(new Runnable() {
            @Override public void run() {
                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                e.printStackTrace();
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
            }
        });
    }



}
