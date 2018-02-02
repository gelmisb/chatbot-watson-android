package com.example.vmac.WatBot;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.VideoView;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;

public class GoogleSignInTrial extends AppCompatActivity {

    GoogleSignInClient mGoogleSignInClient;
    MainActivity mainActivity;
    LoginButton loginButton;
    CallbackManager callbackManager;
    String username;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_google_sign_in_trial);

        // Google sign in options
        SignInButton signInButton = findViewById(R.id.sign_in_button);
        signInButton.setSize(SignInButton.SIZE_STANDARD);
        findViewById(R.id.sign_in_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (view.getId()) {
                    case R.id.sign_in_button:
                        signIn();
                        break;
                }
            }
        });

        ActivityManager am = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
        assert am != null;
        List<ActivityManager.RunningAppProcessInfo> list2= am.getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo ti : list2) {

            Log.i("IMPORTANCE CODE",String.valueOf(ti.importance));

            if(ti.importance == 400){
                LoginManager.getInstance().logOut();
            }
        }


        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        final VideoView videoView =
                (VideoView) findViewById(R.id.videoView1);

        videoView.setVideoPath(
                "https://www.ibm.com/watson/assets/duo/video/Watson_Avatar_Ambient-square-071817.mp4");

        videoView.setOnPreparedListener (new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.setLooping(true);
            }
        });

        videoView.start();

        // Facebook sign in options
        callbackManager = CallbackManager.Factory.create();

        loginButton = (LoginButton) findViewById(R.id.login_button);
        loginButton.setReadPermissions(Arrays.asList("user_status, user_posts"));

        // Callback registration
        loginButton.registerCallback(callbackManager,
                new FacebookCallback<LoginResult>() {

                    @Override
                    public void onSuccess(LoginResult loginResult) {

                        Log.i("Success!!", loginResult.toString());



                        GraphRequest request = GraphRequest.newMeRequest(
                                loginResult.getAccessToken(),


                                new GraphRequest.GraphJSONObjectCallback() {

                                    @Override
                                    public void onCompleted(
                                            JSONObject object,
                                            GraphResponse response) {


                                        Intent intent = new Intent(getApplicationContext(), MainScreenTime.class);

                                        try {
//                                            UserInfo userInfo = new UserInfo();
                                            UserInfo.setUsername(response.getJSONObject().get("name").toString());

                                            intent.putExtra("name", response.getJSONObject().get("name").toString());
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
                                        Log.i("Facebook Feed", response.toString());
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


    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, 1);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Facebook login
        if(loginButton.isEnabled()){
            callbackManager.onActivityResult(requestCode, resultCode, data);
        }


        if (requestCode == 1) {
            // The Task returned from this call is always completed, no need to attach a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
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

//
//    public String getUsername()  {
//        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(this);
//        if (acct != null) {
//            String personName = acct.getDisplayName();
//            String personGivenName = acct.getGivenName();
//            String personFamilyName = acct.getFamilyName();
//            String personEmail = acct.getEmail();
//            String personId = acct.getId();
//            return personName;
//        }
//        return null;
//    }

}