package com.soumik.ridesharingapp.activities.driversActivity

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.location.Location
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import com.firebase.geofire.GeoFire
import com.firebase.geofire.GeoLocation
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.soumik.ridesharingapp.R
import com.soumik.ridesharingapp.activities.MainActivity
import com.soumik.ridesharingapp.appUtils.showToast
import kotlinx.android.synthetic.main.activity_driver_map.*

class DriverMapActivity : AppCompatActivity(), OnMapReadyCallback,
                            GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
                            com.google.android.gms.location.LocationListener {


    private lateinit var mMap: GoogleMap
    private lateinit var googleApiClient: GoogleApiClient
    private lateinit var lastLocation: Location
    private lateinit var locationRequest: LocationRequest
    private lateinit var mAuth: FirebaseAuth
    private lateinit var currentDriver: FirebaseUser
    private var driverStatus:Boolean = false

    @SuppressLint("MissingPermission")
    override fun onConnected(p0: Bundle?) {

        locationRequest = LocationRequest()
        locationRequest.interval=1000
        locationRequest.fastestInterval=1000
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient,locationRequest,this)
    }

    override fun onConnectionSuspended(p0: Int) {

    }

    override fun onConnectionFailed(p0: ConnectionResult) {

    }

    override fun onLocationChanged(p0: Location?) {

        lastLocation = p0!!

        var latLng:LatLng = LatLng(p0.latitude,p0.longitude)
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng!!))
        mMap.animateCamera(CameraUpdateFactory.zoomTo(18F))

        var driverID = FirebaseAuth.getInstance().currentUser?.uid

        var availableDriverRef:DatabaseReference = FirebaseDatabase.getInstance().reference.child("Available Drivers")

        var geoFire : GeoFire = GeoFire(availableDriverRef)
        geoFire.setLocation(driverID, GeoLocation(p0.latitude,p0.longitude))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_driver_map)

        mAuth = FirebaseAuth.getInstance()

        currentDriver = mAuth.currentUser!!

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        fbtn_logout.setOnClickListener{

            mAuth.signOut()

            logoutDriver()
        }
    }

    private fun logoutDriver() {

        driverStatus = true
        disconnectDriver()

        startActivity(Intent(this,MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK))
        finish()
        showToast(applicationContext,"Successfully Logged Out")
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        buildGoogleApiClient()

        mMap.isMyLocationEnabled=true

//        // Add a marker in Sydney and move the camera
//        val sydney = LatLng(-34.0, 151.0)
//        mMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))
    }

    private fun buildGoogleApiClient(){

        googleApiClient = GoogleApiClient.Builder(this)
            .addConnectionCallbacks(this)
            .addOnConnectionFailedListener(this)
            .addApi(LocationServices.API)
            .build()

        googleApiClient.connect()
    }

    override fun onStop() {
        super.onStop()

        if (!driverStatus){
            disconnectDriver()
        }
    }

    private fun disconnectDriver() {

        var driverID = currentDriver.uid

        Log.i("111",""+driverID)

        var availableDriverRef:DatabaseReference = FirebaseDatabase.getInstance().reference.child("Available Drivers")

        var geoFire = GeoFire(availableDriverRef)
        geoFire.removeLocation(driverID)
        LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient,this)
    }
}
