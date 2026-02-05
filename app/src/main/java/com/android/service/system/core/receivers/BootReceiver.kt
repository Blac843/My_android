package com.android.service.system.core.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat
import com.android.service.system.core.services.CoreSyncService

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // यह कोड तब चलता है जब फोन स्विच ऑफ होकर ऑन होता है
        if (intent.action == Intent.ACTION_BOOT_COMPLETED || 
            intent.action == "android.intent.action.QUICKBOOT_POWERON") {
            
            Log.d("SystemCore", "Boot Completed. Reviving Service...")
            val serviceIntent = Intent(context, CoreSyncService::class.java)
            ContextCompat.startForegroundService(context, serviceIntent)
        }
    }
}
