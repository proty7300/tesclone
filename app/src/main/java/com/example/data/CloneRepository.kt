package com.example.data

import kotlinx.coroutines.flow.Flow

class CloneRepository(private val cloneDao: CloneDao) {
    val allClones: Flow<List<CloneEntity>> = cloneDao.getAllClones()
    val cloneCount: Flow<Int> = cloneDao.getCloneCount()

    suspend fun getClonesByPackage(packageName: String): List<CloneEntity> {
        return cloneDao.getClonesByPackage(packageName)
    }

    suspend fun insert(clone: CloneEntity): Long {
        return cloneDao.insertClone(clone)
    }

    suspend fun update(clone: CloneEntity) {
        cloneDao.updateClone(clone)
    }

    suspend fun delete(clone: CloneEntity) {
        cloneDao.deleteClone(clone)
    }

    suspend fun deleteById(id: Int) {
        cloneDao.deleteCloneById(id)
    }
}
