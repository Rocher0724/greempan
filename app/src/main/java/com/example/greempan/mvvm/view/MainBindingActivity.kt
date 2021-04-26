package com.example.greempan.mvvm.view

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ImageDecoder
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.example.greempan.DrawViewKt
import com.example.greempan.R
import com.example.greempan.databinding.ActivityMainBinding
import com.example.greempan.mvvm.viewmodel.MainViewModel
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream

class MainBindingActivity : AppCompatActivity() {
    private lateinit var mBinding: ActivityMainBinding
    private val model: MainViewModel by viewModels()

    lateinit var drawView: DrawViewKt

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        mBinding.lifecycleOwner = this
        mBinding.viewModel = model

//    mBinding.viewModel.undoRedoClickFlag.observe(this , Observer {
//      it.invalidate()
//
//    })
        val resource = Resources.getSystem()
        val config = resource.configuration
        onConfigurationChanged(config)

        drawView = findViewById(R.id.drawView)

    }

    private var onTouchListener: View.OnTouchListener? = null

    fun setOnTouchListener(onTouchListener: View.OnTouchListener?) {
        this.onTouchListener = onTouchListener
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        when (newConfig.orientation) {
            Configuration.ORIENTATION_LANDSCAPE -> {
//                Toast.makeText(applicationContext, "가로", Toast.LENGTH_SHORT).show()
            }
            Configuration.ORIENTATION_PORTRAIT -> {
//                Toast.makeText(applicationContext, "세로", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private var permissionStorage = arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )

    private fun verifyStoragePermission() {
        requestMultiplePermissions.launch(permissionStorage)
    }

    private val requestMultiplePermissions = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
        run {
            if (permissions[Manifest.permission.READ_EXTERNAL_STORAGE] == true
                && permissions[Manifest.permission.WRITE_EXTERNAL_STORAGE] == true) {
                pickImageFromGallery()
            } else {
                Toast.makeText(this, "Not Available", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = MediaStore.Images.Media.CONTENT_TYPE
        intent.data = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        requestActivity.launch(intent)
    }

    private val requestActivity: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()) { activityResult ->
        if (activityResult.resultCode == RESULT_OK) {
            try {
                val bitmap: Bitmap = activityResult.data!!.data!!.uriToBitmap(this)
                drawView.setBitmap(bitmap)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun pickImageFromPictureFolder() {
        // TODO ???
        verifyStoragePermission()
    }


    private fun getBitmapFromView(v: View): Bitmap? {
        var screenshot: Bitmap? = null
        try {
            screenshot = Bitmap.createBitmap(v.measuredWidth, v.measuredHeight, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(screenshot)
            v.draw(canvas)
        } catch (e: Exception) {
            Log.e("MainBindingActivity", "Failed to capture screenshot because:" + e.message)
        }
        return screenshot
    }

    private fun saveBitmapToPictureFolder(bitmap: Bitmap) {
        val filename = "${System.currentTimeMillis()}.jpg"
        var fos: OutputStream? = null

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            this.contentResolver?.also { resolver ->
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
                }
                val imageUri: Uri? = resolver.insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    contentValues
                )
                fos = imageUri?.let { resolver.openOutputStream(it) }
                resolver.openOutputStream(imageUri!!)

            }
        } else {
            val imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            val image = File(imagesDir, filename)
            fos = FileOutputStream(image)
        }

        fos?.use {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
            Toast.makeText(this, "Captured GreemPan and saved to Gallery", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveDrawingBoard() {
        val drawingLayout = findViewById<View>(R.id.paintLayout)
        val bitmap = getBitmapFromView(drawingLayout)
        if (bitmap != null) {
            saveBitmapToPictureFolder(bitmap)
        }
    }

    @Throws(IOException::class)
    fun Uri.uriToBitmap(context: Context): Bitmap =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, this)) { decoder: ImageDecoder, _: ImageDecoder.ImageInfo?, _: ImageDecoder.Source? ->
                decoder.isMutableRequired = true
                decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
            }
        } else {
            BitmapDrawable(
                context.resources,
                MediaStore.Images.Media.getBitmap(context.contentResolver, this)
            ).bitmap
        }
}