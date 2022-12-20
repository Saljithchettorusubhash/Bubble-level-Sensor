package com.example.bubblelevelsensor

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.hardware.SensorManager.SENSOR_DELAY_NORMAL
import android.os.Bundle
import android.view.OrientationEventListener
import android.view.Window
import android.view.WindowManager
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.base.bubble.R
import java.math.RoundingMode
import java.text.DecimalFormat

class MainActivity : AppCompatActivity() , SensorEventListener{
    private var orientationEventListener: OrientationEventListener? = null
    private  var sensorEventListener : SensorEventListener? = null
    private var sensor: Sensor? = null
    private var sensorManager: SensorManager? = null
    private var screenOrientation = 0
    private val sensorDelay = 16 * 1000
    private val lpfAmount = 1f // low pass filter
    private var lpfFilteredValues: FloatArray ?=null
    private var baseView: BubbleBaseView ? = null
    private var tvAngle: TextView? = null
    private var maxRange  =10
    private var minRange  = -10
    override fun onCreate(savedInstanceState: Bundle?) {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        tvAngle = findViewById<TextView>(R.id.angle)
        baseView = findViewById<BubbleBaseView>(R.id.bView)
        window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        listenOrientation()
        //setup rotation sensor listener
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        if (sensorManager!!.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
            sensor = sensorManager!!.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
            sensorManager!!.registerListener(this, sensor, SENSOR_DELAY_NORMAL)
        }
    }
    override fun onPause() {
        super.onPause()
        if(sensorManager!=null)
            sensorManager!!.unregisterListener(this)
    }
    override fun onResume() {
        super.onResume()
        if(sensorEventListener!=null)
            sensorManager!!.registerListener(this,sensorManager!!.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),SENSOR_DELAY_NORMAL)

    }
    override fun onDestroy() {
        super.onDestroy()
        try{
            if (sensorManager != null && sensor != null){
                sensorManager!!.unregisterListener(this)
                sensorManager = null
            }
            if(sensor != null){
                sensorManager = null
            }
        }catch (error:Exception){}
    }
    // orientation change listener
    private fun listenOrientation(){
        orientationEventListener =
            object : OrientationEventListener(this, sensorDelay) {
                override fun onOrientationChanged(orientation: Int) {
                    if (orientation >= 315 || orientation < 45) {
                        screenOrientation = 0
                    } else if (orientation >= 45 && orientation < 135) {
                        screenOrientation = 90
                    } else if (orientation >= 135 && orientation < 225) {
                        screenOrientation = 180
                    } else if (orientation >= 225 && orientation < 315) {
                        screenOrientation = 270
                    }
                }
            }
        if ((orientationEventListener as OrientationEventListener).canDetectOrientation()) {
            (orientationEventListener as OrientationEventListener).enable()
        } else {
            (orientationEventListener as OrientationEventListener).disable()
        }
    }

    private fun processSensorValues(event: SensorEvent) {
        lpfFilteredValues =setupSmoothBubbleMovement(event.values, lpfFilteredValues)
        //lpfFilteredValues = event.values
        val x: Double = lpfFilteredValues!![0].toDouble()
        val y: Double = lpfFilteredValues!![1].toDouble()
        val z: Double = lpfFilteredValues!![2].toDouble()


        //get pitch and roll angles
        var pitch = Math.atan(x / Math.sqrt(Math.pow(y, 2.0) + Math.pow(z, 2.0)))
        var roll = Math.atan(y / Math.sqrt(Math.pow(x, 2.0) + Math.pow(z, 2.0)))

        var correctedPitch = Math.round(Math.toDegrees(pitch))
        var correctedRoll = Math.round(Math.toDegrees(roll))

        //storing list of values based on range [-10,10]
        val sensorValue = SensorValue(correctedPitch.toDouble(),correctedRoll.toDouble())

        //draw the bubble level
        baseView!!.doDraw(sensorValue, screenOrientation)

        // limiting to  range [-10,10]
        if (correctedPitch< minRange) {
            correctedPitch = minRange.toLong()
        } else if (correctedPitch > maxRange) {
            correctedPitch = maxRange.toLong()
        }
        if (correctedRoll < minRange) {
            correctedRoll =  minRange.toLong()
        } else if (correctedRoll > maxRange) {
            correctedRoll = maxRange.toLong()
        }

        var angle = Math.atan2(x,y)/(Math.PI/180)

        tvAngle!!.text ="x: " + correctedPitch +  ",  y: " + correctedRoll+" , Angle: "+roundMe(angle)
    }

    // algorithm to render sensor data in a smooth way
    private fun setupSmoothBubbleMovement(current: FloatArray, previous: FloatArray? ):FloatArray{
        if(previous==null) return  current
        for (i in current.indices) {
            previous[i] =
                previous[i] + lpfAmount * (current[i] - previous[i])
        }
        return previous
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if(event==null) return
        processSensorValues(event)
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
        return
    }

    private fun roundMe(value:Double) : String{
        val df = DecimalFormat("#.##")
        df.roundingMode = RoundingMode.HALF_UP
        return df.format(value).toString()
    }
}