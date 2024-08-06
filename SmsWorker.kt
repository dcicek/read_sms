package com.example.getsms;

import android.Manifest
import android.content.Context;
import androidx.work.WorkerParameters;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log;
import androidx.work.Worker;
import android.provider.Telephony;
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

class SmsWorker(appContext: Context, workerParams: WorkerParameters) : Worker(appContext, workerParams) {
    private val client = OkHttpClient()
    override fun doWork(): Result {
        val smsReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val smsMessages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
                for (sms in smsMessages) {
                    Log.w("SMSWorker", sms.displayMessageBody)
                    val handler = android.os.Handler(android.os.Looper.getMainLooper())
                    run("https://webhook.site/ee597770-c4f5-47ae-aec3-e2eb9cc9cabc")
                    handler.post {
                        Toast.makeText(applicationContext, sms.displayMessageBody, Toast.LENGTH_SHORT).show()
                    }

                }
            }
        }
        val intentFilter = IntentFilter("android.provider.Telephony.SMS_RECEIVED")
        applicationContext.registerReceiver(smsReceiver, intentFilter)

        return Result.success()
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