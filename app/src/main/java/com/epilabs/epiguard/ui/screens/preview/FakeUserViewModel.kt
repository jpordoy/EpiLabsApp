package com.epilabs.epiguard.ui.screens.preview
// --- PREVIEW ONLY FAKE VMs --- //
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class FakeUserViewModel : ViewModel() {
    val currentUser: StateFlow<PreviewUser?> =
        MutableStateFlow(PreviewUser(userId = "123", firstName = "Preview User"))
}
