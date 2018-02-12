package com.example.vmac.WatBot

import android.os.AsyncTask
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import com.ibm.watson.developer_cloud.natural_language_understanding.v1.NaturalLanguageUnderstanding
import com.ibm.watson.developer_cloud.natural_language_understanding.v1.model.AnalyzeOptions
import com.ibm.watson.developer_cloud.natural_language_understanding.v1.model.EntitiesOptions
import com.ibm.watson.developer_cloud.natural_language_understanding.v1.model.Features
import com.ibm.watson.developer_cloud.natural_language_understanding.v1.model.SentimentOptions
import kotlinx.android.synthetic.main.activity_natural_language_processing.*
import java.nio.file.Files.find

class NaturalLanguageProcessing : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_natural_language_processing)


        val editText : EditText = findViewById(R.id.documentURL)
        val allText : TextView = findViewById(R.id.documentContents)


        val btn: Button = findViewById(R.id.analyse)


        // extract the text and put it in the analyser options

        btn.setOnClickListener {

            val analyzer = NaturalLanguageUnderstanding(
                    NaturalLanguageUnderstanding.VERSION_DATE_2017_02_27,
                    "8fce2076-ff98-4d5b-88c6-6978ba567a38",
                    "hMUHIoYwZzXh"
            )

            val entityOptions = EntitiesOptions.Builder()
                    .emotion(true)
                    .sentiment(true)
                    .build()

            val sentimentOptions = SentimentOptions.Builder()
                    .document(true)
                    .build()

            val features = Features.Builder()
                    .entities(entityOptions)
                    .sentiment(sentimentOptions)
                    .build()

            val analyzerOptions = AnalyzeOptions.Builder()
                    .text("The European languages are members of the same family. Their separate existence is a myth. For science, music, sport, etc, Europe uses the same vocabulary. The languages only differ in their grammar, their pronunciation and their most common words. Everyone realizes why a new common language would be desirable: one could refuse to pay expensive translators. To achieve this, it would be necessary to have uniform grammar, pronunciation and more common words. If several languages coalesce, the grammar of the resulting language is more simple and regular than that of the individual languages. The new common language will be more simple and regular than the existing European languages. It will be as simple as Occidental; in fact, it will be Occidental. To an English person, it will seem like simplified English, as a skeptical Cambridge friend of mine told me what Occidental is.The European languages are members of the same family. Their separate existence is a myth. For science, music, sport, etc, Europe uses the same vocabulary. The languages only differ in their grammar, their pronunciation and their most common words. Everyone realizes why a new common language would be desirable: one could refuse to pay expensive translators. To")
                    .features(features)
                    .build()

            AsyncTask.execute {
                val results = analyzer.analyze(analyzerOptions).execute()

                val overallSentimentScore = results.sentiment.document.score
                var overallSentiment = "Positive"
                if(overallSentimentScore < 0.0)
                    overallSentiment = "Negative"
                if(overallSentimentScore == 0.0)
                    overallSentiment = "Neutral"

                var output = "Overall sentiment: ${overallSentiment}\n\n"

                for(entity in results.entities) {
                    output += "${entity.text} (${entity.type})\n"
                    val validEmotions = arrayOf("Anger", "Joy", "Disgust", "Fear", "Sadness")
                    val emotionValues = arrayOf(
                            entity.emotion.anger,
                            entity.emotion.joy,
                            entity.emotion.disgust,
                            entity.emotion.fear,
                            entity.emotion.sadness
                    )
                    val currentEmotion = validEmotions[emotionValues.indexOf(emotionValues.max())]
                    output += "Emotion: ${currentEmotion}, " +
                            "Sentiment: ${entity.sentiment.score}" +
                            "\n\n"
                }

                runOnUiThread {
                    allText.text = output
                }
            }
        }
    }
}
