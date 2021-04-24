package com.example.greempan.viewModel

import android.Manifest
import android.app.Application
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Paint
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.example.greempan.MainActivity
import com.example.greempan.model.DrawingState
import java.io.IOException
import java.util.*


class MainViewModel(application: Application) : AndroidViewModel(application){


    var count = MutableLiveData<Int>()
    var liveVisible = MutableLiveData<Boolean>()
    var undoStates = MutableLiveData<ArrayList<DrawingState>>()
    var redoStates = MutableLiveData<ArrayList<DrawingState>>()
    var penPaint = MutableLiveData<Paint>(Paint(Paint.DEV_KERN_TEXT_FLAG))
    var undoRedoClickFlag = MutableLiveData<Boolean>(false)

    init {
        count.value = 0
    }

    fun onTouchEvent() {

    }
    fun save() {

    }
    fun load() {

    }
    fun add() {
//        verifyStoragePermission()
    }
    fun undo() {
        undoRedoClickFlag.value = true
        if (undoStates.value!!.size > 0) {
            redoStates.value!!.add(undoStates.value!!.removeAt(undoStates.value!!.size - 1))
        }
    }
    fun redo() {
        undoRedoClickFlag.value = true
        if (redoStates.value!!.size > 0) {
            undoStates.value!!.add(redoStates.value!!.removeAt(redoStates.value!!.size - 1))
        }
    }
    fun pen() {
        penPaint.value?.let {
            it.xfermode = null
            it.strokeWidth = 5f
        }
    }
    fun erase() {
        penPaint.value?.let {
            it.xfermode = null
            it.strokeWidth = 5f
        }
    }

}