package com.example.androidallfunctions

import android.R
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import java.util.*


class BootUpReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
//            startActivityNotification(context, 1, "hi", "hello")
    }

    fun startActivityNotification(
        context: Context, notificationID: Int,
        title: String?, message: String?
    ) {
        val mNotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        //Create GPSNotification builder
        val mBuilder: NotificationCompat.Builder

        //Initialise ContentIntent
        val ContentIntent = Intent(context, TestActivity::class.java)
        ContentIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                Intent.FLAG_ACTIVITY_CLEAR_TASK
        val ContentPendingIntent = PendingIntent.getActivity(
            context,
            0,
            ContentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        mBuilder = NotificationCompat.Builder(context)
            .setSmallIcon(R.drawable.sym_def_app_icon)
            .setContentTitle(title)
            .setContentText(message)
            .setColor(context.resources.getColor(R.color.background_dark))
            .setAutoCancel(true)
            .setContentIntent(ContentPendingIntent)
            .setDefaults(Notification.DEFAULT_LIGHTS or Notification.DEFAULT_VIBRATE)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val mChannel = NotificationChannel(
                "1",
                "Activity Opening Notification",
                NotificationManager.IMPORTANCE_HIGH
            )
            mChannel.enableLights(true)
            mChannel.enableVibration(true)
            mChannel.setDescription("Activity opening notification")
            mBuilder.setChannelId("1")
            Objects.requireNonNull(mNotificationManager).createNotificationChannel(mChannel)
        }
        Objects.requireNonNull(mNotificationManager).notify(
            "TAG_NOTIFICATION", notificationID,
            mBuilder.build()
        )
    }
}