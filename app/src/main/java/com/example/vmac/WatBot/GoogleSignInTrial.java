package com.example.vmac.WatBot;


import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.ComponentCallbacks2;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Handler;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.Profile;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GoogleSignInTrial extends AppCompatActivity implements ComponentCallbacks2 {


    String c, userprefs;
    LoginButton loginButton;
    CallbackManager callbackManager;
    UserModel userModel;
    private final static int INTERVAL = 1000 * 60 ; //2 minutes
    Handler mHandler = new Handler();
    SharedPreferences sharedPref;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);


        setContentView(R.layout.activity_google_sign_in_trial);
        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);     //  Fixed Portrait orientation



        if(isLoggedIn()){
            Intent intent = new Intent(getApplicationContext(), MainScreenTime.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        }

        sharedPref= getSharedPreferences("mypref", 0);

        userModel = new UserModel();

        // Facebook sign in options
        callbackManager = CallbackManager.Factory.create();

        loginButton = (LoginButton) findViewById(R.id.login_button);
        loginButton.setReadPermissions(Arrays.asList("user_status, user_posts, user_likes"));

        // Callback registration
        loginButton.registerCallback(callbackManager,
                new FacebookCallback<LoginResult>() {


                    /**
                     * When user logs in successfully this method is called
                       @param loginResult
                     */
                    @Override
                    public void onSuccess(final LoginResult loginResult) {

                        // Retrieving access token for login result
                        final GraphRequest request = GraphRequest.newMeRequest(
                                loginResult.getAccessToken(),


                                new GraphRequest.GraphJSONObjectCallback() {

                                    // When the GraphRequest is successfully retrieved
                                    // Set users name and extract Facebook feed
                                    @Override
                                    public void onCompleted(
                                            JSONObject object,
                                            GraphResponse response) {

                                        // Set the username
                                        try {
                                            JSONObject getPosts = response.getJSONObject();
                                            JSONObject theReal = getPosts.getJSONObject("posts");
                                            JSONArray theReal2 = theReal.getJSONArray("data");

                                            int count = theReal2.length();

                                            for(int i = 0; i < count; i++){
                                                JSONObject posts = theReal2.getJSONObject(i);
                                                if(posts.has("message")){
                                                    userModel.addMessages(new UserMessages((String)posts.get("message")));
                                                }
                                            }

                                            c = userModel.getMessageList().toString();
                                            MediaPlayer mp = MediaPlayer.create(getApplicationContext(), R.raw.whoop);
                                            mp.start();
                                            Pattern pt = Pattern.compile("[^a-zA-Z0-9 .,!]");
                                            Matcher match= pt.matcher(c);
                                            while(match.find())
                                            {
                                                c = c.replace(match.group(), "");
                                            }

                                            userModel.getMessageList().clear();

//                                            UserInfo.setUsername(response.getJSONObject().get("name").toString());
                                            // Create object of SharedPreferences.
                                            SharedPreferences.Editor editor= sharedPref.edit();

                                            //put your value
                                            editor.putString("username", Profile.getCurrentProfile().getFirstName());

                                            editor.putString("userPrefs", c);
                                            //commits your edits
                                            editor.apply();

                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }


                                        // When all the actions are completed transfer the user to the Main Menu
                                        Intent intent = new Intent(getApplicationContext(), MainScreenTime.class);
                                        startActivity(intent);

                                    }


                                });

                        // This is for getting user's feed
                        // When user logs in this is retrieved and analysed
                        // Stored in a flash memory
                        Bundle parameters = new Bundle();
                        parameters.putString("fields", "id,name,link,posts");
                        request.setParameters(parameters);
                        request.executeAsync();
                    }

                    @Override
                    public void onCancel() {
                        Log.i("Cancelled!!", "Cancelled");
                    }

                    @Override
                    public void onError(FacebookException exception) {
                        Log.i("Error!!", exception.toString());
                    }
                });
        onTrimMemory(ComponentCallbacks2.TRIM_MEMORY_MODERATE);

    }




    // Method for checking if the user has logged in
    public boolean isLoggedIn() {
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        return accessToken != null;
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
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