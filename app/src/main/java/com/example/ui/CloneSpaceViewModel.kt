package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.CloneDatabase
import com.example.data.CloneEntity
import com.example.data.CloneRepository
import com.example.sandbox.SandboxController
import com.example.sandbox.SandboxLog
import com.example.utils.AppInfo
import com.example.utils.PackageScanner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CloneSpaceViewModel(application: Application) : AndroidViewModel(application) {

    private val database = CloneDatabase.getDatabase(application)
    private val repository = CloneRepository(database.cloneDao())

    // All active clones in sandbox
    val allClones: StateFlow<List<CloneEntity>> = repository.allClones
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val cloneCount: StateFlow<Int> = repository.cloneCount
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    // Scanner of installed packages
    private val _scannedApps = MutableStateFlow<List<AppInfo>>(emptyList())
    val scannedApps: StateFlow<List<AppInfo>> = _scannedApps.asStateFlow()

    // Sandboxing states
    private val sandboxController = SandboxController(application)
    val sandboxLogs: StateFlow<List<SandboxLog>> = sandboxController.logs
    val isSandboxConnected: StateFlow<Boolean> = sandboxController.isConnected

    // State for current selected clone executing in sandbox
    private val _activeRunningClone = MutableStateFlow<CloneEntity?>(null)
    val activeRunningClone: StateFlow<CloneEntity?> = _activeRunningClone.asStateFlow()

    // Loading & search state
    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    init {
        // Start scanner and binding
        scanInstalledApps()
        sandboxController.connect()
    }

    fun scanInstalledApps() {
        viewModelScope.launch {
            _isScanning.value = true
            val list = withContext(Dispatchers.IO) {
                PackageScanner.getScanList(getApplication())
            }
            _scannedApps.value = list
            _isScanning.value = false
        }
    }

    // Attempt to clone an app (max 100 clones limit)
    fun cloneApp(
        app: AppInfo,
        customName: String,
        customColorHex: String,
        isIsolated: Boolean,
        fakeDeviceId: String,
        fakeAndroidId: String,
        onLimitReached: () -> Unit,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            val count = allClones.value.size
            if (count >= 100) {
                onLimitReached()
                return@launch
            }

            // Determine clone index (highest index for this package + 1)
            val existingClonesForApp = repository.getClonesByPackage(app.packageName)
            val nextIndex = if (existingClonesForApp.isEmpty()) {
                1
            } else {
                existingClonesForApp.maxOf { it.cloneNumber } + 1
            }

            val finalName = customName.ifEmpty { "${app.name} Clone $nextIndex" }
            
            // Build entity
            val entity = CloneEntity(
                name = finalName,
                packageName = app.packageName,
                cloneNumber = nextIndex,
                colorHex = customColorHex,
                isIsolated = isIsolated,
                fakeDeviceId = fakeDeviceId,
                fakeAndroidId = fakeAndroidId,
                createdTime = System.currentTimeMillis()
            )

            withContext(Dispatchers.IO) {
                repository.insert(entity)
            }
            onSuccess()
        }
    }

    // Launch a clone inside the sandboxed isolated process
    fun launchClone(clone: CloneEntity) {
        _activeRunningClone.value = clone
        sandboxController.clearLogs()
        sandboxController.launchAppInSandbox(
            packageName = clone.packageName,
            cloneIndex = clone.cloneNumber,
            fakeId = clone.fakeDeviceId
        )
    }

    // Delete clone from system
    fun deleteClone(clone: CloneEntity) {
        viewModelScope.launch {
            if (_activeRunningClone.value?.id == clone.id) {
                _activeRunningClone.value = null
            }
            withContext(Dispatchers.IO) {
                repository.delete(clone)
            }
        }
    }

    fun clearConsole() {
        sandboxController.clearLogs()
    }

    fun stopActiveSandbox() {
        _activeRunningClone.value = null
        sandboxController.disconnect()
        // Reconnect so it's ready for next launch
        sandboxController.connect()
    }

    override fun onCleared() {
        sandboxController.disconnect()
        super.onCleared()
    }
}
