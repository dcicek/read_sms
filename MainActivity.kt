package com.example.getsms

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel


class MainActivity: FlutterActivity() {

    var prefs: SharedPreferences? = null
    private lateinit var sharedPrefManager: SharedPrefManager
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        prefs = getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)

        super.configureFlutterEngine(flutterEngine)
        sharedPrefManager = SharedPrefManager(this)
        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, "com.example.getsms").setMethodCallHandler apply@{ call, result ->
            val arguments = call.arguments as? Map<*, *> ?: return@apply
            if(call.method == "getInfo")
            {
                val data = sharedPrefManager.read(arguments.get(key = "param").toString())
                result.success(data)
            }
            else
            {
                sharedPrefManager.save("sender", arguments.get(key = "sender").toString())
                sharedPrefManager.save("tag", arguments.get(key = "tag").toString())
                sharedPrefManager.save("appName", arguments.get(key = "appName").toString())
                result.success("Ok")
            }

        }
    }

}
