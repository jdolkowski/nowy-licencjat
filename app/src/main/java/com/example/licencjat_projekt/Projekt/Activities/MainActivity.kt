package com.example.licencjat_projekt.Projekt.Activities

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.licencjat_projekt.Projekt.Activities.CommunityActivity
import com.example.licencjat_projekt.Projekt.Activities.QuizMainActivity
import com.example.licencjat_projekt.Projekt.Activities.SignInActivity
import com.example.licencjat_projekt.Projekt.Models.ReadQuizModel
import com.example.licencjat_projekt.Projekt.database.Quiz
import com.example.licencjat_projekt.Projekt.database.Quizes
import com.example.licencjat_projekt.Projekt.utils.QuizesList
import com.example.licencjat_projekt.R
import com.google.android.material.navigation.NavigationView
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.time.format.DateTimeFormatter

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener, View.OnClickListener {
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var toolbar: Toolbar
    private var offsetId: Long = 0L
    private var quizesCount: Long = 0L
    private var quizesList = ArrayList<ReadQuizModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        toolbar = findViewById(R.id.toolbar)
        navigationView = findViewById(R.id.main_nav_view)
        drawerLayout = findViewById(R.id.main_drawer_layout)

        setSupportActionBar(toolbar)
        supportActionBar!!.title = ""

        //navigation drawer menu

        navigationView.bringToFront()
        val toggle = ActionBarDrawerToggle(
            this,
            drawerLayout,
            toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        navigationView.setNavigationItemSelectedListener(this)
        main_firstPage.setOnClickListener(this)
        main_backPage.setOnClickListener(this)
        main_nextPage.setOnClickListener(this)
        main_lastPage.setOnClickListener(this)
        firstFive()
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen((GravityCompat.START))) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.drawer_menu_create_quiz -> run {
                val intent = Intent(
                    this,
                    QuizMainActivity::class.java
                )
                startActivity(intent)
            }
            R.id.drawer_menu_profile -> run {
                val intent = Intent(
                    this,
                    ProfileActivity::class.java
                )
                startActivity(intent)
            }
            R.id.drawer_menu_community -> run {
                val intent = Intent(
                    this,
                    CommunityActivity::class.java
                )
                startActivity(intent)
            }
            R.id.drawer_menu_logout -> run {
                finish()
            }
        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.main_firstPage -> {
                firstFive()
                quizesRecyclerView(quizesList)

            }
            R.id.main_backPage -> {
                if(offsetId >= 5L) {
                    previousFive()
                    quizesRecyclerView(quizesList)
                }
            }
            R.id.main_nextPage -> {
                if(offsetId + 5 <= quizesCount) {
                    nextFive()
                    quizesRecyclerView(quizesList)
                }
            }
            R.id.main_lastPage -> {
                lastFive()
                quizesRecyclerView(quizesList)
            }
        }
    } //TODO: read pages WITEK
    private fun firstFive() {
        this.offsetId = 0L
        val list = runBlocking {
            return@runBlocking newSuspendedTransaction(Dispatchers.IO) {
                Quiz.all().limit(5).toList()
            }
        }
        if (list.isNotEmpty())
            exposedToModel(list)
        for (i in list){
            Log.e("quiz", i.title)
        }

        Log.e("first", "$offsetId")
    }

    private fun nextFive() {
        this.offsetId += 5L
        val list = runBlocking {
            return@runBlocking newSuspendedTransaction(Dispatchers.IO) {
                Quiz.all().limit(5, offsetId).toList()
            }
        }
        if (list.isNotEmpty())
            exposedToModel(list)
        else
            this.offsetId -= 5L
        Log.e("next", "$offsetId")
    }

    private fun previousFive() {
        this.offsetId -= 5L
        val list = runBlocking {
            return@runBlocking newSuspendedTransaction(Dispatchers.IO) {
                Quiz.all().limit(5, offsetId).toList()
            }
        }
        if (list.isNotEmpty())
            exposedToModel(list)
        else
            this.offsetId += 5L
        Log.e("prev", "$offsetId")
    }

    private fun lastFive() {
        if(quizesCount.mod(5) != 0) {
            this.offsetId = quizesCount - quizesCount.mod(5)
        }
        else{
            this.offsetId = quizesCount - 5
        }
        Log.e("last", "$offsetId")
        val list = runBlocking {
            return@runBlocking newSuspendedTransaction(Dispatchers.IO) {
                if(quizesCount.mod(5) != 0) {
                    Quiz.all().orderBy(Quizes.id to SortOrder.DESC).limit((quizesCount.mod(5)))
                        .toList()
                }else{
                    Quiz.all().orderBy(Quizes.id to SortOrder.DESC).limit(5)
                        .toList()
                }
            }
        }
        if (list.isNotEmpty())
            exposedToModel(list.reversed())
    }

    //TODO: get number of quizes WITEK
    private fun getQuizesNumber(){
        val n = runBlocking {
            return@runBlocking newSuspendedTransaction(Dispatchers.IO) {
                Quiz.all().count()
            }
        }
        quizesCount = n
    }
    private fun exposedToModel(list: List<Quiz>) {
        val quizesArrayList = ArrayList<ReadQuizModel>()
        for (i in list) {
            quizesArrayList.add(
                ReadQuizModel(
                    i.title,
                    i.time_limit,
                    i.description,
                    "xd",
                    i.gz_text,
                    true,
                    i.invitation_code,
                    i.image.bytes,
                )
            )
        }

        quizesList = quizesArrayList
    }

    private fun quizesRecyclerView(quizes: ArrayList<ReadQuizModel>) {
        main_rv_quizes.layoutManager = LinearLayoutManager(this)
        main_rv_quizes.setHasFixedSize(true)
        val quizesList = QuizesList(this, quizes)
        main_rv_quizes.adapter = quizesList

        quizesList.setOnClickListener(object : QuizesList.OnClickListener {
            override fun onClick(position: Int, model: ReadQuizModel) {
                val intent = Intent(
                    this@MainActivity,
                    DetailQuizActivity::class.java
                )

                intent.putExtra( //passing object to activity
                    QUIZ_DETAILS,
                    model
                )
                startActivityForResult(intent, QUIZ_CODE)
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == QUIZ_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                firstFive()
                getQuizesNumber()
                if (quizesList.size > 0) {
                    //changing no places string with the recycler view list
                    main_rv_quizes.visibility = View.VISIBLE
                    main_no_quizes.visibility = View.GONE
                    quizesRecyclerView(quizesList)
                } else {
                    //when there are no places right announcement shown
                    main_rv_quizes.visibility = View.GONE
                    main_no_quizes.visibility = View.VISIBLE
                }
            }
        }
    }

    companion object {
        var QUIZ_DETAILS = "quiz_details"
        var QUIZ_CODE = 1
    }
}