package com.example.bubblelevelsensor

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.widget.Toast


class BubbleBaseView: View {
    private val radius = 55f
    private var paint: Paint? = null
    private var borderWidth = 0
    private var centerW= 0
    private var centerH= 0
    private var screenWidth = 0
    private var screenHeight = 0
    private var orientation = 0
    private var sensorValue: SensorValue? = null
    //Multiplier to scale content along the axis
    private var multiplier1d = 20
    private var multiplier2d = 15
    private var maxRange  = 10
    private var minRange  = -10
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

    private fun initView() {
        paint = Paint(Paint.ANTI_ALIAS_FLAG)
        borderWidth =(radius + 1).toInt()
    }
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthSpec = MeasureSpec.getSize(widthMeasureSpec)
        val heightSpec = MeasureSpec.getSize(heightMeasureSpec)
        setMeasuredDimension(widthSpec, heightSpec / 2)
    }
    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        screenWidth = width
        screenHeight = height
        centerW = width / 2
        centerH= height / 2
        paint!!.style = Paint.Style.FILL
        if ((orientation == 0 || orientation == 180) && (sensorValue!!.roll >= 45 && sensorValue!!.roll < 135 || sensorValue!!.roll <= -45 && sensorValue!!.roll > -135)) {
            portraitView(canvas)
        } else if (orientation == 90 || orientation == 270) {
            landscapeView(canvas)
        } else {
            //draw2d(canvas)
            drawFlatSurface(canvas)
        }
    }
    private fun portraitView(canvas: Canvas?) {
        var x: Float =0f
        var y: Float = 0f
        paint!!.style = Paint.Style.FILL
        if (sensorValue!!.pitch > maxRange) {
            x = (screenWidth - (maxRange * multiplier1d + centerW)).toFloat()
            y = centerH.toFloat()
        } else if (sensorValue!!.pitch < minRange) {
            x = (screenWidth - (minRange * multiplier1d + centerW)).toFloat()
            y = centerH.toFloat()
        } else {
            x = (screenWidth - (sensorValue!!.pitch * multiplier1d + centerW)).toFloat()
            y = centerH.toFloat()
        }
        paint!!.color = Color.RED
        paint!!.style = Paint.Style.FILL
        //drawing the bubble
        if (sensorValue!!.roll > 0 + 5 || sensorValue!!.roll  < 0 - 5)
            paint!!.color=Color.RED  else paint!!.color=Color.GREEN
        canvas!!.drawCircle(x, y, radius, paint!!)
        drawBorder(canvas)
    }
    private fun landscapeView(canvas: Canvas?) {
        var x: Float =0f
        var y: Float = 0f
        paint!!.style = Paint.Style.FILL
        //checking range (-10/10)
        if (sensorValue!!.roll > maxRange) {
            x = (screenWidth - (maxRange * multiplier1d + centerW)).toFloat()
            y = centerH.toFloat()
        } else if (sensorValue!!.roll < minRange) {
            x = (screenWidth - (maxRange * multiplier1d + centerW)).toFloat()
            y = centerH.toFloat()
        } else {
            x = (screenWidth - (sensorValue!!.roll * multiplier1d + centerW)).toFloat()
            y = centerH.toFloat()
        }
        //drawing the bubble
        if (sensorValue!!.roll > 0 + 5 || sensorValue!!.roll  < 0 - 5)
            paint!!.color=Color.RED  else paint!!.color=Color.GREEN
        canvas!!.drawCircle(x, y, radius, paint!!)
        drawBorder(canvas)
    }
    private fun drawBorder(canvas: Canvas?){
        //outer border params
        var left:Int = (centerW - maxRange * multiplier1d- radius).toInt()
        var top:Int = centerH - borderWidth
        var right:Int = (centerW + maxRange * multiplier1d +  radius).toInt()
        var bottom:Int = centerH + borderWidth
        //draw center dot
        paint!!.color = Color.BLACK
        canvas!!.drawCircle(centerW.toFloat(), centerH.toFloat(), 5f, paint!!)
        //border line markings
        canvas!!.drawLine(centerW- radius,top.toFloat(),centerW - radius,bottom.toFloat(),paint!!)
        canvas!!.drawLine(centerW + radius,top.toFloat(),centerW + radius,bottom.toFloat(),paint!!)
        //tolerance lines
        paint!!.color = Color.LTGRAY
        canvas!!.drawLine(centerW - radius - 5 * multiplier1d,top.toFloat(),centerW - radius - 5 * multiplier1d, bottom.toFloat(),paint!!)
        canvas!!.drawLine( centerW + radius + 5 * multiplier1d , top.toFloat(),centerW + radius + 5 * multiplier1d, bottom.toFloat(), paint!! )
        //outer border
        paint!!.style = Paint.Style.STROKE
        paint!!.color = Color.BLACK
        canvas.drawRect(left.toFloat(),top.toFloat(),right.toFloat(),bottom.toFloat(),paint!!)
    }
    private fun drawFlatSurface(canvas: Canvas?) { // 2d Bubble drawing
        var x: Float =0f
        var y: Float = 0f
        paint!!.style = Paint.Style.FILL
        // min and max of pitch
        x= if(sensorValue!!.pitch > maxRange) (maxRange * multiplier2d + centerW).toFloat()
        else if (sensorValue!!.pitch < minRange) (minRange * multiplier2d + centerW).toFloat()
        else (sensorValue!!.pitch * multiplier2d + centerW).toFloat()
        // min and max of roll
        y =
            if (sensorValue!!.roll > maxRange) (screenHeight - (maxRange * multiplier2d + centerH)).toFloat()
            else if (sensorValue!!.roll < minRange)  (minRange * multiplier2d + centerH).toFloat()
            else (screenHeight  - (sensorValue!!.roll * multiplier2d + centerH)).toFloat()
        //draw circle bubble
        // 5 is sensitivity tolerance
        if (sensorValue!!.roll > 0 + 5 || sensorValue!!.roll  < 0 - 5 || sensorValue!!.pitch  > 0 + 5 || sensorValue!!.pitch < 0 - 5)
            paint!!.color=Color.RED  else paint!!.color=Color.GREEN
        canvas!!.drawCircle(x, y, radius, paint!!)
        //draw center dot
        paint!!.color = Color.BLACK
        canvas.drawCircle(centerW.toFloat(), centerH.toFloat(), 5f, paint!!)
        //inner border circle
        paint!!.style = Paint.Style.STROKE
        canvas.drawCircle(centerW.toFloat(), centerH.toFloat(), radius + multiplier2d, paint!!)
        //outer border circle
        paint!!.style = Paint.Style.STROKE
        canvas.drawCircle(centerW.toFloat(), centerH.toFloat(), 10f + centerH - 35f, paint!!) // 35 is the gap
        //tolerance border circle
        paint!!.style = Paint.Style.STROKE
        paint!!.color = Color.LTGRAY
        canvas.drawCircle(centerW.toFloat(), centerH.toFloat(), 5f * multiplier2d + radius, paint!!)
    }
    fun doDraw(value: SensorValue, sOrientation: Int) {
        sensorValue = value
        orientation = sOrientation
        invalidate()
    }
}