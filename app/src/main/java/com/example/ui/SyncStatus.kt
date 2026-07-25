package com.example.ui

sealed class SyncStatus {
    object Idle : SyncStatus()
    object Syncing : SyncStatus()
    object Success : SyncStatus()
    object WaitingForInternet : SyncStatus()
    data class Failed(val error: String) : SyncStatus()
}

enum class SyncOption {
    MERGE,
    LOCAL_ONLY,
    CLOUD_ONLY
}
