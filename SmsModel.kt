package com.example.getsms

import com.google.gson.annotations.SerializedName;
data class Sms(
        @SerializedName("Sender")val sender: String,
        @SerializedName("Message")val message: String,
        @SerializedName("AppName")val appName: String,
        @SerializedName("Tags")val tag: String,
        @SerializedName("SmsDateTime")val smsDateTime: String,
)