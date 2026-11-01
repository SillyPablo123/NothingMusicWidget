package com.nothing.musicwidget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationManagerCompat

class MainActivity : AppCompatActivity() {

    private lateinit var tvStatus: TextView
    private lateinit var btnPermission: Button
    private lateinit var btnPinWidget: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvStatus = findViewById(R.id.tv_status)
        btnPermission = findViewById(R.id.btn_permission)
        btnPinWidget = findViewById(R.id.btn_pin_widget)

        btnPermission.setOnClickListener {
            startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
        }

        btnPinWidget.setOnClickListener {
            pinWidgetToHomeScreen()
        }
    }

    override fun onResume() {
        super.onResume()
        updatePermissionStatus()
    }

    private fun updatePermissionStatus() {
        val isGranted = NotificationManagerCompat.getEnabledListenerPackages(this).contains(packageName)
        if (isGranted) {
            tvStatus.text = "ESTADO: LISTO ✓ (Detectando música)"
            tvStatus.setTextColor(getColor(R.color.nothing_green))
            btnPermission.text = "Permiso Concedido ✓"
            btnPermission.isEnabled = false
            btnPermission.alpha = 0.6f
        } else {
            tvStatus.text = "ESTADO: Requiere permiso para leer música de Spotify / Sistema"
            tvStatus.setTextColor(getColor(R.color.nothing_red))
            btnPermission.text = "Conceder Acceso a Música"
            btnPermission.isEnabled = true
            btnPermission.alpha = 1.0f
        }
    }

    private fun pinWidgetToHomeScreen() {
        val appWidgetManager = AppWidgetManager.getInstance(this)
        val myProvider = ComponentName(this, NothingMusicWidgetProvider::class.java)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (appWidgetManager.isRequestPinAppWidgetSupported) {
                val successCallback = PendingIntent.getBroadcast(
                    this,
                    0,
                    Intent(this, NothingMusicWidgetProvider::class.java),
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                appWidgetManager.requestPinAppWidget(myProvider, null, successCallback)
                Toast.makeText(this, "Añadiendo widget a la pantalla de inicio...", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Mantén presionada tu pantalla de inicio y añade 'Nothing Music Widget'.", Toast.LENGTH_LONG).show()
            }
        } else {
            Toast.makeText(this, "Mantén presionada tu pantalla de inicio y añade 'Nothing Music Widget'.", Toast.LENGTH_LONG).show()
        }
    }
}
