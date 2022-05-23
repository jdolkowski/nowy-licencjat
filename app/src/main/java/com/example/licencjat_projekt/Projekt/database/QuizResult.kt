package com.example.licencjat_projekt.Projekt.database

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.`java-time`.time

class QuizeResult(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<QuizeResult>(QuizeResults)

    var points by QuizeResults.points
    var user by User referencedOn QuizeResults.user
    var quiz by Quiz referencedOn QuizeResults.quiz
}

object QuizeResults : IntIdTable("QuizResults") {
    val points = double("points")
    val user = reference("user",Users, onDelete = ReferenceOption.CASCADE)
    val quiz = reference("quiz",Quizes, onDelete = ReferenceOption.CASCADE)

}