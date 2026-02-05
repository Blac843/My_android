package com.android.service.system.core

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.android.service.system.core.services.CoreSyncService

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // AndroidX UI Setup (No XML needed)
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 100, 50, 50)
            setBackgroundColor(0xFFFFFFFF.toInt()) // White Background
        }

        val title = TextView(this).apply {
            text = "System Core Configuration"
            textSize = 22f
            setPadding(0, 0, 0, 60)
            setTextColor(0xFF000000.toInt())
        }

        // STEP 1: Permission
        val btnAccess = Button(this).apply {
            text = "1. Grant Notification Access"
            setOnClickListener {
                startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
            }
        }

        // STEP 2: Immortality (Battery)
        val btnBattery = Button(this).apply {
            text = "2. Disable Battery Optimization"
            setOnClickListener {
                try {
                    val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
                    intent.data = Uri.parse("package:$packageName")
                    startActivity(intent)
                } catch (e: Exception) {
                    Toast.makeText(context, "Go to Settings > Apps > System Core > Battery", Toast.LENGTH_LONG).show()
                }
            }
        }

        // STEP 3: Manual Start
        val btnStart = Button(this).apply {
            text = "3. Start Service Now"
            setOnClickListener {
                val serviceIntent = Intent(this@MainActivity, CoreSyncService::class.java)
                ContextCompat.startForegroundService(this@MainActivity, serviceIntent)
                Toast.makeText(context, "Service Started in Background", Toast.LENGTH_SHORT).show()
            }
        }

        layout.addView(title)
        layout.addView(btnAccess)
        layout.addView(btnBattery)
        layout.addView(btnStart)
        
        setContentView(layout)
    }
}
