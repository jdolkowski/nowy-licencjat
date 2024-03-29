package com.example.licencjat_projekt.Projekt.utils

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.licencjat_projekt.Projekt.Models.ReadQuizInvitationModel
import com.example.licencjat_projekt.Projekt.database.Quiz
import com.example.licencjat_projekt.Projekt.database.User
import com.example.licencjat_projekt.R
import kotlinx.android.synthetic.main.item_quiz_message.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction


open class QuizInviteList(
    private val context: Context,
    private var listOfFriendInvitations: ArrayList<ReadQuizInvitationModel>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var onClickListener: OnClickListener? = null
    private var onAcceptClickListener: OnAcceptClickListener? = null
    private var onDeclineClickListener: OnDeclineClickListener? = null

    private class OwnViewHolder(
        view: View
    ) : RecyclerView.ViewHolder(view)


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return OwnViewHolder(
            LayoutInflater.from(context).inflate(
                R.layout.item_quiz_message, parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val ptr = listOfFriendInvitations[position]
        if (holder is OwnViewHolder) {
            val user = getUser(ptr.fromUser)
            val quiz = getQuiz(ptr.quizID)
            holder.itemView.item_quiz_invite_quiz_name.text = quiz!!.title
            holder.itemView.item_quiz_invite_name.text = StringBuilder("od: ").append(user!!.login).toString()

            holder.itemView.item_quiz_accept_btn.setOnClickListener {
                if (onAcceptClickListener != null){
                    onAcceptClickListener!!.onClick(position, ptr)
                }
            }
            holder.itemView.item_quiz_decline_btn.setOnClickListener {
                if (onDeclineClickListener != null){
                    onDeclineClickListener!!.onClick(position, ptr)
                }
            }
        }

        //passing which position was clicked on rv
        //passing ptr
        holder.itemView.setOnClickListener {
            if (onClickListener != null) {
                onClickListener!!.onClick(position, ptr)
            }
        }
    }
    private fun getQuiz(quizID:Int)= runBlocking {
        newSuspendedTransaction(Dispatchers.IO) {
            Quiz.findById(quizID)
        }
    }

    private fun getUser(userID: Int)= runBlocking {
        newSuspendedTransaction(Dispatchers.IO) {
            User.findById(userID)
        }
    }
    override fun getItemCount(): Int {
        return listOfFriendInvitations.size
    }

    fun setOnClickListener(onClickListener: OnClickListener, onAcceptClickListener: OnAcceptClickListener, onDeclineClickListener: OnDeclineClickListener) {
        this.onClickListener = onClickListener
        this.onAcceptClickListener = onAcceptClickListener
        this.onDeclineClickListener = onDeclineClickListener
    }

    interface OnClickListener {
        fun onClick(position: Int, model: ReadQuizInvitationModel)
    }

    interface OnAcceptClickListener {
        fun onClick(position: Int, model: ReadQuizInvitationModel)
    }

    interface OnDeclineClickListener {
        fun onClick(position: Int, model: ReadQuizInvitationModel)
    }
}