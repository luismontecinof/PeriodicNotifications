package com.montecino.periodicnotifications

import android.Manifest
import android.app.AlarmManager
import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.montecino.periodicnotifications.databinding.ActivityMainBinding
import java.util.Calendar

class MainActivity : AppCompatActivity() {

    lateinit var mainBinding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainBinding = ActivityMainBinding.inflate(layoutInflater)
        val view = mainBinding.root
        setContentView(view)

        val calendar = Calendar.getInstance()

        mainBinding.buttonSet.setOnClickListener {
            //Request POST_NOTIFICATION permission for API 33 and above
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED){
                    if(shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)){
                        //show a message
                        Snackbar.make(
                            mainBinding.constraintLayout,
                            "Please allow the permission to take notification",
                            Snackbar.LENGTH_LONG
                        ).setAction("Allow"){
                            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS),1)
                        }.show()
                    } else {
                        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS),1)
                    }
                } else {
                    setNotificationTime()
                }
            } else {
                setNotificationTime()
            }
        }
    }

    //setting a specific time for notification
    fun setNotificationTime(){

        //get current time
        val calendar = Calendar.getInstance()
        val currentHour = calendar.get(Calendar.HOUR)
        val currentMinute = calendar.get(Calendar.MINUTE)

        //creating MaterialTimePicker and set the current time
        val timePicker = MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_24H)
            .setHour(currentHour)
            .setMinute(currentMinute)
            .setTitleText("Set Notification Time")
            .build()
        timePicker.show(supportFragmentManager,"1")

        timePicker.addOnPositiveButtonClickListener {

            //get the selected time
            calendar.set(Calendar.HOUR_OF_DAY, timePicker.hour)
            calendar.set(Calendar.MINUTE, timePicker.minute)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)

            //create intent and pending intent
            val intent = Intent(this, NotificationReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(this, 2, intent, PendingIntent.FLAG_IMMUTABLE)

            //create repeated Alarm
            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.setInexactRepeating(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                AlarmManager.INTERVAL_DAY,
                pendingIntent)
            Toast.makeText(applicationContext, "Notification time set!", Toast.LENGTH_SHORT).show()
            Log.i("PeriodicNotification","Notification time set!")
        }
    }

    //result  of the request permission
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == 1 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){

            setNotificationTime()

        }else{
            Snackbar.make(
                mainBinding.constraintLayout,
                "Please allow the permission to take notification",
                Snackbar.LENGTH_LONG
            ).setAction("Allow"){
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS),1)
            }.show()
        }

    }
}