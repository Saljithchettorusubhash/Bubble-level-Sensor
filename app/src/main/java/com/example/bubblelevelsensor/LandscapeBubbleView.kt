package com.example.bubblelevelsensor

import android.app.Activity
import android.content.Context
import android.graphics.*
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.AttributeSet
import android.view.View
import java.math.RoundingMode
import java.text.DecimalFormat
import kotlin.math.max
import kotlin.math.min

class LandscapeBubbleView  : View {
    private var screenWidth = 0
    private var screenHeight = 0
    private var centerOfCanvas = Point(0,0)
    private var rectW = 0
    private var yPos = 0
    private var paint: Paint = Paint()
    private var sensorManager: SensorManager? = null
    private val rotationMatrix = FloatArray(16)
    private val orientationValues = FloatArray(4)
    //bearings = forward /backward rotation - in X axis
    private val bearing = FloatArray(500)
    //pitch = forward /backward rotation - in Y axis
    private val pitch = FloatArray(500)
    //roll =  left/right rotation - in Z axis
    private val roll = FloatArray(500)
    private var readingCount = 0
    constructor(context: Context?) : super(context) {
        initView()
    }
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        initView()
    }
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        initView()
    }
    private fun initView(){
        sensorManager = (context as Activity).getSystemService(Context.SENSOR_SERVICE) as SensorManager
        sensorManager!!.registerListener(  object : SensorEventListener {
            override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
                return
            }
            override fun onSensorChanged(event: SensorEvent?) {
                event ?: return
                processSensorValues(event)
            }
        }, sensorManager!!.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR), SensorManager.SENSOR_DELAY_UI)
    }
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        screenWidth = measuredWidth
        screenHeight = measuredHeight
        setMeasuredDimension(screenWidth, screenHeight)
    }
    public override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
//        if((orientationValues[2].toDouble()+90) > 115 || 0 <= (orientationValues[2].toDouble()+90)
//            && (orientationValues[2].toDouble()+90) <= 50
//        ){
//            drawFrame(canvas)
//            drawBubbleForFrontView(canvas)
//        }else{

        drawBubbleForFlatSurface(canvas)
        // }
        drawText(canvas)
        invalidate()
    }
    private fun drawFrame(canvas: Canvas) {
        paint.reset()
        paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.color = Color.DKGRAY
        centerOfCanvas = Point(screenWidth / 2, screenHeight / 2)
        rectW =(min(screenWidth,screenHeight) * 0.90f).toInt()
        val rectH = 200
        val left = centerOfCanvas.x - rectW / 2
        yPos = centerOfCanvas.y - rectH / 2
        val right = centerOfCanvas.x + rectW / 2
        val bottom = centerOfCanvas.y + rectH / 2
        val rect = Rect(left, yPos, right, bottom)
        canvas.drawRect(rect, paint)
    }
    private fun drawBubbleForFrontView(canvas: Canvas) {
        paint.reset()
        paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.color = Color.WHITE
        var pitch = (height / 2.22f + orientationValues[1]+90).toDouble()
        pitch = min((height / 2.22f + 100).toDouble(),pitch)
        pitch = max((height / 2.22f).toDouble(), pitch)
        canvas.drawCircle(pitch.toFloat(), yPos.toFloat()+20f, 32f, paint)
    }
    private fun drawBubbleForFlatSurface(canvas: Canvas) {
        paint.reset()
        paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.color = Color.GREEN
        canvas.drawCircle(height / 2.0.toFloat(), screenWidth / 2.0.toFloat(), 300f, paint)
        paint.color = Color.DKGRAY
        canvas.drawCircle(height / 2.0.toFloat(), screenWidth / 2.0.toFloat(), 75f, paint)
        canvas.drawCircle(screenWidth / 2.0.toFloat(), screenWidth / 2.0.toFloat(), 75f, paint)
        val x = ((screenHeight / 2) + orientationValues[1]).toDouble()
        val y = ((screenWidth / 2) - orientationValues[2]).toDouble()
        paint.color = Color.WHITE
        canvas.drawCircle(x.toFloat(), y.toFloat(), 50f, paint)
    }
    private fun drawText(canvas: Canvas) {
        paint.reset()
        paint.textSize =20f
        var text = "X-axis : " + round(orientationValues[2]) + ",  Max Value : ${round(roll.maxOf { it })}"+",  Min Value : ${round(roll.minOf { it })}"
        paint.getTextBounds(text,0,text.length,Rect())
        val width = paint.measureText(text)
        canvas.drawText(text, centerOfCanvas.x.toFloat()-(width/2), (screenHeight-100).toFloat(), paint)
    }
    private fun processSensorValues(event: SensorEvent) {
        SensorManager.getRotationMatrixFromVector(rotationMatrix,event.values)
        SensorManager.getOrientation(rotationMatrix, orientationValues)
        orientationValues[0] = Math.toDegrees(orientationValues[0].toDouble()).toFloat()
        orientationValues[1] = Math.toDegrees(orientationValues[1].toDouble()).toFloat()
        orientationValues[2] = Math.toDegrees(orientationValues[2].toDouble()).toFloat()
        bearing[readingCount] = orientationValues[0]
        pitch[readingCount] = orientationValues[1]
        roll[readingCount] = orientationValues[2]
        if (readingCount == 499) readingCount=0 else readingCount++
    }
    private fun round(number: Float): String {
        val df = DecimalFormat("#.##")
        df.roundingMode = RoundingMode.HALF_UP
        return df.format(number).toString()
    }
}