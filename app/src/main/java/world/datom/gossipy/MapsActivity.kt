package world.datom.gossipy

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.location.LocationListener
import android.location.LocationManager

import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.internal.ContextUtils.getActivity
import world.datom.gossipy.databinding.ActivityMapsBinding
import androidx.annotation.NonNull
import com.android.volley.Request;
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import java.io.File
import java.io.FileOutputStream
import java.io.PrintWriter
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    lateinit var mMap: GoogleMap
    lateinit var binding: ActivityMapsBinding
    lateinit var mFusedLocationClient: FusedLocationProviderClient
    private var locationPermissionGranted = false
    private val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        getLocationPermission()
        try {
            mMap.isMyLocationEnabled = true;
            mMap.setOnMapLongClickListener { point: LatLng ->
                Log.i("sonny", "long press" + point)
                mMap.addMarker(MarkerOptions().position(point).title("Stigmata"))
            }

            mMap.setInfoWindowAdapter(object : GoogleMap.InfoWindowAdapter {
                val webview = WebView(this@MapsActivity)
                val foo = webview.setWebViewClient(WebViewClient())

                override fun getInfoWindow(arg: Marker): View? {
                    return null;
                }

                override fun getInfoContents(arg: Marker): View {
                    //webview.loadData("<h1> wassup world </h1>", "text/html",null)
                    webview.loadUrl("https://www.google.com")
                    return webview
                }

            })

            mFusedLocationClient.lastLocation
                .addOnSuccessListener(this) { location ->
                    if (location != null) {
                        val here = LatLng(location.latitude, location.longitude)
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(here, 13.0f))
                    }
                }
            //logLocation()
        } catch (ex: SecurityException) {
            error(ex)
        }
    }

    @SuppressLint("MissingPermission")
    private fun logLocation() {
        val locationManager: LocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        Log.i("sonny", "storing " + filesDir.absoluteFile)
        val gossipyFileStr = filesDir.absolutePath + "/gossipy.json"
        val writer = PrintWriter(gossipyFileStr)
        val queue = Volley.newRequestQueue(this)
        val dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss z");

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 10.0f, object : LocationListener {
            override fun onLocationChanged(location: Location) {
                val lat_lon_alt = doubleArrayOf(location.latitude, location.longitude, location.altitude)
                val lat_lon_alt_as_str = lat_lon_alt.contentToString()
                writer.append(lat_lon_alt_as_str + "\n")
                writer.flush()
                val locationLog = File(gossipyFileStr).readText()
                Log.i("sonny", "log:" + locationLog)
                val now = ZonedDateTime.now()
                val nowStr = dtf.format(now)
                val queryStr = "lat=${location.latitude}&lon=${location.longitude}&alt=${location.altitude}&timestamp=${nowStr}"
                val saveLocationURL = "https://beta.datom.world/api/save-location?${queryStr}"
                val httpGet = StringRequest(Request.Method.GET, saveLocationURL, Response.Listener { response ->
                    Log.i("sonny", saveLocationURL)
                    Log.i("sonny", "response " + response)
                }, Response.ErrorListener {
                    Log.i("sonny", "error getting respone")
                })

                queue.add(httpGet)
            }
        })
    }


    private fun getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(
                this.applicationContext,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            == PackageManager.PERMISSION_GRANTED
        ) {
            locationPermissionGranted = true

        } else {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION
            )
        }
    }

}
