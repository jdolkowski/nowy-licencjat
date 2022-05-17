package com.example.licencjat_projekt.Projekt.Activities

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.annotation.RequiresPermission
import com.example.licencjat_projekt.Projekt.Models.ReadAnswerModel
import com.example.licencjat_projekt.Projekt.Models.ReadQuestionModel
import com.example.licencjat_projekt.Projekt.Models.ReadQuizModel
import com.example.licencjat_projekt.Projekt.database.*
import com.example.licencjat_projekt.Projekt.database.Answers.question
import com.example.licencjat_projekt.Projekt.database.Questions.quiz
import com.example.licencjat_projekt.R
import kotlinx.android.synthetic.main.activity_detail_quiz.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.statements.api.ExposedBlob
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

class DetailQuizActivity1 : AppCompatActivity(), View.OnClickListener {
    private var quizDetails: ReadQuizModel? = null
    private var questionsList = arrayListOf<ReadQuestionModel>()
    private var answersList = ArrayList<ReadAnswerModel>()

    companion object {
        var QUESTION_DETAILS = "question_details"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_quiz)

        if (intent.hasExtra(MainActivity.QUIZ_DETAILS)) {
            quizDetails = intent.getSerializableExtra(MainActivity.QUIZ_DETAILS) as ReadQuizModel
        }
        if (intent.hasExtra(UserQuizesActivity.QUIZ_DETAILS)) {
            quizDetails = intent.getSerializableExtra(UserQuizesActivity.QUIZ_DETAILS) as ReadQuizModel
        }
        if (intent.hasExtra(SearchActivity.QUIZ_DETAILS)) {
            quizDetails = intent.getSerializableExtra(SearchActivity.QUIZ_DETAILS) as ReadQuizModel
        }

        if (quizDetails != null) {
            setSupportActionBar(detail_quiz_toolbar)
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            detail_quiz_toolbar.setNavigationOnClickListener {
                onBackPressed()
            }
            supportActionBar!!.title = quizDetails!!.title
            detail_quiz_image.setImageBitmap(byteArrayToBitmap(quizDetails!!.image))
            detail_quiz_title.text = quizDetails!!.title
            detail_quiz_description.text = quizDetails!!.description
            detail_quiz_tags.text = quizDetails!!.tags
        }
        question_display_btn_back.setOnClickListener(this)
        detail_quiz_author_name.setText(getAuthorName())
        detail_quiz_number_questions.setText(getNOQuestions())
    }

    //decode image read from db
    private fun byteArrayToBitmap(
        data: ByteArray
    ): Bitmap {
        return BitmapFactory.decodeByteArray(
            data,
            0,
            data.size
        )
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.question_display_btn_back -> {
                val intent = Intent(
                    this,
                    QuestionsShowActivity::class.java
                )
                intent.putExtra(QUESTION_DETAILS, quizDetails)
                startActivity(intent)
                finish()
            }
        }
    }
    private fun getAuthorName(): String{ //TODO: (WITOLD) autor quizu
        return quizDetails!!.author
    }
    private fun getNOQuestions(): String{ //TODO (WITOLD) liczba pytań quizu
        return quizDetails!!.no_questions.toString()
    }
}