package com.example.vmac.WatBot;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.transition.Explode;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.vision.text.Text;

public class SemanticResult extends AppCompatActivity {


    private String emotion;
    private String full;
    private Button backButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Making it full screen
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        // Setting flags from previous iinstance
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // inside your activity (if you did not enable transitions in your theme)
        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);

        // set an exit transition
        getWindow().setEnterTransition(new Explode());

        // Setting content view
        setContentView(R.layout.activity_semantic_result);

        //  Fixed Portrait orientation
        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        backButton = (Button)findViewById(R.id.backBtn);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // stop talking when the application is closed.

                Intent intent = new Intent(getApplicationContext(), MainScreenTime.class);
                startActivity(intent);
                finish();

            }
        });


        SharedPreferences sharedPref= getSharedPreferences("mypref", 0);

        emotion = sharedPref.getString("userEmotionAnalysis", "");
        full = sharedPref.getString("fullResult", "");

        TextView result = (TextView) findViewById(R.id.results);

        result.setText(full);

    }
}
