package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CloneDao {
    @Query("SELECT * FROM cloned_apps ORDER BY createdTime DESC")
    fun getAllClones(): Flow<List<CloneEntity>>

    @Query("SELECT * FROM cloned_apps WHERE packageName = :packageName")
    suspend fun getClonesByPackage(packageName: String): List<CloneEntity>

    @Query("SELECT COUNT(*) FROM cloned_apps")
    fun getCloneCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClone(clone: CloneEntity): Long

    @Update
    suspend fun updateClone(clone: CloneEntity)

    @Delete
    suspend fun deleteClone(clone: CloneEntity)

    @Query("DELETE FROM cloned_apps WHERE id = :id")
    suspend fun deleteCloneById(id: Int)
}
