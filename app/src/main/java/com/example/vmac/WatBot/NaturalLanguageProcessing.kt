package com.example.vmac.WatBot

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log

import com.ibm.watson.developer_cloud.natural_language_understanding.v1.NaturalLanguageUnderstanding
import com.ibm.watson.developer_cloud.natural_language_understanding.v1.model.*


class NaturalLanguageProcessing : Activity() {

    var userPrefs: String = ""
    var userSemanticAnalysis: String = ""
    var newsSemanticAnalysis: String = ""
    var output: String  = ""

    val analyzer = NaturalLanguageUnderstanding(
            NaturalLanguageUnderstanding.VERSION_DATE_2017_02_27,
            "8fce2076-ff98-4d5b-88c6-6978ba567a38",
            "hMUHIoYwZzXh"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_natural_language_processing)

        val sharedPref = getSharedPreferences("mypref", 0)

        userPrefs = sharedPref.getString("userPrefs", "")

        semanticAnalysis(userPrefs)
    }

    companion object {

        fun passTheKey(str: String) {
            NaturalLanguageProcessing().semanticAnalysis(str)
        }


        fun newIntent(context: Context, sentiment: String){
            val intent = Intent(context, BaseActivity::class.java)
            intent.putExtra("Sentiment", sentiment)
            context.startActivity(intent)
        }
    }

    fun semanticAnalysis(text: String){
        val sharedPref = getSharedPreferences("mypref", 0)

        val editor = sharedPref.edit()
        AsyncTask.execute {

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
                    .text(text)
                    .features(features)
                    .build()

            val response = analyzer
                    .analyze(parameters)
                    .execute()


            val overallSentimentScore = response.sentiment.document.score
            var overallSentiment = "Positive"
            if (overallSentimentScore < 0.0)
                overallSentiment = "Negative"
            if (overallSentimentScore == 0.0)
                overallSentiment = "Neutral"

            output = "Overall sentiment: ${overallSentiment}\n\n"


            if (text == userPrefs) {
                editor.putString("userSemanticAnalysis", output)
                editor.putString("newsSemanticAnalysis", "")

            } else {
                editor.putString("newsSemanticAnalysis", output)
                editor.putString("userSemanticAnalysis", "")

            }

            editor.apply()



            for (entity in response.entities) {
                output += "${entity.text} (${entity.type}) Sentiment: ${entity.sentiment.score}\n"
            }

            runOnUiThread {

                userSemanticAnalysis = sharedPref.getString("userSemanticAnalysis", "")
                newsSemanticAnalysis = sharedPref.getString("newsSemanticAnalysis", "")
                Log.i("Sending stuff - User", userSemanticAnalysis)
                Log.i("Sending stuff - News", newsSemanticAnalysis)

                newIntent(this, userSemanticAnalysis)

            }

        }

        finish()

    }

}
