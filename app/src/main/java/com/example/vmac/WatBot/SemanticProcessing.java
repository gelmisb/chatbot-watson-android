package com.example.vmac.WatBot;

import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.ibm.watson.developer_cloud.alchemy.v1.model.DocumentSentiment;
import com.ibm.watson.developer_cloud.http.ServiceCallback;
import com.ibm.watson.developer_cloud.natural_language_understanding.v1.NaturalLanguageUnderstanding;
import com.ibm.watson.developer_cloud.tone_analyzer.v3.ToneAnalyzer;
import com.ibm.watson.developer_cloud.tone_analyzer.v3.model.Tone;
import com.ibm.watson.developer_cloud.tone_analyzer.v3.model.ToneAnalysis;
import com.ibm.watson.developer_cloud.tone_analyzer.v3.model.ToneOptions;
import com.ibm.watson.developer_cloud.tone_analyzer.v3.model.ToneScore;

import org.apache.commons.io.IOUtils;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.google.android.gms.internal.zzagr.runOnUiThread;

public class SemanticProcessing extends AppCompatActivity {

    EditText userInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_semantic_processing);
        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);     //  Fixed Portrait orientation

        final ToneAnalyzer toneAnalyzer = new ToneAnalyzer("2017-07-01");


        String username = "e62059ce-718a-4534-b5d0-f683bf3fc35e";
        String password = "npXI7CO026L0";

        toneAnalyzer.setUsernameAndPassword(username, password);

        Button analyzeButton = (Button)findViewById(R.id.analyze_button);

        analyzeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userInput = (EditText)findViewById(R.id.user_input);

                final String textToAnalyze = userInput.getText().toString();

                ToneOptions options = new ToneOptions.Builder()
                        .addTone(Tone.EMOTION)
                        .html(false).build();

                toneAnalyzer.getTone(textToAnalyze, options).enqueue(
                        new ServiceCallback<ToneAnalysis>() {
                            @Override
                            public void onResponse(ToneAnalysis response) {
                                List<ToneScore> scores = response.getDocumentTone()
                                        .getTones()
                                        .get(0)
                                        .getTones();

                                String detectedTones = "";
                                for(ToneScore score:scores) {
                                    if(score.getScore() > 0.5f) {
                                        detectedTones += score.getName() + " ";
                                    }
                                }

                                final String toastMessage =
                                        "The following emotions were detected:\n\n"
                                                + detectedTones.toUpperCase();

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(getBaseContext(),
                                                toastMessage, Toast.LENGTH_LONG).show();
                                    }
                                });
                            }

                            @Override
                            public void onFailure(Exception e) {
                                e.printStackTrace();
                            }
                        });


            }
        });




    }


}


