package com.example.licencjat_projekt.Projekt.Activities

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.licencjat_projekt.Projekt.Models.ReadQuizModel
import com.example.licencjat_projekt.Projekt.Models.ReadReportModel
import com.example.licencjat_projekt.R
import kotlinx.android.synthetic.main.activity_detail_quiz.*
import kotlinx.android.synthetic.main.activity_detail_quiz.detail_quiz_toolbar
import kotlinx.android.synthetic.main.activity_detail_report.*

class DetailReportActivity : AppCompatActivity() {
    private var quizDetails: ReadReportModel? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_report)

        if (intent.hasExtra(ReportsActivity.QUIZ_DETAILS)) {
            quizDetails = intent.getSerializableExtra(ReportsActivity.QUIZ_DETAILS) as ReadReportModel
        }

        setSupportActionBar(detail_report_toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        detail_report_toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
        if(quizDetails != null){
            supportActionBar!!.title = quizDetails!!.title
            detail_report_image.setImageBitmap(byteArrayToBitmap(quizDetails!!.image))
            detail_report_title.text = quizDetails!!.invitation_code
            detail_report_description.text = quizDetails!!.description
            detail_report_tags.text = quizDetails!!.tags
            detail_report_timer.text = quizDetails!!.time_limit.toString() + " minut(y)"
            if(quizDetails!!.points.isNaN()){
                detail_report_user_score.text = getUserById(quizDetails!!.by) + ", 0.0 /" +
                        quizDetails!!.max_points.toString() + ".0"
            }
            else {
                detail_report_user_score.text = getUserById(quizDetails!!.by) + ", " +
                        quizDetails!!.points.toString() + " /" +
                        quizDetails!!.max_points.toString() + ".0"
            }
        }
    }

    private fun byteArrayToBitmap(
        data: ByteArray
    ): Bitmap {
        return BitmapFactory.decodeByteArray(
            data,
            0,
            data.size
        )
    }
    private fun getUserById(id: Int): String{ //TODO: (WITOLD)
        return "Jan Kowalski"
    }
}