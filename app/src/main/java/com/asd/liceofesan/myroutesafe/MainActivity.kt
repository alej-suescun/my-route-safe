package com.asd.liceofesan.myroutesafe


import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.OnMyLocationButtonClickListener
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import kotlin.system.exitProcess


class MainActivity : AppCompatActivity(), OnMapReadyCallback, OnMyLocationButtonClickListener {
    //mafufadas
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    //private lateinit var locationCallback: OnMapReadyCallback
    private lateinit var currentLocation: Location
    private var hayPermisos = false
    private var mGoogleMap:GoogleMap? = null
    private val permissionCode = 101
    private val CHANNEL_ID = "MY_ROUTE_SAFE"
    private val EXTRA_NOTIFICATION_ID = "MY_ROUTE_SAFE2"
    @SuppressLint("MissingPermission", "ObsoleteSdkInt")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        //de aqui pa abajo escribir

        Log.d("ASD", "Permisos notif")
        //Permisos de notificacion
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { //Tiramisu es android 13
            val permisos = arrayListOf(Manifest.permission.POST_NOTIFICATIONS)
            val permisosArray = permisos.toTypedArray()
            tiene_permisos_notif(permisosArray)
        }

        //Inicializa el servicio
        val intent = Intent(this, RastreoLocation::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }


        val mapaPrincipal = supportFragmentManager
            .findFragmentById(R.id.mapaPrincipal) as SupportMapFragment
        mapaPrincipal.getMapAsync(this)

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        fetchLocation()
        /*
        //notificacion que "no se quita"
        createNotificationChannel()

        //Accion cuando se presiona
        val intent = Intent(this, MainActivity::class.java)
        val intentPendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        //boton de cerrar
        val cerrarIntent = Intent(this, NotificationAppKiller::class.java)
        val flag_notif = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            PendingIntent.FLAG_IMMUTABLE
        } else { 0 }

        val cerrarPendingIntent: PendingIntent =
            PendingIntent.getBroadcast(this, 0, cerrarIntent,
                flag_notif)

        val titulo_notificaion = "My Route Safe se esta ejecutando"
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setBadgeIconType(NotificationCompat.BADGE_ICON_LARGE) //Icono
            .setSmallIcon(R.mipmap.ic_launcher)
            .setSilent(true) //Notificacion silenciosa
            .setContentText(titulo_notificaion) //titulo
            .setPriority(NotificationCompat.PRIORITY_MAX) //Prioridad
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setCategory(LOCATION_SERVICE)
            .setContentIntent(intentPendingIntent)
            .addAction(0, getString(R.string.cerrar), cerrarPendingIntent)
            .setOngoing(true)


        val notificationManager = NotificationManagerCompat.from(this)

        //Android 13 para en adelante necesita permisos
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { //Tiramisu es android 13
            val permisos = arrayListOf(Manifest.permission.POST_NOTIFICATIONS)
            val permisosArray = permisos.toTypedArray()
            tiene_permisos_notif(permisosArray)
        }

        notificationManager.notify(1, notification.build())*/
        Log.d("Creation", "Si compilo")


    }
    
    /*
    override fun onResume() {
        super.onResume()
        if (requestingLocationUpdates) startLocationUpdates()
    }


    override fun onPause() {
        super.onPause()
        stopLocationUpdates()
    }
    */

    @SuppressLint("MissingPermission") //comentar si crashea
    private fun fetchLocation(){
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        val getLocation=
            fusedLocationProviderClient.lastLocation.addOnSuccessListener {
                location -> if (location != null) {
            currentLocation = location

            //si crashea comentar esto
            val mapFragment = supportFragmentManager
                .findFragmentById(R.id.mapaPrincipal) as SupportMapFragment
            mapFragment.getMapAsync(this)
            }
        }
        hayPermisos = true
    }

    //Funcion de para pedir permisos
    private fun solicitarPermisos(permisos: Array<String>) {
        requestPermissions(permisos, permissionCode)
    }

    private fun tienePermisos(permisos: Array<String>){
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            solicitarPermisos(permisos)
            // for ActivityCompat#requestPermissions for more details.
            return
        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode){

            permissionCode -> if(grantResults.isNotEmpty() && grantResults[0]==
                PackageManager.PERMISSION_GRANTED){
                fetchLocation()
            }
        }
    }

    //Mapa y sus configs
    override fun onMapReady(googleMap: GoogleMap) {
        this.mGoogleMap = googleMap

        val permisos = arrayListOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        val permisosArray = permisos.toTypedArray()
        //Ubicacion del liceo fesan 4.756723052321578, -74.0954306734966
        var latLng=LatLng (4.756723052321578, -74.0954306734966)
        // Configuraciones
        googleMap.isTrafficEnabled = true

        val maxZoomDisp = googleMap.maxZoomLevel
        googleMap.setMinZoomPreference(12.0f)
        googleMap.setMaxZoomPreference(maxZoomDisp)


        tienePermisos(permisosArray)

        @SuppressLint("MissingPermission") //Permisos pedidos arriba
        if (hayPermisos){
            //ubicacion
            googleMap.isMyLocationEnabled = true
            googleMap.setOnMyLocationButtonClickListener(this)
            if (::currentLocation.isInitialized) {
                latLng =LatLng (currentLocation.latitude, currentLocation.longitude)}
        }

        //camara
        googleMap.animateCamera(CameraUpdateFactory.newLatLng(latLng))
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 20f))

    }

    override fun onMyLocationButtonClick(): Boolean {
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        mGoogleMap?.animateCamera(CameraUpdateFactory.zoomTo(21.0f))

        return false
    }

/*
    //Notificacion
    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is not in the Support Library.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
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
    }*/

    private fun tiene_permisos_notif(permisos: Array<String>){
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            solicitarPermisos(permisos)
            return
        }
    }
    /*
    class NotificationAppKiller: BroadcastReceiver() {
        val activity = MainActivity()
        override fun onReceive(context: Context?, intent: Intent?) {

            if (intent!=null){
                exitProcess(0)
            }
        }
    }*/
}