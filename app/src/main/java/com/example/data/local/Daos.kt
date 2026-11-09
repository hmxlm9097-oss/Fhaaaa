package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CloudPhoneDao {
    @Query("SELECT * FROM cloud_phones ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<CloudPhoneEntity>>

    @Query("SELECT * FROM cloud_phones WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): CloudPhoneEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(device: CloudPhoneEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(devices: List<CloudPhoneEntity>)

    @Query("DELETE FROM cloud_phones WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("UPDATE cloud_phones SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: String, status: String)
}

@Dao
interface ActivityLogDao {
    @Query("SELECT * FROM activity_logs ORDER BY timestamp DESC LIMIT 200")
    fun observeRecent(): Flow<List<ActivityLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(log: ActivityLogEntity)

    @Query("DELETE FROM activity_logs")
    suspend fun clearAll()
}

@Dao
interface LocationHistoryDao {
    @Query("SELECT * FROM location_history ORDER BY timestamp DESC LIMIT 100")
    fun observeAll(): Flow<List<LocationHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: LocationHistoryEntity)

    @Query("DELETE FROM location_history")
    suspend fun clearAll()
}

@Dao
interface FileTransferDao {
    @Query("SELECT * FROM file_transfers ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<FileTransferEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: FileTransferEntity)

    @Query("DELETE FROM file_transfers WHERE id = :id")
    suspend fun deleteById(id: String)
}

@Dao
interface DeviceSessionDao {
    @Query("SELECT * FROM device_sessions ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<DeviceSessionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(session: DeviceSessionEntity)

    @Query("UPDATE device_sessions SET approvalState = :state WHERE id = :id")
    suspend fun updateApprovalState(id: String, state: String)

    @Query("UPDATE device_sessions SET approvalState = 'DISCONNECTED' WHERE approvalState != 'DISCONNECTED'")
    suspend fun disconnectAll()
}
