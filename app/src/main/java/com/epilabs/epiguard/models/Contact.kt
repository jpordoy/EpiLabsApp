package com.epilabs.epiguard.models

data class Contact(
    val contactId: String = "",
    val userId: String = "", // Owner of this contact
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val contactNumber: String = "",
    val isSystemUser: Boolean = false, // Is this contact also a user of our system?
    val linkedUserId: String = "", // If isSystemUser = true, their userId
    val createdAt: Long = System.currentTimeMillis()
)

