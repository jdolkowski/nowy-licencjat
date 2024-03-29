package com.example.licencjat_projekt.Projekt.Activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.licencjat_projekt.Projekt.Models.*
import com.example.licencjat_projekt.Projekt.database.*
import com.example.licencjat_projekt.Projekt.utils.FriendsList
import com.example.licencjat_projekt.Projekt.utils.currentUser
import com.example.licencjat_projekt.Projekt.utils.falseToken
import com.example.licencjat_projekt.R
import kotlinx.android.synthetic.main.activity_community.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.dao.with
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.or
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.time.LocalDateTime


class CommunityQuizInviteActivity : AppCompatActivity() {
    private var quizDetails: ReadQuizModel? = null
    private var friendsList = ArrayList<ReadUsermodel>()
    private lateinit var toastCorrect: Toast
    private lateinit var toastIncorrect: Toast
    private lateinit var toastAuthor: Toast

    override fun onCreate(savedInstanceState: Bundle?) {
        if (falseToken()){
            val intent = Intent(
                this,
                SignInActivity::class.java
            )
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            intent.putExtra("EXIT",true)
            startActivity(intent)
        }
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_community)

        setSupportActionBar(community_toolbar)
        community_toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
        supportActionBar!!.title = "Zaproś do quizu"

        if (intent.hasExtra(DetailQuizActivity.QUIZ_DETAILS)) {
            quizDetails =
                intent.getSerializableExtra(DetailQuizActivity.QUIZ_DETAILS)
                        as ReadQuizModel
        }

        toastCorrect = Toast.makeText(
            this,
            "Zaprosiłeś użytkownika do quizu",
            Toast.LENGTH_SHORT
        )

        toastIncorrect = Toast.makeText(
            this,
            "Użytkownik już otrzymał zaproszenie",
            Toast.LENGTH_SHORT
        )

        toastAuthor = Toast.makeText(
            this,
            "Użytkownik jest autorem quizu",
            Toast.LENGTH_SHORT
        )

        getAllFriends()
        friendsRecyclerView(friendsList)
    }

    private fun getAllFriends() = runBlocking {
        newSuspendedTransaction(Dispatchers.IO) {
            val list =
                Friend.find { (
                        (Friends.from eq currentUser!!.id) or
                        (Friends.to eq currentUser!!.id)) and
                        (Friends.status eq 1)
                }.with(Friend::to, Friend::from).toList()
            if (list.isNotEmpty())
                exposedToFriendModel(list)
        }
    }

    private fun exposedToFriendModel(l: List<Friend>) {
        for (i in l) {
            if (currentUser!!.id == i.from.id)
                friendsList.add(
                    ReadUsermodel(
                        id = i.to.id.value,
                        login = i.to.login,
                        profile_picture = i.to.profile_picture!!.bytes,
                        creation_time = i.to.creation_time.toString()
                    )
                )
            else
                friendsList.add(
                    ReadUsermodel(
                        id = i.from.id.value,
                        login = i.from.login,
                        profile_picture = i.from.profile_picture!!.bytes,
                        creation_time = i.from.creation_time.toString()
                    )
                )
        }
    }

    private fun sendInvitationToDataBase(userId: Int, quizId: Int): Boolean {
        return runBlocking {
            return@runBlocking newSuspendedTransaction(Dispatchers.IO) {
                val q = Quiz.findById(quizId)!!
                val t = User.findById(userId)!!
                if (q.user != t) {
                    QuizInvitation.new {
                        status = 0
                        from = currentUser!!
                        to = t
                        quiz = q
                        time_sent = LocalDateTime.now()
                    }
                    return@newSuspendedTransaction true
                } else
                    return@newSuspendedTransaction false
            }
        }
    }

    private fun isUserInvited(userId: Int, quizId: Int): Boolean {
        return runBlocking {
            val x = newSuspendedTransaction(Dispatchers.IO) {
                QuizInvitation.find {
                    (QuizInvitations.to eq userId) and
                        (QuizInvitations.quiz eq quizId)
                }.toList()
            }
            return@runBlocking !x.isEmpty()
        }
    }

    private fun friendsRecyclerView(friends: ArrayList<ReadUsermodel>) {
        community_rv_friends.layoutManager = LinearLayoutManager(this)
        community_rv_friends.setHasFixedSize(true)
        val friendsList = FriendsList(this, friends)
        community_rv_friends.adapter = friendsList

        friendsList.setOnClickListener(object : FriendsList.OnClickListener {

            override fun onClick(position: Int, model: ReadUsermodel) {
                val userID = model.id
                val quizID = quizDetails!!.id
                if (isUserInvited(userID, quizID)) {
                    toastIncorrect.show()
                } else {
                    if(sendInvitationToDataBase(userID, quizID)) {
                        toastCorrect.show()
                    }
                    else {
                        toastAuthor.show()
                    }
                }
                finish()
            }
        })
    }
}