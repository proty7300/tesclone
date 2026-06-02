package com.example.sandbox

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.Message
import android.os.Messenger
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class SandboxLog(
    val id: String = java.util.UUID.randomUUID().toString(),
    val message: String,
    val timestamp: Long
)

class SandboxController(private val context: Context) {

    private val TAG = "SandboxController"

    private val _logs = MutableStateFlow<List<SandboxLog>>(emptyList())
    val logs: StateFlow<List<SandboxLog>> = _logs.asStateFlow()

    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    private var serviceMessenger: Messenger? = null
    private var isBound = false

    // Incoming messenger in host app to receive logs from isolated process
    private val incomingHandler = Handler(Looper.getMainLooper()) { msg ->
        when (msg.what) {
            IsolatedSandboxService.MSG_SANDBOX_LOG -> {
                val data = msg.data
                val logText = data.getString("log_text") ?: ""
                val logTime = data.getLong("log_time", System.currentTimeMillis())
                
                appendLog(logText, logTime)
                true
            }
            IsolatedSandboxService.MSG_SANDBOX_TERMINATED -> {
                appendLog("🔴 [Sandbox] Process terminated.", System.currentTimeMillis())
                _isConnected.value = false
                true
            }
            else -> false
        }
    }

    private val hostMessenger = Messenger(incomingHandler)

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            serviceMessenger = Messenger(service)
            isBound = true
            _isConnected.value = true
            
            // Register this host messenger with the isolated process
            try {
                val msg = Message.obtain(null, IsolatedSandboxService.MSG_REGISTER_CLIENT)
                msg.replyTo = hostMessenger
                serviceMessenger?.send(msg)
                appendLog("🔗 [Host Engine] Binded to Isolated Process. Handshake initialized.", System.currentTimeMillis())
            } catch (e: Exception) {
                appendLog("❌ [Host Engine] Handshake failed: ${e.message}", System.currentTimeMillis())
                Log.e(TAG, "Error in service connection registration", e)
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            serviceMessenger = null
            isBound = false
            _isConnected.value = false
            appendLog("⚠️ [Host Engine] Isolated process disconnected abruptly.", System.currentTimeMillis())
        }
    }

    private fun appendLog(text: String, time: Long) {
        val newLog = SandboxLog(message = text, timestamp = time)
        _logs.value = _logs.value + newLog
    }

    fun clearLogs() {
        _logs.value = emptyList()
    }

    fun connect() {
        if (!isBound) {
            val intent = Intent(context, IsolatedSandboxService::class.java)
            context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
            appendLog("⚡ [Host Engine] Spawning isolated sandboxed thread...", System.currentTimeMillis())
        }
    }

    fun disconnect() {
        if (isBound) {
            try {
                context.unbindService(serviceConnection)
            } catch (e: Exception) {
                Log.e(TAG, "Error unbinding service", e)
            }
            isBound = false
            serviceMessenger = null
            _isConnected.value = false
            appendLog("🔌 [Host Engine] Isolated Sandbox service destroyed.", System.currentTimeMillis())
        }
    }

    fun launchAppInSandbox(packageName: String, cloneIndex: Int, fakeId: String) {
        if (!isBound || serviceMessenger == null) {
            appendLog("⚠️ [Host Engine] Isolated process is offline. Reconnecting...", System.currentTimeMillis())
            connect()
            // Schedule the launch after binding occurs
            Handler(Looper.getMainLooper()).postDelayed({
                sendLaunchMessage(packageName, cloneIndex, fakeId)
            }, 1000)
        } else {
            sendLaunchMessage(packageName, cloneIndex, fakeId)
        }
    }

    private fun sendLaunchMessage(packageName: String, cloneIndex: Int, fakeId: String) {
        try {
            val msg = Message.obtain(null, IsolatedSandboxService.MSG_LAUNCH_CLONE)
            msg.data = Bundle().apply {
                putString(IsolatedSandboxService.KEY_PACKAGE_NAME, packageName)
                putInt(IsolatedSandboxService.KEY_CLONE_INDEX, cloneIndex)
                putString(IsolatedSandboxService.KEY_FAKE_DEVICE_ID, fakeId)
            }
            serviceMessenger?.send(msg)
        } catch (e: Exception) {
            appendLog("❌ [Host Engine] Failed to dispatch launch event: ${e.message}", System.currentTimeMillis())
        }
    }
}
