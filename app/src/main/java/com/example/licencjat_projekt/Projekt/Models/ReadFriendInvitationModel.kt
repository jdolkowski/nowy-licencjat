package com.example.licencjat_projekt.Projekt.Models

import com.example.licencjat_projekt.Projekt.database.User
import java.io.Serializable

class ReadFriendInvitationModel(
    var fromUser: User?,
    var toUser: User?,
    var status: Int,
    ): Serializable