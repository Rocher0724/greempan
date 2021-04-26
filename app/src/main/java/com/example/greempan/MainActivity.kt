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
import android.graphics.Color
import android.graphics.ImageDecoder
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.registerForActivityResult
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.greempan.mvvm.model.DrawingState
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.util.ArrayList

class MainActivity : AppCompatActivity() {
  lateinit var drawView: DrawViewKt
  private var btnList = ArrayList<Button>()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    val resource = Resources.getSystem()
    val config = resource.configuration
    onConfigurationChanged(config)

    drawView = findViewById(R.id.drawView)

    findViewById<View>(R.id.btnUndo).setOnClickListener {
      if (btnList.hasNotContains(it as Button)) btnList.add(it)
      changeBtnColor(it)
      drawView.undo()
    }
    findViewById<View>(R.id.btnRedo).setOnClickListener {
      if (btnList.hasNotContains(it as Button)) btnList.add(it)
      changeBtnColor(it)
      drawView.redo()
    }
    findViewById<View>(R.id.btnErase).setOnClickListener {
      if (btnList.hasNotContains(it as Button)) btnList.add(it)
      changeBtnColor(it)
      drawView.setToEraser()
    }
    findViewById<View>(R.id.btnPen).setOnClickListener {
      if (btnList.hasNotContains(it as Button)) btnList.add(it)
      changeBtnColor(it)
      drawView.setToPen()
    }
    findViewById<View>(R.id.btnAdd).setOnClickListener {
      if (btnList.hasNotContains(it as Button)) btnList.add(it)
      changeBtnColor(it)
      verifyStoragePermission()
    }
    findViewById<View>(R.id.btnSave).setOnClickListener {
      if (btnList.hasNotContains(it as Button)) btnList.add(it)
      changeBtnColor(it)
      saveDrawingBoard()
    }
    findViewById<View>(R.id.btnLoad).setOnClickListener {
      if (btnList.hasNotContains(it as Button)) btnList.add(it)
      changeBtnColor(it)
      pickImageFromPictureFolder()
    }
  }

  private fun ArrayList<Button>.hasNotContains(btn: Button): Boolean {
    return !this.contains(btn)
  }

  private fun changeBtnColor(clickedBtn: Button) {
    for (btn in btnList) {
      if (btn == clickedBtn) {
        btn.setBackgroundColor(Color.parseColor("#707070"))
        continue
      }
      btn.setBackgroundColor(Color.parseColor("#B7B7B7"))
    }
  }

  override fun onConfigurationChanged(newConfig: Configuration) {
    super.onConfigurationChanged(newConfig)
    when (newConfig.orientation) {
      Configuration.ORIENTATION_LANDSCAPE -> {
//        Toast.makeText(applicationContext, "가로", Toast.LENGTH_SHORT).show()
      }
      Configuration.ORIENTATION_PORTRAIT -> {
//        Toast.makeText(applicationContext, "세로", Toast.LENGTH_SHORT).show()
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