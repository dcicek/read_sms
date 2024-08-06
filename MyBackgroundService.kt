package com.example.getsms

import android.app.Service
import android.content.BroadcastReceiver
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.IBinder
import android.provider.Telephony
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.time.Clock
import java.time.ZonedDateTime


class MyBackgroundService : Service() {
    private val client = OkHttpClient()
    private var sender = ""
    private var tag = ""
    private var appName = ""

    override fun onCreate() {
        super.onCreate()
        Log.d("MyBackgroundService", "Service created")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("MyBackgroundService", "Service started")
        intent?.let {
            sender = it.getStringExtra("sender").toString()
            tag = it.getStringExtra("tag").toString()
            appName = it.getStringExtra("appName").toString()
            println("Sender: $sender")
            println("tag: $tag")
            println("appName: $appName")
        }
        getSms()
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("MyBackgroundService", "Service destroyed")
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun getSms()
    {
        val smsReceiver = object: BroadcastReceiver(){
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onReceive(p0: Context?, p1: Intent?) {
                for (sms in Telephony.Sms.Intents.getMessagesFromIntent(p1)) {
                    Log.w(ContentValues.TAG, sms.displayMessageBody )
                    val handler = android.os.Handler(android.os.Looper.getMainLooper())
                    run("https://webhook.site/9e0d4cbe-0d48-449d-9085-1199824be6d2")
                  //  handler.post {
                    //    Toast.makeText(applicationContext, sms.displayMessageBody, Toast.LENGTH_SHORT).show()
                   // }

                    val cl: Clock = Clock.systemUTC()

                    val lt : ZonedDateTime = ZonedDateTime.now(cl)

                    if(sms.originatingAddress.toString().contains(sender))
                    {
                        val model = Sms(sender = sender, message = sms.displayMessageBody, appName = appName, tag = tag, smsDateTime = lt.toString())

                        handler.post {
                            Toast.makeText(applicationContext, sms.originatingAddress, Toast.LENGTH_SHORT).show()
                        }
                    }


                }
            }
        }
        registerReceiver(smsReceiver, IntentFilter("android.provider.Telephony.SMS_RECEIVED"))
    }

    fun run(url: String) {
        val request = Request.Builder()
                .url(url)
                .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {}
            override fun onResponse(call: Call, response: Response) = println(response.body()?.string())
        })
    }
}
