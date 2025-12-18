package com.epilabs.epiguard.data.repo

import com.epilabs.epiguard.models.Contact
import com.epilabs.epiguard.utils.Result
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class ContactRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    suspend fun getContacts(): Result<List<Contact>> {
        return try {
            val currentUser = auth.currentUser ?: return Result.Error("Not authenticated")

            val querySnapshot = firestore.collection("contacts")
                .whereEqualTo("userId", currentUser.uid)
                .get()
                .await()

            val contacts = querySnapshot.documents.mapNotNull { doc ->
                doc.toObject(Contact::class.java)?.copy(contactId = doc.id)
            }

            Result.Success(contacts)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to get contacts")
        }
    }

    suspend fun addContact(contact: Contact): Result<String> {
        return try {
            val currentUser = auth.currentUser ?: return Result.Error("Not authenticated")

            val contactWithUserId = contact.copy(userId = currentUser.uid)
            val documentRef = firestore.collection("contacts").add(contactWithUserId).await()

            Result.Success(documentRef.id)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to add contact")
        }
    }

    suspend fun updateContact(contact: Contact): Result<Unit> {
        return try {
            firestore.collection("contacts")
                .document(contact.contactId)
                .set(contact)
                .await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to update contact")
        }
    }

    suspend fun deleteContact(contactId: String): Result<Unit> {
        return try {
            firestore.collection("contacts")
                .document(contactId)
                .delete()
                .await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to delete contact")
        }
    }

    suspend fun linkContactToUser(contactId: String, userEmail: String): Result<Unit> {
        return try {
            // Find user by email
            val userQuery = firestore.collection("users")
                .whereEqualTo("email", userEmail)
                .limit(1)
                .get()
                .await()

            if (userQuery.documents.isEmpty()) {
                return Result.Error("User not found with email: $userEmail")
            }

            val linkedUserId = userQuery.documents[0].id

            // Update contact with linked user info
            firestore.collection("contacts")
                .document(contactId)
                .update(mapOf(
                    "isSystemUser" to true,
                    "linkedUserId" to linkedUserId
                ))
                .await()

            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to link contact to user")
        }
    }
}