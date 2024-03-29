package com.example.licencjat_projekt.Projekt.Activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.licencjat_projekt.Projekt.Models.ReadQuizModel
import com.example.licencjat_projekt.Projekt.database.*
import com.example.licencjat_projekt.Projekt.utils.QuizesList
import com.example.licencjat_projekt.Projekt.utils.falseToken
import com.example.licencjat_projekt.R
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_profile.*
import kotlinx.android.synthetic.main.activity_search.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

class SearchActivity : AppCompatActivity(), View.OnClickListener {
    private var searchString: String? = null
    private var offsetId: Long = 0L
    private var quizesCount: Long = 0L
    private var searchCode: Boolean = false
    private var quizesList = ArrayList<ReadQuizModel>()
    companion object {
        var QUIZ_DETAILS = "quiz_details"
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        if (falseToken()){
            val intent = Intent(this,SignInActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            intent.putExtra("EXIT",true)
            startActivity(intent)
        }
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)
        setSupportActionBar(search_toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        search_toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        supportActionBar!!.title = ""
        search_firstPage.setOnClickListener(this)
        search_backPage.setOnClickListener(this)
        search_nextPage.setOnClickListener(this)
        search_lastPage.setOnClickListener(this)
        search_btn_search.setOnClickListener(this)
        search_checkBox.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.search_btn_search -> {
                searchString = search_et.text.toString()
                if (searchString!!.isNotEmpty()) {
                    firstFive(searchString!!)
                    getQuizesNumber(searchString!!)
                    quizesRecyclerView(quizesList)
                }
            }
            R.id.search_checkBox -> {
                searchCode = search_checkBox.isChecked
            }

            R.id.search_firstPage -> {
                if (searchString!!.isNotEmpty()) {
                    if (offsetId != 0L) {
                        firstFive(searchString!!)
                        quizesRecyclerView(quizesList)
                    }
                }
            }

            R.id.search_backPage -> {
                if (searchString!!.isNotEmpty()) {
                    if (offsetId >= 5L) {
                        prevFive(searchString!!)
                        quizesRecyclerView(quizesList)
                    }
                }
            }

            R.id.search_nextPage -> {
                if (searchString!!.isNotEmpty()) {
                    if (offsetId + 5 <= quizesCount) {
                        nextFive(searchString!!)
                        quizesRecyclerView(quizesList)
                    }
                }
            }

            R.id.search_lastPage -> {
                if (searchString!!.isNotEmpty()) {
                    if (offsetId + 5 < quizesCount) {
                        lastFive(searchString!!)
                        quizesRecyclerView(quizesList)
                    }
                }
            }
        }
    }

    private fun firstFive(str: String) {
        if (searchCode) {
            searchDBForInviteCode(str)
        } else {
            quizesList.clear()
            searchString = searchString!!.lowercase()
            this.offsetId = 0L
            runBlocking {
                newSuspendedTransaction(Dispatchers.IO) {
                    val list = Quiz.wrapRows(
                        Quizes.innerJoin(QuizTags).innerJoin(Tags).select {
                        ((Tags.name.lowerCase() like "$str%") or
                                (Quizes.title.lowerCase() like "$str%")) and
                                (Quizes.private eq false)
                    }.withDistinct().limit(5)).toList()
                    if (list.isNotEmpty())
                        exposedToModel(list.distinct())
                }
            }
        }
    }

    private fun nextFive(str: String) {
        if (searchCode) {
            searchDBForInviteCode(str)
        } else {
            searchString = searchString!!.lowercase()
            this.offsetId += 5L
            runBlocking {
                newSuspendedTransaction(Dispatchers.IO) {
                    val list = Quiz.wrapRows(Quizes.innerJoin(QuizTags).innerJoin(Tags).select {
                        ((Tags.name.lowerCase() like "$str%") or
                                (Quizes.title.lowerCase() like "$str%")) and
                                (Quizes.private eq false)
                    }.withDistinct().limit(5,offsetId)).toList()
                    if (list.isNotEmpty())
                        exposedToModel(list.distinct())
                    else
                        offsetId -= 5L
                }
            }
        }
    }

    private fun prevFive(str: String) {
        if (searchCode) {
            searchDBForInviteCode(str)
        } else {
            searchString = searchString!!.lowercase()
            this.offsetId -= 5L
            runBlocking {
                newSuspendedTransaction(Dispatchers.IO) {
                    val list = Quiz.wrapRows(Quizes.innerJoin(QuizTags).innerJoin(Tags).select {
                        ((Tags.name.lowerCase() like "$str%") or
                                (Quizes.title.lowerCase() like "$str%")) and
                                (Quizes.private eq false)
                    }.withDistinct().limit(5,offsetId)).toList()
                    if (list.isNotEmpty())
                        exposedToModel(list.distinct())
                    else
                        offsetId += 5L
                }
            }
        }
    }

    private fun lastFive(str: String) {
        if (searchCode) {
            searchDBForInviteCode(str)
        } else {
            searchString = searchString!!.lowercase()
            if (quizesCount.mod(5) != 0) {
                this.offsetId = quizesCount - quizesCount.mod(5)
            } else {
                this.offsetId = quizesCount - 5
            }
            runBlocking {
                newSuspendedTransaction(Dispatchers.IO) {
                    var list = emptyList<Quiz>()
                    if (quizesCount.mod(5) != 0) {
                        list = Quiz.wrapRows(Quizes.innerJoin(QuizTags).innerJoin(Tags).select {
                            ((Tags.name.lowerCase() like "$str%") or
                                    (Quizes.title.lowerCase() like "$str%")) and
                                    (Quizes.private eq false)
                        }.withDistinct().orderBy(
                            Quizes.id to SortOrder.DESC)
                        ).limit((quizesCount.mod(5))).toList()
                    } else {
                        list = Quiz.wrapRows(Quizes.innerJoin(QuizTags).innerJoin(Tags).select {
                            ((Tags.name.lowerCase() like "$str%") or
                                    (Quizes.title.lowerCase() like "$str%")) and
                                    (Quizes.private eq false)
                        }.withDistinct().orderBy(
                            Quizes.id to SortOrder.DESC)
                        ).limit(5).toList()
                    }
                    if (list.isNotEmpty())
                        exposedToModel(list.reversed().distinct())
                }
            }
        }
    }

    private fun searchDBForInviteCode(s: String) = runBlocking {
        offsetId = 0
        newSuspendedTransaction(Dispatchers.IO) {
            val q = Quiz.find { Quizes.invitation_code eq s }.toList()
            quizesList.clear()
            if (q.isNotEmpty())
                quizesList.add(
                    ReadQuizModel(
                        q[0].id.value,
                        q[0].title,
                        q[0].time_limit,
                        q[0].description,
                        getQuizTags(q[0]),
                        q[0].gz_text,
                        q[0].private,
                        q[0].invitation_code,
                        q[0].image.bytes,
                        q[0].user.login,
                        q[0].questions,
                    )
                )
        }

    }

    private fun getQuizTags(q: Quiz): String {
        val tmp = runBlocking {
            newSuspendedTransaction(Dispatchers.IO) {
                val query = Tags.innerJoin(QuizTags).slice(Tags.columns).select {
                    QuizTags.quiz eq q.id
                }.withDistinct()
                return@newSuspendedTransaction Tag.wrapRows(query).toList()
            }
        }
        var tags = ""
        for (i in tmp) {
            tags += i.name + " "
        }
        return tags
    }

    private fun getQuizesNumber(str: String) {
        val n = runBlocking {
            return@runBlocking newSuspendedTransaction(Dispatchers.IO) {
                Quiz.find { (Quizes.title.lowerCase() like "$str%") and
                        (Quizes.private eq false) }.count()
            }
        }
        quizesCount = n
    }

    private fun exposedToModel(list: List<Quiz>) {

        val quizesArrayList = ArrayList<ReadQuizModel>()
        for (i in list) {
            getQuizTags(i)
            quizesArrayList.add(
                ReadQuizModel(
                    i.id.value,
                    i.title,
                    i.time_limit,
                    i.description,
                    getQuizTags(i),
                    i.gz_text,
                    i.private,
                    i.invitation_code,
                    i.image.bytes,
                    i.user.login,
                    i.questions,
                )
            )
        }
        quizesList = quizesArrayList
    }

    private fun quizesRecyclerView(quizes: ArrayList<ReadQuizModel>) {

        search_rv_quizes.layoutManager = LinearLayoutManager(this)
        search_rv_quizes.setHasFixedSize(true)
        val quizesList = QuizesList(this, quizes)
        search_rv_quizes.adapter = quizesList

        quizesList.setOnClickListener(object : QuizesList.OnClickListener {
            override fun onClick(position: Int, model: ReadQuizModel) {
                val intent = Intent(
                    this@SearchActivity,
                    DetailQuizActivity::class.java
                )

                intent.putExtra(
                    QUIZ_DETAILS,
                    model
                )
                startActivity(intent)
            }
        })
    }
}