package com.example.licencjat_projekt.Projekt.Models

data class ReadAnswerModel(
    var answer_text: String,
    var answer_image: ByteArray,
    var is_Correct: Boolean = false
)