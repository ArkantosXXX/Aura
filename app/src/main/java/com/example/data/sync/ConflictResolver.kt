package com.example.data.sync

import com.example.data.local.entities.TaskEntity

class ConflictResolver {

    /**
     * Resolves entity conflicts between local Room database and remote Firestore slave.
     * Uses version number + lastModifiedAt comparison.
     */
    fun resolveTaskConflict(localTask: TaskEntity, remoteTask: TaskEntity, isAdminOverride: Boolean = false): TaskEntity {
        if (isAdminOverride) return remoteTask

        if (localTask.version > remoteTask.version) {
            return localTask
        } else if (remoteTask.version > localTask.version) {
            return remoteTask
        }

        // Version numbers equal: comparison by lastModifiedAt (last-write-wins)
        return if (localTask.modifiedAt >= remoteTask.modifiedAt) {
            localTask
        } else {
            remoteTask
        }
    }
}
