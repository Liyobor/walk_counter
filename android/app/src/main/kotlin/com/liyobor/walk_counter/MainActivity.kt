package com.liyobor.walk_counter

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.*
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.EventChannel
import io.flutter.plugin.common.MethodChannel
import timber.log.Timber

class MainActivity: FlutterActivity() {

    object Constants {
        const val ACTIVITY_RECOGNITION = 87
    }

    private val streamHandler = EventStreamHandler()
    private val methodChannel = "com.liyobor.demo/method"
    private val eventChannel = "com.liyobor.demo/events"

    private lateinit var sensorMgr: SensorManager
    private var stepDetectSensor: Sensor? = null
    private val stepDetectSensorListener = object: SensorEventListener {
        private var step = 0
        override fun onSensorChanged(p0: SensorEvent?) {
            step++
            Timber.i("step = $step")
            streamHandler.onStepChange(step)
//            Timber.i("p0 = ${p0?.values?.get(0)}")
        }

        override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
            Timber.i("onAccuracyChanged")
        }

        fun resetStep(){
            step = 0
            streamHandler.onStepChange(step)
        }

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode){
            Constants.ACTIVITY_RECOGNITION ->{
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                ){
                    stepDetectSensor?.also {
                        sensorMgr.registerListener(stepDetectSensorListener,it,SensorManager.SENSOR_DELAY_NORMAL)
                    }
                    Timber.i("You have the ACTIVITY_RECOGNITION Permission,")
                } else {
                    Timber.i("denied ACTIVITY_RECOGNITION !!! $requestCode -> ${grantResults[0]}")
                }
            }
            else ->{

            }
        }
    }

    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)
        EventChannel(flutterEngine.dartExecutor.binaryMessenger, eventChannel).setStreamHandler(
            streamHandler
        )
        MethodChannel(
            flutterEngine.dartExecutor.binaryMessenger,
            methodChannel
        ).setMethodCallHandler { call, result ->
            when(call.method){
                "reset" -> {
                    stepDetectSensorListener.resetStep()
                }
                else -> result.notImplemented()
            }
        }
    }

    private fun requestActivityRecognitionPermission() = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION) == PackageManager.PERMISSION_DENIED){
            Timber.i("請開啟權限")
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACTIVITY_RECOGNITION,
                ),
                Constants.ACTIVITY_RECOGNITION
            )
        }else{
            stepDetectSensor?.also {
                sensorMgr.registerListener(stepDetectSensorListener,it,SensorManager.SENSOR_DELAY_NORMAL)
            }
            null
        }
    }else{
        Toast.makeText(context,"版本不支援計步器", Toast.LENGTH_LONG).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.uprootAll()
        Timber.plant(Timber.DebugTree())

        sensorMgr = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        stepDetectSensor = sensorMgr.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)


        requestActivityRecognitionPermission()

    }


    inner class EventStreamHandler : EventChannel.StreamHandler{
        private var eventSink: EventChannel.EventSink? = null
        override fun onListen(p0: Any?, p1: EventChannel.EventSink?) {
            eventSink = p1
        }

        override fun onCancel(p0: Any?) {
            eventSink = null
        }

        fun onStepChange(count:Int){
            Handler(Looper.getMainLooper() ?: return).post {
                eventSink?.success(mapOf("count" to count))
            }
        }

    }
}
