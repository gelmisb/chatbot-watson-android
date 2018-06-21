package com.example.vmac.WatBot;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.transition.Fade;
import android.support.transition.TransitionSet;
import android.support.v4.view.animation.FastOutLinearInInterpolator;
import android.support.v4.view.animation.LinearOutSlowInInterpolator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.login.LoginManager;
import com.facebook.login.widget.LoginButton;
import com.transitionseverywhere.extra.Scale;

public class WelcomeScreen extends AppCompatActivity {


    protected AlphaAnimation fadeIn = new AlphaAnimation(0.0f , 1.0f ) ;
    protected AlphaAnimation fadeIn1 = new AlphaAnimation(0.0f , 1.0f ) ;
    protected AlphaAnimation fadeIn2 = new AlphaAnimation(0.0f , 1.0f ) ;
    protected AlphaAnimation fadeIn3 = new AlphaAnimation(0.0f , 1.0f ) ;
    protected AlphaAnimation fadeIn4 = new AlphaAnimation(0.0f , 1.0f ) ;
    protected AlphaAnimation fadeIn5 = new AlphaAnimation(0.0f , 1.0f ) ;
    protected AlphaAnimation fadeIn6 = new AlphaAnimation(0.0f , 1.0f ) ;

    protected AlphaAnimation fadeOut = new AlphaAnimation( 1.0f , 0.0f ) ;
    protected AlphaAnimation fadeOut1 = new AlphaAnimation( 1.0f , 0.0f ) ;
    protected AlphaAnimation fadeOut2 = new AlphaAnimation( 1.0f , 0.0f ) ;
    protected AlphaAnimation fadeOut3 = new AlphaAnimation( 1.0f , 0.0f ) ;
    protected AlphaAnimation fadeOut4 = new AlphaAnimation( 1.0f , 0.0f ) ;
    protected AlphaAnimation fadeOut5 = new AlphaAnimation( 1.0f , 0.0f ) ;

    public Button contnue;
    public ImageView showOff;


    TextView tvWelcome, intro1, intro2, intro3, intro4, intro5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome_screen);


        SharedPreferences pref = getSharedPreferences("ActivityPREF", Context.MODE_PRIVATE);
        if(pref.getBoolean("activity_executed", false)){
            Intent intent = new Intent(this, GoogleSignInTrial.class);
            startActivity(intent);
            finish();
        } else {
            SharedPreferences.Editor ed = pref.edit();
            ed.putBoolean("activity_executed", true);
            ed.apply();
        }

        showOff = (ImageView)findViewById(R.id.imageViewCircle);

        String name = getIntent().getStringExtra("name");

        contnue = (Button) findViewById(R.id.continueButton);
        contnue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), GoogleSignInTrial.class);
                startActivity(intent);
            }
        });

        tvWelcome = (TextView) findViewById(R.id.welcomeMes);
        intro1 = (TextView) findViewById(R.id.intro_1);
        intro2 = (TextView) findViewById(R.id.intro_2);
        intro3 = (TextView) findViewById(R.id.intro_3);
        intro4 = (TextView) findViewById(R.id.intro_4);
        intro5 = (TextView) findViewById(R.id.intro_5);

        tvWelcome.setVisibility(View.INVISIBLE);
        intro1.setVisibility(View.INVISIBLE);
        intro2.setVisibility(View.INVISIBLE);
        intro3.setVisibility(View.INVISIBLE);
        intro4.setVisibility(View.INVISIBLE);
        intro5.setVisibility(View.INVISIBLE);
        contnue.setVisibility(View.INVISIBLE);
        showOff.setVisibility(View.INVISIBLE);


        fadeIn.setDuration(2500);
        fadeIn.setFillAfter(true);
        tvWelcome.startAnimation(fadeIn);

        fadeIn.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                tvWelcome.startAnimation(fadeOut);
                intro1.setEnabled(true);
                intro1.startAnimation(fadeIn1);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        fadeIn1.setDuration(2500);
        fadeIn1.setFillAfter(true);

        fadeIn1.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                intro1.startAnimation(fadeOut1);
                intro2.setEnabled(true);
                intro2.startAnimation(fadeIn2);

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        fadeIn2.setDuration(2500);
        fadeIn2.setFillAfter(true);





        fadeIn2.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                intro2.startAnimation(fadeOut2);
                intro3.setEnabled(true);
                intro3.startAnimation(fadeIn3);

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        fadeIn3.setDuration(2500);
        fadeIn3.setFillAfter(true);

        fadeIn3.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                intro3.startAnimation(fadeOut3);
                intro4.setEnabled(true);
                intro4.startAnimation(fadeIn4);

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        fadeIn4.setDuration(2500);
        fadeIn4.setFillAfter(true);


        fadeIn4.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                intro4.startAnimation(fadeOut4);
                intro5.setEnabled(true);
                intro5.startAnimation(fadeIn5);

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });


        fadeIn5.setDuration(2500);
        fadeIn5.setFillAfter(true);

        fadeIn5.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                intro5.startAnimation(fadeOut5);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        fadeOut.setDuration(2000);
        fadeOut.setFillAfter(true);

        fadeOut1.setDuration(2000);
        fadeOut1.setFillAfter(true);

        fadeOut2.setDuration(2000);
        fadeOut2.setFillAfter(true);

        fadeOut3.setDuration(2000);
        fadeOut3.setFillAfter(true);

        fadeOut4.setDuration(2000);
        fadeOut4.setFillAfter(true);

        fadeOut5.setDuration(2000);
        fadeOut5.setFillAfter(true);


        tvWelcome.setText("Welcome ");


        fadeIn6.setDuration(2500);
        fadeIn6.setFillAfter(true);
        fadeIn6.setStartOffset(15500);
        contnue.startAnimation(fadeIn6);
        showOff.startAnimation(fadeIn6);

    }
}
