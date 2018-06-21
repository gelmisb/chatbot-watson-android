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
    var output: String  = ""
    var currentEmotion: String = ""

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

        fun newIntent(context: Context){
            val intent = Intent(context, BaseActivity::class.java)
            context.startActivity(intent)
        }
    }

    fun semanticAnalysis(text: String){
        val sharedPref: SharedPreferences = getSharedPreferences("mypref", 0)

        AsyncTask.execute {

            val entityOptions = EntitiesOptions.Builder()
                    .sentiment(true)
                    .build()

            val emotion = EmotionOptions.Builder()
                    .document(true)
                    .build()

            val sentimentOptions = SentimentOptions.Builder()
                    .document(true)
                    .build()

            val features = Features.Builder()
                    .entities(entityOptions)
                    .sentiment(sentimentOptions)
                    .emotion(emotion)
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
//
//            val validEmotions = arrayOf("Anger", "Disgust", "Fear", "Joy", "Sadness")
//
//            val emotionValues = arrayOf(
//                    response.emotion.document.emotion.anger,
//                    response.emotion.document.emotion.disgust,
//                    response.emotion.document.emotion.fear,
//                    response.emotion.document.emotion.joy,
//                    response.emotion.document.emotion.sadness
//            )

            for(entity in response.entities) {
                output += "${entity.text} (${entity.type})\n"
                val validEmotions = arrayOf("Anger", "Joy", "Disgust", "Fear", "Sadness")
                val emotionValues = arrayOf(
                        response.emotion.document.emotion.anger,
                        response.emotion.document.emotion.joy,
                        response.emotion.document.emotion.disgust,
                        response.emotion.document.emotion.fear,
                        response.emotion.document.emotion.sadness
                )

                var sentScore = "Positive"

                if (entity.sentiment.score < 0.0)
                    sentScore = "Negative"

                if (entity.sentiment.score == 0.0)
                    sentScore = "Neutral"

                currentEmotion = validEmotions[emotionValues.indexOf(emotionValues.max())]

                output += "Emotion: ${currentEmotion}, " +
                        "Sentiment: ${entity.sentiment.score}\n" +
                        "Sentiment value: ${sentScore}" +
                        "\n\n"

            }

            editor.putString("fullResult", output)
            editor.putString("userSemanticAnalysis", overallSentiment)
            editor.putString("userEmotionAnalysis", currentEmotion)

            editor.apply()


            runOnUiThread {

                newIntent(this)

            }

        }

        finish()

    }

}
