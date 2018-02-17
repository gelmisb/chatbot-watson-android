package com.example.vmac.WatBot

import android.os.AsyncTask
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import com.ibm.watson.developer_cloud.natural_language_understanding.v1.NaturalLanguageUnderstanding
import com.ibm.watson.developer_cloud.natural_language_understanding.v1.model.*
import kotlinx.android.synthetic.main.activity_natural_language_processing.*
import java.nio.file.Files.find


class NaturalLanguageProcessing : AppCompatActivity() {

    var userPrefs: String = ""
    var targets: ArrayList<String>? = null

    val analyzer = NaturalLanguageUnderstanding(
            NaturalLanguageUnderstanding.VERSION_DATE_2017_02_27,
            "8fce2076-ff98-4d5b-88c6-6978ba567a38",
            "hMUHIoYwZzXh"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_natural_language_processing)


        val sharedPref = getSharedPreferences("mypref", 0)
        userPrefs = sharedPref.getString("userPrefs", "")

        Log.i("UserPrefs", userPrefs)


        val allText : TextView = findViewById(R.id.documentContents)


        val btn: Button = findViewById(R.id.analyse)



        btn.setOnClickListener( {
            AsyncTask.execute {
//                val results = analyzer.analyze(analyzerOptions).execute()

                val emotion = EmotionOptions.Builder()
                        .document(true)
                        .build()


                val entityOptions = EntitiesOptions.Builder()
                        .emotion(true)
                        .sentiment(true)
                        .build()

                val keywordsOptions = KeywordsOptions.Builder()
                        .emotion(true)
                        .sentiment(true)
                        .build()

                val sentimentOptions = SentimentOptions.Builder()
                        .document(true)
                        .build()

                val features = Features.Builder()
                        .emotion(emotion)
                        .entities(entityOptions)
                        .keywords(keywordsOptions)
                        .sentiment(sentimentOptions)
                        .build()

                val parameters = AnalyzeOptions.Builder()
                        .text(userPrefs)
                        .features(features)
                        .build()

                val response = analyzer
                        .analyze(parameters)
                        .execute()


                val overallSentimentScore = response.sentiment.document.score
                var overallSentiment = "Positive"
                if(overallSentimentScore < 0.0)
                    overallSentiment = "Negative"
                if(overallSentimentScore == 0.0)
                    overallSentiment = "Neutral"

                var output = "Overall sentiment: ${overallSentiment}\n\n"

                for(entity in response.entities) {
                    output += "${entity.text} (${entity.type})\n"

                    val validEmotions = arrayOf("Anger", "Joy", "Disgust", "Fear", "Sadness")

//                    val emotionValues = arrayOf(
//                            entity.emotion.anger,
//                            entity.emotion.joy,
//                            entity.emotion.disgust,
//                            entity.emotion.fear,
//                            entity.emotion.sadness
//                    )
//
//                    val currentEmotion = validEmotions[emotionValues.indexOf(emotionValues.max())]

                    output += "Emotion: " +
                            "Sentiment: ${entity.sentiment.score}" +
                            "\n\n"
                }

                runOnUiThread {
                    allText.text = output
                    Log.i("Sending stuff", output)
                }
            }
        })
    }
}
