package com.asd.liceofesan.myroutesafe


import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.OnRequestPermissionsResultCallback
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.GoogleMap.OnMyLocationButtonClickListener

class MainActivity : AppCompatActivity(), OnMapReadyCallback, OnMyLocationButtonClickListener {
    //mafufadas
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    //private lateinit var locationCallback: OnMapReadyCallback
    private lateinit var currentLocation: Location
    private var hayPermisos = false
    private var mGoogleMap:GoogleMap? = null
    private val permissionCode = 101
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

        val mapaPrincipal = supportFragmentManager
            .findFragmentById(R.id.mapaPrincipal) as SupportMapFragment
        mapaPrincipal.getMapAsync(this)

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        fetchLocation()
    }

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

        @SuppressLint("MissingPermission")
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
}