package com.example.greempan.mvvm.adapter

import android.graphics.Paint
import androidx.databinding.BindingAdapter
import androidx.lifecycle.MutableLiveData
import com.example.greempan.mvvm.model.DrawingState
import com.example.greempan.mvvm.view.DrawingView
import java.util.ArrayList

object BindingAdapter {

    @JvmStatic
    @BindingAdapter("undoStates")
    fun DrawingView.setUndoState(value: MutableLiveData<ArrayList<DrawingState>>) {
        this.setUndoState(value)
    }
    @JvmStatic
    @BindingAdapter("redoStates")
    fun DrawingView.setRedoState(value: MutableLiveData<ArrayList<DrawingState>>) {
        this.setRedoState(value)
    }

    @JvmStatic
    @BindingAdapter("penPaint")
    fun DrawingView.setPenPaint(value: MutableLiveData<Paint>) {
        this.setPenPaint(value)
    }
    @JvmStatic
    @BindingAdapter("undoRedoFlag")
    fun DrawingView.setUndoRedoFlag(value: MutableLiveData<Boolean>) {
        this.setUndoRedoFlag(value)
    }
}