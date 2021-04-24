package com.example.greempan

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
import android.view.View.OnTouchListener
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.example.greempan.viewModel.MainViewModel
import com.example.greempan.databinding.ActivityMainBinding
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream

class MainActivity : AppCompatActivity() {
  lateinit var drawView: DrawViewKt

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    val resource = Resources.getSystem()
    val config = resource.configuration
    onConfigurationChanged(config)

    drawView = findViewById(R.id.drawView)

    findViewById<View>(R.id.btnUndo).setOnClickListener {
      drawView.undo()
    }
    findViewById<View>(R.id.btnRedo).setOnClickListener {
      drawView.redo()
    }
    findViewById<View>(R.id.btnErase).setOnClickListener {
      drawView.setToEraser()
    }
    findViewById<View>(R.id.btnPen).setOnClickListener {
      drawView.setToPen()
    }
    findViewById<View>(R.id.btnAdd).setOnClickListener {
      verifyStoragePermission()
    }
    findViewById<View>(R.id.btnSave).setOnClickListener {
      saveDrawingBoard()
    }
    findViewById<View>(R.id.btnLoad).setOnClickListener {
      pickImageFromPictureFolder()
    }
  }

  override fun onConfigurationChanged(newConfig: Configuration) {
    super.onConfigurationChanged(newConfig)
    when (newConfig.orientation) {
      Configuration.ORIENTATION_LANDSCAPE -> {
        Toast.makeText(applicationContext, "가로", Toast.LENGTH_SHORT).show()
      }
      Configuration.ORIENTATION_PORTRAIT -> {
        Toast.makeText(applicationContext, "세로", Toast.LENGTH_SHORT).show()
      }
    }
  }

  private var permissionStorage = arrayOf(
    Manifest.permission.READ_EXTERNAL_STORAGE,
    Manifest.permission.WRITE_EXTERNAL_STORAGE
  )
  private val reqCodeSelectImage: Int = 100
  private val reqCodeExternalStorage: Int = 101

  private fun verifyStoragePermission() {
    val readPermission = ContextCompat.checkSelfPermission(
      this,
      Manifest.permission.WRITE_EXTERNAL_STORAGE
    )
    if (readPermission != PackageManager.PERMISSION_GRANTED) {
      ActivityCompat.requestPermissions(this, permissionStorage, reqCodeExternalStorage)
    } else {
      pickImageFromGallery()
    }
  }

  private fun pickImageFromGallery() {
    val intent = Intent(Intent.ACTION_PICK)
    intent.type = MediaStore.Images.Media.CONTENT_TYPE
    intent.data = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
    startActivityForResult(intent, reqCodeSelectImage)
  }

  private fun pickImageFromPictureFolder() {
//        val intent = Intent(Intent.ACTION_PICK)
//        intent.type = "image/*"
//        intent.data = MediaStore.Images.Media.EXTERNAL_CONTENT_URI

//        startActivityForResult(intent, reqCodeSelectImage)
//        put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
//        put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg")
//        put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
//    val drawingLayout = findViewById<View>(R.id.paintLayout) ?: return
//
//    val sdf = SimpleDateFormat("yyyyMMddHHmmss") //년,월,일,시간 포멧 설정
//    val time = Date() //파일명 중복 방지를 위해 사용될 현재시간
//    val currentTime = sdf.format(time) //String형 변수에 저장
//
//    val path = this.getExternalFilesDir(null).toString() + "/Camera"
//    Log.d("fw", "path : $path")
//    val file = File(path)
//
//    if (file.list().isNotEmpty()) {
//      Log.d("fw", "" + file.list()[0])
//    } else {
//      Log.d("fw", "empty")
//    }

  }


  private fun getBitmapFromView(v: View): Bitmap? {
    var screenshot: Bitmap? = null
    try {
      screenshot = Bitmap.createBitmap(v.measuredWidth, v.measuredHeight, Bitmap.Config.ARGB_8888)
      val canvas = Canvas(screenshot)
      v.draw(canvas)
    } catch (e: Exception) {
      Log.e("MainActivity", "Failed to capture screenshot because:" + e.message)
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
      Toast.makeText(this, "Captured View and saved to Gallery", Toast.LENGTH_SHORT).show()
    }
  }

  private fun saveDrawingBoard() {
    val drawingLayout = findViewById<View>(R.id.paintLayout)
    val bitmap = getBitmapFromView(drawingLayout)
    if (bitmap != null) {
      saveBitmapToPictureFolder(bitmap)
    }
  }

  override fun onRequestPermissionsResult(
    requestCode: Int,
    permissions: Array<out String>,
    grantResults: IntArray
  ) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    when (requestCode) {
      reqCodeExternalStorage -> {
        if (grantResults.isNotEmpty()) {
          pickImageFromGallery()
        } else {
          Toast.makeText(this, "Not Available", Toast.LENGTH_SHORT).show()
        }
      }
    }
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    when (requestCode) {
      reqCodeExternalStorage -> {
        if (resultCode == RESULT_OK) pickImageFromGallery()
      }
      reqCodeSelectImage -> {
        if (resultCode == RESULT_OK) {
          try {
            val bitmap: Bitmap = data!!.data!!.uriToBitmap(this)
            drawView.setBitmap(bitmap)
          } catch (e: IOException) {
            e.printStackTrace()
          }
        }
      }
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