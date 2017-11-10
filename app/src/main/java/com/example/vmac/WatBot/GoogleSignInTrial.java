//package com.example.vmac.WatBot;
//
//import android.content.Intent;
//import android.support.annotation.NonNull;
//import android.support.v7.app.AppCompatActivity;
//import android.os.Bundle;
//import android.view.View;
//
//import com.google.android.gms.auth.api.signin.GoogleSignIn;
//import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
//import com.google.android.gms.auth.api.signin.GoogleSignInClient;
//import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
//import com.google.android.gms.common.SignInButton;
//import com.google.android.gms.tasks.OnCompleteListener;
//import com.google.android.gms.tasks.Task;
//
//public class GoogleSignInTrial extends AppCompatActivity {
//
//    GoogleSignInClient mGoogleSignInClient;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_google_sign_in_trial);
//
////
////        SignInButton signInButton = findViewById(R.id.sign_in_button);
////        signInButton.setSize(SignInButton.SIZE_STANDARD);
////        findViewById(R.id.sign_in_button).setOnClickListener(new View.OnClickListener() {
////            @Override
////            public void onClick(View view) {
////                switch (view.getId()) {
////                    case R.id.sign_in_button:
////                        signIn();
////                        break;
////                    // ...
////                }
////
////            }
////        });
//
////        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
////                .requestEmail()
////                .build();
////
////        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
////
//
//    }
//
//
//    private void signIn() {
////        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
////        startActivityForResult(signInIntent, RC_SIGN_IN);
//    }
//
//}
