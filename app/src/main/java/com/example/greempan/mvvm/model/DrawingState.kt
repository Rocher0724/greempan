package com.example.greempan.mvvm.model

import android.graphics.Paint
import android.graphics.Path

class DrawingState (p: Path, paint: Paint?) {
  var path: Path
  var paint: Paint

  init {
    val copyPaint = Paint(paint)
    path = p
    this.paint = copyPaint
  }
}