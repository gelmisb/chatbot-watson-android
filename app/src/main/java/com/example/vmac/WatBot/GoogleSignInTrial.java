package com.example.vmac.WatBot;


import android.content.ComponentCallbacks2;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

public class GoogleSignInTrial extends AppCompatActivity implements ComponentCallbacks2 {

    GoogleSignInClient mGoogleSignInClient;
    LoginButton loginButton;
    CallbackManager callbackManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_google_sign_in_trial);

        if(isLoggedIn()){
            Intent intent = new Intent(getApplicationContext(), MainScreenTime.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        }


        // Facebook sign in options
        callbackManager = CallbackManager.Factory.create();

        loginButton = (LoginButton) findViewById(R.id.login_button);
        loginButton.setReadPermissions(Arrays.asList("user_status, user_posts"));

        // Callback registration
        loginButton.registerCallback(callbackManager,
                new FacebookCallback<LoginResult>() {

                    @Override
                    public void onSuccess(LoginResult loginResult) {

//                        Log.i("Success!!", loginResult.getAccessToken().getApplicationId());
//                        Log.i("Logged in", "" + isLoggedIn());
//                        Log.i("Success!!3", loginResult.getAccessToken().getExpires().toString());
//


                        GraphRequest request = GraphRequest.newMeRequest(
                                loginResult.getAccessToken(),


                                new GraphRequest.GraphJSONObjectCallback() {

                                    @Override
                                    public void onCompleted(
                                            JSONObject object,
                                            GraphResponse response) {


                                        Intent intent = new Intent(getApplicationContext(), MainScreenTime.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

                                        try {
//                                            UserInfo userInfo = new UserInfo();
                                            UserInfo.setUsername(response.getJSONObject().get("name").toString());

//                                            intent.putExtra("name", response.getJSONObject().get("name").toString());
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }

                                        startActivity(intent);
                                    }
                                });

                        Bundle parameters = new Bundle();
                        parameters.putString("fields", "id,name,link");
                        request.setParameters(parameters);
                        request.executeAsync();

                        new GraphRequest(
                                AccessToken.getCurrentAccessToken(),
                                "/me/feed",
                                parameters,
                                HttpMethod.GET,
                                new GraphRequest.Callback() {
                                    public void onCompleted(GraphResponse response) {
                                        JSONObject fd = response.getJSONObject();
                                        try {
                                            Object info = fd.get("data");
//                                            Log.i("Facebook data", fd.get("data").);

                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }

//                                            Log.i("Facebook Feed", response.getJSONObject().get("name").toString());
                                        //                                        Log.i("Facebook Feed", response.getJSONObject().get("").toString());
                                    }
                                }
                        ).executeAsync();

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
                                /* make the API call */

    }

    public boolean isLoggedIn() {
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        return accessToken != null;
    }


    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, 1);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);

    }



    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);

            // Signed in successfully, show authenticated UI.
            Intent intent = new Intent(this, MainScreenTime.class);
            intent.putExtra("name", account.getDisplayName());
            startActivity(intent);

        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w("Tag", "signInResult:failed code=" + e.getStatusCode());
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