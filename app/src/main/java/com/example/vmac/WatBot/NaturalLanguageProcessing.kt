package com.example.vmac.WatBot

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log

import com.ibm.watson.developer_cloud.natural_language_understanding.v1.NaturalLanguageUnderstanding
import com.ibm.watson.developer_cloud.natural_language_understanding.v1.model.*
import com.ibm.watson.developer_cloud.natural_language_understanding.v1.model.EmotionOptions




class NaturalLanguageProcessing : Activity() {

    var userPrefs: String = ""
    var userSemanticAnalysis: String = ""
    var userEmotionAnalysis: String = ""
    var newsSemanticAnalysis: String = ""
    var output: String  = ""



    val analyzer = NaturalLanguageUnderstanding(
            NaturalLanguageUnderstanding.VERSION_DATE_2017_02_27,
            "8fce2076-ff98-4d5b-88c6-6978ba567a38",
            "hMUHIoYwZzXh"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val sharedPref: SharedPreferences = getSharedPreferences("mypref", 0)



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
        val sharedPref: SharedPreferences = getSharedPreferences("mypref", 0)

        AsyncTask.execute {


            val emotion = EmotionOptions.Builder()
                    .document(true)
                    .build()

            val entityOptions = EntitiesOptions.Builder()
                    .emotion(true)
                    .sentiment(true)
                    .build()

            val sentimentOptions = SentimentOptions.Builder()
                    .document(true)
                    .build()

            val features = Features.Builder()
                    .emotion(emotion)
                    .entities(entityOptions)
                    .sentiment(sentimentOptions)
                    .build()

            val analyzerOptions = AnalyzeOptions.Builder()
                    .text(text)
                    .features(features)
                    .build()

            val response = analyzer
                    .analyze(analyzerOptions)
                    .execute()


            val overallSentimentScore = response.sentiment.document.score
            var overallSentiment = "Positive"
            if (overallSentimentScore < 0.0)
                overallSentiment = "Negative"
            if (overallSentimentScore == 0.0)
                overallSentiment = "Neutral"

            output = "Overall sentiment: ${overallSentiment}\n\n"


            val editor = sharedPref.edit()


            for(entity in response.entities) {
                output += "${entity.text} (${entity.type})\n"
                val validEmotions = arrayOf("Anger", "Joy", "Disgust", "Fear", "Sadness")

                if(entity.emotion != null) {

                    val emotionValues = arrayOf(
                            entity.emotion.anger,
                            entity.emotion.joy,
                            entity.emotion.disgust,
                            entity.emotion.fear,
                            entity.emotion.sadness
                    )
                }

                val emotionValues = arrayOf("Anger", "Joy", "Disgust", "Fear", "Sadness")

                val currentEmotion = validEmotions[emotionValues.indexOf(emotionValues.max())]
                output += "Emotion: ${currentEmotion}, " +
                        "Sentiment: ${entity.sentiment.score}" +
                        "\n\n"


                if (text == userPrefs) {
                    editor.putString("userSemanticAnalysis", output)
                    editor.putString("userEmotionAnalysis", currentEmotion)
                    editor.putString("newsSemanticAnalysis", "")

                } else {
                    editor.putString("newsSemanticAnalysis", output)
                    editor.putString("userSemanticAnalysis", "")

                }

            }

            editor.apply()


            runOnUiThread {

                userSemanticAnalysis = sharedPref.getString("userSemanticAnalysis", "")
                userEmotionAnalysis = sharedPref.getString("userEmotionAnalysis", "")

                Log.i("Sending stuff - User", userSemanticAnalysis)
                Log.i("Sending stuff - News", userEmotionAnalysis)

                newIntent(this, userSemanticAnalysis)
                Log.i("emotion????", output)

            }

        }

        finish()

    }

}
