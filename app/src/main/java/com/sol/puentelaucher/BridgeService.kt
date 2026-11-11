package com.sol.puentelaucher

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper

class BridgeService : Service() {

    private val handler = Handler(Looper.getMainLooper())
    private var repeticiones = 0

    // Paquete oficial de Projectivy Launcher
    private val PAQUETE_PROJECTIVY = "com.spocky.projengmenu"

    private val ráfagaDeSalto = object : Runnable {
        override fun run() {
            // Ejecuta 15 disparos (1 cada 2 segundos = cubre los primeros 30 segundos de arranque)
            if (repeticiones < 15) {
                abrirProjectivy()
                repeticiones++
                handler.postDelayed(this, 2000)
            } else {
                stopSelf() // Cuando ya ganó Projectivy, el servicio descansa para no gastar memoria
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        crearNotificacionSilenciosa()
        handler.post(ráfagaDeSalto)
    }

    private fun abrirProjectivy() {
        val launchIntent = packageManager.getLaunchIntentForPackage(PAQUETE_PROJECTIVY)
        if (launchIntent != null) {
            launchIntent.addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK or
                Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED or
                Intent.FLAG_ACTIVITY_CLEAR_TOP
            )
            startActivity(launchIntent)
        }
    }

    private fun crearNotificacionSilenciosa() {
        val channelId = "bridge_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Puente Activo", NotificationManager.IMPORTANCE_LOW)
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
            val notification = Notification.Builder(this, channelId)
                .setContentTitle("Iniciando sistema...")
                .setSmallIcon(android.R.drawable.ic_media_play)
                .build()
            startForeground(1, notification)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        repeticiones = 0
        handler.removeCallbacks(ráfagaDeSalto)
        handler.post(ráfagaDeSalto)
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
