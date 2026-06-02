package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cloned_apps")
data class CloneEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val packageName: String,
    val cloneNumber: Int,
    val colorHex: String,
    val isEnabled: Boolean = true,
    val isIsolated: Boolean = true,
    val fakeDeviceId: String = "",
    val fakeAndroidId: String = "",
    val createdTime: Long = System.currentTimeMillis()
)
