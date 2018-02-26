package com.example.vmac.WatBot;

import android.Manifest;
import android.content.ComponentCallbacks2;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Geocoder;
import android.location.Location;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.ResultReceiver;
import android.os.Vibrator;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.util.Log;
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
import com.facebook.login.LoginManager;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.ibm.mobilefirstplatform.clientsdk.android.analytics.api.Analytics;
import com.ibm.mobilefirstplatform.clientsdk.android.core.api.BMSClient;
import com.ibm.mobilefirstplatform.clientsdk.android.core.api.ResponseListener;
import com.ibm.mobilefirstplatform.clientsdk.android.logger.api.Logger;
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
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class MainScreenTime extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, ComponentCallbacks2 {

    public Button weatherButton, botSpeak, news, semantic;
    private ImageButton recordingButton;

    private FusedLocationProviderClient mFusedLocationClient;
    protected Location mLastLocation;
    private AddressResultReceiver mResultReceiver = new AddressResultReceiver(new Handler());

    private RecyclerView recyclerView;
    private ChatAdapter mAdapter;
    private ArrayList messageArrayList;
    private EditText inputMessage;
    private Map<String,Object> context = new HashMap<>();

    StreamPlayer streamPlayer;
    private MediaPlayer mp;

    private Logger myLogger;

    private boolean initialRequest;
    private boolean permissionToRecordAccepted = false;
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private static String TAG = "MainActivity";
    private static final int RECORD_REQUEST_CODE = 101;

    private boolean listening = false;

    private SpeechToText speechService;
    private TextToSpeech textToSpeech;
    private MicrophoneInputStream capture;
    private Context mContext;

    private String workspace_id;
    private String conversation_username;
    private String conversation_password;
    private String STT_username;
    private String STT_password;
    private String TTS_username;
    private String TTS_password;
    private String analytics_APIKEY;

    private SpeakerLabelsDiarization.RecoTokens recoTokens;
    private MicrophoneHelper microphoneHelper;
    private ComponentName cn;
    private int helloCount = 0;
    private int countColours = 0;
    private Timer timer;

    ConstraintLayout mainLayout;

    private String name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        SharedPreferences sharedPref= getSharedPreferences("mypref", 0);
        name = sharedPref.getString("username", "");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_screen_time);
        mContext = getApplicationContext();


        mainLayout = (ConstraintLayout) findViewById(R.id.mainLayout);


        // A logout button for the user to log out whenever they need to
        // When user logs out it will bring them to the sign in screen to be authenticated again
        LoginButton loginButton = (LoginButton)findViewById(R.id.login_button);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LoginManager.getInstance().logOut();
                Intent intent = new Intent(getApplicationContext(), GoogleSignInTrial.class);
                startActivity(intent);
            }
        });

        // All the username and passwords for IBm Bluemix services

        // IBM Bluemix Watson Conversation
        conversation_username = "98e3168d-7c24-4958-81f2-aaa303ab9775";
        conversation_password = "A3CCaGkbrBi5";

        // IBM Bluemix Watson Speech - To - Text
        STT_username = "3b09c22d-6681-4a51-b070-1b17a29859d7";
        STT_password = "CFJcDBkcbC4G";

        // // IBM Bluemix Workspace for conversation
        workspace_id = "66a84a01-8b2e-4ec3-8b9f-c80ceeb6d707";

        // IBM Bluemix Watson Text - to - Speech service
        TTS_username = "c0e182f0-b270-4da3-8b29-3688aa598322";
        TTS_password = "sarujZ1gbc40";

        // IBM Bluemix Watson Analytics for any request problems
        analytics_APIKEY = mContext.getString(R.string.mobileanalytics_apikey);

        //Bluemix Mobile Analytics
        BMSClient.getInstance().initialize(getApplicationContext(), BMSClient.REGION_US_SOUTH);
        Analytics.init(getApplication(), "Mia-Testing", analytics_APIKEY, false, Analytics.DeviceEvent.ALL);

        // Logger looks through and makes sure this is recorded
        myLogger = Logger.getLogger("myLogger");

        // Send recorded usage analytics to the Mobile Analytics Service
        Analytics.send(new ResponseListener() {
            @Override
            public void onSuccess(com.ibm.mobilefirstplatform.clientsdk.android.core.api.Response response) {
                // Handle Analytics send success here.
                Log.i("Analytics", "Success");
            }

            @Override
            public void onFailure(com.ibm.mobilefirstplatform.clientsdk.android.core.api.Response response, Throwable throwable, JSONObject jsonObject) {
                // Handle Analytics send failure here.
                Log.i("Analytics", "Failure");
            }
        });

        // Send logs to the Mobile Analytics Service
        Logger.send(new ResponseListener() {
            @Override
            public void onSuccess(com.ibm.mobilefirstplatform.clientsdk.android.core.api.Response response) {
                Log.i("Logger", "Success");
                // Handle Logger send success here.
            }

            @Override
            public void onFailure(com.ibm.mobilefirstplatform.clientsdk.android.core.api.Response response, Throwable throwable, JSONObject jsonObject) {
                // Handle Logger send failure here.
                Log.i("Logger", "Failure");
            }
        });

        // User input params initialisation
        inputMessage = (EditText) findViewById(R.id.message);

        // Custom font for the application
        String customFont = "Montserrat-Regular.ttf";
        Typeface typeface = Typeface.createFromAsset(getAssets(), customFont);
        inputMessage.setTypeface(typeface);

        // View for showing everything
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);

        // Arraylist of messages between the AI and the user
        messageArrayList = new ArrayList<>();

        // ArrayList message adapter
        mAdapter = new ChatAdapter(messageArrayList);

        // Microphone helper to activate the recording
        microphoneHelper = new MicrophoneHelper(this);

        // Layout manager configuration with the recycler
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);

        this.inputMessage.setText("");
        this.initialRequest = true;

        // Sending the initial welcoming message
        sendMessage();


        //Watson Text-to-Speech Service on Bluemix
        textToSpeech = new TextToSpeech();
        textToSpeech.setUsernameAndPassword(TTS_username, TTS_password);

        // Checking user permissions that the audio can be recorded and used
        int permission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO);

        // If permissions have been denied by the user
        if (permission != PackageManager.PERMISSION_GRANTED) {
            Log.i("Tagging", "Permission to record denied");
            makeRequest();
        }


        // Touch listener for the recycler view tf the user wants to hear the message
        // That has been sent again wither from the Ai or from the user itself
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

        // UI initialisation for all the components on the screen

        // Weather screen
        weatherButton = (Button) findViewById(R.id.buttonWeather);

        // Watson conversation AI communication and messaging screen
        botSpeak = (Button) findViewById(R.id.talkBtn);

        // Checking the latest news
        news = (Button) findViewById(R.id.newsBtn);

        // Semantic analysis
        semantic = (Button) findViewById(R.id.semanticBtn);

        // Recording button with a touch listener
        recordingButton = (ImageButton) findViewById(R.id.record_button);

        inputMessage = (EditText) findViewById(R.id.message);


        // News button listener action
        news.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mp = MediaPlayer.create(getApplicationContext(), R.raw.whoop);
                mp.start();
                Intent intent = new Intent(getApplicationContext(), News.class);
                startActivity(intent);
            }
        });

        // Watson conversation view button listener action
        botSpeak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mp = MediaPlayer.create(getApplicationContext(), R.raw.whoop);
                mp.start();
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
            }
        });

        // Watson conversation view button listener action
        semantic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mp = MediaPlayer.create(getApplicationContext(), R.raw.whoop);
                mp.start();
                speak("Let me analyse that for you");
                Intent intent = new Intent(getApplicationContext(), NaturalLanguageProcessing.class);
                startActivity(intent);
            }
        });

        // Weather button listener action
        weatherButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mp = MediaPlayer.create(getApplicationContext(), R.raw.whoop);
                mp.start();
                Intent intent = new Intent(getApplicationContext(), WeatherApp.class);
                startActivity(intent);
            }
        });

        // getting the last location on the application
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Last known location called to update the UI when the screen is launched
        getLastKnownLocation();


        // Here, thisActivity is the current activity
        // Checking if all the permissions have been granted
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


        recordingButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {

                if(motionEvent.getAction() == MotionEvent.ACTION_DOWN){

                    recordingButton.setImageResource(R.drawable.microphone);
                    mp = MediaPlayer.create(getApplicationContext(), R.raw.whoop);
                    mp.start();

                    try {
                        Vibrator vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                        recordingButton.setImageResource(R.drawable.microphone1);

                        if (vibe != null) {
                            vibe.vibrate(100);
                        }

                        recordMessage();

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    Log.i("Key", "Key event down " + motionEvent);
                }

                if(motionEvent.getAction() == MotionEvent.ACTION_UP){
                    recordingButton.setImageResource(R.drawable.microphone);


                    try {
                        recordMessage();
                        sendMessage();

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }



                    Log.i("Key", "Key event up " + motionEvent);
                    Log.i("Message", " " + inputMessage.getText());

                }

                return false;
            }
        });

        onTrimMemory(ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN);


    }

    /**
     * For the AI - to - user communication
     * using the streamPlayer for the audio
     * where the action is threaded to reserve memory resources
     * @param outMessage
     */
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

    /**
     * Making Audio requests
     */
    protected void makeRequest() {
        ActivityCompat.requestPermissions(this,
                new String[]{android.Manifest.permission.RECORD_AUDIO},
                MicrophoneHelper.REQUEST_PERMISSION);
    }


    /**
     * Sending a message to Watson Conversation Service
     */
    public void sendMessage()  {

        final String inputmessage = this.inputMessage.getText().toString().trim();




        // Done
        if(inputMessage.getText().toString().contains("calendar")){
            Intent intent = new Intent();

            //Froyo or greater (mind you I just tested this on CM7 and the less than froyo one worked so it depends on the phone...)
            cn = new ComponentName("com.google.android.calendar", "com.android.calendar.LaunchActivity");

            intent.setComponent(cn);
            startActivity(intent);
        }

        if(inputMessage.getText().toString().contains("celebration")){
            timer=new Timer();
            MyTimerTask myTimerTask =new MyTimerTask();
            //schedule to change background color every second
            mp = MediaPlayer.create(getApplicationContext(), R.raw.celeb);
            mp.start();
            timer.schedule(myTimerTask,500,500);
        }

        // Done
        if(inputMessage.getText().toString().contains("call")){
            Intent intent = new Intent(Intent.ACTION_VIEW, ContactsContract.Contacts.CONTENT_URI);
            startActivity(intent);
        }

        // Done
        if(inputMessage.getText().toString().contains("weather")){
            Intent intent = new Intent(this, WeatherApp.class);
            startActivity(intent);
        }

        // Done
        if(inputMessage.getText().toString().contains("music")){
            Intent intent = Intent.makeMainSelectorActivity(Intent.ACTION_MAIN, Intent.CATEGORY_APP_MUSIC);
            startActivity(intent);
        }

        if(inputMessage.getText().toString().contains("news")){
            Intent intent = new Intent(this, News.class);
            startActivity(intent);
        }


        if(!this.initialRequest) {
            Message inputMessage = new Message();
            inputMessage.setMessage(inputmessage);
            inputMessage.setId("1");
            messageArrayList.add(inputMessage);
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

                    //Passing Context of last conversation
                    if(response.getContext() !=null) {
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
                                    if(helloCount == 0 ){
                                        outMessage.setMessage("Hello " + name + ", how can I help?");
                                    } else {
                                        outMessage.setMessage("Welcome back " + name);
                                    }
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


        if(!listening) {
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

            listening = true;
            Toast.makeText(this,"Listening", Toast.LENGTH_SHORT).show();

        } else {
            try {
                microphoneHelper.closeInputStream();
                listening = false;
                Toast.makeText(this,"Stopped Listening", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Check Internet Connection
     * @return this
     */
    private boolean checkInternetConnection() {

        // get Connectivity Manager object to check connection
        ConnectivityManager cm =
                (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);

        assert cm != null;
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();

        // Check for network connections
        if (isConnected){
            return true;
        }
        else {
            Toast.makeText(this, " No Internet Connection available ", Toast.LENGTH_LONG).show();
            return false;
        }
    }


    /**
     * THIS IS AN EASTER EGG, DONE ONLY FOR CRINGY PURPOSES
     */
    public class MyTimerTask extends TimerTask {


        @Override
        public void run() {
            //Since we want to change something which is on hte UI
            //so we have to run on UI thread..
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    countColours++;
                    Log.i("Entered", "Celebration mode " +countColours);

                    Vibrator vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

                    if (vibe != null) {
                        vibe.vibrate(150);
                    }

                    Random random = new Random();//this is random generator
                    mainLayout.setBackgroundColor(Color.rgb(random.nextInt(255), random.nextInt(255), random.nextInt(255)));

                    if(countColours == 30){
                        timer.cancel();
                        mainLayout.setBackgroundResource(R.drawable.blurryimage);
                        mp.stop();
                        countColours = 0;
                    }
                }
            });
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
        runOnUiThread(new Runnable() {
            @Override public void run() {
                inputMessage.setText(text);
            }
        });
    }


    private void enableMicButton() {
        runOnUiThread(new Runnable() {
            @Override public void run() {
                recordingButton.setEnabled(true);
            }
        });
    }

    private void showError(final Exception e) {
        runOnUiThread(new Runnable() {
            @Override public void run() {
                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        });
    }




    /**
     * Getting the last known location on the device
     * that has been registered
     */
    public void getLastKnownLocation() {

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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

    /**
     * This method is for updating the UI with the latest weather news
     * This accesses the API call and inputs all the information giving
     * the user a live feed of the weather forecast
     */
    private void updateUI(){

        String url = "https://09ddab72-92bc-4c73-8eeb-994f8c7b9e64:LoR8rebnJL@twcservice.eu-gb.mybluemix.net/api/weather/v1/geocode/" + mLastLocation.getLatitude() + "/" + mLastLocation.getLongitude() + "/forecast/hourly/48hour.json";

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {

                // Extracting weather information from the JSON file
                try{

                    JSONObject weatherObject = new JSONObject(response);

                    JSONArray metadata = weatherObject.getJSONArray("forecasts");

                    JSONObject fd = metadata.getJSONObject(0);


                    // Converting the temperature to Celsius
                    int temp = fd.getInt("temp");

                    int thisTime = temp -32;

                    double nearly = Math.round (thisTime / 1.8);

                    // Getting the current weather phrases
                    String currentWeather =   nearly  + "°C";

                    String imageloc= "icon" + fd.getString("icon_code");

                    // Getting the right image according to the weather
                    ImageView imageView = (ImageView) findViewById(R.id.imageView);

                    int id  = getBaseContext().getResources().getIdentifier(imageloc, "drawable", getBaseContext().getPackageName());

                    imageView.setImageResource(id);


                    // Setting the current weather phrase
                    TextView weatherPhrase = (TextView) findViewById(R.id.weather);
                    weatherPhrase.setText(currentWeather);

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

        // Adding the request to the request queue for it to be extracted and returned
        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        requestQueue.add(stringRequest);
    }

    // Fetching the current address using the sensitive intent service
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


    /**
     * Inner class for the current address recovery
     */
    class AddressResultReceiver extends ResultReceiver {
        public AddressResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {

            // Show a toast message if an address was found.
            if (resultCode == Constants.SUCCESS_RESULT) {
                Log.d("App", getString(R.string.address_found)) ;
            }
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
