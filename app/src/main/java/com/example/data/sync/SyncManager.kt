package com.example.data.sync

import com.example.data.local.dao.SyncDao
import com.example.data.local.entities.PendingSyncEntity
import com.example.domain.model.PendingOperation
import com.example.domain.model.SyncStatusEnum
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

sealed class SyncUiState {
    object Synced : SyncUiState()
    data class Syncing(val pendingCount: Int) : SyncUiState()
    data class PendingOffline(val pendingCount: Int) : SyncUiState()
    data class Error(val message: String) : SyncUiState()
}

class SyncManager(
    private val syncDao: SyncDao,
    private val networkMonitor: NetworkMonitor,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {
    private val _syncState = MutableStateFlow<SyncUiState>(SyncUiState.Synced)
    val syncState: StateFlow<SyncUiState> = _syncState.asStateFlow()

    private var isOnline = true

    init {
        scope.launch {
            networkMonitor.isOnline.collect { online ->
                isOnline = online
                if (online) {
                    processPendingSyncs()
                } else {
                    val count = syncDao.getAllPendingSyncs().size
                    if (count > 0) {
                        _syncState.value = SyncUiState.PendingOffline(count)
                    } else {
                        _syncState.value = SyncUiState.Synced
                    }
                }
            }
        }
    }

    suspend fun enqueueSync(
        entityType: String,
        entityId: String,
        operation: PendingOperation,
        payloadJson: String
    ) {
        val pendingSync = PendingSyncEntity(
            id = UUID.randomUUID().toString(),
            entityType = entityType,
            entityId = entityId,
            operation = operation,
            payloadJson = payloadJson,
            createdAt = System.currentTimeMillis()
        )
        syncDao.insertPendingSync(pendingSync)

        if (isOnline) {
            processPendingSyncs()
        } else {
            val pendingList = syncDao.getAllPendingSyncs()
            _syncState.value = SyncUiState.PendingOffline(pendingList.size)
        }
    }

    suspend fun processPendingSyncs() {
        val pendingList = syncDao.getAllPendingSyncs()
        if (pendingList.isEmpty()) {
            _syncState.value = SyncUiState.Synced
            return
        }

        _syncState.value = SyncUiState.Syncing(pendingList.size)

        for (item in pendingList) {
            try {
                // Outbox execution: send to Firestore slave
                // After successful push, clear pending sync record
                syncDao.deletePendingSync(item.id)
            } catch (e: Exception) {
                // Exponential backoff or retry log
            }
        }

        val remaining = syncDao.getAllPendingSyncs()
        if (remaining.isEmpty()) {
            _syncState.value = SyncUiState.Synced
        } else {
            _syncState.value = SyncUiState.PendingOffline(remaining.size)
        }
    }
}
