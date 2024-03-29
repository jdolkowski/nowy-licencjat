package com.example.licencjat_projekt.Projekt.Activities

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.licencjat_projekt.Projekt.Models.ReadUsermodel
import com.example.licencjat_projekt.Projekt.database.*
import com.example.licencjat_projekt.Projekt.utils.currentUser
import com.example.licencjat_projekt.Projekt.utils.falseToken
import com.example.licencjat_projekt.R
import kotlinx.android.synthetic.main.activity_profile.*
import kotlinx.android.synthetic.main.activity_profile_add.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.or
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

class ProfileActivityAdd : AppCompatActivity(), View.OnClickListener {
    private var visitatedUser: ReadUsermodel? = null
    private var invitationSend: Boolean = false
    private var maxFriends: Boolean = false
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
        setContentView(R.layout.activity_profile_add)
        setSupportActionBar(profileadd_toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        profileadd_toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        if (intent.hasExtra(CommunityActivity.PROFILE_DETAILS)) {
            visitatedUser =
                intent.getSerializableExtra(CommunityActivity.PROFILE_DETAILS)
                        as ReadUsermodel
        }

        profile_add_add_friend.setOnClickListener(this)

        supportActionBar!!.title = ""
        if(visitatedUser != null){
            profileadd_main_login.text = visitatedUser!!.login
            profileadd_image.setImageBitmap(
                byteArrayToBitmap(visitatedUser!!.profile_picture)
            )
            profileadd_quiz_taken.text = userQuizTaken()
        }
        checkIfInvitationSend()
        checkNOFriends()
        if(invitationSend){
            profile_add_add_friend.text = "Zaproszenie zostało wysłane..."
        }
    }

    private fun sendFriendInvitationToDatabase() = runBlocking {
        newSuspendedTransaction(Dispatchers.IO) {
            Friend.new {
                status = 0
                from = currentUser!!
                to = User.findById(visitatedUser!!.id)!!
            }
        }
    }


    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.profile_add_add_friend -> {
                if(!invitationSend && !maxFriends) {
                    sendFriendInvitationToDatabase()
                    Toast.makeText(
                        this,
                        "Zaprosiłeś użytkownika do znajomych.",
                        Toast.LENGTH_LONG
                    ).show()
                    finish()
                }else if(maxFriends){
                    Toast.makeText(
                        this,
                        "Maksymalna ilość znajomych (50) osiagnięta!",
                        Toast.LENGTH_LONG
                    ).show()
                }
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

    private fun userQuizTaken(): String {
        return runBlocking {
            return@runBlocking newSuspendedTransaction(Dispatchers.IO) {
                return@newSuspendedTransaction QuizeResult.find {
                    QuizeResults.by eq visitatedUser!!.id
                }.count().toString()
            }
        }
    }
    private fun checkIfInvitationSend()= runBlocking{
        newSuspendedTransaction(Dispatchers.IO) {
            invitationSend = !Friend.find { (
                    (Friends.to eq visitatedUser!!.id) and
                    (Friends.from eq currentUser!!.id)) or
                    ((Friends.from eq visitatedUser!!.id) and
                            (Friends.to eq currentUser!!.id))
            }.empty()
        }
    }
    private fun checkNOFriends() =runBlocking{
        newSuspendedTransaction(Dispatchers.IO) {
            maxFriends = Friend.find { (
                    (Friends.to eq currentUser!!.id) or
                    (Friends.from eq currentUser!!.id)) and
                    (Friends.status eq 1)
            }.count()>=50
        }
    }
}