// MainActivity.kt
package com.example.androidassignment

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.androidassignment.R
import com.example.androidassignment.adapters.ImageAdapter
import okhttp3.*
import org.json.JSONArray
import java.io.IOException
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var imageAdapter: ImageAdapter
    private var currentRequest: Call? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = GridLayoutManager(this, 3)
        imageAdapter = ImageAdapter(this, emptyList())
        recyclerView.adapter = imageAdapter

        fetchImageUrls()
    }

    override fun onDestroy() {
        super.onDestroy()
        currentRequest?.cancel()
    }

    private fun fetchImageUrls() {
        val url = "https://acharyaprashant.org/api/v2/content/misc/media-coverages?limit=100"
        val request = Request.Builder()
            .url(url)
            .build()

        val client = OkHttpClient.Builder()
            .callTimeout(5, TimeUnit.SECONDS) // Set a timeout for the API call
            .build()

        currentRequest = client.newCall(request)
        currentRequest?.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                val jsonData = response.body?.string()
                val imageUrls = parseImageUrls(jsonData)
                runOnUiThread {
                    imageAdapter.setImageUrls(imageUrls)
                }
            }
        })
    }

    private fun parseImageUrls(jsonData: String?): List<String> {
        val imageUrls = mutableListOf<String>()
        if (!jsonData.isNullOrBlank()) {
            val jsonArray = JSONArray(jsonData)
            for (i in 0 until jsonArray.length()) {
                val jsonObject = jsonArray.getJSONObject(i)
                val thumbnail = jsonObject.getJSONObject("thumbnail")
                val domain = thumbnail.getString("domain")
                val basePath = thumbnail.getString("basePath")
                val key = thumbnail.getString("key")
                val imageURL = "$domain/$basePath/0/$key"
                imageUrls.add(imageURL)
            }
        }
        return imageUrls
    }
}
