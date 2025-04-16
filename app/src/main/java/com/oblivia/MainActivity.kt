```kotlin
package com.oblivia

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

class MainActivity : AppCompatActivity() {
    private lateinit var db: TipDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        db = TipDatabase.getDatabase(this)

        val clipboardText = ClipboardListener.getClipboardText(this)
        sendChaosToServer(clipboardText)
    }

    private fun sendChaosToServer(chaos: String) {
        val client = OkHttpClient()
        val json = JSONObject().put("text", chaos)
        val body = RequestBody.create(MediaType.parse("application/json"), json.toString())
        val request = Request.Builder()
            .url("https://oblivia.onrender.com/analyze")
            .post(body)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {}
            override fun onResponse(call: Call, response: Response) {
                val tip = JSONObject(response.body()?.string() ?: "").optString("tip")
                lifecycleScope.launch {
                    withContext(Dispatchers.IO) {
                        db.tipDao().insert(Tip(0, tip))
                    }
                    findViewById<TextView>(R.id.tipTextView).text = tip
                }
            }
        })
    }
}
