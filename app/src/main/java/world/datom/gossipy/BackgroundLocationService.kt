package world.datom.gossipy

import android.annotation.SuppressLint
import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.IBinder
import android.util.Log
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import java.io.File
import java.io.PrintWriter
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class BackgroundLocationService : Service(){
    override fun onCreate() {
        super.onCreate()
        logLocation()
    }

    override fun onBind(intent: Intent?): IBinder? {
        TODO("Not yet implemented")
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
}