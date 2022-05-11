package com.example.licencjat_projekt.Projekt.utils

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.recyclerview.widget.RecyclerView
import com.example.licencjat_projekt.Projekt.Models.FriendInvitationModel
import com.example.licencjat_projekt.R
import kotlinx.android.synthetic.main.item_message.view.*

open class FriendInvite(
    private val context: Context,
    private var listOfFriendInvites: ArrayList<FriendInvitationModel>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var onClickListener: OnClickListener? = null

    private class OwnViewHolder(
        view: View
    ) : RecyclerView.ViewHolder(view)


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return OwnViewHolder(
            LayoutInflater.from(context).inflate(
                R.layout.item_message, parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val ptr = listOfFriendInvites[position]
        if (holder is OwnViewHolder) {
            holder.itemView.item_invite_name.text = ptr.fromUser
            //passing which position was clicked on rv
            //passing ptr
            holder.itemView.setOnClickListener {
                if (onClickListener != null) {
                    onClickListener!!.onClick(position, ptr)
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return listOfFriendInvites.size
    }

    fun setOnClickListener(onClickListener: OnClickListener) {
        this.onClickListener = onClickListener
    }

    interface OnClickListener {
        fun onClick(position: Int, model: FriendInvitationModel)
    }

    inner class InvitationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        private var acceptBtn: Button =  itemView.findViewById(R.id.item_accept_btn)
        private var declineBtn: Button =  itemView.findViewById(R.id.item_decline_btn)

        fun updateInvitation(invitation: FriendInvitationModel){
            acceptBtn.setOnClickListener {
                //TODO: add both users to friend list in DB
            }
            declineBtn.setOnClickListener{
                //TODO: delete both users from friend list in DB
                deleteEvent(invitation)
            }
        }
    }

    private fun deleteEvent(invitation: FriendInvitationModel) {
        listOfFriendInvites.filterTo(listOfFriendInvites) { it != invitation }
    }
}