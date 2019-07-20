package com.soumik.ridesharingapp.activities.usersActivity

import android.annotation.SuppressLint
import android.content.Intent
import android.location.Location
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.firebase.geofire.GeoFire
import com.firebase.geofire.GeoLocation
import com.firebase.geofire.GeoQuery
import com.firebase.geofire.GeoQueryEventListener
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.soumik.ridesharingapp.R
import com.soumik.ridesharingapp.activities.MainActivity
import com.soumik.ridesharingapp.activities.SettingsActivity
import com.soumik.ridesharingapp.appUtils.showToast
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_driver_map.*
import kotlinx.android.synthetic.main.activity_settings.*
import kotlinx.android.synthetic.main.activity_user_map.*

class UserMapActivity : AppCompatActivity(), OnMapReadyCallback,
    GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
    com.google.android.gms.location.LocationListener{

    private lateinit var mMap: GoogleMap
    private lateinit var googleApiClient: GoogleApiClient
    private lateinit var lastLocation: Location
    private lateinit var locationRequest: LocationRequest
    private lateinit var userReference: DatabaseReference
    private lateinit var driverAvailableReference: DatabaseReference
    private lateinit var driverReference: DatabaseReference
    private lateinit var driverLocationReference: DatabaseReference
    private lateinit var userPickupLocation:LatLng

    private lateinit var mAuth: FirebaseAuth
    private lateinit var currentUser: FirebaseUser
    private var driverFound:Boolean = false
    private var requestType:Boolean = false
    private var radius:Double = 1.00
    private lateinit var currentUserID:String
    private lateinit var availableDriverID:String
    private lateinit var driverMarker:Marker
    private lateinit var pickupMarker: Marker
    private lateinit var driverLocationRefListener:ValueEventListener
    private lateinit var geoQuery: GeoQuery

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
        driverAvailableReference = FirebaseDatabase.getInstance().reference.child("Available Drivers")
        driverLocationReference = FirebaseDatabase.getInstance().reference.child("Drivers Working")

        btn_call_a_car.setOnClickListener {

            if (requestType){

                requestType = false

                geoQuery.removeAllListeners()
                driverLocationReference.removeEventListener(driverLocationRefListener)

                if (driverFound!=null){
                    driverReference = FirebaseDatabase.getInstance().reference.child("Users").child("Drivers").child(availableDriverID).child("customerID")

                    driverReference.removeValue()

                    availableDriverID = null.toString()
                }

                driverFound = false
                radius = 1.00

                var customerID = FirebaseAuth.getInstance().currentUser?.uid

                var geoFire = GeoFire(userReference)
                geoFire.removeLocation(customerID)

                if (pickupMarker!=null){
                    pickupMarker.remove()
                }

                if (driverMarker!=null){
                    driverMarker.remove()
                }

                btn_call_a_car.text = "Call a Car"
                layout_relative.visibility = View.GONE

            } else {
                requestType = true

                var customerID = FirebaseAuth.getInstance().currentUser?.uid

                var geoFire = GeoFire(userReference)
                geoFire.setLocation(customerID, GeoLocation(lastLocation.latitude,lastLocation.longitude))

                userPickupLocation = LatLng(lastLocation.latitude,lastLocation.longitude)

              pickupMarker =   mMap.addMarker(MarkerOptions().position(userPickupLocation).title("My Location").icon(BitmapDescriptorFactory.fromResource(R.drawable.user)))

                btn_call_a_car.text = "Finding your ride...."

                getNearbyDriver()
            }
        }

        fbtn_logout_user.setOnClickListener {

            mAuth.signOut()
            logoutUser()

        }

        fbtn_settings_user.setOnClickListener {

            startActivity(Intent(this, SettingsActivity::class.java).putExtra(SettingsActivity.TYPE,"Customers"))
        }

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    private fun getNearbyDriver() {

        var geoFire = GeoFire(driverAvailableReference)
        var geoQuery = geoFire.queryAtLocation(GeoLocation(userPickupLocation.latitude,userPickupLocation.longitude),radius)
        geoQuery.removeAllListeners()

        geoQuery.addGeoQueryEventListener(object : GeoQueryEventListener{
            override fun onGeoQueryReady() {

                if (!driverFound){

                    radius += 1
                    getNearbyDriver()
                }
            }

            override fun onKeyEntered(key: String?, location: GeoLocation?) {

                if (!driverFound && requestType){

                    driverFound=true
                    availableDriverID = key!!

                    driverReference = FirebaseDatabase.getInstance().reference.child("Users").child("Drivers").child(availableDriverID)

                    var driverMap:HashMap<String,Any> = HashMap()
                    driverMap["customerID"] = currentUserID

                    driverReference.updateChildren(driverMap)

                    getDriverLocation()

                    btn_call_a_car.text = "Finding Driver Location..."
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

    private fun getDriverLocation() {

       driverLocationRefListener =  driverLocationReference.child(availableDriverID).child("l").addValueEventListener(object : ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists() && requestType){
                    var driverLocationMap : List<Object> = p0.value as List<Object>

                    var locationLat : Double = 0.0
                    var locationLng : Double = 0.0
                    btn_call_a_car.text = "Driver Found"

                    layout_relative.visibility = View.VISIBLE
                    getAssignedDriverInformation()

                    if (driverLocationMap[0]!=null){
                        locationLat = driverLocationMap[0].toString() as Double
                    }
                    if (driverLocationMap[1]!=null){
                        locationLng = driverLocationMap[1].toString() as Double
                    }

                    var driverLatLng:LatLng = LatLng(locationLat,locationLng)

                    if (driverMarker!=null){
                        driverMarker.remove()
                    }

                    var userLocation:Location = Location("")
                    userLocation.latitude = userPickupLocation.latitude
                    userLocation.longitude = userPickupLocation.longitude

                    var driverLocation:Location = Location("")
                    driverLocation.latitude = driverLatLng.latitude
                    driverLocation.longitude = driverLatLng.longitude

                    var distance:Float = driverLocation.distanceTo(userLocation)

                    if (distance<90){
                        btn_call_a_car.text = "Your Rider has arrived"
                    } else {
                        btn_call_a_car.text = "Driver is $distance Away from you"
                    }

                    driverMarker = mMap.addMarker(MarkerOptions().position(driverLatLng).title("Your Driver is here").icon(BitmapDescriptorFactory.fromResource(R.drawable.car)))
                }
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

    private fun getAssignedDriverInformation(){

        var reference:DatabaseReference = FirebaseDatabase.getInstance().reference.child("Users").child("Drivers")
            .child(availableDriverID)

        reference.addValueEventListener(object :ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {
                if(p0.exists()&&p0.childrenCount>0){

                    var name = p0.child("name").value.toString()
                    var phone = p0.child("phone").value.toString()
                    var car = p0.child("car").value.toString()

                    tv_name_driver.text = name
                    tv_phone_driver.text = phone
                    tv_car_name_driver.text = car


                    if (p0.hasChild("image")) {
                        var image = p0.child("image").value.toString()
                        Picasso.get().load(image).into(iv_driver_image)
                    }
                }
            }
        })
    }

    override fun onStop() {
        super.onStop()

    }
}
