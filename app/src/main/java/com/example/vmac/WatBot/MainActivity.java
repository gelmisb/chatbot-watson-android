package com.example.vmac.WatBot;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.facebook.login.LoginManager;
import com.facebook.login.widget.LoginButton;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
//import com.ibm.mobilefirstplatform.clientsdk.android.analytics.api.Analytics;
import com.ibm.mobilefirstplatform.clientsdk.android.analytics.api.Analytics;
import com.ibm.mobilefirstplatform.clientsdk.android.core.api.BMSClient;
import com.ibm.mobilefirstplatform.clientsdk.android.core.api.Response;
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

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ChatAdapter mAdapter;
    private ArrayList messageArrayList;
    private EditText inputMessage;
    private ImageButton btnSend;
    private ImageButton btnRecord;
    private Map<String,Object> context = new HashMap<>();
    StreamPlayer streamPlayer;
    private boolean initialRequest;
    private boolean permissionToRecordAccepted = false;
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private static String TAG = "MainActivity";
    private static final int RECORD_REQUEST_CODE = 101;

    private boolean listening = false;
    private SpeechToText speechService;
    private TextToSpeech textToSpeech;
    private MicrophoneInputStream capture;
    private Logger myLogger;
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


    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = getApplicationContext();

        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);     //  Fixed Portrait orientation


        LoginButton loginButton = (LoginButton)findViewById(R.id.login_button);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LoginManager.getInstance().logOut();
                Intent intent = new Intent(getApplicationContext(), GoogleSignInTrial.class);
                startActivity(intent);
            }
        });

        // API code access keys
        conversation_username = "98e3168d-7c24-4958-81f2-aaa303ab9775";
        conversation_password = "A3CCaGkbrBi5";
        workspace_id = "66a84a01-8b2e-4ec3-8b9f-c80ceeb6d707";
        STT_username = "3b09c22d-6681-4a51-b070-1b17a29859d7";
        STT_password = "CFJcDBkcbC4G";
        TTS_username = "c0e182f0-b270-4da3-8b29-3688aa598322";
        TTS_password = "sarujZ1gbc40";
        analytics_APIKEY = mContext.getString(R.string.mobileanalytics_apikey);


        //Bluemix Mobile Analytics
        BMSClient.getInstance().initialize(getApplicationContext(), BMSClient.REGION_US_SOUTH);

        Analytics.init(getApplication(), "Mia-Testing", analytics_APIKEY, false, Analytics.DeviceEvent.ALL);

        myLogger = Logger.getLogger("myLogger");
        // Send recorded usage analytics to the Mobile Analytics Service
        Analytics.send(new ResponseListener() {
            @Override
            public void onSuccess(Response response) {
                // Handle Analytics send success here.
            }

            @Override
            public void onFailure(Response response, Throwable throwable, JSONObject jsonObject) {
                // Handle Analytics send failure here.
            }
        });

        // Send logs to the Mobile Analytics Service
        Logger.send(new ResponseListener() {
            @Override
            public void onSuccess(Response response) {
                // Handle Logger send success here.
            }

            @Override
            public void onFailure(Response response, Throwable throwable, JSONObject jsonObject) {
                // Handle Logger send failure here.
            }
        });

        inputMessage = (EditText) findViewById(R.id.message);
        btnSend = (ImageButton) findViewById(R.id.btn_send);

        btnRecord= (ImageButton) findViewById(R.id.btn_record);

        String customFont = "Montserrat-Regular.ttf";
        Typeface typeface = Typeface.createFromAsset(getAssets(), customFont);
        inputMessage.setTypeface(typeface);
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



        //Watson Text-to-Speech Service on Bluemix
        textToSpeech = new TextToSpeech();
        textToSpeech.setUsernameAndPassword(TTS_username, TTS_password);


        int permission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "Permission to record denied");
            makeRequest();
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

        btnSend.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if(checkInternetConnection()) {
                    sendMessage();
                }
            }
        });

        btnRecord.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                try {
                    recordMessage();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

    };

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

    protected void makeRequest() {
        ActivityCompat.requestPermissions(this,
            new String[]{Manifest.permission.RECORD_AUDIO},
            MicrophoneHelper.REQUEST_PERMISSION);
    }


    // Sending a message to Watson Conversation Service
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
            myLogger.info("Sending a message to Watson Conversation Service");
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
            Toast.makeText(MainActivity.this,"Listening....Click to Stop", Toast.LENGTH_LONG).show();

        } else {
            try {
                microphoneHelper.closeInputStream();
                listening = false;
                Toast.makeText(MainActivity.this,"Stopped Listening....Click to Start", Toast.LENGTH_LONG).show();
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
            recoTokens = new SpeakerLabelsDiarization.RecoTokens();
            if(speechResults.getSpeakerLabels() !=null)
            {
                recoTokens.add(speechResults);
                Log.i("SPEECHRESULTS",speechResults.getSpeakerLabels().get(0).toString());

            }
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
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void enableMicButton() {
        runOnUiThread(new Runnable() {
            @Override public void run() {
                btnRecord.setEnabled(true);
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
                Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
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



