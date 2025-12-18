package com.epilabs.epiguard.models

data class SeizureModel(
    val seizureID: Int = 0,
    val userID: Int,
    val timestamp: String,
    val seizureType: String?,
    val duration: Int?,
    val description: String?,
    val triggers: String?,
    val medicationTaken: String?,
    val postSeizureSymptoms: String?,
    val notes: String?
)


