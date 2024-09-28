package com.asd.liceofesan.myroutesafe

import android.Manifest
import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.requestPermissions
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.ServiceCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY
import kotlin.system.exitProcess

/*
    El servicio rastreara la ubicacion
    Si la Actividad se quita el servicio de se ira a segundo plano
 */
class RastreoLocation: Service() {

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private lateinit var notificationManager: NotificationManager
    private var serviceRunningInForeground = false
    private val localBinder = LocalBinder()




    override fun onCreate() {
        super.onCreate()

        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    }
/*
    private fun createLocationRequest() {
        val locationRequest = LocationRequest.Builder(10000,
            5000) //En milisegundos
            .setPriority(PRIORITY_HIGH_ACCURACY)

        //Consultar la documentacion actual de gms LocationRequestpara modificar esta vaina
    }

    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }
*/
    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Toast.makeText(this, "Ubicacion Activa", Toast.LENGTH_SHORT).show()

        // Crear la notificación
        val notification = generateNotification()

        // Iniciar el servicio en primer plano
        startForeground(NOTIFICATION_ID, notification)
        Log.d("Servicio", "Servicio Inicializado")
        return START_STICKY
    }


    override fun onBind(intent: Intent?): IBinder {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_DETACH)
        } else {
            stopForeground(true)
        }
        serviceRunningInForeground = false
        //configurationChange = false
        return localBinder
    }

    override fun onRebind(intent: Intent?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_DETACH)
        } else {
            stopForeground(true)
        }
        serviceRunningInForeground = false
        //configurationChange = false
        super.onRebind(intent)
    }

    override fun onUnbind(intent: Intent?): Boolean {

        val notification = generateNotification()
        startForeground(
            /* id = */ NOTIFICATION_ID,
            /* notification = */ notification
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ServiceCompat.startForeground(this,
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION)
        }
        serviceRunningInForeground = true

        return true
    }

    override fun onDestroy() {
        Toast.makeText(this, "My Route Safe se ha cerrado",
            Toast.LENGTH_SHORT).show()
    }

    @SuppressLint("ObsoleteSdkInt")
    private fun generateNotification(): Notification{
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) { //Generar channel ID
            val name = getString(R.string.channel_name)
            val descriptionText = getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system.
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        /*val intent = Intent(this, MainActivity::class.java)*/
        /*val intentPendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_IMMUTABLE)*/
        //boton de cerrar
        /*
        val cerrarIntent = Intent(this, NotificationAppKiller::class.java)
        val flag_notif = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            PendingIntent.FLAG_IMMUTABLE
        } else { 0 }*/

        /*
        val cerrarPendingIntent: PendingIntent =
            PendingIntent.getBroadcast(this, 0, cerrarIntent,
                flag_notif)*/

        val titulo_notificaion = "My Route Safe se esta ejecutando"

        val notificationCompatBuilder =
            NotificationCompat.Builder(applicationContext, CHANNEL_ID)

        return notificationCompatBuilder
            .setBadgeIconType(NotificationCompat.BADGE_ICON_LARGE) //Icono
            .setSmallIcon(R.mipmap.ic_launcher)
            .setSilent(true) //Notificacion silenciosa
            .setContentText(titulo_notificaion) //titulo
            .setPriority(NotificationCompat.PRIORITY_MAX) //Prioridad
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setCategory(LOCATION_SERVICE)
            //.setContentIntent(intentPendingIntent)
            //.addAction(0, getString(R.string.cerrar), cerrarPendingIntent)
            .setOngoing(true)
            .build()
    }




    inner class LocalBinder : Binder() {
        internal val service: RastreoLocation
            get() = this@RastreoLocation
    }

    companion object {

        private const val TAG = "RastreoLocation"

        private const val PACKAGE_NAME = "com.asd.liceofesan.myroutesafe"

        internal const val ACTION_FOREGROUND_ONLY_LOCATION_BROADCAST =
            "$PACKAGE_NAME.action.FOREGROUND_ONLY_LOCATION_BROADCAST"

        internal const val EXTRA_LOCATION = "$PACKAGE_NAME.extra.LOCATION"

        private const val EXTRA_CANCEL_LOCATION_TRACKING_FROM_NOTIFICATION =
            "$PACKAGE_NAME.extra.CANCEL_LOCATION_TRACKING_FROM_NOTIFICATION"

        private const val NOTIFICATION_ID = 888

        private val CHANNEL_ID = "MY_ROUTE_SAFE"
    }

   /* class NotificationAppKiller: BroadcastReceiver() {
        val activity = MainActivity()
        override fun onReceive(context: Context?, intent: Intent?) {

            if (intent != null) {
                val mnotificationManager =
                    context?.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
                mnotificationManager.cancel(1)
                exitProcess(0)

            }
        }
    }*/
}