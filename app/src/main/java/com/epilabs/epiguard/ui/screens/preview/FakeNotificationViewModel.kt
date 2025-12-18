package com.epilabs.epiguard.ui.screens.preview

import androidx.lifecycle.ViewModel
import com.epilabs.epiguard.data.repo.NotificationRepository
import com.epilabs.epiguard.models.Notification

class FakeNotificationViewModel : ViewModel() {
    val notifications = listOf(
        Notification(
            contactID = 1,
            userID = 123, // fake user id
            message = "Possible seizure detected at 22:00",
            timestamp = System.currentTimeMillis(),
            isRead = false
        ),
        Notification(
            contactID = 2,
            userID = 123,
            message = "Update your contact list",
            timestamp = System.currentTimeMillis() - 3600000,
            isRead = true
        )
    )
}
