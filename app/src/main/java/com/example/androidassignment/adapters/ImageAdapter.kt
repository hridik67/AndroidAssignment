package com.example.androidassignment.adapters

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask
import android.util.LruCache
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.androidassignment.R
import java.io.File
import java.io.FileOutputStream
import java.lang.ref.WeakReference
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.Executors

class ImageAdapter(private val context: Context, private var imageUrls: List<String>) :
    RecyclerView.Adapter<ImageAdapter.ImageViewHolder>() {

    private val memoryCache: LruCache<String, Bitmap> = LruCache(10)
    private val diskCacheExecutor = Executors.newSingleThreadExecutor()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_image, parent, false)
        return ImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        holder.bind(imageUrls[position])
    }

    override fun getItemCount(): Int {
        return imageUrls.size
    }

    fun setImageUrls(newImageUrls: List<String>) {
        imageUrls = newImageUrls
        notifyDataSetChanged()
    }

    inner class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.imageView)
        private var currentTask: AsyncTask<String, Void, Bitmap?>? = null
        private var currentImageUrl: String? = null

        fun bind(imageUrl: String) {
            imageView.setImageBitmap(null)
            currentImageUrl = imageUrl
            currentTask = ImageLoaderTask().execute(imageUrl)
        }

        private inner class ImageLoaderTask : AsyncTask<String, Void, Bitmap?>() {
            override fun doInBackground(vararg params: String): Bitmap? {
                val imageUrl = params[0]
                return try {
                    val cachedBitmap = memoryCache.get(imageUrl)
                    if (cachedBitmap != null) {
                        cachedBitmap
                    } else {
                        val url = URL(imageUrl)
                        val connection = url.openConnection() as HttpURLConnection
                        connection.doInput = true
                        connection.connect()
                        val inputStream = connection.inputStream
                        val bitmap = BitmapFactory.decodeStream(inputStream)
                        saveBitmapToMemoryCache(imageUrl, bitmap)
                        saveBitmapToDiskCache(imageUrl, bitmap)
                        bitmap
                    }
                } catch (e: Exception) {
                    null
                }
            }

            override fun onPostExecute(result: Bitmap?) {
                if (currentImageUrl == currentImageUrl) {
                    imageView.setImageBitmap(result)
                }
            }
        }
    }

    private fun saveBitmapToMemoryCache(imageUrl: String, bitmap: Bitmap) {
        memoryCache.put(imageUrl, bitmap)
    }

    private fun saveBitmapToDiskCache(imageUrl: String, bitmap: Bitmap) {
        val fileName = imageUrl.substringAfterLast('/')
        diskCacheExecutor.execute {
            val file = File(context.cacheDir, fileName)
            try {
                FileOutputStream(file).use { outputStream ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
