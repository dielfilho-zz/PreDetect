package com.example.app

import android.content.Context
import android.support.v4.app.NotificationCompat
import android.support.v4.app.NotificationManagerCompat

object NotificationCreator {

    fun create(context: Context, title: String, message: String = "") {
        val builder : NotificationCompat.Builder = NotificationCompat.Builder(context, "com.example.app:NOTIFICATION_CHANNEL_ID")

        builder
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentText(message)
            .setContentTitle(title)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        val notification = builder.build()

        NotificationManagerCompat.from(context).notify(0, notification)
    }

}