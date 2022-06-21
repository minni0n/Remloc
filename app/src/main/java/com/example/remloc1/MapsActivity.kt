package com.example.remloc1

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.os.Build
import android.os.Bundle
import android.system.Os.remove
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.remloc1.Data.PlacesData
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_maps.*
import java.io.IOException


class MapsActivity : AppCompatActivity(), OnMapReadyCallback, LocationListener, GoogleMap.OnMapClickListener,
    GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener{


    private val GOOGLEMAP_COMPASS = "GoogleMapCompass" // [4]

    private val GOOGLEMAP_TOOLBAR = "GoogleMapToolbar" // [3]

    private val GOOGLEMAP_ZOOMIN_BUTTON = "GoogleMapZoomInButton" // [2]child[0]



    private lateinit var auth: FirebaseAuth
    private lateinit var database : DatabaseReference
    private var mMap: GoogleMap? = null
    private lateinit var mLastLocation: Location
    private var currentLocationLatLng: LatLng? = null
    private var mCurrLocationMarker: Marker? = null
    private var mGoogleApiClient: GoogleApiClient? = null
    private lateinit var mLocationRequest: LocationRequest
    private var markerOnMap: MarkerOptions? = null
    private lateinit var locationSearch: EditText
    private lateinit var searchBtn: ImageButton
    private lateinit var saveBtn: Button
    private lateinit var mapView: View
    private var marker: Marker? = null
    private var searchActive: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        locationSearch = findViewById(R.id.et_search)
        searchBtn = findViewById(R.id.search)
        saveBtn = findViewById(R.id.savePlaceBtn)

//      Make a geolaction btn bot left
        mapView = myMaps.requireView()
        val locationButton= (mapView.findViewById<View>(Integer.parseInt("1")).parent as View).findViewById<View>(Integer.parseInt("2"))
        val rlp=locationButton.layoutParams as (RelativeLayout.LayoutParams)
        rlp.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
        rlp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);rlp.setMargins(0,0,30,70);
//        ------------------------------------



        currentLocationLatLng?.let { it-> CameraUpdateFactory.newLatLng(it) }
            ?.let { mMap!!.animateCamera(it) }

        saveBtn.isEnabled = searchActive

        searchBtn.setOnClickListener {

            searchLocation()
            saveBtn.isEnabled = searchActive
        }

        saveBtn.setOnClickListener {

            showChooseNameDialog(locationSearch.text.toString())
        }

        val mapFragment = supportFragmentManager.findFragmentById(R.id.myMaps) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {

        mMap = googleMap

        moveZoomControls(mapView, 30,-1,-1,70, horizontal = false, vertical = false)

        moveCompass(mapView,30,160,-1,-1, horizontal = true, vertical = false)
        mMap!!.uiSettings.isZoomControlsEnabled = true



        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ){
                buildGoogleApiClient()
                mMap!!.isMyLocationEnabled = true
            }
        }else{
            buildGoogleApiClient()
            mMap!!.isMyLocationEnabled = true
        }
    }

    protected fun buildGoogleApiClient(){
        mGoogleApiClient = GoogleApiClient.Builder(this)
            .addConnectionCallbacks(this)
            .addOnConnectionFailedListener(this)
            .addApi(LocationServices.API).build()
        mGoogleApiClient!!.connect()
    }

    override fun onLocationChanged(location: Location) {

        mLastLocation = location
        mCurrLocationMarker?.remove()

        //here
        val latLng = LatLng(location.latitude, location.longitude)
        currentLocationLatLng = latLng
        val markerOptions = MarkerOptions()
        markerOptions.position(latLng)
        markerOptions.title("Current Position")
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
        mCurrLocationMarker = mMap!!.addMarker(markerOptions)

        mMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 11f))

        if (mGoogleApiClient != null){
            LocationServices.getFusedLocationProviderClient(this)
        }
    }


    override fun onConnected(p0: Bundle?) {

        mLocationRequest = LocationRequest()
        mLocationRequest.interval = 1000
        mLocationRequest.fastestInterval = 1000
        mLocationRequest.priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ){
            LocationServices.getFusedLocationProviderClient(this)
        }
    }

    override fun onConnectionSuspended(p0: Int) {

    }

    override fun onConnectionFailed(p0: ConnectionResult) {

    }


    private fun moveView(
        view: View?,
        left: Int,
        top: Int,
        right: Int,
        bottom: Int,
        horizontal: Boolean,
        vertical: Boolean
    ) {
        try {
            assert(view != null)

            // replace existing layout params
            val rlp = RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
            )
            if (left >= 0) {
                rlp.addRule(RelativeLayout.ALIGN_PARENT_START, RelativeLayout.TRUE)
                rlp.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE)
            }
            if (top >= 0) {
                rlp.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE)
            }
            if (right >= 0) {
                rlp.addRule(RelativeLayout.ALIGN_PARENT_END, RelativeLayout.TRUE)
                rlp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE)
            }
            if (bottom >= 0) {
                rlp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE)
            }
            if (horizontal) {
                rlp.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE)
            }
            if (vertical) {
                rlp.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE)
            }
            rlp.setMargins(left, top, right, bottom)
            view!!.layoutParams = rlp
        } catch (ex: Exception) {
            Log.e(TAG, "moveView() - failed: " + ex.localizedMessage)
            ex.printStackTrace()
        }
    }

    private fun moveCompass(
        mapView: View?,
        left: Int,
        top: Int,
        right: Int,
        bottom: Int,
        horizontal: Boolean,
        vertical: Boolean
    ) {
        assert(mapView != null)
        val compass = mapView!!.findViewWithTag<View>(GOOGLEMAP_COMPASS)
        compass?.let { moveView(it, left, top, right, bottom, horizontal, vertical) }
    }

    private fun moveToolbar(
        mapView: View?,
        left: Int,
        top: Int,
        right: Int,
        bottom: Int,
        horizontal: Boolean,
        vertical: Boolean
    ) {
        assert(mapView != null)
        val toolbar = mapView!!.findViewWithTag<View>(GOOGLEMAP_TOOLBAR)
        toolbar?.let { moveView(it, left, top, right, bottom, horizontal, vertical) }
    }

    private fun moveZoomControls(
        mapView: View?,
        left: Int,
        top: Int,
        right: Int,
        bottom: Int,
        horizontal: Boolean,
        vertical: Boolean
    ) {
        assert(mapView != null)
        val zoomIn = mapView!!.findViewWithTag<View>(GOOGLEMAP_ZOOMIN_BUTTON)

        // we need the parent view of the zoomin/zoomout buttons - it didn't have a tag
        // so we must get the parent reference of one of the zoom buttons
        val zoomInOut = zoomIn.parent as View
        moveView(zoomInOut, left, top, right, bottom, horizontal, vertical)
    }

    private fun searchLocation() {

        marker?.remove()

        val location: String = this.locationSearch.text.toString().trim()
        var addressList: List<Address>? = null

        if (location == ""){
            searchActive = false
//            Toast.makeText(this, "provide location", Toast.LENGTH_SHORT).show()
        }else{
            val geoCoder = Geocoder(this)
            try {
                addressList = geoCoder.getFromLocationName(location, 1)
            }catch (e: IOException){
                e.printStackTrace()
            }

            val address = addressList!![0]
            val latLng = LatLng(address.latitude, address.longitude)

            markerOnMap = MarkerOptions().position(latLng).title(location)

            searchActive = true

            marker = mMap!!.addMarker(markerOnMap!!)
            mMap!!.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15F))
//            mMap!!.animateCamera(CameraUpdateFactory.newLatLng(latLng))
        }
    }

    override fun onMapClick(p0: LatLng) {
        TODO("Not yet implemented")
    }


    private fun savePlaceToFB(labelName:String){

        val locationSearch: EditText = findViewById(R.id.et_search)
        val location: String = locationSearch.text.toString().trim()
        var addressList: List<Address>? = null

        if (location == ""){
//            Toast.makeText(this, "provide location", Toast.LENGTH_SHORT).show()
        }else{
            val geoCoder = Geocoder(this)
            try {
                addressList = geoCoder.getFromLocationName(location, 1)
            }catch (e: IOException){
                e.printStackTrace()
            }

            val address = addressList!![0]


            saveDataToFirebase(address, labelName)
        }

    }



    @SuppressLint("SetTextI18n")
    private fun showChooseNameDialog(locationSearch: String) {

//        //Get label from sharedPref
//        val sp: SharedPreferences = getSharedPreferences("Label", MODE_PRIVATE)
//        val defaultLabel = sp.getInt("myLabel", -1)
//        //

        val builder = AlertDialog.Builder(this)
        val alert = builder.create()
        val inflater = layoutInflater
        val dialogLayout = inflater.inflate(R.layout.provide_label_layout, null)

        val label = dialogLayout.findViewById<EditText>(R.id.provideLabel)
        val checkBox = dialogLayout.findViewById<CheckBox>(R.id.checkBox)

        checkBox.setOnCheckedChangeListener { _, isChecked  ->

            if(isChecked){
                label.isEnabled = false
                label.setText(locationSearch)
            }else{
                label.isEnabled = true
                label.setText("")
                label.hint = getString(R.string.provide_label)
            }
        }

        with(alert){

            val btnOk = dialogLayout.findViewById<Button>(R.id.buttonOk)
            val btnCancel = dialogLayout.findViewById<Button>(R.id.buttonCancel)

            btnOk.setOnClickListener {

                savePlaceToFB(label.text.toString())
                cancel()
                val intent = Intent(this@MapsActivity, MapActivity::class.java)
                startActivity(intent)
            }

            btnCancel.setOnClickListener {

                Log.d("Main","Negative button clicked")
                cancel()
            }

            setView(dialogLayout)
            show()
        }
    }


    //Saving data to Firebase
    private fun saveDataToFirebase(address: Address, location: String){

        auth = FirebaseAuth.getInstance()
        val uid = auth.currentUser?.uid

        val addressLine: String = address.getAddressLine(0)
        val placeName: String = location
        val longitude: Double = address.longitude
        val latitude: Double = address.latitude


        if (uid!= null){

            database = FirebaseDatabase.getInstance("https://remloc1-86738-default-rtdb.europe-west1.firebasedatabase.app").getReference(uid)
            val key: String? = database.push().key
            val action = PlacesData(addressLine, placeName, longitude, latitude)
            database.child("Places//$key").setValue(action).addOnCompleteListener{
                if(it.isSuccessful){

                    Toast.makeText(this, "Success", Toast.LENGTH_SHORT).show()

                }else{

                    Toast.makeText(this, "Failed to update data", Toast.LENGTH_SHORT).show()

                }
            }

        }
//        }

    }

}