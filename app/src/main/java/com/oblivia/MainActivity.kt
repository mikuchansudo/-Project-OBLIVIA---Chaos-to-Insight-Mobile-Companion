package com.oblivia

import android.content.Context
import android.location.Location
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.OnSuccessListener
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

class MainActivity : AppCompatActivity() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var db: TipDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        db = TipDatabase.getDatabase(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val clipboardText = ClipboardListener.getClipboardText(this)
        sendChaosToServer(clipboardText)

        // Fetch the current location
        getCurrentLocation { location ->
            val locationText = "Lat: ${location?.latitude}, Lon: ${location?.longitude}"
            sendChaosToServer(locationText)
        }
    }

    // Function to get the current location
    private fun getCurrentLocation(onLocationReceived: (Location?) -> Unit) {
        fusedLocationClient.lastLocation
            .addOnSuccessListener(this, OnSuccessListener { location: Location? ->
                onLocationReceived(location)
            })
    }

    private fun sendChaosToServer(chaos: String) {
        val client = OkHttpClient()
        val json = JSONObject().put("text", chaos)
        val body = RequestBody.create(MediaType.parse("application/json"), json.toString())
        val request = Request.Builder()
            .url("https://oblivia.onrender.com/analyze")  // Replace with your backend URL
            .post(body)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {}
            override fun onResponse(call: Call, response: Response) {
                val tip = JSONObject(response.body()?.string() ?: "").optString("tip")
                runOnUiThread {
                    findViewById<TextView>(R.id.tipTextView).text = tip
                }
            }
        })
    }
}
