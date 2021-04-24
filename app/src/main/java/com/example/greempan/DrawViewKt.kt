package com.example.greempan

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.example.greempan.model.DrawingState
import java.util.*

class DrawViewKt: View {
    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    private var undoStates = ArrayList<DrawingState>()
    private var redoStates = ArrayList<DrawingState>()

    private var path: Path

    var penPaint: Paint = Paint(Paint.DEV_KERN_TEXT_FLAG)
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
    private var undoRedoClickFlag = false

    init {
        penPaint.isAntiAlias = true
        penPaint.color = Color.BLUE
        penPaint.strokeWidth = 5f
        penPaint.style = Paint.Style.STROKE

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

    fun setToPen() {
        penPaint.xfermode = null
        penPaint.strokeWidth = 5f
    }

    fun setToEraser() {
        penPaint.xfermode = clear
        penPaint.strokeWidth = 50f
    }

    override fun onDraw(canvas: Canvas) {
        if (undoRedoClickFlag) {
            drawingBitmap = resetBitmap!!.copy(Bitmap.Config.ARGB_8888, true)
            penDrawingCanvas = Canvas(drawingBitmap!!)
            for (p in undoStates) {
                penDrawingCanvas!!.drawPath(p.path, p.paint)
            }
            undoRedoClickFlag = false
        }

        canvas.drawBitmap(imageBackgroundBitmap!!, 0f, 0f, bitmapPaint)
        canvas.drawBitmap(drawingBitmap!!, 0f, 0f, bitmapPaint)
    }
    fun undo() {
        undoRedoClickFlag = true
        if (undoStates.size > 0) {
            redoStates.add(undoStates.removeAt(undoStates.size - 1))
        }
        invalidate()
    }

    fun redo() {
        undoRedoClickFlag = true
        if (redoStates.size > 0) {
            undoStates.add(redoStates.removeAt(redoStates.size - 1))
        }
        invalidate()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        redoStates.clear()
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
                penDrawingCanvas!!.drawPath(path, penPaint)
            }
            MotionEvent.ACTION_UP -> {
                penDrawingCanvas!!.drawPath(path, penPaint)
                undoStates.add(DrawingState(path, penPaint))
                path = Path()
            }
        }
        invalidate()
        return super.onTouchEvent(event)
    }

    fun setBitmap(bitmap: Bitmap?) {
        imageCanvas!!.drawBitmap(bitmap!!, 0f, 0f, null)
    }
}