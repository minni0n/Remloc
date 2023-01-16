package com.remlocteam.remloc1

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.*
import android.widget.TextView.OnEditorActionListener
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
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
import com.remlocteam.remloc1.Data.PlacesData
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        Utils().checkAllPermissions(this)

        title = getString(R.string.find_place)
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



        locationSearch.setOnEditorActionListener(OnEditorActionListener { _, actionId, event ->
            if (event != null && event.keyCode == KeyEvent.KEYCODE_ENTER || actionId == EditorInfo.IME_ACTION_DONE) {
                searchLocation()
                hideKeybord()
            }
            false
        })

        searchBtn.setOnClickListener {
            searchLocation()
            hideKeybord()
        }

        saveBtn.setOnClickListener {
            if (marker!=null){
                showChooseNameDialog()
            }
            else{
                Toast.makeText(this, "Proszę wyszukać lokalizację!", Toast.LENGTH_SHORT).show()
            }
        }





        val mapFragment = supportFragmentManager.findFragmentById(R.id.myMaps) as SupportMapFragment
        mapFragment.getMapAsync(this)


        mapFragment.getMapAsync { googleMap ->
            googleMap.setOnMapClickListener { latLng ->
                // Remove the previous marker, if any
                marker?.remove()
                // Add a new marker to the map at the clicked location

                var addressList: List<Address>? = null
                val geoCoder = Geocoder(this)
                addressList = geoCoder.getFromLocation(latLng.latitude, latLng.longitude, 1)

                if (addressList.isNotEmpty() && addressList != null){
                    marker = googleMap.addMarker(MarkerOptions().position(latLng))
                }else{
                    Toast.makeText(this, getString(R.string.cant_find), Toast.LENGTH_SHORT).show()
                }

            }
        }
    }

    private fun hideKeybord() {
        val view = this.currentFocus
        if (view != null) {
            val hideKey = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            hideKey.hideSoftInputFromWindow(view.windowToken, 0)
        } else {
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
        }
    }

    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {

        mMap = googleMap

        moveZoomControls(mapView, 30,-1,-1,70, horizontal = false, vertical = false)

        moveCompass(mapView,30,160,-1,-1, horizontal = true, vertical = false)
        mMap!!.uiSettings.isZoomControlsEnabled = true

        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                // Got last known location. In some rare situations this can be null.
                if (location != null) {
                    // zoom in on the user's location
                    val latLng = LatLng(location.latitude, location.longitude)
                    val cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 15f)
                    mMap?.animateCamera(cameraUpdate)
                }
            }

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
        markerOptions.title("Aktualna pozycja")
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

        if (location != ""){
            val geoCoder = Geocoder(this)
            try {
                addressList = geoCoder.getFromLocationName(location, 1)
                if (addressList!!.isNotEmpty()){
                    val address = addressList[0]
                    val latLng = LatLng(address.latitude, address.longitude)

                    markerOnMap = MarkerOptions().position(latLng).title(location)



                    marker = mMap!!.addMarker(markerOnMap!!)
                    mMap!!.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15F))

                }else{
                    Toast.makeText(this, getString(R.string.cant_find), Toast.LENGTH_SHORT).show()
                }

            }catch (e: IOException){
                e.printStackTrace()
            }
        }
    }

    override fun onMapClick(p0: LatLng) {
        TODO("Not yet implemented")
    }




    private fun savePlaceToFB(address:Address, locationName: String){

        auth = FirebaseAuth.getInstance()
        val uid = auth.currentUser?.uid

        val addressLine: String = address.getAddressLine(0)
        val placeName: String = locationName
        val longitude: Double = address.longitude
        val latitude: Double = address.latitude


        if (uid!= null){

            database = FirebaseDatabase.getInstance(getString(R.string.firebase_database_url)).getReference(uid)
            val key: String? = database.push().key
            val action = PlacesData(addressLine, placeName, longitude, latitude)
            database.child("Places//$key").setValue(action).addOnCompleteListener{
                if(it.isSuccessful){

                    Toast.makeText(this, getString(R.string.success), Toast.LENGTH_SHORT).show()

                }else{

                    Toast.makeText(this, getString(R.string.failed_to_upd_data), Toast.LENGTH_SHORT).show()

                }
            }

        }

    }



    @SuppressLint("SetTextI18n", "InflateParams")
    private fun showChooseNameDialog() {

        val builder = AlertDialog.Builder(this)
        val alert = builder.create()
        val inflater = layoutInflater
        val dialogLayout = inflater.inflate(R.layout.provide_label_layout, null)

        val label = dialogLayout.findViewById<EditText>(R.id.provideLabel)
        val checkBox = dialogLayout.findViewById<CheckBox>(R.id.checkBox)

        var addressList: List<Address>? = null

        val geoCoder = Geocoder(this)
        try {
            addressList = geoCoder.getFromLocation(marker!!.position.latitude, marker!!.position.longitude, 1)
        }catch (e: IOException){
            e.printStackTrace()
        }
        val address = addressList!![0]
        var trimmedAddressLine = ""
        checkBox.setOnCheckedChangeListener { _, isChecked  ->

            if(isChecked){
                label.isEnabled = false
                val addressLineNow = address.getAddressLine(0)
                trimmedAddressLine = addressLineNow.substringBefore(",")
                label.setText(trimmedAddressLine)
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

                val locationName = if(!checkBox.isChecked){
                    label.text.toString()
                }else{
                    trimmedAddressLine
                }
                if(locationName!=""){
                    savePlaceToFB(address, locationName)
                    cancel()
                    val intent = Intent(this@MapsActivity, MapActivity::class.java)
                    startActivity(intent)
                }else{
                    Toast.makeText(this@MapsActivity, "Set the place name!", Toast.LENGTH_SHORT).show()
                }
            }

            btnCancel.setOnClickListener {

                Log.d("Main","Negative button clicked")
                cancel()
            }

            setView(dialogLayout)
            show()
        }
    }

}