package com.soumik.ridesharingapp.activities.usersActivity

import android.annotation.SuppressLint
import android.content.Intent
import android.location.Location
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.firebase.geofire.GeoFire
import com.firebase.geofire.GeoLocation
import com.firebase.geofire.GeoQueryEventListener
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
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.soumik.ridesharingapp.R
import com.soumik.ridesharingapp.activities.MainActivity
import com.soumik.ridesharingapp.appUtils.showToast
import kotlinx.android.synthetic.main.activity_user_map.*

class UserMapActivity : AppCompatActivity(), OnMapReadyCallback,
    GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
    com.google.android.gms.location.LocationListener{

    private lateinit var mMap: GoogleMap
    private lateinit var googleApiClient: GoogleApiClient
    private lateinit var lastLocation: Location
    private lateinit var locationRequest: LocationRequest
    private lateinit var userReference: DatabaseReference
    private lateinit var driverLocationReference: DatabaseReference
    private lateinit var userPickupLocation:LatLng

    private lateinit var mAuth: FirebaseAuth
    private lateinit var currentUser: FirebaseUser
    private var driverFound:Boolean = false
    private var radius:Double = 1.00
    private lateinit var currentUserID:String
    private lateinit var availableDriverID:String

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

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_map)

        mAuth = FirebaseAuth.getInstance()

        currentUser = mAuth.currentUser!!

        currentUserID = currentUser.uid

        userReference = FirebaseDatabase.getInstance().reference.child("Active Users")
        driverLocationReference = FirebaseDatabase.getInstance().reference.child("Available Drivers")

        btn_call_a_car.setOnClickListener {

            var geoFire = GeoFire(userReference)
            geoFire.setLocation(currentUserID, GeoLocation(lastLocation.latitude,lastLocation.longitude))

            userPickupLocation = LatLng(lastLocation.latitude,lastLocation.longitude)

            mMap.addMarker(MarkerOptions().position(userPickupLocation).title("Pick your customer from here"))

            btn_call_a_car.text = "Finding your ride...."

            getNearbyDriver()
        }

        fbtn_logout_user.setOnClickListener {

            mAuth.signOut()
            logoutUser()

        }

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    private fun getNearbyDriver() {

        var geoFire = GeoFire(driverLocationReference)
        var geoQuery = geoFire.queryAtLocation(GeoLocation(userPickupLocation.latitude,userPickupLocation.longitude),radius)
        geoQuery.removeAllListeners()

        geoQuery.addGeoQueryEventListener(object : GeoQueryEventListener{
            override fun onGeoQueryReady() {

                if (!driverFound){

                    radius++
                    getNearbyDriver()
                }
            }

            override fun onKeyEntered(key: String?, location: GeoLocation?) {

                if (!driverFound){

                    driverFound=true
                    availableDriverID = key!!
                }
            }

            override fun onKeyMoved(key: String?, location: GeoLocation?) {

            }

            override fun onKeyExited(key: String?) {

            }

            override fun onGeoQueryError(error: DatabaseError?) {

            }
        })
    }

    private fun logoutUser() {

        startActivity(Intent(this, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK))
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
    }

    private fun buildGoogleApiClient() {

        googleApiClient = GoogleApiClient.Builder(this)
            .addConnectionCallbacks(this)
            .addOnConnectionFailedListener(this)
            .addApi(LocationServices.API)
            .build()

        googleApiClient.connect()
    }

    override fun onStop() {
        super.onStop()

    }
}
