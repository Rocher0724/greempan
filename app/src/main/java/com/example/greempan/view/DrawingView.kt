package com.example.greempan.view

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.lifecycle.MutableLiveData
import com.example.greempan.model.DrawingState
import java.util.*

class DrawingView: View {
    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    private var undoStates = MutableLiveData<ArrayList<DrawingState>>()
    private var redoStates = MutableLiveData<ArrayList<DrawingState>>()

    private var path: Path

//    var penPaint: Paint = Paint(Paint.DEV_KERN_TEXT_FLAG)
    private lateinit var penPaint: MutableLiveData<Paint>

    var transPaint: Paint = Paint(Paint.DEV_KERN_TEXT_FLAG)
    var clear = PorterDuffXfermode(PorterDuff.Mode.CLEAR)

    private var drawingBitmap: Bitmap? = null
    private var imageBackgroundBitmap: Bitmap? = null
    private var resetBitmap: Bitmap? = null

    private var penDrawingCanvas: Canvas? = null
    private var imageCanvas: Canvas? = null
    private var bitmapPaint: Paint? = null

    private var pointX: Float = 0.toFloat()
    private var pointY: Float = 0.toFloat()
    private var undoRedoClickFlag = MutableLiveData<Boolean>()

    private lateinit var imageCanvas2: MutableLiveData<Canvas>


    init {
//        penPaint.value.isAntiAlias = true
//        penPaint.value.color = Color.BLUE
//        penPaint.value.strokeWidth = 5f
//        penPaint.value.style = Paint.Style.STROKE
//
        transPaint.isAntiAlias = true
        transPaint.color = Color.TRANSPARENT
        transPaint.strokeWidth = 5f
        transPaint.style = Paint.Style.STROKE

        path = Path()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        drawingBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        imageBackgroundBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        resetBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)

        penDrawingCanvas = Canvas(drawingBitmap!!)
        imageCanvas = Canvas(imageBackgroundBitmap!!)
    }

    override fun onDraw(canvas: Canvas) {
        if (undoRedoClickFlag.value!!) {
            drawingBitmap = resetBitmap!!.copy(Bitmap.Config.ARGB_8888, true)
            penDrawingCanvas = Canvas(drawingBitmap!!)
            for (p in undoStates.value!!) {
                penDrawingCanvas!!.drawPath(p.path, p.paint)
            }
            undoRedoClickFlag.value = false
        }

        canvas.drawBitmap(imageBackgroundBitmap!!, 0f, 0f, bitmapPaint)
        canvas.drawBitmap(drawingBitmap!!, 0f, 0f, bitmapPaint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        redoStates.value!!.clear()
        pointX = event.getX(0)
        pointY = event.getY(0)
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                path.reset()
                path.moveTo(pointX, pointY)
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                path.lineTo(pointX, pointY)
                penPaint.value?.let { penDrawingCanvas!!.drawPath(path, it) }
            }
            MotionEvent.ACTION_UP -> {
                penPaint.value?.let {
                    penDrawingCanvas!!.drawPath(path, it)
                    undoStates.value!!.add(DrawingState(path, it))
                }

                path = Path()
            }
        }
        invalidate()
        return super.onTouchEvent(event)
    }

    fun setBitmap(bitmap: Bitmap?) {
        imageCanvas!!.drawBitmap(bitmap!!, 0f, 0f, null)
    }

    fun setUndoState (value: MutableLiveData<ArrayList<DrawingState>>) {
        undoStates = value
    }
    fun setRedoState (value: MutableLiveData<ArrayList<DrawingState>>) {
        redoStates = value
    }
    fun setPenPaint (value: MutableLiveData<Paint>) {
        penPaint = value
        penPaint.value!!.isAntiAlias = true
        penPaint.value!!.color = Color.BLUE
        penPaint.value!!.strokeWidth = 5f
        penPaint.value!!.style = Paint.Style.STROKE
    }
    fun setUndoRedoFlag (value: MutableLiveData<Boolean>) {
        undoRedoClickFlag = value

    }

}