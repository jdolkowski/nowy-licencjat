package com.example.licencjat_projekt.Projekt.Activities

import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.licencjat_projekt.Projekt.Models.ReadUsermodel
import com.example.licencjat_projekt.Projekt.database.*
import com.example.licencjat_projekt.Projekt.utils.FriendsList
import com.example.licencjat_projekt.Projekt.utils.UsersList
import com.example.licencjat_projekt.Projekt.utils.currentUser
import com.example.licencjat_projekt.Projekt.utils.falseToken
import com.example.licencjat_projekt.R
import kotlinx.android.synthetic.main.activity_community.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.dao.with
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.lowerCase
import org.jetbrains.exposed.sql.or
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

class CommunityActivity : AppCompatActivity(), View.OnClickListener {
    private var searchUserString: String? = null
    private var friendsList = ArrayList<ReadUsermodel>()
    private var usersList = ArrayList<ReadUsermodel>()
    private lateinit var layoutColorInitial: Drawable
    private lateinit var friendsLinearLayout: LinearLayout
    companion object {
        var PROFILE_DETAILS = "profile_details"
    }
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

        friendsLinearLayout = findViewById(R.id.community_own_friends_linear)
        layoutColorInitial = friendsLinearLayout.background

        setSupportActionBar(community_toolbar)
        community_toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
        supportActionBar!!.title = "Społeczność"

        val colorDrawable = ColorDrawable(
            ContextCompat.getColor(
                this,
                R.color.purple_05
            )
        )
        community_toolbar.background = colorDrawable

        community_own_friends_linear.setOnClickListener(this)
        community_search_btn.setOnClickListener(this)

        getAllFriends()
        friendsRecyclerView(friendsList)
    }

    private fun getAllFriends() = runBlocking {
        newSuspendedTransaction(Dispatchers.IO) {
            val list =
                Friend.find { ((Friends.from eq currentUser!!.id) or
                        (Friends.to eq currentUser!!.id)) and
                        (Friends.status eq 1)
                }.with(Friend::to, Friend::from).toList()
            if (list.isNotEmpty())
                exposedToFriendModel(list)
        }
    }

    private fun getLikeUsers() = runBlocking {
        newSuspendedTransaction(Dispatchers.IO) {
            val list = Friend.find { (
                    (Friends.from eq currentUser!!.id) and
                    (Friends.status eq 1)) or
                    ((Friends.to eq currentUser!!.id) and
                    (Friends.status eq 1))
            }.toList()
            val fL = list.map { it.to.id }
            val fL1 = list.map { it.from.id }
            val x =
                User.find { (Users.login.lowerCase() like
                        "$searchUserString%") and
                        (Users.id notInList fL + fL1) and
                        (Users.id neq currentUser!!.id)
                }.toList()
            exposedToUserModel(x)
        }
    }

    private fun exposedToUserModel(l: List<User>) {
        for (i in l) {
            usersList.add(
                ReadUsermodel(
                    id = i.id.value,
                    login = i.login,
                    profile_picture = i.profile_picture!!.bytes,
                    creation_time = i.creation_time.toString()
                )
            )
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

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.community_toolbar_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id: Int = item.itemId
        if (id == R.id.action_community_toolbar_add) {
            community_search_linear.visibility =
                if (community_search_linear.visibility == View.VISIBLE) {
                    View.GONE
                } else {
                    View.VISIBLE
                }
            community_own_friends_linear.background =
                if (community_own_friends_linear.background != layoutColorInitial) {
                    layoutColorInitial
                } else {
                    ColorDrawable(
                            ContextCompat.getColor(
                            this,
                            R.color.gray_tint
                        )
                    )
                }
            return true
        }
        return if (id == R.id.action_community_toolbar_invitation) {

            val intent = Intent(
                this,
                CommunityInviteMsg::class.java
            )
            startActivity(intent)
            finish()
            return true

        } else super.onOptionsItemSelected(item)
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.community_own_friends_linear -> {
                community_search_linear.visibility = View.GONE
            }
            R.id.community_search_btn -> {
                searchUserString = search_et.text.toString()
                if (searchUserString!!.isNotEmpty()) {
                    usersList.clear()
                    searchUserString = searchUserString!!.lowercase()
                    getLikeUsers()
                    usersRecyclerView(usersList)
                }
            }
        }
    }

    private fun friendsRecyclerView(friends: ArrayList<ReadUsermodel>) {
        community_rv_friends.layoutManager = LinearLayoutManager(this)
        community_rv_friends.setHasFixedSize(true)
        val friendsList = FriendsList(this, friends)
        community_rv_friends.adapter = friendsList

        friendsList.setOnClickListener(object : FriendsList.OnClickListener {

            override fun onClick(position: Int, model: ReadUsermodel) {

                val intent = Intent(
                    this@CommunityActivity,
                    ProfileFriendActivity::class.java
                )

                intent.putExtra(PROFILE_DETAILS, model)
                startActivity(intent)
            }
        })
    }

    private fun usersRecyclerView(users: ArrayList<ReadUsermodel>) {
        community_rv_users.layoutManager = LinearLayoutManager(this)
        community_rv_users.setHasFixedSize(true)
        val usersList = UsersList(this, users)
        community_rv_users.adapter = usersList

        usersList.setOnClickListener(object : UsersList.OnClickListener {

            override fun onClick(position: Int, model: ReadUsermodel) {
                val intent = Intent(
                    this@CommunityActivity,
                    ProfileActivityAdd::class.java
                )

                intent.putExtra(PROFILE_DETAILS, model)
                startActivity(intent)
            }
        })
    }
}