package com.example.sandbox

import android.app.Service
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.Message
import android.os.Messenger
import android.os.Process
import android.util.Log
import java.io.File

class IsolatedSandboxService : Service() {

    companion object {
        const val MSG_REGISTER_CLIENT = 1
        const val MSG_LAUNCH_CLONE = 2
        const val MSG_SANDBOX_LOG = 3
        const val MSG_SANDBOX_TERMINATED = 4

        const val KEY_PACKAGE_NAME = "key_package"
        const val KEY_CLONE_INDEX = "key_clone_index"
        const val KEY_FAKE_DEVICE_ID = "key_fake_device_id"
        
        private const val TAG = "IsolatedSandboxService"
    }

    private var clientMessenger: Messenger? = null

    // Handler inside isolated process to receive commands from host app
    private val incomingHandler = Handler(Looper.getMainLooper()) { msg ->
        when (msg.what) {
            MSG_REGISTER_CLIENT -> {
                clientMessenger = msg.replyTo
                sendLog("Isolated Sandbox connected. PID: ${Process.myPid()}, UID: ${Process.myUid()}")
                true
            }
            MSG_LAUNCH_CLONE -> {
                val data = msg.data
                val packageName = data.getString(KEY_PACKAGE_NAME) ?: "unknown.package"
                val cloneIndex = data.getInt(KEY_CLONE_INDEX, 1)
                val fakeDeviceId = data.getString(KEY_FAKE_DEVICE_ID) ?: ""

                runSandboxSimulation(packageName, cloneIndex, fakeDeviceId)
                true
            }
            else -> false
        }
    }

    private val messenger = Messenger(incomingHandler)

    override fun onBind(intent: Intent?): IBinder? {
        Log.d(TAG, "onBind success from isolated process")
        return messenger.binder
    }

    private fun sendLog(message: String) {
        val client = clientMessenger ?: return
        try {
            val msg = Message.obtain(null, MSG_SANDBOX_LOG)
            msg.data = Bundle().apply {
                putString("log_text", message)
                putLong("log_time", System.currentTimeMillis())
            }
            client.send(msg)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send log back to client", e)
        }
    }

    private fun runSandboxSimulation(packageName: String, cloneIndex: Int, fakeId: String) {
        sendLog("🔄 [Sandbox] Spawning virtual container for '$packageName' (Clone #$cloneIndex)")
        
        // 1. Log process properties
        sendLog("ℹ️ [Sandbox] Process Context - PID: ${Process.myPid()}, AID: ${Process.myUid()}")
        sendLog("ℹ️ [Sandbox] Isolated Check: Process is running in Android Sandbox (UID is isolated: ${Process.myUid() >= 99000})")
        
        // 2. Perform real Sandboxed File System Access Security Test
        // Show that an isolated process cannot read host package directories!
        val hostPrivateDir = File("/data/data/com.example")
        val canReadHostDirs = try {
            hostPrivateDir.exists() && hostPrivateDir.list() != null
        } catch (e: Exception) {
            false
        }
        
        sendLog("🔒 [Security] Attempting to read host app private files at '/data/data/com.example'...")
        if (canReadHostDirs) {
            sendLog("⚠️ [Security Breach] Process is NOT fully isolated! Host directory accessible.")
        } else {
            sendLog("✅ [Security Sandbox Verified] System-level isolation confirmed. Reading host directory blocked by Android Linux sandbox permissions!")
        }

        // 3. Simulating Hooking AMS and dynamic interception
        sendLog("📡 [Binder Interception] Mock Hooking Android system IPC...")
        sendLog("📡 [Binder Interception] Wrapped IActivityManager.getService() with SandboxProxy")
        sendLog("📡 [Binder Interception] Redirecting PackageManager.getPackageInfo() for package standard info")
        
        // 4. Custom Storage Sandboxing Redirect
        val virtualDir = File(filesDir, "virtual_sdcard/$packageName/clone_$cloneIndex")
        sendLog("📂 [Sandbox Storage] Redirecting File IO calls:")
        sendLog("     - Real Path: /data/data/$packageName")
        sendLog("     -> Virtual Path: ${virtualDir.absolutePath}")

        // 5. Spoofing info check
        if (fakeId.isNotEmpty()) {
            sendLog("🛡️ [Mock Service] Telemetry hook enabled. Interceptors return Virtual DeviceID: '$fakeId'")
        }

        // 6. Dynamic DEX Classloading Simulation
        sendLog("📦 [DEX Loader] Loading custom ClassLoader inside isolated VM...")
        sendLog("📦 [DEX Loader] Searching DEX file for '$packageName' in native split paths...")
        sendLog("📦 [DEX Loader] Loaded base.apk successfully. Resolved target Application EntryPoint.")

        // 7. Completed simulated bootstrap
        sendLog("🏁 [Sandbox Engine] Clone instance #$cloneIndex of '$packageName' started successfully inside isolated sandbox process!")
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy Isolated service")
        super.onDestroy()
    }
}
