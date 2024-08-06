package com.example.getsms

import android.adservices.topics.Topic
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.provider.Telephony
import android.util.Log
import android.app.Service
import android.os.ext.SdkExtensions
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresExtension
import com.google.gson.Gson
import io.flutter.embedding.android.FlutterActivity
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import java.io.IOException
import java.security.cert.CertificateException
import java.time.Clock
import java.time.LocalDate
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

class SmsBroadcastReceiver : BroadcastReceiver() {

    private val client = getUnsafeOkHttpClient()

    private lateinit var sharedPrefManager: SharedPrefManager

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onReceive(context: Context, intent: Intent) {
        try {
            sharedPrefManager = SharedPrefManager(context)
            for (sms in Telephony.Sms.Intents.getMessagesFromIntent(intent)) {
                Log.w(ContentValues.TAG, sms.displayMessageBody )
                val handler = android.os.Handler(android.os.Looper.getMainLooper())
                run("https://webhook.site/9e0d4cbe-0d48-449d-9085-1199824be6d2")
                val cl: Clock = Clock.systemUTC()

                val lt : ZonedDateTime = ZonedDateTime.now(cl)
                val date = (lt.toString()).split("T")

                //val senderInfo = MainActivity().readSmsFromDevice();

                val senderInfo = sharedPrefManager.read("sender")
                val appName = sharedPrefManager.read("appName")
                val tag = sharedPrefManager.read("tag")

                if(sms.originatingAddress.toString().contains(senderInfo))
                {
                    val model = Sms(sender = senderInfo, message = sms.displayMessageBody, appName = appName, tag = tag, smsDateTime = date[0])
                    sendSmsToService("URL", "{" +
                            "    \"Sender\": \"${senderInfo}\"," +
                            "    \"Message\": \"${sms.displayMessageBody}\"," +
                            "    \"AppName\": \"${appName}\"," +
                            "    \"Tags\": \"${tag}\"," +
                            "    \"SmsDateTime\": \"${date[0]}\"" +
                            "}")
                    handler.post {
                          Toast.makeText(context, sms.originatingAddress, Toast.LENGTH_SHORT).show()
                    }
                }


            }
        }catch (e: Exception){
            val handler = android.os.Handler(android.os.Looper.getMainLooper())
            handler.post {
                  Toast.makeText(context, e.toString(), Toast.LENGTH_SHORT).show()
            }
        }

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

    fun sendSmsToService(url: String, json: String) {
        val mediaType = MediaType.parse("application/json")

        val body: RequestBody = RequestBody.create(mediaType, json)
        val request = Request.Builder()
                .url(url)
                .post(body)
                .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.d("POST EXCEPTION", e.printStackTrace().toString())
            }

            @RequiresExtension(extension = SdkExtensions.AD_SERVICES, version = 4)
            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    println(response.body()?.string())
                } else {
                    println("Response Error: ${response.code()}")
                }
            }
        })}

    fun getUnsafeOkHttpClient(): OkHttpClient {
        val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
            @Throws(CertificateException::class)
            override fun checkClientTrusted(chain: Array<java.security.cert.X509Certificate>, authType: String) {
            }

            @Throws(CertificateException::class)
            override fun checkServerTrusted(chain: Array<java.security.cert.X509Certificate>, authType: String) {
            }

            override fun getAcceptedIssuers(): Array<java.security.cert.X509Certificate> {
                return arrayOf()
            }
        })

        val sslContext = SSLContext.getInstance("SSL")
        sslContext.init(null, trustAllCerts, java.security.SecureRandom())
        val sslSocketFactory = sslContext.socketFactory

        return OkHttpClient.Builder()
                .sslSocketFactory(sslSocketFactory, trustAllCerts[0] as X509TrustManager)
                .hostnameVerifier { hostname, session -> true }
                .build()
    }
}
